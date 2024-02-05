import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class DNSQuestion {
    private final String qName;
    private final int qType, qClass, offset;

    public DNSQuestion(String qName, int qType, int qClass, int offset) {
        this.qName = qName;
        this.qType = qType;
        this.qClass = qClass;
        this.offset = offset;
    }

    // 解码 DNS 问题部分
    public static DNSQuestion decodeQuestion(DataInputStream inputStream, Position position) throws IOException {
        // 在读取标签之前获取偏移位置
        int offsetPosition = position.getCurrentPosition();
        // 读取问题部分的格式
        // 读取 QNAME 字段
        StringBuilder stringBuilder = new StringBuilder();
        // 读取第一个标签的长度字节
        int lengthOctet = inputStream.readUnsignedByte();
        position.addCurrentPosition(1);
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
        // 读取 QTYPE 和 QCLASS 字段
        int qType = inputStream.readUnsignedShort();
        int qClass = inputStream.readUnsignedShort();
        position.addCurrentPosition(4);
        return new DNSQuestion(stringBuilder.toString(), qType, qClass, offsetPosition);
    }

    // 编码 DNS 问题部分
    public void encodeQuestion(DataOutputStream outputStream) throws IOException {
        // 写入问题部分的格式
        outputStream.write(getBytes(qName));
        // 终止 qName
        outputStream.writeByte(0);//end qName
        outputStream.writeShort(qType);
        outputStream.writeShort(qClass);
    }

    public String getQName() {
        return qName;
    }

    public int getQType() {
        return qType;
    }

    public int getQClass() {
        return qClass;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "DNSQuestion{" +
                "qName='" + qName + '\'' +
                ", qType=" + qType +
                ", qClass=" + qClass +
                ", offset=" + offset +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        // 用于 hashCode()
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion that = (DNSQuestion) o;
        return qType == that.qType && qClass == that.qClass && offset == that.offset && Objects.equals(qName, that.qName);
    }

    @Override
    public int hashCode() {
        // 用于在 DNSCache 中查找 DNSRecord
        return Objects.hash(qName, qType, qClass, offset);
    }

    private byte[] getBytes(String value) {
        byte[] bytes = new byte[value.length() + 1];
        int count = 0;
        String[] strings = value.split("\\.");
        for (String string : strings) {
            // 获取长度字节
            bytes[count++] = (byte) string.length();
            // 获取每个字符的字节
            for (char c : string.toCharArray()) {
                bytes[count++] = (byte) c;
            }
        }
        return bytes;
    }
}
