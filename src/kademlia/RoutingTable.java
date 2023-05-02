package kademlia;

import java.math.BigInteger;
import java.util.*;

public class RoutingTable {
    public BigInteger nodeId; // node ID of the node this routing table belongs to
    public ArrayList<KBucket> kBuckets; // bucket range in ascending order
    public Config config;

    public RoutingTable(BigInteger nodeId, Config config) {
        this.nodeId = nodeId;
        this.config = config;

        // set initial bucket (covers entire range)
        kBuckets = new ArrayList<>();
        BigInteger rangeLower = new BigInteger("0");
        BigInteger rangeUpper = new BigInteger("2").pow(config.bitSpace).subtract(new BigInteger("1"));
        kBuckets.add(new KBucket(config, rangeLower, rangeUpper));
    }

    public int getBucketIdxFor(Node node) {
        // loop buckets to get correct bucket for node
        for (int i = 0; i < this.kBuckets.size(); i++) {
            // if contactId is leq than bucket i's upper limit, it belongs to bucket i
            if (node.id.compareTo(kBuckets.get(i).rangeUpper) < 1) {
                return i;
            }
        }
        return 0; // should never reach here
    }

    public void addContact(Node node) {
        int index = this.getBucketIdxFor(node);
        KBucket bucket = kBuckets.get(index);

        if (bucket.addNode(node)) {
            // bucket not full / node already in, job done
            return;
        }

        if (bucket.hasInRange(this.nodeId) || bucket.depth() % 5 != 0) {
            this.splitBucket(index);
            this.addContact(node);
        }
        // TODO: add node to potential replacement list
    }

    public void removeContact(Node node)
    {
        int index = this.getBucketIdxFor(node);
        KBucket kBucket = kBuckets.get(index);

        kBucket.removeNode(node);
    }
    public void splitBucket(int index) {
        ArrayList<KBucket> splits = kBuckets.get(index).split();
        kBuckets.set(index, splits.get(0));
        kBuckets.add(index+1, splits.get(1));
    }

    public boolean isNewNode(Node node) {
        int index = getBucketIdxFor(node);
        return kBuckets.get(index).isNewNode(node);
    }

    public List<Node> findNeighbors(Node node, Node exclude) {
        Comparator<Node> comparator = (Node n1, Node n2) -> n1.distanceTo(node).compareTo(n2.distanceTo(node));
        PriorityQueue<Node> nodes = new PriorityQueue<>(1, comparator);

        for (Node neighbor : new TableTraverser<Node>(this, node)) {
            if (neighbor == null) {
                break;
            }
            if (neighbor.id.equals(node.id) || (exclude != null && neighbor.id.equals(exclude.id))) {
                continue;
            }
            nodes.add(neighbor);
        }

        List<Node> out = new ArrayList<>();
        while (nodes.size() > 0 && out.size() < this.config.k) {
            out.add(nodes.poll());
        }
        return out;
    }
}