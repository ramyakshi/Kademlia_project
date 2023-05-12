package simulator;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import kademlia.*;

public class EDSimulator {
    private static final PriorityQueue<Event> queue = new PriorityQueue<Event>();
    private static final List<Protocol> protocols = new ArrayList<>(); // we pick from this list to create random events
    public static final HashMap<BigInteger, Protocol> nodeIdToProtocol = new HashMap<BigInteger, Protocol>();

    private static int seed;
    public static int maxLatency = 0;

    public static Random globalRandom;

    public static final List<GetRecordBookKeeper> getRecords = new ArrayList<>();

    public static final List<SetRecordBookKeeper> setRecords = new ArrayList<>();

    private EDSimulator() {} // prevent construction

    public static void finish()
    {
        protocols.clear();
        nodeIdToProtocol.clear();
        getRecords.clear();
        setRecords.clear();
        queue.clear();
    }
    public static void start(int nNodes, int nBootstraps, int bitSpace, int k, int alpha,int maxLatency, int nGetReq, int nSetReq, int seed, int refreshEvery, float churnFrac) throws Exception{

        globalRandom = new Random(seed);
        int initialSeed = seed;
        EDSimulator.maxLatency = maxLatency;

        // 1. Create nodes
        int createdNodes = 0;
        // Increment seed and pass it as value to random constructor while constructing Config
        while (createdNodes < nNodes) {
            // if our bit-space is small, it may clash, just loop until created as needed
            Protocol protocol = new Protocol(new Config(bitSpace, k, new Random(seed++), alpha, refreshEvery));
            Protocol prevProtocol = nodeIdToProtocol.putIfAbsent(protocol.node.id, protocol);
            if (prevProtocol == null) {
                protocols.add(protocol);
                createdNodes += 1;
            }
        }

        // 2. Queue a bootstrap event for each node, using 'nBootstrap' other random nodes to perform the bootstrap
        for(int i=1; i<nNodes; i++)
        {
            List<Integer> randomIndexes = EDSimulator.getRandomNumbers(nBootstraps, i, seed++);
            List<Node> bootstrapNodes = new ArrayList<>();
            for(int index : randomIndexes)
            {
                bootstrapNodes.add(protocols.get(index).node);
            }
            EDSimulator.add(queue.size(), Event.BOOTSTRAP, protocols.get(i).node,null, new Payload(bootstrapNodes));
        }

        //EDSimulator.add(1,Event.PING_REQUEST,protocols.get(2).node,protocols.get(4).node,new Payload("PING"));
        //EDSimulator.add(queue.size(), Event.STORE_REQUEST,protocols.get(2).node,protocols.get(8).node,new Payload(BigInteger.valueOf(4),"3"));
        //EDSimulator.add(queue.size()+1, Event.VALUE_LOOKUP_REQUEST,protocols.get(9).node,null,new Payload(new Node(BigInteger.valueOf(4))));
        //testRemoteStore(protocols.get(2).node,0);
        //EDSimulator.add(queue.size(), Event.RPC_FIND_VAL_REQUEST,protocols.get(2).node,protocols.get(3).node,new Payload(BigInteger.valueOf(5),null));
        //EDSimulator.add(1, Event.NODE_LOOKUP_REQUEST, protocols.get(1).node,protocols.get(randomNode).node,new Payload(protocols.get(randomNode).node));

        //EDSimulator.add(queue.size(),Event.SET_REQUEST,protocols.get(13).node,null,new Payload(BigInteger.valueOf(33),"51"));
        //EDSimulator.add(queue.size(),Event.GET_REQUEST,protocols.get(18).node,null,new Payload(BigInteger.valueOf(33),"51"));

        // 3. Queue SET requests
        List<BigInteger> keySet = new ArrayList<>();
        for(int i=0;i<nSetReq;i++)
        {
            int randomIndex = globalRandom.nextInt(nNodes);
            BigInteger randomKey = BigInteger.valueOf(globalRandom.nextInt());
            keySet.add(randomKey);
            EDSimulator.add(queue.size(),Event.SET_REQUEST,protocols.get(randomIndex).node,null,new Payload(randomKey,randomKey.toString()));
        }

        // 4. Queue Churn
        List<Node> nodesToKill = new ArrayList<>();
        List<BigInteger> nodeIdsToKill = new ArrayList<>();
        if(churnFrac>0)
        {
            List<Integer> indicesToKill = getRandomNumbers((int)Math.ceil(churnFrac*nNodes),nNodes,seed++);
            for(int i : indicesToKill)
            {
                nodesToKill.add(protocols.get(i).node);
                nodeIdsToKill.add(protocols.get(i).node.getId());
            }

            EDSimulator.add(queue.size(),Event.KILL_NODE,null,null,new Payload(nodesToKill));
        }

        // 5. Queue Refresh
        // Adding refresh to check if better results, comment out to simulate without refresh, with churn
        System.out.println("Number of Protocols before refresh "+protocols.size());
        if(churnFrac>0) {
            for (int i = 0; i < protocols.size(); i++) {
                if(!nodesToKill.contains(protocols.get(i).node))
                    EDSimulator.add(queue.size(), Event.REFRESH_OPERATION, protocols.get(i).node, null, null);
            }
        }

        // 6. Queue GET
        System.out.println("Number of Protocols before get "+protocols.size());
        int correct = 0;
        for(int i=0;i<nGetReq;i++)
        {
            System.out.println("Adding GET_REQ to queue");
            //System.out.println(nodeIdToProtocol.size());
            int randomIndex = globalRandom.nextInt(protocols.size());
            while(nodesToKill.contains(protocols.get(randomIndex).node))
            {
                System.out.println("Generating another random");
                randomIndex = globalRandom.nextInt(protocols.size());
            }
            int randomKeyIndex = globalRandom.nextInt(keySet.size());
            EDSimulator.add(queue.size(), Event.GET_REQUEST,protocols.get(randomIndex).node,null,new Payload(keySet.get(randomKeyIndex),null));
            System.out.println("Get request "+i+" added");
        }

        // Perform the actual simulation
        boolean exit = false;
        while (!exit && queue.size()>0) {
            exit = executeNext();
        }
        System.out.println("BitSpace|Seed|K|Alpha|Max Latency|NumNodes|NumBootstrap|NumGET|NumSET|Churn rate|NumSuccess|Total LookupTime|Total NumRounds");
        try(FileWriter fw = new FileWriter("GETOutput.txt", true);
            BufferedWriter bw = new BufferedWriter(fw))
        {
            String line = bitSpace+"|"+initialSeed+"|"+k+"|"+alpha+"|"+maxLatency+"|"+nNodes+"|"+nBootstraps+"|"+nGetReq+"|"+nSetReq+"|"+churnFrac+"|";
            bw.write(line);
        }

        // Print stats + clear state
        printSet();
        printGet();
        finish();
    }

