package simulator;

import java.util.*;

import kademlia.*;

/**
* Payload is a superset of all possible payloads (different RPCs), just use the appropriate constructor
 * if there are too many types, we can refactor into an interface / generic
 */
public class Payload {
    public Node node;
    public List<Node> nodes;
    public Payload(Node node) {
        this.node = node;
    }
    public Payload(List<Node> nodes) {
        this.nodes = nodes;
    }

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
