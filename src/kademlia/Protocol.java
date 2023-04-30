package kademlia;

import components.Content;
import simulator.EDSimulator;
import simulator.Event;
import simulator.Payload;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Protocol {
    public Node node; // (Dumb) Node representation of this Protocol, without state
    public RoutingTable routingTable;

    public NodeStorage storage;

    public static long nowTime = 0;

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
        //System.out.println("Welcome if new called for - " + node.getId()+" on routing table of " + this.node.getId());
        if (!routingTable.isNewNode(node)) {
            //System.out.println("Returning because node " + node.getId()+" is not new");
            return;
        }
        //System.out.println("New Node is being added - "+node.getId());
        // TODO: store keys in new node
        for(Map.Entry<BigInteger,Content> stored : storage.getNodeStorageTable().entrySet())
        {
            BigInteger key = stored.getKey();
            Content value = stored.getValue();
            // Get digest of key
            Node keynode = new Node(Util.digest(key));
            List<Node> neighbours = this.routingTable.findNeighbors(node,this.node);
            /*System.out.println("Neighbours - ");
            for(Node n : neighbours)
            {
                System.out.print(n.getId()+" ");
            }*/
            System.out.println();
            boolean is_new_node_closer = false,this_closest = false;
            if(neighbours.size()>0)
            {
             BigInteger last = neighbours.get(neighbours.size()-1).distanceTo(keynode);
             is_new_node_closer = node.distanceTo(keynode).compareTo(last) < 0;
             BigInteger first = neighbours.get(0).distanceTo(keynode);
             this_closest = node.distanceTo(keynode).compareTo(first) < 0;
            }
            if(neighbours==null || neighbours.size()==0 || (is_new_node_closer && this_closest))
            {
                EDSimulator.add(nowTime+1,Event.STORE_REQUEST,this.node,node,new Payload(key,value.getValue()));
            }

        }
        routingTable.addContact(node);
    }

    /**
     * This node received a FIND_NODE request
     */
    public List<Node> callFindNode(HashMap<BigInteger, Protocol> nodeToProtocolMap, Node nodeToAsk, BigInteger targetId) {
        Protocol protocol = nodeToProtocolMap.getOrDefault(nodeToAsk.getId(), null);
        Event result = null;
        if(protocol==null)
        {
            RoutingTable table = this.getRoutingTable();
            table.removeContact(nodeToAsk);
            System.out.println("Protocol with id - "+nodeToAsk+" does not exist");
            return null;
        }
        if (protocol != null) {
            result = protocol.rpcFindNodeRequest(this.node, targetId);
        }
        // handle call response - function call
        if (result != null) {
            //result.sender = nodeToAsk;
            //handleResponse(result,nodeToProtocolMap);
            EDSimulator.add(nowTime+1,Event.RPC_FIND_NODE_RESPONSE,result.sender,result.target,result.payload);
        }
        return result.payload.nodes;
    }

    public Event rpcFindNodeRequest(Node sender, BigInteger targetId) {
        this.welcomeIfNew(sender);
        //System.out.println("Node " + this.node.getId() + " received FIND_NODE request from " + sender.getId() + " for target " + targetId);

        List<Node> foundNodes = this.routingTable.findNeighbors(new Node(targetId),  sender);
        //System.out.println("size - " + foundNodes.size());
        Payload payload = new Payload(foundNodes);
        return new Event(nowTime+1, Event.RPC_FIND_NODE_RESPONSE, this.node, sender, payload);
    }

    /*public void rpcFindNodeRequest(Node sender, Node receiver) {
        this.welcomeIfNew(sender);
        List<Node> neighbors = this.routingTable.findNeighbors(receiver, sender);
        EDSimulator.add(1, Event.RPC_FIND_NODE_RESPONSE, this.node, sender, new Payload(neighbors));
    }*/

    /**
     * Performed when receiving a response to FIND_NODE (or feel free refactor to more general case)
     */
    /*public void rpcFindNodeResponse(List<Node> nodes) {
        // TODO: change this to complete node lookup procedure, now it just adds the contacts received
        for (Node node : nodes) {
            this.welcomeIfNew(node);
        }
        // is this response from which node?
        // how many requests i have outstanding?
        // should i send another request to another node?
    }*/


    /**
     * Called when this node joins the network.
     * @param bootstrapNode
     *  The known node to ask for a list of neighbors closest to self
     */
    public void bootstrap(List<Node> nodes, HashMap<BigInteger,Protocol> nodeToProtocolMap) {
        List<Node> bootstrappedNodes = new ArrayList<>();
        for (Node node : nodes) {
            Node bootstrappedNode = bootstrapNode(nodeToProtocolMap,node);
            if (bootstrappedNode != null) {
                bootstrappedNodes.add(bootstrappedNode);
            }
        }

        /*NetworkCrawler spider = new NetworkCrawler(this, localNode, bootstrappedNodes, ksize, alpha);
        return spider.find();*/
    }

    public Node bootstrapNode(HashMap<BigInteger,Protocol> nodeToProtocolMap,Node node) {
        boolean success = this.callPing(nodeToProtocolMap, node.getId());
        if (success) {
            return node;
        } else {
            return null;
        }
    }

    /*public void bootstrap(Node bootstrapNode) {
        // TODO: change this to complete node lookup procedure, now it just calls FIND_NODE on the bootstrap node
        this.welcomeIfNew(bootstrapNode);
        Payload payload = new Payload(this.node);
        EDSimulator.add(nowTime+1, Event.RPC_FIND_NODE_REQUEST, this.node, bootstrapNode, payload);
    }*/

    public void rpcNodeLookUpRequest(NetworkCrawler crawler, Node target, HashMap<BigInteger, Protocol> map)
    {
        List<Node> lookedUpNodes = crawler.nodeLookupBegin(target, map, nowTime, this.node);
        EDSimulator.add(nowTime+1, Event.NODE_LOOKUP_RESPONSE, target,this.node,new Payload(lookedUpNodes));
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
    public void callRemoteStore(HashMap<BigInteger,Protocol> nodeToProtocolMap, BigInteger NodeToAsk,BigInteger key, String value)
    {
        //System.out.println("Call store sender " + this.node.getId()+" receiver " + NodeToAsk);
        Node sender = this.node;
        Protocol protocol = nodeToProtocolMap.getOrDefault(NodeToAsk,null);
        //System.out.println("Found protocol for "  + protocol.node.getId());
        if(protocol == null)
        {
            RoutingTable table = this.getRoutingTable();
            table.removeContact(new Node(NodeToAsk));
            System.out.println("Target node with Id- " + NodeToAsk +" does not exist");
            return;
        }
        Event result = null;
        if(protocol!=null)
        {
            result = protocol.rpcStoreRequest(sender,key,value);
        }
        // handle call response - function call
        if(result!=null)
        {
            result.sender = new Node(NodeToAsk);
            //handleResponse(result,nodeToProtocolMap);
            EDSimulator.add(nowTime+1,Event.STORE_RESPONSE,result.sender,result.target,result.payload);
        }

    }
    public Event rpcStoreRequest(Node sender,BigInteger key,String value)
    {
        System.out.println("Sender is - "+sender.getId()+" Receiver is - " + this.node.getId());
        //System.out.println(this.node.getId()+" storing key "+ key+" and value "+ value);
        this.welcomeIfNew(sender);
        this.storage.setValue(key,value,0);
        //System.out.println("Stored: " + this.storage.getContentValue(key));
        //EDSimulator.add(5,Event.STORE_RESPONSE,this.node,sender,new Payload(key,value));
        return new Event(nowTime+1,Event.STORE_RESPONSE,this.node,sender,new Payload(key,value,"OK"));
    }

    public boolean callPing(HashMap<BigInteger, Protocol> nodeToProtocolMap, BigInteger nodeIdToAsk) {
        Protocol protocol = nodeToProtocolMap.getOrDefault(nodeIdToAsk, null);
        if(protocol==null)
        {
            RoutingTable table = this.getRoutingTable();
            table.removeContact(new Node(nodeIdToAsk));
            System.out.println("Target node with Id- " + nodeIdToAsk +" does not exist");
            return false;
        }
        Event result = null;
        if (protocol != null) {
            result = protocol.rpcPingRequest(this.node);
        }
        // handle call response - function call
        if (result != null) {
            result.sender = new Node(nodeIdToAsk);
            handleResponse(result,nodeToProtocolMap);
            //EDSimulator.add(nowTime+1,Event.PING_RESPONSE,result.sender,result.target,result.payload);
        }
        return true;
    }

    public Event rpcPingRequest(Node sender) {
        // Welcome the sender if it's a new node
        this.welcomeIfNew(sender);

        // Reply with a PING_RESPONSE event
        return new Event(nowTime+1, Event.PING_RESPONSE, this.node, sender, new Payload("OK"));
    }



    public void handleResponse(Event event, HashMap<BigInteger,Protocol> nodeToProtocolMap)
    {
        //System.out.println("Handle response called");
        // equivalent to not getting a response
        /*if(event.payload.message=="NOT_OK")
        {
            // TODO: - print useful information - maybe which event's response is Not ok ?
            RoutingTable table = this.getRoutingTable();
            table.removeContact(event.sender);
            return;
        }*/
        switch(event.type) {
            case Event.STORE_RESPONSE:
                System.out.println("Node " + event.sender + " stored value " + event.payload.valueToStore);
                break;
            case Event.RPC_FIND_NODE_RESPONSE:
                System.out.println("Node " + event.sender.getId()+" returned " + event.payload.nodes.size()+" nodes");
                break;
            case Event.PING_RESPONSE:
                System.out.println("Node " + event.target.getId() +" received ping response from " + event.sender.getId());
                break;
        }
        //System.out.println(event.target.getId());
        Protocol protocol = nodeToProtocolMap.get(event.target.getId());
        protocol.welcomeIfNew(event.sender);
        return;
    }

    public void printStorage()
    {
        System.out.println("Key-value pairs stored on "+ this.node.getId());

        for(Map.Entry<BigInteger,Content> entry : this.storage.getNodeStorageTable().entrySet())
        {
            System.out.println("<"+entry.getKey()+", "+entry.getValue().getValue()+">");
        }
        System.out.println("--------");
    }
    /**
     * Called by simulator
     */
    public void processEvent(Event event, NetworkCrawler crawler, HashMap<BigInteger, Protocol> map) {
        nowTime = Math.max(nowTime,event.timestamp);
        switch (event.type) {

            case Event.BOOTSTRAP:
                this.bootstrap(event.payload.nodes,map);
                break;
            case Event.RPC_FIND_NODE_REQUEST:
                this.callFindNode(map,event.target, event.payload.node.getId());
                break;
            case Event.RPC_FIND_NODE_RESPONSE, Event.STORE_RESPONSE, Event.PING_RESPONSE:
                this.handleResponse(event,map);
                break;
            case Event.NODE_LOOKUP_REQUEST:
                this.rpcNodeLookUpRequest(crawler,event.target,map);
                break;
            case Event.STORE_REQUEST:
                this.callRemoteStore(map,event.target.getId(),event.payload.keyToStore, event.payload.valueToStore);
                break;
            case Event.PING_REQUEST:
                this.callPing(map,event.target.getId());
                break;
        }
    }

}