    public static void startDynamic(int nNodes, int nBootstraps, int bitSpace, int k, int alpha,int maxLatency, int nGetReq, int nSetReq,int seed, float churnFrac) throws Exception {
        globalRandom = new Random(seed);
        int initialSeed = seed;
        EDSimulator.maxLatency = maxLatency;

        // 1. create an initial subset of nodes
        int nStartingNodes = nNodes / 2; // hardcode to half
        for(int i = 0; i < nStartingNodes; i++) // bootstrap first half of nodes, keep second half for dynamic joins
        {
            createNode(bitSpace, k, seed++, alpha, alpha);

            Node newNode = protocols.get(protocols.size()-1).node;
            List<Node> bootstrappeeNodes = new ArrayList<>(globalRandom.nextInt(protocols.size()));

            EDSimulator.add(0, Event.BOOTSTRAP, newNode,null, new Payload(bootstrappeeNodes));

            executeNext();
        }

        // 2. execute (not just queue) join/kill/set/get, one-by-one, probabilistically
        int nTotalEvents = (int) ((nNodes / 2) + nGetReq + nSetReq + (nNodes * churnFrac)); // dictates how many events to queue + probabilities of each
        int rollForJoin = nNodes / 2; // range of roll for join event
        int rollForSet = rollForJoin + nSetReq;
        int rollForGet = rollForSet + nGetReq;

        List<BigInteger> keySet = new ArrayList<>(); // list of all keys that have been set by network

        for (int i = 0; i < nTotalEvents; i++) {
            int roll = globalRandom.nextInt(nTotalEvents);
            if (roll < rollForJoin) {
                createNode(bitSpace, k, seed++, alpha, alpha);

                Node node = protocols.get(protocols.size()-1).node;
                List<Node> bootstrappeeNodes = new ArrayList<>(globalRandom.nextInt(protocols.size()));

                EDSimulator.add(0, Event.BOOTSTRAP, node,null, new Payload(bootstrappeeNodes));
                executeNext();
            } else if (roll < rollForSet) {
                BigInteger key = BigInteger.valueOf(globalRandom.nextInt());
                keySet.add(key);
                int node = globalRandom.nextInt(protocols.size()-1);

                EDSimulator.add(0,Event.SET_REQUEST,protocols.get(node).node,null,new Payload(key,key.toString()));
                executeNext();
            } else if (roll < rollForGet) {
                if (keySet.size() == 0) {
                    continue;
                }
                int node = globalRandom.nextInt(protocols.size()-1);
                int key = globalRandom.nextInt(keySet.size());

                EDSimulator.add(0, Event.GET_REQUEST,protocols.get(node).node,null,new Payload(keySet.get(key),null));
                executeNext();

            } else { // churn event (roll is inside churn roll range)
                int node = globalRandom.nextInt(protocols.size());

                EDSimulator.add(0,Event.KILL_NODE,null,null,new Payload(new ArrayList<>(node)));
                executeNext();
            }
            // TODO: add dynamic refresh events
        }

        System.out.println("BitSpace|Seed|K|Alpha|Max Latency|NumNodes|NumBootstrap|NumGET|NumSET|Churn rate|NumSuccess|Total LookupTime|Total NumRounds");
        try(FileWriter fw = new FileWriter("GETOutput.txt", true);
            BufferedWriter bw = new BufferedWriter(fw))
        {
            String line = bitSpace+"|"+initialSeed+"|"+k+"|"+alpha+"|"+maxLatency+"|"+nNodes+"|"+nBootstraps+"|"+nGetReq+"|"+nSetReq+"|"+churnFrac+"|";
            bw.write(line);
        }

        // Print stats + clear state
        printSet();
        printGet();
        finish();
    }

