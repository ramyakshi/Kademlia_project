package kademlia;

import java.util.Map;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Vector;

public class KBucket {
    public LinkedHashMap<BigInteger, Node> nodes;
    public int k;
    public BigInteger rangeLower;
    public BigInteger rangeUpper;

    public




    public KBucket(int k, BigInteger rangeLower, BigInteger rangeUpper) {
        this.k = k;
        this.rangeLower = rangeLower;
        this.rangeUpper = rangeUpper;
        this.nodes = new LinkedHashMap<>();
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
        return nodeId.compareTo(rangeLower) == 1 && nodeId.compareTo(rangeUpper) == -1;
    }

    public Vector<KBucket> split() {
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
        Vector<KBucket> out = new Vector<>();
        out.add(one);
        out.add(two);
        return out;
    }
}