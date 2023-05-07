package simulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import kademlia.Node;
public class SetRecordBookKeeper {
    public final long eventId;

    public final long lookUpTime;

    public final int numRounds;

    public final String valueToStore;

    public final Node initiator;
    public final BigInteger keyToStore;

    public final boolean success;

    public List<Node> nodesOnWhichStored;
    public SetRecordBookKeeper(long eventId, long lookUpTime, int numRounds,Node initiator, String valueToStore, BigInteger keyToStore, boolean success, List<Node> nodes) {
        this.eventId = eventId;
        this.lookUpTime = lookUpTime;
        this.numRounds = numRounds;
        this.initiator = initiator;
        this.valueToStore = valueToStore;
        this.keyToStore = keyToStore;
        this.success = success;
        this.nodesOnWhichStored = nodes;
    }
}
