import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class DNSRecord {
    private final String name, rdData;
    private final byte[] nameOffset;
    private final int rType, rClass, rdLength;
    private final long ttl;
    private final LocalDateTime expiredDateTime;

    public DNSRecord(String name, byte[] nameOffset, int rType, int rClass, long ttl, int rdLength, String rdData) {
        this.name = name;
        this.nameOffset = nameOffset;
        this.rType = rType;
        this.rClass = rClass;
        this.ttl = ttl;
        this.rdLength = rdLength;
        this.rdData = rdData;
        this.expiredDateTime = LocalDateTime.now().plusSeconds(ttl);
    }

    // 解码 DNS 记录
    public static DNSRecord decodeRecord(DataInputStream inputStream, DNSQuestion dnsQuestion, Position position) throws IOException {
        // 读取资源记录的格式
        // 读取 NAME 字段
        int lengthOctet = inputStream.readUnsignedByte();
        position.addCurrentPosition(1);
        // 检查消息压缩方案
        String name = null;
        byte[] nameOffset = null;
        if ((lengthOctet & 0xC0) == 0xC0) {
            nameOffset = new byte[2];
            // 读取消息压缩方案
            nameOffset[0] = (byte) lengthOctet;
            nameOffset[1] = (byte) inputStream.readUnsignedByte();
            position.addCurrentPosition(1);
            // 如果偏移匹配，则从 DNSQuestion 获取名称
            if ((nameOffset[0] << 8 | nameOffset[0]) == dnsQuestion.getOffset())
                name = dnsQuestion.getQName();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            while (lengthOctet != 0) {
                for (int i = 0; i < lengthOctet; i++) {
                    // 读取字节并将其转换为字符
                    stringBuilder.append((char) inputStream.read());
                    position.addCurrentPosition(1);
                }
                stringBuilder.append('.');
                lengthOctet = inputStream.read();
                position.addCurrentPosition(1);
            }
            // 去掉额外的字符 '.'
            stringBuilder.setLength(stringBuilder.length() - 1);
            name = stringBuilder.toString();
        }
        // 读取 TYPE、CLASS、TTL 和 RDLENGTH 字段
        int rType = inputStream.readUnsignedShort();
        int rClass = inputStream.readUnsignedShort();
        long ttl = inputStream.readInt();
        int rdLength = inputStream.readUnsignedShort();
        position.addCurrentPosition(10);
        byte[] rdData = inputStream.readNBytes(rdLength);
        position.addCurrentPosition(rdLength);
        StringBuilder rdDataBuilder = new StringBuilder();
        for (byte rd : rdData) {
            // 将字节转换为无符号值
            rdDataBuilder.append(rd & 0xFF);
            rdDataBuilder.append('.');
        }
        // 去掉额外的字符 '.'
        rdDataBuilder.setLength(rdDataBuilder.length() - 1);
        return new DNSRecord(name, nameOffset, rType, rClass, ttl, rdLength, rdDataBuilder.toString());
    }

    // 编码 DNS 记录
    public void encodeRecord(DataOutputStream outputStream) throws IOException {
        // 写入资源记录的格式
        if (nameOffset != null)
            outputStream.write(nameOffset);
        else
            outputStream.write(getCharacterBytes(name));
        outputStream.writeShort(rType);
        outputStream.writeShort(rClass);
        outputStream.writeInt((int) ttl);
        outputStream.writeShort(rdLength);
        outputStream.write(getBytes(rdData));
    }

    public String getRdData() {
        return rdData;
    }

    // 检查时间戳是否有效
    public boolean isTimestampValid() {
        return LocalDateTime.now().isBefore(expiredDateTime);
    }

    @Override
    public String toString() {
        return "DNSRecord{" +
                "name='" + name + '\'' +
                ", nameOffset=" + Arrays.toString(nameOffset) +
                ", rType=" + rType +
                ", rClass=" + rClass +
                ", rdData='" + rdData + '\'' +
                ", ttl=" + ttl +
                ", rdLength=" + rdLength +
                '}';
    }

    private byte[] getCharacterBytes(String value) {
        byte[] bytes = new byte[value.length() + 1];
        int counter = 0;
        String[] strings = value.split("\\.");
        for (String string : strings) {
            // 获取长度字节
            bytes[counter++] = (byte) string.length();
            // 获取每个字符的字节
            for (char c : string.toCharArray()) {
                bytes[counter++] = (byte) c;
            }
        }
        return bytes;
    }

    private byte[] getBytes(String value) {
        String[] strings = value.split("\\.");
        byte[] bytes = new byte[strings.length];
        int count = 0;
        for (String string : strings) {
            bytes[count++] = (byte) Short.parseShort(string);
        }
        return bytes;
    }
}
