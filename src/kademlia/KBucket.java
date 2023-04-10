package kademlia;

import java.util.Map;
import java.util.LinkedHashMap;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class KBucket {
    public LinkedHashMap<BigInteger, Node> nodes;
    public LinkedHashMapLRU<BigInteger, Node> replacementNodes;
    public Config config;
    public BigInteger rangeLower; // inclusive
    public BigInteger rangeUpper; // inclusive

    public KBucket(Config config, BigInteger rangeLower, BigInteger rangeUpper) {
        this.config = config;
        this.rangeLower = rangeLower;
        this.rangeUpper = rangeUpper;
        this.nodes = new LinkedHashMap<>();
        this.replacementNodes = new LinkedHashMapLRU<>(config.k);
    }

    public Iterator<Node> getNodes() {
        return this.nodes.values().iterator();
    }

    public boolean addNode(Node node) {
        if (this.nodes.containsKey(node.id)) {
            // if already in bucket, move to tail
            this.nodes.remove(node.id);
            this.nodes.put(node.id, node);
        } else if (this.nodes.size() < this.config.k) {
            // else if bucket has capacity, add
            this.nodes.put(node.id, node);
        } else {
            // add to replacement list
            if (this.replacementNodes.containsKey(node.id)) {
                this.replacementNodes.remove(node.id);
            }
            this.replacementNodes.put(node.id, node);
            return false;
        }
        return true;
    }

    public void removeNode(Node node) {
        if (this.replacementNodes.containsKey(node.id)) {
            this.replacementNodes.remove(node.id);
        }
        if (this.nodes.containsKey(node.id)) {
            this.nodes.remove(node.id);

            if (this.replacementNodes.size() > 0) {
                Node newNode = this.replacementNodes.pop();
                this.nodes.put(newNode.id, newNode);
            }
        }
    }

    public boolean hasInRange(BigInteger nodeId) {
        return nodeId.compareTo(rangeLower) > -1 && nodeId.compareTo(rangeUpper) < 1;
    }

    public ArrayList<KBucket> split() {
        BigInteger mid = rangeLower.add(rangeUpper).divide(new BigInteger("2"));
        KBucket one = new KBucket(config, rangeLower, mid);
        KBucket two = new KBucket(config, mid.add(new BigInteger("1")), rangeUpper);

        // split bucket nodes
        for (Map.Entry<BigInteger, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (node.id.compareTo(mid) < 1) {
                one.addNode(node);
            } else {
                two.addNode(node);
            }
        }
        // split replacement nodes
        for (Map.Entry<BigInteger, Node> entry : replacementNodes.entrySet()) {
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

    public int depth() {
        String lowerBits = this.rangeLower.toString(2);
        String upperBits = this.rangeUpper.toString(2);
        int d = 0;
        for (int i = 0; i < this.rangeUpper.bitLength(); i++) {
            if (lowerBits.charAt(i) == upperBits.charAt(i)) {
                d += 1;
            } else {
                break;
            }
        }
        // since BigInteger.toString strips leading 0's, we need to add it back
        return d + (config.bitSpace - upperBits.length());
    }

    public boolean isNewNode(Node node) {
        return !nodes.containsKey(node.id);
    }
}