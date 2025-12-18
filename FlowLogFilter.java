import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlowLogFilter {

    public static void main(String[] args) {
        Config config = parseArgs(args);
        if (config.filePath == null) {
            throw new IllegalArgumentException("Missing --file argument");
        }
        try {
            processFile(config);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    /**
     *  Process the flow log file based on the provided configuration.
     * @param config
     * @throws IOException
     */
    private static void processFile(Config config) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(config.filePath));
        String line;
        Map<ConnectionKey, Integer> connectionCount = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] fields = trimmed.split("\\s+");
            Record record = parseRecord(fields, config, line);
            if (record == null) {
                continue;
            }
            if (filterRecord(record, config)) {
                System.out.println(record.raw);
                if (config.countConnection) {
                    connectionCount.merge(record.getConnectionKey(), 1, Integer::sum);
                }
            }
        }
        reader.close();
        
        if (config.countConnection && connectionCount.size() > 0) {
            System.out.println("\n--- Connection Counts ---");
            connectionCount.forEach((k, v) -> System.out.println(k + " : " + v));
        }
    }

    /**
     * Parse a line of the flow log file into a Record object.
     * @param fields
     * @param config
     * @param line
     * @return
     */
    static Record parseRecord(String[] fields, Config config, String line) {
        if (fields.length == 0 || fields.length <= config.srcIndex || fields.length <= config.destIndex 
            || fields.length <= config.srcPortIndex || fields.length <= config.destPortIndex 
            || fields.length <= config.protocolIndex) {
            System.err.println("Invalid line (not enough fields): " + line);
            return null;
        }

        String sourceIp = fields[config.srcIndex];
        String destIp = fields[config.destIndex];
        String sourcePort = fields[config.srcPortIndex];
        String destPort = fields[config.destPortIndex];
        String protocol = fields[config.protocolIndex];

        if (!isIpv4(sourceIp) || !isIpv4(destIp)) {
            System.err.println("Invalid IP: " + sourceIp + " or " + destIp);
            return null;
        }
        return new Record(line, sourceIp, destIp, sourcePort, destPort, protocol);
    }

    /**
     *  Filter a record based on the provided configuration.
     * @param record
     * @param config
     * @return
     */
    private static boolean filterRecord(Record record, Config config) {
        // Filter by source IP if specified
        if (config.srcIp != null && !config.srcIp.isEmpty()) {
            if (!config.srcIp.contains(record.sourceIp)) {
                return false;
            }
        }
        // Filter by destination IP if specified
        if (config.destIp != null && !config.destIp.isEmpty()) {
            if (!config.destIp.contains(record.destIp)) {
                return false;
            }
        }
        // Filter by source port if specified (0 means no filter)
        if (config.srcPort > 0) {
            try {
                int port = Integer.parseInt(record.sourcePort);
                if (port != config.srcPort) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // Filter by destination port if specified (0 means no filter)
        if (config.destPort > 0) {
            try {
                int port = Integer.parseInt(record.destPort);
                if (port != config.destPort) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // Filter by protocol if specified (0 means no filter)
        if (config.protocol > 0) {
            try {
                int proto = Integer.parseInt(record.protocol);
                if (proto != config.protocol) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     *  Check if a string is a valid IPv4 address.
     * @param ip
     * @return
     */
    private static boolean isIpv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     *  Parse command line arguments into a Config object.
     * @param args
     * @return
     */
    private static Config parseArgs(String[] args) {
        Config config = new Config();
        for (int i = 0; i < args.length; i++) {
            String currArg = args[i];
            if (currArg.equals("--file")) {
                config.filePath = getArgValue(args, i, "--file");
                i++;
            } else if (currArg.equals("--srcIp")) {
                config.srcIp = convertToList(getArgValue(args, i, "--srcIp"));
                i++;
            } else if (currArg.equals("--destIp")) {
                config.destIp = convertToList(getArgValue(args, i, "--destIp"));
                i++;
            } else if (currArg.equals("--srcPort")) {
                config.srcPort = convertToInt(getArgValue(args, i, "--srcPort"));
                i++;
            } else if (currArg.equals("--destPort")) {
                config.destPort = convertToInt(getArgValue(args, i, "--destPort"));
                i++;
            } else if (currArg.equals("--protocol")) {
                config.protocol = convertToInt(getArgValue(args, i, "--protocol"));
                i++;
            } else if (currArg.equals("--countConnection")) {
                config.countConnection = true;
            } else if (currArg.equals("--srcIndex")) {
                config.srcIndex = convertToInt(getArgValue(args, i, "--srcIndex"));
                i++;
            } else if (currArg.equals("--destIndex")) {
                config.destIndex = convertToInt(getArgValue(args, i, "--destIndex"));
                i++;
            } else if (currArg.equals("--srcPortIndex")) {
                config.srcPortIndex = convertToInt(getArgValue(args, i, "--srcPortIndex"));
                i++;
            } else if (currArg.equals("--destPortIndex")) {
                config.destPortIndex = convertToInt(getArgValue(args, i, "--destPortIndex"));
                i++;
            } else if (currArg.equals("--protocolIndex")) {
                config.protocolIndex = convertToInt(getArgValue(args, i, "--protocolIndex"));
                i++;
            } else if (!currArg.startsWith("--")) {
                // Skip values (already consumed by previous argument)
                continue;
            } else {
                throw new IllegalArgumentException("Unknown argument " + currArg);
            }
        }
        return config;
    }

    private static String getArgValue(String[] args, int index, String argName) {
        if (index + 1 >= args.length) {
            throw new IllegalArgumentException("Missing value for argument " + argName);
        }
        return args[index + 1];
    }

    private static List<String> convertToList(String argValue) {
        return Arrays.stream(argValue.split(",")).map(String::trim).collect(Collectors.toList());
    }

    private static int convertToInt(String argValue) {
        return Integer.parseInt(argValue);
    }
}
