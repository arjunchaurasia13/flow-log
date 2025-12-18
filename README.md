# Flow Log Filter

A CLI tool to filter and analyze AWS VPC Flow Logs.

## Quick Start

```bash
# Compile
javac *.java

# Run
java FlowLogFilter --file sample_flow.log
```

## Usage

```bash
java FlowLogFilter --file <logfile> [filters...]
```

**Filters:**
- `--srcIp <ip>` - filter by source IP (supports comma-separated list)
- `--destIp <ip>` - filter by destination IP
- `--srcPort <port>` - filter by source port
- `--destPort <port>` - filter by destination port  
- `--protocol <num>` - filter by protocol (6=TCP, 17=UDP)
- `--countConnection` - show connection counts at the end

**Examples:**
```bash
# Filter HTTP traffic from 10.0.0.1
java FlowLogFilter --file flow.log --srcIp 10.0.0.1 --destPort 80

# Count all connections
java FlowLogFilter --file flow.log --countConnection

# Filter TCP only
java FlowLogFilter --file flow.log --protocol 6
```

## Running Tests

```bash
# Download JUnit (one time)
mkdir -p lib
curl -L -o lib/junit-platform-console-standalone.jar \
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.0/junit-platform-console-standalone-1.10.0.jar

# Compile & run tests
javac -cp lib/junit-platform-console-standalone.jar:. *.java
java -jar lib/junit-platform-console-standalone.jar --class-path . --scan-class-path
```

## Flow Log Format

Expects standard AWS VPC Flow Log v2 format:
```
version account-id interface-id srcaddr dstaddr srcport dstport protocol packets bytes start end action log-status
```

Default field indices: srcaddr=3, dstaddr=4, srcport=5, dstport=6, protocol=7

Can be customized with `--srcIndex`, `--destIndex`, etc. if your log format differs.
