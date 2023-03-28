package kademlia;

import java.math.BigInteger;

public class Protocol {
    public BigInteger nodeId; // 160-bit identifier
    public RoutingTable routingTable;

    public Protocol(Config config) {
        this.nodeId = new BigInteger(config.bitSpace, config.rng);
        this.routingTable = new RoutingTable(nodeId, config);
    }
}