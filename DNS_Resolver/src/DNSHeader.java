import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DNSHeader {
    private final int id, flags, qdCount, anCount, nsCount, arCount;

    public DNSHeader(int id, int flags, int qdCount, int anCount, int nsCount, int arCount) {
        this.id = id;
        this.flags = flags;
        this.qdCount = qdCount;
        this.anCount = anCount;
        this.nsCount = nsCount;
        this.arCount = arCount;
    }

    // 解码 DNS 报文头部
    public static DNSHeader decodeHeader(DataInputStream inputStream, Position position) throws IOException {
        // 读取头部部分的格式
        int id = inputStream.readUnsignedShort();
        int flags = inputStream.readUnsignedShort();
        int qdCount = inputStream.readUnsignedShort();
        int anCount = inputStream.readUnsignedShort();
        int nsCount = inputStream.readUnsignedShort();
        int arCount = inputStream.readUnsignedShort();
        position.addCurrentPosition(12);
        return new DNSHeader(id, flags, qdCount, anCount, nsCount, arCount);
    }

    // 构建响应的 DNS 报文头部
    public static DNSHeader buildResponseHeader(DNSHeader queryHeader, DNSRecord dnsRecord) {
        // 逐位设置标志字段
        int value = 0;
        value = setNthBit(value, 15);   // QR 位
        value = setNthBit(value, 8);    // RD 位
        value = setNthBit(value, 7);    // RA 位
        if (dnsRecord.getRdData().equals("0.0.0.0")) {
            // 对于不存在的主机响应，设置 RCODE 为 3
            value = setNthBit(value, 0);
            value = setNthBit(value, 1);
        }
        return new DNSHeader(queryHeader.getId(), value, queryHeader.getQdCount(), 1, 0, queryHeader.getArCount());
    }

    // 编码 DNS 报文头部
    public void encodeHeader(DataOutputStream outputStream) throws IOException {
        // 写入头部部分的格式
        outputStream.writeShort(id);
        outputStream.writeShort(flags);
        outputStream.writeShort(qdCount);
        outputStream.writeShort(anCount);
        outputStream.writeShort(nsCount);
        outputStream.writeShort(arCount);
    }

    public int getId() {
        return id;
    }

    public int getQr() {
        return (flags & 0x8000) >> 15;
    }

    public int getOpCode() {
        return (flags & 0x7800) >> 11;
    }

    public int getAa() {
        return (flags & 0x400) >> 10;
    }

    public int getTc() {
        return (flags & 0x200) >> 9;
    }

    public int getRd() {
        return (flags & 0x100) >> 8;
    }

    public int getRa() {
        return (flags & 0x80) >> 7;
    }

    public int getZ() {
        return (flags & 0x70) >> 4;
    }

    public int getRCode() {
        return (flags & 0xF);
    }

    public int getQdCount() {
        return qdCount;
    }

    public int getAnCount() {
        return anCount;
    }

    public int getArCount() {
        return arCount;
    }

    @Override
    public String toString() {
        return "DNSHeader{" +
                "ID=" + id +
                ", QR=" + getQr() +
                ", Opcode=" + getOpCode() +
                ", AA=" + getAa() +
                ", TC=" + getTc() +
                ", RD=" + getRd() +
                ", RA=" + getRa() +
                ", Z=" + getZ() +
                ", RCODE=" + getRCode() +
                ", QDCOUNT=" + qdCount +
                ", ANCOUNT=" + anCount +
                ", NSCOUNT=" + nsCount +
                ", ARCOUNT=" + arCount +
                '}';
    }

    private static int setNthBit(int value, int nth) {
        // 将第 nth 位设置为 1
        return ((1 << nth) | value);
    }
}
