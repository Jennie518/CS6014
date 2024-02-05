import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DNSMessage {
    private DNSHeader dnsHeader;
    private final ArrayList<DNSQuestion> dnsQuestions;
    private final ArrayList<DNSRecord> dnsRecords;
    private final byte[] additionalRecords;

    public DNSMessage(DNSHeader dnsHeader, ArrayList<DNSQuestion> dnsQuestions, ArrayList<DNSRecord> dnsRecords,
                      byte[] additionalRecords) {
        this.dnsHeader = dnsHeader;
        this.dnsQuestions = dnsQuestions;
        this.dnsRecords = dnsRecords;
        this.additionalRecords = additionalRecords;
    }

    // 解码 DNS 消息
    public static DNSMessage decodeMessage(byte[] data) throws IOException {
        // 当前输入流的位置
        Position position = new Position();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
        // 解码数据为 DNSHeader
        DNSHeader dnsHeader = DNSHeader.decodeHeader(inputStream, position);
        // 如果 Opcode 是标准查询，解码数据为 DNSQuestion
        ArrayList<DNSQuestion> dnsQuestions = new ArrayList<>();
        ArrayList<DNSRecord> dnsRecords = new ArrayList<>();
        if (dnsHeader.getOpCode() == 0) {
            // 如果有问题，解码问题
            for (int i = 0; i < dnsHeader.getQdCount(); i++) {
                DNSQuestion dnsQuestion = DNSQuestion.decodeQuestion(inputStream, position);
                dnsQuestions.add(dnsQuestion);
                // 如果有答案，解码答案
                if (dnsHeader.getQr() == 1) {
                    for (int j = 0; j < dnsHeader.getAnCount(); j++) {
                        dnsRecords.add(DNSRecord.decodeRecord(inputStream, dnsQuestion, position));
                    }
                }
            }
        }
        // 读取附加记录
        byte[] additionalRecords = null;
        if (dnsHeader.getArCount() == 1) {
            additionalRecords = byteArrayInputStream.readNBytes(23);
            position.addCurrentPosition(23);
        }
        byteArrayInputStream.close();
        inputStream.close();
        return new DNSMessage(dnsHeader, dnsQuestions, dnsRecords, additionalRecords);
    }

    // 编码 DNS 消息
    public byte[] encodeMessage() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        // 编码 DNSHeader、DNSQuestions、DNSRecords，以及如果存在的 AdditionalRecords
        dnsHeader = DNSHeader.buildResponseHeader(dnsHeader, dnsRecords.get(0));
        dnsHeader.encodeHeader(outputStream);
        for (DNSQuestion dnsQuestion : dnsQuestions) {
            dnsQuestion.encodeQuestion(outputStream);
        }
        for (DNSRecord dnsRecord : dnsRecords) {
            dnsRecord.encodeRecord(outputStream);
        }
        if (additionalRecords != null)
            outputStream.write(additionalRecords);
        byteArrayOutputStream.close();
        outputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public DNSHeader getDnsHeader() {
        return dnsHeader;
    }

    public ArrayList<DNSQuestion> getDnsQuestions() {
        return dnsQuestions;
    }

    public ArrayList<DNSRecord> getDnsRecords() {
        return dnsRecords;
    }

    public void setDnsRecord(DNSRecord dnsRecord) {
        dnsRecords.add(dnsRecord);
    }

    @Override
    public String toString() {
        return "DNSMessage{" +
                "dnsHeader=" + dnsHeader +
                ", dnsQuestions=" + dnsQuestions +
                ", dnsRecords=" + dnsRecords +
                ", additionalRecords=" + Arrays.toString(additionalRecords) +
                '}';
    }
}
