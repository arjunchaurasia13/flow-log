import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlowLogFilterTest {

    @Test
    public void testParseRecordSuccess() {
        String line = "2 123456789012 eni-abc123 10.0.0.1 192.168.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK";
        Record record = FlowLogFilter.parseRecord(line.split("\\s+"), createConfig(), line);
        assertNotNull(record);
        assertEquals("10.0.0.1", record.sourceIp);
        assertEquals("192.168.0.1", record.destIp);
        assertEquals("12345", record.sourcePort);
        assertEquals("80", record.destPort);
        assertEquals("6", record.protocol);
    }

    @Test
    public void testParseRecordInvalidSourceIp() {
        String line = "2 123456789012 eni-abc123 not-an-ip 192.168.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK";
        Record record = FlowLogFilter.parseRecord(line.split("\\s+"), createConfig(), line);
        assertNull(record);
    }

    @Test
    public void testParseRecordInvalidDestIp() {
        String line = "2 123456789012 eni-abc123 10.0.0.1 999.999.999.999 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK";
        Record record = FlowLogFilter.parseRecord(line.split("\\s+"), createConfig(), line);
        assertNull(record);
    }

    @Test
    public void testParseRecordNotEnoughFields() {
        String line = "2 123456789012 eni-abc123";
        Record record = FlowLogFilter.parseRecord(line.split("\\s+"), createConfig(), line);
        assertNull(record);
    }

    @Test
    public void testParseRecordEmptyFields() {
        String[] fields = new String[0];
        Record record = FlowLogFilter.parseRecord(fields, createConfig(), "");
        assertNull(record);
    }

    @Test
    public void testValidIpAddresses() {
        String[] validLines = {
            "2 123456789012 eni-abc123 0.0.0.0 255.255.255.255 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK",
            "2 123456789012 eni-abc123 192.168.1.1 10.0.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK",
            "2 123456789012 eni-abc123 172.16.0.1 172.31.255.254 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK"
        };
        
        for (String line : validLines) {
            Record record = FlowLogFilter.parseRecord(line.split("\\s+"), createConfig(), line);
            assertNotNull(record, line);
        }
    }

    @Test
    public void testInvalidIpAddresses() {
        String[] invalidLines = {
            "2 123456789012 eni-abc123 256.0.0.1 192.168.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK",
            "2 123456789012 eni-abc123 10.0.0 192.168.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK",
            "2 123456789012 eni-abc123 10.0.0.1.5 192.168.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK",
            "2 123456789012 eni-abc123 abc.def.ghi.jkl 192.168.0.1 12345 80 6 10 100 1617690000 1617690060 ACCEPT OK"
        };
        
        for (String line : invalidLines) {
            Record record = FlowLogFilter.parseRecord(line.split("\\s+"), createConfig(), line);
            assertNull(record, line);
        }
    }

    @Test
    public void testConnectionKeyEquals() {
        ConnectionKey key1 = new ConnectionKey("10.0.0.1", "192.168.0.1", "12345", "80", "6");
        ConnectionKey key2 = new ConnectionKey("10.0.0.1", "192.168.0.1", "12345", "80", "6");
        ConnectionKey key3 = new ConnectionKey("10.0.0.2", "192.168.0.1", "12345", "80", "6");
        
        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
    }

    @Test
    public void testConnectionKeyHashCode() {
        ConnectionKey key1 = new ConnectionKey("10.0.0.1", "192.168.0.1", "12345", "80", "6");
        ConnectionKey key2 = new ConnectionKey("10.0.0.1", "192.168.0.1", "12345", "80", "6");
        
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testRecordGetConnectionKey() {
        Record record = new Record("raw", "10.0.0.1", "192.168.0.1", "12345", "80", "6");
        ConnectionKey key = record.getConnectionKey();
        
        assertNotNull(key);
        assertEquals("10.0.0.1", key.sourceIp);
        assertEquals("192.168.0.1", key.destIp);
    }

    @Test
    public void testConfigDefaults() {
        Config config = new Config();
        
        assertEquals(3, config.srcIndex);
        assertEquals(4, config.destIndex);
        assertEquals(5, config.srcPortIndex);
        assertEquals(6, config.destPortIndex);
        assertEquals(7, config.protocolIndex);
        assertFalse(config.countConnection);
    }

    private Config createConfig() {
        Config config = new Config();
        config.srcIndex = 3;
        config.destIndex = 4;
        config.srcPortIndex = 5;
        config.destPortIndex = 6;
        config.protocolIndex = 7;
        return config;
    }
}
