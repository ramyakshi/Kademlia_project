package kademlia;

import components.Content;
import simulator.EDSimulator;
import simulator.Event;
import simulator.Payload;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Protocol {
    public Node node; // (Dumb) Node representation of this Protocol, without state
    public RoutingTable routingTable;

    public NodeStorage storage;

    public Protocol(Config config) {
        BigInteger nodeId = new BigInteger(config.bitSpace, config.rng);
        this.node = new Node(nodeId);
        this.routingTable = new RoutingTable(nodeId, config);
        this.storage = new NodeStorage();
    }

    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
    }
    public void welcomeIfNew(Node node) {
        if (!routingTable.isNewNode(node)) {
            return;
        }
        //System.out.println("New Node is being added - "+node.getId());
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
            this.welcomeIfNew(node);
        }
        // is this response from which node?
        // how many requests i have outstanding?
        // should i send another request to another node?
    }


    /**
     * Called when this node joins the network.
     * @param bootstrapNode
     *  The known node to ask for a list of neighbors closest to self
     */
    public void bootstrap(Node bootstrapNode) {
        // TODO: change this to complete node lookup procedure, now it just calls FIND_NODE on the bootstrap node
        this.welcomeIfNew(bootstrapNode);
        Payload payload = new Payload(this.node);
        EDSimulator.add(1, Event.RPC_FIND_NODE_REQUEST, this.node, bootstrapNode, payload);
    }

    public void rpcNodeLookUpRequest(NetworkCrawler crawler, Node target, HashMap<BigInteger, Protocol> map)
    {
        List<Node> lookedUpNodes = crawler.nodeLookupBegin(target, map);
        EDSimulator.add(1, Event.NODE_LOOKUP_RESPONSE, target,this.node,new Payload(lookedUpNodes));
    }

    public void rpcNodeLookUpResponse(List<Node> nodes)
    {
        System.out.print("Node lookup returned - ");
        //System.out.println(lookedUpNodes.size());
        for(Node n : nodes)
        {
            System.out.print(n.getId()+" ");
        }
        System.out.println();
    }
    public void callRemoteStore(HashMap<BigInteger,Protocol> nodeToProtocolMap, BigInteger NodeToAsk, BigInteger key, String value, Node sender)
    {
        Protocol protocol = nodeToProtocolMap.get(NodeToAsk);
        EDSimulator.add(4,Event.STORE_REQUEST,sender,this.node,new Payload(key,value));
        protocol.rpcStoreRequest(sender,key,value);
    }
    public void rpcStoreRequest(Node sender,BigInteger key,String value)
    {
        System.out.println(this.node.getId()+" storing key "+ key+" and value "+ value);
        this.welcomeIfNew(sender);
        this.storage.setValue(key,value,0);
        EDSimulator.add(5,Event.STORE_RESPONSE,this.node,sender,new Payload(key,value));
    }

    public void rpcStoreResponse(String value)
    {
        System.out.println("Node "+this.node.getId()+" stored value " +value);
    }

    /**
     * Called by simulator
     */
    public void processEvent(Event event, NetworkCrawler crawler, HashMap<BigInteger, Protocol> map) {
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
            case Event.NODE_LOOKUP_REQUEST:
                this.rpcNodeLookUpRequest(crawler,event.target,map);
                break;
            case Event.STORE_REQUEST:
                this.rpcStoreRequest(event.sender,event.payload.keyToStore, event.payload.valueToStore);
                break;
            case Event.STORE_RESPONSE:
                this.rpcStoreResponse(event.payload.valueToStore);
                break;
        }
    }

}