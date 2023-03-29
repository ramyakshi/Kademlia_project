package simulator;

import kademlia.*;

public class Event implements Comparable<Event> {
    public long timestamp;
    public int type;
    public static final int BOOTSTRAP = 1;
    public static final int RPC_FIND_NODE_REQUEST = 2;
    public static final int RPC_FIND_NODE_RESPONSE = 3;

    public Node sender;
    public Node target;
    public Payload payload;

    public Event(long timestamp, int type, Node sender, Node target, Payload payload) {
        this.timestamp = timestamp;
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.payload = payload;
    }

    @Override
    public int compareTo(Event o) {
        return Long.compare(this.timestamp, o.timestamp);
    }

    @Override
    public String toString() {
        String type = switch (this.type) {
            case BOOTSTRAP -> "BOOTSTRAP";
            case RPC_FIND_NODE_REQUEST -> "RPC_FIND_NODE_REQUEST";
            case RPC_FIND_NODE_RESPONSE -> "RPC_FIND_NODE_RESPONSE";
            default -> "UNKNOWN";
        };
        return String.format("%d %s", this.timestamp, type);
    }
}