public class Record {
    String raw;
    String sourceIp;
    String destIp;
    String sourcePort;
    String destPort;
    String protocol;

    public Record(String raw, String sourceIp, String destIp, String sourcePort, String destPort, String protocol) {
        this.raw = raw;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
        this.protocol = protocol;
    }

    public ConnectionKey getConnectionKey() {
        return new ConnectionKey(sourceIp, destIp, sourcePort, destPort, protocol);
    }
}
