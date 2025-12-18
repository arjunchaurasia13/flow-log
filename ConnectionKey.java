import java.util.Objects;

public class ConnectionKey {
    String sourceIp;
    String destIp;
    String sourcePort;
    String destPort;
    String protocol;

    public ConnectionKey(String sourceIp, String destIp, String sourcePort, String destPort, String protocol) {
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
        this.protocol = protocol;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConnectionKey other = (ConnectionKey) obj;
        return sourceIp.equals(other.sourceIp) && destIp.equals(other.destIp) && sourcePort.equals(other.sourcePort) && destPort.equals(other.destPort) && protocol.equals(other.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceIp, destIp, sourcePort, destPort, protocol);
    }

    @Override
    public String toString() {
        return sourceIp + " -> " + destIp + ":" + destPort + " (proto:" + protocol + ")";
    }
}
