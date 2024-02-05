import java.io.IOException;
import java.net.*;

public class DNSServer {
    private final int maxSize = 512, localPort = 8053, serverPort = 53;
    private final String localHost = "127.0.0.1", serverHost = "8.8.8.8";
    private final DatagramPacket localPacket, serverPacket;
    private final DatagramSocket localSocket, serverSocket;

    public DNSServer() throws UnknownHostException, SocketException {
        localPacket = new DatagramPacket(new byte[maxSize], maxSize);
        serverPacket = new DatagramPacket(new byte[maxSize], maxSize, InetAddress.getByName(serverHost), serverPort);
        localSocket = new DatagramSocket(localPort);
        serverSocket = new DatagramSocket();
    }

    public void listen() throws IOException {
        while (true) {
            System.out.println("DNSServer listening for DNS request...");
            decodeLocalPacket();
        }
    }

    private void decodeLocalPacket() throws IOException {
        localSocket.receive(localPacket);
        System.out.println("DNS request packet ( " + localPacket.getLength() + " bytes ) received from " + localHost +
                " at port " + localPort + ".");
        // 解码包成 DNSMessage
        System.out.println("Decoding DNS request packet...");
        DNSMessage dnsMessage = DNSMessage.decodeMessage(localPacket.getData());
        System.out.println("Decoded DNS request packet: " + dnsMessage + ".");
        // 检查 DNSCache 中是否有缓存的 DNSQuestion
        for (DNSQuestion dnsQuestion : dnsMessage.getDnsQuestions()) {
            System.out.println("Searching DNSCache for possible record to question '" + dnsQuestion.getQName()
                    + "'...");
            DNSRecord dnsRecord = DNSCache.get(dnsQuestion);
            if (dnsRecord == null && (dnsRecord = decodeServerPacket()) != null) {
                DNSCache.put(dnsQuestion, dnsRecord);
            } else {
                System.out.println("Found a matching record.");
            }
            dnsMessage.setDnsRecord(dnsRecord);
            System.out.println("Decoded DNS response record for DNS request packet ID="
                    + dnsMessage.getDnsHeader().getId() + ": " + dnsRecord);
        }
        // 编码 DNSMessage 成包
        System.out.println("Encoding DNS response packet...");
        localPacket.setData(dnsMessage.encodeMessage());
        System.out.println("Encoded DNS response packet: " + dnsMessage + ".");
        // 响应 DNS 请求
        localSocket.send(localPacket);
        System.out.println("DNS response packet ( " + localPacket.getLength() + " bytes ) sent to " + localHost +
                " at port " + localPort + ".");
    }

    private DNSRecord decodeServerPacket() throws IOException {
        System.out.println("No matching record.");
        // 转发 DNS 请求到 DNS 服务器
        serverPacket.setData(localPacket.getData());
        System.out.println("Forwarding the DNS request package to " + serverHost + " at port " + serverPort + "...");
        serverSocket.send(serverPacket);
        // 等待 DNS 响应
        serverSocket.receive(serverPacket);
        System.out.println("DNS response packet ( " + serverPacket.getLength() + " bytes ) received from " + serverHost +
                " at port " + serverPort + ".");
        // 解码包成 DNSMessage
        System.out.println("Decoding DNS response packet...");
        DNSMessage dnsMessage = DNSMessage.decodeMessage(serverPacket.getData());
        DNSRecord dnsRecord = null;
        if (dnsMessage.getDnsHeader().getRCode() == 0x3) {
            // 获取第一个 DNSQuestion
            DNSQuestion dnsQuestion = dnsMessage.getDnsQuestions().get(0);
            // 获取偏移值并应用消息压缩方案位
            int offset = dnsQuestion.getOffset();
            offset = setNthBit(offset, 15);     // 消息压缩的第一个字节最左边的位
            offset = setNthBit(offset, 14);     // 消息压缩的第一个字节第二左边的位
            byte[] bytes = new byte[2];
            bytes[0] = (byte) ((offset & 0xFF00) >> 8); // 获取第一个字节从右到左的值并将其右移至第一个字节
            bytes[1] = (byte) (offset & 0xFF);          // 获取第一个字节从右到左的值
            // 处理名称错误
            dnsRecord = new DNSRecord(dnsQuestion.getQName(), bytes, dnsQuestion.getQType(),
                    dnsQuestion.getQClass(), 3600, 4, "0.0.0.0");
        }
        if (dnsRecord != null)
            return dnsRecord;
        else
            return dnsMessage.getDnsRecords().get(0);
    }

    public void close() {
        localSocket.close();
        serverSocket.close();
    }

    private static int setNthBit(int value, int nth) {
        // 设置第 n 位在 int 值中（4 字节）
        return ((1 << nth) | value);
    }
}
