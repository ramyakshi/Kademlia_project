package kademlia;

import java.util.Map;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class KBucket {
    public LinkedHashMap<BigInteger, Node> nodes;
    public int k;
    public BigInteger rangeLower; // inclusive
    public BigInteger rangeUpper; // inclusive

    public KBucket(int k, BigInteger rangeLower, BigInteger rangeUpper) {
        this.k = k;
        this.rangeLower = rangeLower;
        this.rangeUpper = rangeUpper;
        this.nodes = new LinkedHashMap<>();
    }

    public Iterator<Node> getNodes() {
        return this.nodes.values().iterator();
    }

    public boolean addNode(Node node) {
        if (this.nodes.containsKey(node.id)) {
            // if already in bucket, move to tail
            this.nodes.remove(node.id);
            this.nodes.put(node.id, node);
        } else if (this.nodes.size() < this.k) {
            // else if bucket has capacity, add
            this.nodes.put(node.id, node);
        } else {
            // TODO: add to replacement list
            return false;
        }
        return true;


    }

    public boolean hasInRange(BigInteger nodeId) {
        return nodeId.compareTo(rangeLower) > -1 && nodeId.compareTo(rangeUpper) < 1;
    }

    public ArrayList<KBucket> split() {
        BigInteger mid = rangeLower.add(rangeUpper).divide(new BigInteger("2"));
        KBucket one = new KBucket(k, rangeLower, mid);
        KBucket two = new KBucket(k, mid.add(new BigInteger("1")), rangeUpper);
        for (Map.Entry<BigInteger, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (node.id.compareTo(mid) < 1) {
                one.addNode(node);
            } else {
                two.addNode(node);
            }
        }
        ArrayList<KBucket> out = new ArrayList<>();
        out.add(one);
        out.add(two);
        return out;
    }

    public boolean isNewNode(Node node) {
        return !nodes.containsKey(node.id);
    }
}