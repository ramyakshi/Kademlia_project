package kademlia;

import simulator.EDSimulator;
import simulator.Event;
import simulator.Payload;

import java.math.BigInteger;
import java.util.List;

public class Protocol {
    public Node node; // (Dumb) Node representation of this Protocol, without state
    public RoutingTable routingTable;

    public Protocol(Config config) {
        BigInteger nodeId = new BigInteger(config.bitSpace, config.rng);
        this.node = new Node(nodeId);
        this.routingTable = new RoutingTable(nodeId, config);
    }

    public void welcomeIfNew(Node node) {
        if (!routingTable.isNewNode(node)) {
            return;
        }
        // TODO: store keys in new node
        routingTable.addContact(node);
    }

    /**
     * This node received a FIND_NODE request
     */
    public void rpcFindNodeRequest(Node sender, Node receiver) {
        this.welcomeIfNew(sender);
        List<Node> neighbors = this.routingTable.findNeighbors(receiver, sender);
        EDSimulator.add(1, Event.RPC_FIND_NODE_RESPONSE, this.node, sender, new Payload(neighbors));
    }

    /**
     * Performed when receiving a response to FIND_NODE (or feel free refactor to more general case)
     */
    public void rpcFindNodeResponse(List<Node> nodes) {
        // TODO: change this to complete node lookup procedure, now it just adds the contacts received
        for (Node node : nodes) {
            this.routingTable.addContact(node);
        }
    }


    /**
     * Called when this node joins the network.
     * @param bootstrapNode
     *  The known node to ask for a list of neighbors closest to self
     */
    public void bootstrap(Node bootstrapNode) {
        // TODO: change this to complete node lookup procedure, now it just calls FIND_NODE on the bootstrap node
        this.routingTable.addContact(bootstrapNode);
        Payload payload = new Payload(this.node);
        EDSimulator.add(1, Event.RPC_FIND_NODE_REQUEST, this.node, bootstrapNode, payload);
    }


    /**
     * Called by simulator
     */
    public void processEvent(Event event) {
        switch (event.type) {

            case Event.BOOTSTRAP:
                this.bootstrap(event.payload.node);
                break;
            case Event.RPC_FIND_NODE_REQUEST:
                this.rpcFindNodeRequest(event.sender, event.payload.node);
                break;
            case Event.RPC_FIND_NODE_RESPONSE:
                this.rpcFindNodeResponse(event.payload.nodes);
                break;
        }
    }
}