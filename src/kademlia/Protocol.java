package kademlia;

import components.Content;
import simulator.*;

import java.math.BigInteger;
import java.util.*;

import static kademlia.Util.digest;

public class Protocol {
    public Node node; // (Dumb) Node representation of this Protocol, without state
    public RoutingTable routingTable;

    public NodeStorage storage;

    public static long nowTime = 0;

    public static int k;

    public static int alpha;

    public Set<Node> welcomed = new HashSet<>();
    public Protocol(Config config) {
        this.k = config.k;
        this.alpha = config.alpha;
        BigInteger nodeId = new BigInteger(config.bitSpace, config.rng);
        this.node = new Node(nodeId);
        this.routingTable = new RoutingTable(nodeId, config);
        this.storage = new NodeStorage();
    }

    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
    }

    public void refresh(HashMap<BigInteger, Protocol> nodeToProtocol)
    {
        System.out.println("Calling refresh for node "+this.node.getId());
        RoutingTable table = this.getRoutingTable();
        ArrayList<BigInteger> randomBigInt = new ArrayList<>();
        for(KBucket bucket : table.kBuckets)
        {
            BigInteger upper = bucket.rangeUpper;
            BigInteger lower = bucket.rangeLower;

            BigInteger random = Util.getRandomBigInteger(lower,upper,EDSimulator.globalRandom);
            randomBigInt.add(random);
        }
        for(BigInteger randomBig : randomBigInt)
        {
            NetworkCrawler crawler = new NetworkCrawler(this.k,this.alpha,this);
            crawler.nodeLookupBegin(new Node(randomBig),nodeToProtocol,nowTime);
        }
    }
    public void welcomeIfNew(Node node, HashMap<BigInteger, Protocol> nodeToProtocolMap) {

        //System.out.println("Welcome if new called for - " + node.getId()+" on routing table of " + this.node.getId());
        if (!routingTable.isNewNode(node) || this.welcomed.contains(node)) {
            //System.out.println("Returning because node " + node.getId()+" is not new");
            return;
        }
        else {
            this.welcomed.add(node);
        }
        //System.out.println("New Node is being added - "+node.getId());
        // TODO: store keys in new node
        HashSet<BigInteger> keysToStore = new HashSet<>();
        for(Map.Entry<BigInteger,Content> stored : storage.getNodeStorageTable().entrySet())
        {
            BigInteger key = stored.getKey();
            Content value = stored.getValue();
            // Get digest of key
            Node keynode = new Node(digest(key));
            List<Node> neighbours = this.routingTable.findNeighbors(keynode,this.node);
            /*System.out.println("Neighbours - ");
            for(Node n : neighbours)
            {
                System.out.print(n.getId()+" ");
            }*/
            //System.out.println();
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
                keysToStore.add(key);
                //EDSimulator.add(1,Event.STORE_REQUEST,this.node,node,new Payload(key,value.getValue()));
                //this.callRemoteStore(nodeToProtocolMap,node.getId(),key,value.getValue(),true);
            }

        }

        for(BigInteger key : keysToStore)
        {
            String value = storage.getContentValue(key);
            this.callRemoteStore(nodeToProtocolMap,node.getId(),key,value,true);
        }
        routingTable.addContact(node);
        this.welcomed.remove(node);
    }

    /**
     * This node received a FIND_NODE request
     */
    public Payload callFindValue(HashMap<BigInteger, Protocol> nodeToProtocolMap, Node nodeToAsk, BigInteger targetId, boolean external) {
        Protocol protocol = nodeToProtocolMap.getOrDefault(nodeToAsk.getId(), null);
        Event result = null;
        if (protocol == null) {
            RoutingTable table = this.getRoutingTable();
            table.removeContact(nodeToAsk);
            System.out.println("Protocol with id - " + nodeToAsk + " removed from routing table");
            return null;
        }

        if (protocol != null) {
            result = protocol.rpcFindValueRequest(nodeToProtocolMap,this.node, targetId);
        }

        if (result != null) {
            if (external == false) {
                EDSimulator.add(1, Event.RPC_FIND_VAL_RESPONSE, result.sender, result.target, result.payload);
            } else {
                result.type = Event.DEFAULT;
                this.handleResponse(result, nodeToProtocolMap);
            }
        }
        return result.payload;
    }

    public Payload callFindValue(HashMap<BigInteger, Protocol> nodeToProtocolMap, Node nodeToAsk, BigInteger targetId) {
        return this.callFindValue(nodeToProtocolMap,nodeToAsk,targetId,false);
    }

    public Event rpcFindValueRequest(HashMap<BigInteger, Protocol> nodeToProtocolMap,Node sender, BigInteger targetId) {
        this.welcomeIfNew(sender,nodeToProtocolMap);
        //System.out.println("Node " + this.node.getId() + " received FIND_VALUE request from " + sender.getId() + " for target " + targetId);

        // Check if this node has the value associated with the targetId
        String value = this.storage.getContentValue(targetId);

        if (value != null) {
            System.out.println("Node " + this.node.getId() + " has the value for target " + targetId);
            Payload payload = new Payload(targetId, value);
            return new Event(EDSimulator.globalRandom.nextLong(),1, Event.RPC_FIND_VAL_RESPONSE, this.node, sender, payload);
        } else {
            // If value is not found, return the K closest nodes
            List<Node> foundNodes = this.routingTable.findNeighbors(new Node(targetId), sender);
            Payload payload = new Payload(foundNodes);
            return new Event(EDSimulator.globalRandom.nextLong(),1, Event.RPC_FIND_NODE_RESPONSE, this.node, sender, payload);
        }
    }
    public List<Node> callFindNode(HashMap<BigInteger, Protocol> nodeToProtocolMap, Node nodeToAsk, BigInteger targetId, boolean external) {
        Protocol protocol = nodeToProtocolMap.getOrDefault(nodeToAsk.getId(), null);
        Event result = null;
        if(protocol==null)
        {
            RoutingTable table = this.getRoutingTable();
            table.removeContact(nodeToAsk);
            System.out.println("Protocol with id - "+nodeToAsk+" does not exist");
            return null;
        }
        //if(targetId.equals(BigInteger.valueOf(3)))
        if (protocol != null) {
            result = protocol.rpcFindNodeRequest(this.node, targetId,nodeToProtocolMap);
        }
        // handle call response - function call
        if (result != null) {
            if(external==false)
                EDSimulator.add(1,Event.RPC_FIND_NODE_RESPONSE,result.sender,result.target,result.payload);
            else
            {
                result.type = Event.DEFAULT;
                this.handleResponse(result,nodeToProtocolMap);
            }
        }
        return result.payload.nodes;
    }

    public List<Node> callFindNode(HashMap<BigInteger, Protocol> nodeToProtocolMap, Node nodeToAsk, BigInteger targetId) {
        return this.callFindNode(nodeToProtocolMap,nodeToAsk,targetId,false);
    }

    public Event rpcFindNodeRequest(Node sender, BigInteger targetId,HashMap<BigInteger, Protocol> nodeToProtocolMap) {
        this.welcomeIfNew(sender,nodeToProtocolMap);
        //System.out.println("Node " + this.node.getId() + " received FIND_NODE request from " + sender.getId() + " for target " + targetId);
        List<Node> foundNodes = this.routingTable.findNeighbors(new Node(targetId),  sender);
        //System.out.println("size - " + foundNodes.size());
        Payload payload = new Payload(foundNodes);
        return new Event(EDSimulator.globalRandom.nextLong(),1, Event.RPC_FIND_NODE_RESPONSE, this.node, sender, payload);
    }

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

        NetworkCrawler spider = new NetworkCrawler(this.k,this.alpha, this);
        spider.nodeLookupBegin(this.node,nodeToProtocolMap,nowTime);

        this.refresh(nodeToProtocolMap);
    }

    public Node bootstrapNode(HashMap<BigInteger,Protocol> nodeToProtocolMap,Node node) {
        boolean success = this.callPing(nodeToProtocolMap, node.getId(),true);
        if (success) {
            return node;
        } else {
            return null;
        }
    }

    public void rpcNodeLookUpRequest(Node target, HashMap<BigInteger, Protocol> map)
    {
        NetworkCrawler crawler = new NetworkCrawler(this.k,this.alpha,this);
        List<Node> lookedUpNodes = crawler.nodeLookupBegin(target, map, nowTime);
        EDSimulator.add(1, Event.NODE_LOOKUP_RESPONSE, null,this.node,new Payload(lookedUpNodes));
    }

    public void rpcValueLookUpRequest(Node target, HashMap<BigInteger, Protocol> map)
    {
        NetworkCrawler crawler = new NetworkCrawler(this.k,this.alpha,this);
        String response = crawler.valueLookUpBegin(target, map, nowTime);
        EDSimulator.add(1, Event.VALUE_LOOKUP_RESPONSE, null,this.node,new Payload(null,response));
    }

    public boolean callRemoteStore(HashMap<BigInteger,Protocol> nodeToProtocolMap, BigInteger NodeToAsk,BigInteger key, String value,boolean external)
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
            return false;
        }
        Event result = null;
        if(protocol!=null)
        {
            result = protocol.rpcStoreRequest(sender,key,value,nodeToProtocolMap);
        }
        // handle call response - function call
        if(result!=null)
        {
            result.sender = new Node(NodeToAsk);
            //handleResponse(result,nodeToProtocolMap);
            if(external==false)
                EDSimulator.add(1,Event.STORE_RESPONSE,result.sender,result.target,result.payload);
            else {
                result.type = Event.DEFAULT;
                this.handleResponse(result,nodeToProtocolMap);
            }
        }
        return true;
    }
    public boolean callRemoteStore(HashMap<BigInteger,Protocol> nodeToProtocolMap, BigInteger NodeToAsk,BigInteger key, String value)
    {
        return this.callRemoteStore(nodeToProtocolMap,NodeToAsk,key,value,false);
    }
    public Event rpcStoreRequest(Node sender,BigInteger key,String value,HashMap<BigInteger,Protocol> nodeToProtocolMap)
    {
        //System.out.println("Sender is - "+sender.getId()+" Receiver is - " + this.node.getId());
        //System.out.println(this.node.getId()+" storing key "+ key+" and value "+ value);
        this.welcomeIfNew(sender,nodeToProtocolMap);
        this.storage.setValue(key,value,0);
        return new Event(EDSimulator.globalRandom.nextLong(),1,Event.STORE_RESPONSE,this.node,sender,new Payload(key,value,"OK"));
    }

    public boolean callPing(HashMap<BigInteger, Protocol> nodeToProtocolMap, BigInteger nodeIdToAsk, boolean external) {
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
            result = protocol.rpcPingRequest(this.node,nodeToProtocolMap);
        }
        // handle call response - function call
        if (result != null ) {
            result.sender = new Node(nodeIdToAsk);
            if(external==false){
                EDSimulator.add(1,result.type,result.sender,result.target,result.payload);
            }
            else {
                result.type = Event.DEFAULT;
                handleResponse(result,nodeToProtocolMap);
            }
        }
        return true;
    }
    public boolean callPing(HashMap<BigInteger, Protocol> nodeToProtocolMap, BigInteger nodeIdToAsk) {
        return this.callPing(nodeToProtocolMap,nodeIdToAsk,false);
    }
    public Event rpcPingRequest(Node sender,HashMap<BigInteger, Protocol> nodeToProtocolMap) {
        // Welcome the sender if it's a new node
        this.welcomeIfNew(sender,nodeToProtocolMap);

        // Reply with a PING_RESPONSE event
        return new Event(EDSimulator.globalRandom.nextLong(),1, Event.PING_RESPONSE, this.node, sender, new Payload("OK"));
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
            case Event.DEFAULT,Event.PING_RESPONSE:
                break;
            case Event.STORE_RESPONSE:
                System.out.println("Node " + event.sender + " stored value " + event.payload.valueToStore);
                break;
            case Event.RPC_FIND_NODE_RESPONSE:
                System.out.print("Node " + event.sender.getId()+" returned nodes ");
                for(Node n : event.payload.nodes)
                {
                    System.out.print(n.getId()+" ");
                }
                System.out.println();
                break;
            case Event.RPC_FIND_VAL_RESPONSE:
                if(event.payload.keyToStore!=null)
                {
                    System.out.println("Value " + event.payload.valueToStore +" was found at Node "+event.sender);
                }
                else if(event.payload.nodes!=null){
                    System.out.print("Node " + event.sender.getId()+" returned nodes ");
                    for(Node n : event.payload.nodes)
                    {
                        System.out.print(n.getId()+" ");
                    }
                    System.out.println();
                    break;
                }
        }
        //System.out.println(event.target.getId());
        Protocol protocol = nodeToProtocolMap.get(event.target.getId());
        protocol.welcomeIfNew(event.sender,nodeToProtocolMap);
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

    public String get(long eventId,BigInteger key, HashMap<BigInteger,Protocol> nodeToProtocolMap) {
        BigInteger dkey = Util.digest(key);
        System.out.println("Looking up key " + key);

        // If this node has it, return it
        if (this.storage.getContentValue(dkey) != null) {
            EDSimulator.getRecords.add(new GetRecordBookKeeper(eventId,0,0,this.node,this.storage.getContentValue(dkey),key));
            return this.storage.getContentValue(dkey);
        }
        NetworkCrawler spider = new NetworkCrawler(this.k , this.alpha, this);
        String result = spider.valueLookUpBegin(new Node(dkey),nodeToProtocolMap,nowTime);
        EDSimulator.getRecords.add(new GetRecordBookKeeper(eventId,spider.lookUpTime,spider.numRounds,this.node,result,key));
        //System.out.println("Added to getrecords,curr size - "+EDSimulator.getRecords.size());
        return result;
    }

    public boolean set(long eventId,BigInteger key, String value,HashMap<BigInteger,Protocol> nodeToProtocolMap){
        System.out.println("Setting '" + key + "' = '" + value + "' on network, initiated by " + this.node.getId());
        return setDigest(eventId, key, value,nodeToProtocolMap);

    }

    public boolean setDigest(long eventId,BigInteger key,String value,HashMap<BigInteger,Protocol> nodeToProtocolMap)  {
        BigInteger dkey = digest(key);
        Node keyNode = new Node(dkey);

        NetworkCrawler spider = new NetworkCrawler(this.k, this.alpha, this);
        List<Node> nodes = spider.nodeLookupBegin(keyNode, nodeToProtocolMap,nowTime);
        System.out.println("Setting '" + dkey + "' on " + nodes);

        // If this node is close too, then store here as well
        BigInteger biggest = BigInteger.ZERO;
        for (Node n : nodes) {
            BigInteger distance = n.distanceTo(keyNode);
            if (distance.compareTo(biggest) > 0) {
                biggest = distance;
            }
        }

        if (this.node.distanceTo(keyNode).compareTo(biggest) < 0) {
            this.storage.setValue(dkey, value, 0);
        }

        // Call store on all found nodes
        boolean atLeastOne = false;
        //System.out.println("Nodes found-");
        for (Node n : nodes) {

            //System.out.print(n.getId()+" ");
            boolean call = this.callRemoteStore(nodeToProtocolMap,n.getId(), dkey, value,true);
            atLeastOne = atLeastOne || call;
        }
        //System.out.println();
        EDSimulator.setRecords.add(new SetRecordBookKeeper(eventId,spider.lookUpTime,spider.numRounds,this.node,value,key,atLeastOne,nodes));
        // Return true only if at least one store call succeeded
        return atLeastOne;
    }


    /**
     * Called by simulator
     */
    public void processEvent(Event event,HashMap<BigInteger, Protocol> map) {
        nowTime = Math.max(nowTime,event.timestamp);
        switch (event.type) {

            case Event.BOOTSTRAP:
                this.bootstrap(event.payload.nodes,map);
                break;
            case Event.RPC_FIND_NODE_REQUEST:
                this.callFindNode(map,event.target, event.payload.node.getId());
                break;
            case Event.RPC_FIND_NODE_RESPONSE, Event.STORE_RESPONSE, Event.PING_RESPONSE, Event.RPC_FIND_VAL_RESPONSE,Event.VALUE_LOOKUP_RESPONSE:
                this.handleResponse(event,map);
                break;
            case Event.NODE_LOOKUP_REQUEST:
                this.rpcNodeLookUpRequest(event.payload.node,map);
                break;
            case Event.STORE_REQUEST:
                this.callRemoteStore(map,event.target.getId(),event.payload.keyToStore, event.payload.valueToStore);
                break;
            case Event.PING_REQUEST:
                this.callPing(map,event.target.getId());
                break;
            case Event.RPC_FIND_VAL_REQUEST:
                this.callFindValue(map,event.target,event.payload.keyToStore);
                break;
            case Event.VALUE_LOOKUP_REQUEST:
                this.rpcValueLookUpRequest(event.payload.node,map);
                break;
            case Event.GET_REQUEST:
                String result = this.get(event.eventId,event.payload.keyToStore, map);
                if(result!=null)
                    System.out.println("Value lookup returned " + result);
                break;
            case Event.SET_REQUEST:
                this.set(event.eventId,event.payload.keyToStore, event.payload.valueToStore,map);
                break;
            case Event.REFRESH_OPERATION:
                this.refresh(map);
                break;
        }
    }

}