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
        //System.out.println(protocols.get(1).node.getId());
        for (int i = 0; i < nBootstraps; i++) {
            Node eventTarget = protocols.get(1+i).node; // sequential after bootstrap node
            EDSimulator.add(0, Event.BOOTSTRAP, null, eventTarget, new Payload(bootstrapNode));
        }

        BigInteger randomTarget = BigInteger.ZERO;
        for(int i=0; i< Math.pow((double) 2, bitSpace);i++)
        {
            if(!nodeIds.contains(BigInteger.valueOf(i))) {
                randomTarget = BigInteger.valueOf(i);
                break;
            }
        }
        EDSimulator.add(queue.size()+1,Event.STORE_REQUEST,bootstrapNode,protocols.get(randomNode).node,new Payload(BigInteger.valueOf(50),"50"));
        EDSimulator.add(queue.size()+1, Event.NODE_LOOKUP_REQUEST, null,protocols.get(randomNode).node,new Payload(protocols.get((randomNode+1)%nNodes).node));
        //printEventQueue();
        // perform the actual simulation
        boolean exit = false;
        while (!exit) {
            exit = executeNext(crawler);
        }

        /*System.out.println("Calling findNeighbors for - " + protocols.get(1).node.getId()+" in routing table of -" +protocols.get(0).node.getId());
        List<Node> neighbors = protocols.get(0).routingTable.findNeighbors(protocols.get(1).node,null);
        for(Node n : neighbors)
        {
            System.out.print(n.getId() + " ");
        }
        System.out.println();*/
    }
    public static  void printEventQueue()
    {
        System.out.println("Printing initial queue");
        for(Event event : queue)
        {
            System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload);
        }
    }
    public static void printEndState() {
        for (Protocol protocol : protocols) {
            System.out.printf("Node %s \n", protocol.node);
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
        Event event = queue.poll();
        if (event == null) { // should never happen
            return true;
        }
        CommonState.setTime(event.timestamp);
        System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload);
        Protocol eventTarget = EDSimulator.nodeIdToProtocol.get(event.target.id);
        eventTarget.processEvent(event, crawler, nodeIdToProtocol);
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
