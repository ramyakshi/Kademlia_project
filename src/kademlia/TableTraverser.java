package kademlia;

import java.util.List;
import java.util.Iterator;

public class TableTraverser<T> implements Iterable<T> {
    RoutingTable routingTable;
    Node startNode;

    public TableTraverser(RoutingTable routingTable, Node startNode) {
        this.routingTable = routingTable;
        this.startNode = startNode;
    }

    // code for data structure
    public Iterator<T> iterator() {
        return new TableTraverserIterator<>(this);
    }
}
class TableTraverserIterator<T> implements Iterator<T> {
    public Iterator<Node> currentNodes;
    public List<KBucket> leftBuckets;
    public List<KBucket> rightBuckets;
    public boolean left;
    // constructor
    public TableTraverserIterator(TableTraverser<T> traverser) {
        int idx = traverser.routingTable.getBucketIdxFor(traverser.startNode);
        this.currentNodes = traverser.routingTable.kBuckets.get(idx).getNodes();
        this.leftBuckets = traverser.routingTable.kBuckets.subList(0, idx);
        this.rightBuckets = traverser.routingTable.kBuckets.subList(idx+1, traverser.routingTable.kBuckets.size());
    }

    public boolean hasNext() {
        return this.currentNodes.hasNext() || !this.leftBuckets.isEmpty() || !this.rightBuckets.isEmpty();
    }

    public T next() {
        if (this.currentNodes.hasNext()) {
            return (T) this.currentNodes.next();
        }
        if (!this.leftBuckets.isEmpty()) {
            this.currentNodes = this.leftBuckets.get(this.leftBuckets.size()-1).getNodes();
            this.leftBuckets = this.leftBuckets.subList(0, this.leftBuckets.size()-1);
            this.left = false;
            return this.next();
        }
        if (!this.rightBuckets.isEmpty()) {
            this.currentNodes = this.rightBuckets.get(this.rightBuckets.size()-1).getNodes();
            this.rightBuckets = this.rightBuckets.subList(0, this.rightBuckets.size()-1);
            this.left = true;
            return this.next();
        }
        return null;
    }

    // Used to remove an element. Implement only if needed
    public void remove() {
        // Default throws UnsupportedOperationException.
    }
}