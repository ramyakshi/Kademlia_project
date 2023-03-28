package kademlia;

import java.math.BigInteger;
import java.util.Vector;

public class RoutingTable {
    public BigInteger nodeId; // node ID of the node this routing table belongs to
    public Vector<KBucket> kBuckets; // bucket range in ascending order
    public Config config;

    public RoutingTable(BigInteger nodeId, Config config) {
        this.nodeId = nodeId;
        this.config = config;

        // set initial bucket (covers entire range)
        kBuckets = new Vector<>();
        BigInteger rangeLower = new BigInteger("0");
        BigInteger rangeUpper = new BigInteger("2").pow(160);
        kBuckets.add(new KBucket(config.k, rangeLower, rangeUpper));
    }

    public int getBucketIdxFor(BigInteger contactId) {
        // loop buckets to get correct bucket for node
        for (int i = 0; i < this.kBuckets.size(); i++) {
            // if contactId is leq than bucket i's upper limit, it belongs to bucket i
            if (contactId.compareTo(kBuckets.get(i).rangeUpper) < 1) {
                return i;
            }
        }
        return 0; // should never reach here
    }

    public void addContact(Node node) {
        int index = this.getBucketIdxFor(node.id);
        KBucket bucket = kBuckets.get(index);

        if (bucket.addNode(node)) {
            // bucket not full, just add and return
            return;
        }

        // TODO: Accelerated lookup; depth % 5
        if (bucket.hasInRange(this.nodeId)) {
            this.splitBucket(index);
            this.addContact(node);
        }
    }

    public void splitBucket(int index) {
        Vector<KBucket> splits = kBuckets.get(index).split();
        kBuckets.set(index, splits.get(0));
        kBuckets.insertElementAt(splits.get(1), index+1);
    }
}