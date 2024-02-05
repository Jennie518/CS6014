import java.util.HashMap;

public class DNSCache {
    // 创建一个静态的哈希映射，用于存储 DNSQuestion 到 DNSRecord 的映射关系
    private static final HashMap<DNSQuestion, DNSRecord> DNSHashmap = new HashMap<>();

    // 将 DNSQuestion 和对应的 DNSRecord 放入缓存
    public static void put(DNSQuestion dnsQuestion, DNSRecord dnsRecord) {
        DNSHashmap.put(dnsQuestion, dnsRecord);
    }

    // 根据 DNSQuestion 获取相应的 DNSRecord
    public static DNSRecord get(DNSQuestion dnsQuestion) {
        // 获取与 DNSQuestion 相关联的 DNSRecord
        DNSRecord dnsRecord = DNSHashmap.get(dnsQuestion);

        // 检查 DNSRecord 是否存在并且是否过期
        if (dnsRecord != null && !dnsRecord.isTimestampValid()) {
            // 如果 DNSRecord 已经过期，则从缓存中移除
            DNSHashmap.remove(dnsQuestion);
            dnsRecord = null; // 设置为 null，表示未找到有效的 DNSRecord
        }

        // 返回 DNSRecord（可能为 null）
        return dnsRecord;
    }
}
