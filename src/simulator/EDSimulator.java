package simulator;

import java.math.BigInteger;
import java.util.*;
import kademlia.*;

public class EDSimulator {
    private static final PriorityQueue<Event> queue = new PriorityQueue<Event>();
    private static final List<Protocol> protocols = new ArrayList<>(); // we pick from this list to create random events
    private static final HashMap<BigInteger, Protocol> nodeIdToProtocol = new HashMap<BigInteger, Protocol>();

    private EDSimulator() {} // prevent construction

    /**
     * Runs an experiment
     * @param nNodes - number of nodes
     * @param nBootstraps - number of bootstrap operations (< nNodes)
     */
    public static void start(int nNodes, int nBootstraps, int bitSpace, int k, int alpha) {



        // 1. Create nodes
        int createdNodes = 0;
        HashSet<BigInteger> nodeIds = new HashSet<>();
        // Increment seed and pass it as value to random constructor while constructing Config
        long seed = 30;


        while (createdNodes < nNodes) {
            // if our bit-space is small, it may clash, just loop until created as needed
            Protocol protocol = new Protocol(new Config(bitSpace, k, new Random(seed++),k,alpha));
            Protocol prevProtocol = nodeIdToProtocol.putIfAbsent(protocol.node.id, protocol);
            if (prevProtocol == null) {
                protocols.add(protocol);
                createdNodes += 1;
                nodeIds.add(protocol.node.getId());
            }
        }

        // 2.1 Bootstrap requests from each node to 'nBootstrap' random nodes

        for(int i=1;i<nNodes;i++)
        {
            List<Integer> result = EDSimulator.getRandomNumbers(nBootstraps,i, seed++);
            /*System.out.println("Indexes");
            for(int n : result)
            {
                System.out.print(n+" ");
            }
            System.out.println();*/
            List<Node> nodes = new ArrayList<>();
            for(int index : result)
            {
                nodes.add(protocols.get(index).node);
            }
            /*System.out.println("Nodes to bootstrap");
            for(Node n : nodes)
            {
                System.out.print(n.getId()+" ");
            }
            System.out.println();*/
            EDSimulator.add(queue.size(), Event.BOOTSTRAP,protocols.get(i).node,null,new Payload(nodes));
        }

        //EDSimulator.add(1,Event.PING_REQUEST,protocols.get(2).node,protocols.get(4).node,new Payload("PING"));
        //EDSimulator.add(queue.size(), Event.STORE_REQUEST,protocols.get(0).node,protocols.get(3).node,new Payload(BigInteger.valueOf(7),"7"));
        //EDSimulator.add(queue.size(), Event.VALUE_LOOKUP_REQUEST,protocols.get(6).node,null,new Payload(new Node(BigInteger.valueOf(9))));
        //testRemoteStore(protocols.get(2).node,0);
        //EDSimulator.add(queue.size(), Event.RPC_FIND_VAL_REQUEST,protocols.get(2).node,protocols.get(3).node,new Payload(BigInteger.valueOf(5),null));
        //EDSimulator.add(1, Event.NODE_LOOKUP_REQUEST, protocols.get(1).node,protocols.get(randomNode).node,new Payload(protocols.get(randomNode).node));

        // perform the actual simulation
        boolean exit = false;
        while (!exit && queue.size()>0) {
            exit = executeNext();
        }

        //Reproduce error
        EDSimulator.printRoutingTable(protocols.get(6).node);
        List<Node> nodes = protocols.get(6).getRoutingTable().findNeighbors(new Node(BigInteger.valueOf(9)),protocols.get(6).node);
        System.out.println("Findneighbors returned");
        for(Node n : nodes)
        {
            System.out.print(n.getId()+" ");
        }
        System.out.println();
    }
    public static List<Integer> getRandomNumbers(int k, int n, long seed) {
        if (k >= n) {
            k = n;
        }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            numbers.add(i);
        }
        Random random  = new Random(seed);
        Collections.shuffle(numbers,random);
        return numbers.subList(0, k);

    }

    public static boolean killNode(List<Node> nodes)
    {
        for(Node n : nodes){
            BigInteger id = n.getId();
            nodeIdToProtocol.remove(id);
            Iterator<Protocol> it = protocols.iterator();
            while(it.hasNext())
            {
                if(it.next().node == n)
                {
                    it.remove();
                    break;
                }
            }
            System.out.println("Node "+id+" left network");
        }
        return true;
    }
    public static void testRemoteStore(Node bootstrapNode,int randomNode)
    {
        long nowTime = queue.size();
        EDSimulator.add(nowTime,Event.STORE_REQUEST,bootstrapNode,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(6),"6"));
        EDSimulator.add(nowTime,Event.STORE_REQUEST,protocols.get(randomNode).node,new Node(BigInteger.valueOf(20)),new Payload(BigInteger.valueOf(60),"60"));
        //EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,bootstrapNode,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(70),"70"));
        //EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,protocols.get(randomNode).node,bootstrapNode,new Payload(BigInteger.valueOf(80),"80"));
        EDSimulator.add(nowTime,Event.STORE_REQUEST,protocols.get(4).node,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(10),"10"));
        //EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,protocols.get(2).node,bootstrapNode,new Payload(BigInteger.valueOf(70),"70"));
    }
    public static  void printEventQueue()
    {
        System.out.println("Printing initial queue");
        for(Event event : queue)
        {
            if(event.type == Event.STORE_REQUEST)
            {
                System.out.println(event.payload.keyToStore);
                //System.out.println("event: " + event+" | target: " + event.target + " | sender: " + event.sender +" | payload: <" +
                       // event.payload.keyToStore.toString()+","+event.payload.valueToStore+">");
            }
            else if(event.type!= Event.KILL_NODE)
                System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload);
        }
    }
    public static void printEndState() {
        for (Protocol protocol : protocols) {
            System.out.printf("Node %s \n", protocol.node);
            protocol.printStorage();
            for (KBucket kBucket : protocol.routingTable.kBuckets) {
                System.out.printf("-> kBucket range: %s - %s | depth: %d\n", kBucket.rangeLower, kBucket.rangeUpper, kBucket.depth());
                System.out.printf("--> nodes: ");
                for (BigInteger nodeId : kBucket.nodes.keySet()) {
                    System.out.printf("%s ", nodeId);
                }
                System.out.printf("\n");
            }
        }
    }

    public static void printRoutingTable(Node n) {
        System.out.println("Printing routing table for Node " + n.getId());
        for (Protocol protocol : protocols) {
            if(!protocol.node.getId().equals(n.getId()))
                continue;
            System.out.printf("Node %s \n", protocol.node);
            protocol.printStorage();
            for (KBucket kBucket : protocol.routingTable.kBuckets) {
                System.out.printf("-> kBucket range: %s - %s | depth: %d\n", kBucket.rangeLower, kBucket.rangeUpper, kBucket.depth());
                System.out.printf("--> nodes: ");
                for (BigInteger nodeId : kBucket.nodes.keySet()) {
                    System.out.printf("%s ", nodeId);
                }
                System.out.printf("\n");
            }
        }
    }


    /**
     * Execute and remove the next event from the ordered event list.
     * @return true if the execution should be stopped.
     */
    private static boolean executeNext() {
        Event event = queue.poll();;

        if (event == null) { // should never happen
            return true;
        }
        CommonState.setTime(event.timestamp);
        if(event.type == Event.STORE_REQUEST)
        {
            System.out.println("event: " + event+" | target: " + event.target + " | sender: " + event.sender +" | payload: <" +
                    event.payload.keyToStore.toString()+","+event.payload.valueToStore+">");
        }
        else if(event.type!= Event.KILL_NODE) {
            System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload);
        }
        else if(event.type == Event.RPC_FIND_VAL_REQUEST){
            System.out.println("event: " + event+" | target: " + event.target + " | sender: " + event.sender +" | payload: " +
                    event.payload.keyToStore.toString());
        }
        //REASON: Because kill nodes should not be a protocol event
        if(event.type==Event.KILL_NODE)
        {
            EDSimulator.killNode(event.payload.nodes);
            return false;
        }
        // REASON: Because lookup responses don't have a sender
        if(event.type== Event.NODE_LOOKUP_RESPONSE || event.type == Event.VALUE_LOOKUP_RESPONSE)
        {
            return false;
        }
        Protocol eventSender = EDSimulator.nodeIdToProtocol.getOrDefault(event.sender.id,null);
        if(eventSender==null)
        {
            System.out.println("Sender " + event.sender +" does not exist in network");
            return false;
        }
        // REASON : Because bootstrap events have no target
        if((event.type!= Event.BOOTSTRAP && event.type!=Event.VALUE_LOOKUP_REQUEST && event.type!= Event.NODE_LOOKUP_REQUEST)
                &&  nodeIdToProtocol.getOrDefault(event.target.id,null) == null)
        {
            System.out.println("Target node is not on network - " +event.target.id);
            return false;
        }
        eventSender.processEvent(event, nodeIdToProtocol);
        return queue.size() == 0;
    }

    /**
     * Adds a new event to be scheduled, specifying delay time units and the target node.
     *
     * @param delay
     *   The number of time units before the event is scheduled (non-negative).
     */
    public static void add(long delay, int type, Node sender, Node target, Payload payload) {
        long currTime = CommonState.getTime();
        Event event = new Event(currTime + delay, type, sender, target, payload);
        queue.add(event);
    }

}
