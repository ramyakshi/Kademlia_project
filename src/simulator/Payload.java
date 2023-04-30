package simulator;

import java.math.BigInteger;
import java.util.*;

import kademlia.*;

/**
* Payload is a superset of all possible payloads (different RPCs), just use the appropriate constructor
 * if there are too many types, we can refactor into an interface / generic
 */
public class Payload {
    public Node node;
    public List<Node> nodes;

    //Could also be key, value pair for store RPC
    public BigInteger keyToStore;

    public String valueToStore;

    //Use this generic string for other communications if needed
    public String message;
    public Payload(Node node) {
        this.node = node;
    }
    public Payload(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Payload(BigInteger key, String value){this.keyToStore = key; this.valueToStore = value;}
    public Payload(BigInteger key, String value, String msg)
    {
        this.keyToStore = key;
        this.valueToStore = value;
        this.message = msg;
    }
    public Payload(String message) {this.message = message;}
    @Override
    public String toString() {
        if (this.node == null && this.nodes == null) {
            return "";
        } else if (this.node == null) {
            return this.nodes.toString();
        } else {
            return this.node.toString();
        }
    }
}
