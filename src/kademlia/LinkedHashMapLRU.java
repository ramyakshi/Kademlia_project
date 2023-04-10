package kademlia;

import java.util.*;

public class LinkedHashMapLRU<K,V> extends LinkedHashMap<K,V> {
    private int maxSize;

    public LinkedHashMapLRU(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    public V pop() {
        if (this.size() <= 0) {
            return null;
        }
        Map.Entry<K,V> lastElement = null;
        Iterator<Map.Entry<K, V>> iterator = this.entrySet().iterator();
        while (iterator.hasNext()) { lastElement = iterator.next(); }
        this.remove(lastElement.getKey());
        return lastElement.getValue();
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }
}
