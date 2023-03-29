package simulator;

import java.math.BigInteger;
import java.util.*;

import kademlia.Config;
import kademlia.Node;
import kademlia.Protocol;

public class EDSimulator {
    private static final PriorityQueue<Event> queue = new PriorityQueue<Event>();
    private static final List<Protocol> protocols = new ArrayList<>(); // we pick from this list to create random events
    private static final HashMap<BigInteger, Protocol> nodeIdToProtocol = new HashMap<BigInteger, Protocol>();

    private EDSimulator() {} // prevent construction

    /**
     * Runs an experiment
     */
    public static void start() {
        int nNodes = 2;
        int createdNodes = 0;
        while (createdNodes < nNodes) {
            // since our bit-space is small, it may clash, just loop until created as needed
            Protocol protocol = new Protocol(new Config(3, 1, new Random().nextInt()));
            Protocol prevProtocol = nodeIdToProtocol.putIfAbsent(protocol.node.id, protocol);
            if (prevProtocol == null) {
                protocols.add(protocol);
                createdNodes += 1;
            }
        }

        // add a fixed bootstrap event for initial test
        Node eventTarget = protocols.get(0).node;
        Node payloadTarget = protocols.get(1).node;
        EDSimulator.add(0, Event.BOOTSTRAP, null, eventTarget, new Payload(payloadTarget));

        // perform the actual simulation
        boolean exit = false;
        while (!exit) {
            exit = executeNext();
        }
    }

    public static void printEndState() {
        for (Protocol protocol : protocols) {
            System.out.println(protocol.routingTable.kBuckets);
        }
    }

    /**
     * Execute and remove the next event from the ordered event list.
     * @return true if the execution should be stopped.
     */
    private static boolean executeNext() {
        Event event = queue.poll();
        if (event == null) { // should never happen
            return true;
        }
        CommonState.setTime(event.timestamp);
        System.out.printf("event: %s | target: %s | sender: %s | payload: %s \n", event, event.target, event.sender, event.payload.node);
        Protocol eventTarget = EDSimulator.nodeIdToProtocol.get(event.target.id);
        eventTarget.processEvent(event);
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
