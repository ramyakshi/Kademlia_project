package simulator;
import kademlia.Node;

import java.math.BigInteger;

public class GetRecordBookKeeper {
    public final long eventId;

    public final long lookUpTime;

    public final int numRounds;

    public final Node initiator;

    public final String valueReturned;

    public final BigInteger lookedUpKey;

    public final boolean success;

    public GetRecordBookKeeper(long eventId, long lookUpTime, int numRounds, Node initiator, String valueReturned, BigInteger lookedUpKey) {
        this.eventId = eventId;
        this.lookUpTime = lookUpTime;
        this.numRounds = numRounds;
        this.initiator = initiator;
        this.valueReturned = valueReturned;
        this.lookedUpKey = lookedUpKey;
        if(valueReturned!=null)
            this.success = true;
        else
            this.success = false;
    }
}
