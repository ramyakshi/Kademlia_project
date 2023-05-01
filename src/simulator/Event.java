package simulator;

import kademlia.*;

public class Event implements Comparable<Event> {
    public long timestamp;
    public int type;
    public static final int BOOTSTRAP = 1;
    public static final int RPC_FIND_NODE_REQUEST = 2;
    public static final int RPC_FIND_NODE_RESPONSE = 3;

    public static final int NODE_LOOKUP_REQUEST = 4;

    public static final int NODE_LOOKUP_RESPONSE = 5;

    public static final int STORE_REQUEST = 6;

    public static final int STORE_RESPONSE = 7;

    public static final int PING_REQUEST = 8;

    public static final int PING_RESPONSE = 9;

    public static final int DEFAULT = 10;

    public static final int KILL_NODE = 11;

    public static final int RPC_FIND_VAL_REQUEST = 12;

    public static final int RPC_FIND_VAL_RESPONSE = 13;

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
            case NODE_LOOKUP_REQUEST -> "NODE_LOOKUP_REQUEST";
            case NODE_LOOKUP_RESPONSE -> "NODE_LOOKUP_RESPONSE";
            case STORE_REQUEST -> "STORE_REQUEST";
            case STORE_RESPONSE -> "STORE_RESPONSE";
            case PING_REQUEST -> "PING_REQUEST";
            case PING_RESPONSE -> "PING_RESPONSE";
            case KILL_NODE -> "KILL_NODE";
            case RPC_FIND_VAL_REQUEST -> "RPC_FIND_VAL_REQUEST";
            case RPC_FIND_VAL_RESPONSE -> "RPC_FIND_VAL_RESPONSE";
            default -> "UNKNOWN";
        };
        return String.format("%d %s", this.timestamp, type);
    }
}