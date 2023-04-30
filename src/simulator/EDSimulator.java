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
    public static void start(int nNodes, int nBootstraps, int bitSpace, int k) {
        // 1. Create nodes
        int createdNodes = 0;
        HashSet<BigInteger> nodeIds = new HashSet<>();
        // Increment seed and pass it as value to random constructor while constructing Config
        long seed = 30;
        while (createdNodes < nNodes) {
            // if our bit-space is small, it may clash, just loop until created as needed
            Protocol protocol = new Protocol(new Config(bitSpace, k, new Random(seed++)));
            Protocol prevProtocol = nodeIdToProtocol.putIfAbsent(protocol.node.id, protocol);
            if (prevProtocol == null) {
                protocols.add(protocol);
                createdNodes += 1;
                nodeIds.add(protocol.node.getId());
            }
        }

        Random random=new Random(seed++);
        int randomNode=random.nextInt(nNodes-1)+1;
        //System.out.println("Target node - " + protocols.get(randomNode));
        Node targetForLookup = protocols.get(randomNode).node;


        // 2. Run bootstraps (fixed bootstrap node)
        Node bootstrapNode = protocols.get(0).node;
        NetworkCrawler crawler = new NetworkCrawler(k, 2, protocols.get((randomNode+1)%nNodes));
        List<Node> arg = new ArrayList<>();
        arg.add(bootstrapNode);
        for (int i = 0; i < nBootstraps; i++) {
            Node eventSender = protocols.get(1+i).node; // sequential after bootstrap node
            EDSimulator.add(0, Event.BOOTSTRAP,eventSender , null, new Payload(arg));
        }
        EDSimulator.add(1,Event.RPC_FIND_NODE_REQUEST,protocols.get(4).node,bootstrapNode,new Payload(new Node(BigInteger.valueOf(1))));
        BigInteger randomTarget = BigInteger.ZERO;
        for(int i=0; i< Math.pow((double) 2, bitSpace);i++)
        {
            if(!nodeIds.contains(BigInteger.valueOf(i))) {
                randomTarget = BigInteger.valueOf(i);
                break;
            }
        }
        testRemoteStore(bootstrapNode,randomNode);
        //EDSimulator.add(queue.size()+1, Event.NODE_LOOKUP_REQUEST, null,protocols.get(randomNode).node,new Payload(protocols.get((randomNode+1)%nNodes).node));
        //printEventQueue();
        // perform the actual simulation
        boolean exit = false;
        while (!exit && queue.size()>0) {
            exit = executeNext(crawler);
        }
    }
    public static void testRemoteStore(Node bootstrapNode,int randomNode)
    {
        EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,bootstrapNode,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(4),"4"));
        EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,protocols.get(randomNode).node,bootstrapNode,new Payload(BigInteger.valueOf(60),"60"));
        //EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,bootstrapNode,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(70),"70"));
        //EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,protocols.get(randomNode).node,bootstrapNode,new Payload(BigInteger.valueOf(80),"80"));
        EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,protocols.get(4).node,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(90),"90"));
        EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,protocols.get(2).node,bootstrapNode,new Payload(BigInteger.valueOf(70),"70"));
    }
    public static  void printEventQueue()
    {
        System.out.println("Printing initial queue");
        for(Event event : queue)
        {
            if(event.type == Event.STORE_REQUEST)
                System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload.keyToStore+","+event.payload.valueToStore);
            else
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

    /**
     * Execute and remove the next event from the ordered event list.
     * @return true if the execution should be stopped.
     */
    private static boolean executeNext(NetworkCrawler crawler) {
        Event event = null;
        if(queue.size()>0)
        {
            event = queue.poll();
        }
        if (event == null) { // should never happen
            return true;
        }
        CommonState.setTime(event.timestamp);
        System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload);
        Protocol eventSender = EDSimulator.nodeIdToProtocol.getOrDefault(event.sender.id,null);
        if(event.type!= Event.BOOTSTRAP &&  nodeIdToProtocol.getOrDefault(event.target.id,null) == null)
        {
            System.out.println("Target node is not on network - " +event.target.id);
            return true;
        }
        eventSender.processEvent(event, crawler, nodeIdToProtocol);
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


    public static void checkFindNeighbors()
    {
        /*for(int i=0;i<protocols.size();i++)
        {
            System.out.println(protocols.get(i).node.getId());
        }*/
        System.out.println("Calling FN for node - " + protocols.get(3
        ).node.getId());
        List<Node> nodes = nodeIdToProtocol.get(BigInteger.valueOf(2)).routingTable.findNeighbors(protocols.get(3).node,null);

        for(int i=0;i<nodes.size();i++)
        {
            System.out.println(nodes.get(i).getId());
        }
    }
}
