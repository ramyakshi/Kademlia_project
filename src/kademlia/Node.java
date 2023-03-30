package kademlia;

import java.math.BigInteger;

// Node is a simple object to represent a (neighbor) Kademlia node
// It should have <IP addr, UDP port> in real implementations.
public class Node {

    public BigInteger id;
    public int port;
    public int idLength = 32;

    public transient RoutingTable routingTable;

    public Node(BigInteger id) {
        this.id = id;
    }
    public Node(BigInteger id, int port, RoutingTable table) {
        this.id = id;
        this.port = port;
        this.routingTable = table;
    }

    public BigInteger distanceTo(Node node) {
        return this.id.xor(node.id);
    }

    public BigInteger getId()
    {
        return this.id;
    }

    @Override
    public String toString() {
        if (this == null) {
            return "";
        }
        return this.id.toString();
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}