    public static void createNode(int bitSpace, int k, int seed, int alpha, int maxLatency) {
        while (true) {
            Protocol protocol = new Protocol(new Config(bitSpace, k, new Random(seed), alpha, maxLatency));
            Protocol prevProtocol = nodeIdToProtocol.putIfAbsent(protocol.node.id, protocol);
            if (prevProtocol == null) {
                protocols.add(protocol);
                return;
            }
        }
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

    public static boolean killNodes(List<Node> nodes)
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
        assert protocols.size()==nodeIdToProtocol.size();
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
        if(event.type==Event.KILL_NODE)
        {
            killNodes(event.payload.nodes);
            return false;
        }
        /*if(event.type == Event.STORE_REQUEST)
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
        }*/
        // REASON: Because lookup responses don't have a sender
        if(event.type== Event.NODE_LOOKUP_RESPONSE || event.type == Event.VALUE_LOOKUP_RESPONSE)
        {
            return false;
        }
        Protocol eventSender = EDSimulator.nodeIdToProtocol.getOrDefault(event.sender.id,null);
        if(eventSender==null)
        {
            System.out.println("Sender " + event.sender +" does not exist in network for "+event.type);
            return false;
        }
        // REASON : Because bootstrap events have no target
        if((event.type!= Event.BOOTSTRAP && event.type!=Event.VALUE_LOOKUP_REQUEST && event.type!= Event.NODE_LOOKUP_REQUEST
           && event.type!=Event.SET_REQUEST && event.type != Event.GET_REQUEST && event.type!=Event.REFRESH_OPERATION)
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
     * @param sender
     *   In the sync case - the node that this event will be called on
     */
    public static void add(long delay, int type, Node sender, Node target, Payload payload) {
        long id = Math.abs(EDSimulator.globalRandom.nextLong());
        long currTime = CommonState.getTime();
        Event event = new Event(id,currTime + delay, type, sender, target, payload);
        queue.add(event);
    }

    public static void printGet() throws Exception
    {
        System.out.println("GET Requests");
        //System.out.println("BitSpace|Seed|K|Alpha|Max Latency|NumNodes|NumBootstrap|NumGET|NumSET|NumSuccess|Total LookupTime|Total NumRounds");

        long totLookup = 0, totNumRounds = 0;
        int numSuccess = 0;
        for(int i=0;i<getRecords.size();i++)
        {
            GetRecordBookKeeper event = getRecords.get(i);
            totLookup+= event.lookUpTime;
            totNumRounds+= event.numRounds;
            if(event.valueReturned!=null)
                numSuccess++;
            System.out.println("GET_ID: "+ event.eventId+" | Sender: " +event.initiator+" | Number of rounds: " + event.numRounds+" | Latency: "+event.lookUpTime+"" +
                    " Key searched: "+ event.lookedUpKey +" | Value returned: "+event.valueReturned);
        }
        try(FileWriter fw = new FileWriter("GETOutput.txt", true);
            BufferedWriter bw = new BufferedWriter(fw))
        {
            bw.write(numSuccess+"|"+totLookup+"|"+totNumRounds+"|");
        }
        printStats();
        System.out.println("----------------------");
        return;

    }

    public static void printStats() throws Exception
    {
        List<Double> sizes = new ArrayList<>();
        for(Protocol protocol : protocols)
        {
            sizes.add((double) protocol.storage.getNodeStorageTable().size());
        }
        Double avgLoad = calculateMean(sizes);
        Double standardDev = calculateStandardDeviation(sizes,avgLoad);
        try(FileWriter fw = new FileWriter("GETOutput.txt", true);
            BufferedWriter bw = new BufferedWriter(fw))
        {
            String line = avgLoad+"|"+standardDev;
            bw.write(line);
            bw.newLine();
        }
    }
    public static Double calculateMean(List<Double> numbers) {
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return sum / numbers.size();
    }
    public static Double calculateStandardDeviation(List<Double> numbers, double mean) {
        double varianceSum = 0;
        for (double num : numbers) {
            varianceSum += Math.pow(num - mean, 2);
        }
        double variance = varianceSum / numbers.size();
        return Math.sqrt(variance);
    }

    public static void printSet()
    {
        System.out.println("SET Requests");
        for(SetRecordBookKeeper event : setRecords)
        {
            System.out.print("ID: "+ event.eventId+" | Sender: " +event.initiator+" | Number of rounds: " + event.numRounds+" | Latency: "+event.lookUpTime+"" +
                    " | Key,Value stored: <"+event.keyToStore+","+event.valueToStore+">" + "| Result: "+event.success+" | Nodes on which stored: [");
            for(Node node : event.nodesOnWhichStored)
            {
                System.out.print(node.getId()+",");
            }
            System.out.print("]\n");
        }
        System.out.println("----------------------");
        return;
    }
}
