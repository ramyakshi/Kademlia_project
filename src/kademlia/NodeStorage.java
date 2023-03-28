package kademlia;

import components.Content;

import java.math.BigInteger;
import java.util.*;

public class NodeStorage {

    public int ttl = 604800;

    public HashMap<BigInteger, ArrayList<Content>> nodeStorageTable = new HashMap<>();

    public void setValue(BigInteger key, long value)
    {
        if(!this.nodeStorageTable.containsKey(key))
        {
            this.nodeStorageTable.put(key, new ArrayList<>());
        }
        Content val = new Content(System.nanoTime(), value);
        this.nodeStorageTable.get(key).add(val);
        deleteOlderThan(key, this.ttl);
    }

    public ArrayList<Content> getContent(BigInteger key)
    {
        deleteOlderThan(key, this.ttl);
        return this.nodeStorageTable.get(key);
    }
    public void deleteOlderThan(BigInteger key, long seconds)
    {
        long thresholdTime = System.nanoTime() - seconds;

        ArrayList<Content> contentTable = nodeStorageTable.get(key);
        Iterator<Content> iterator = contentTable.iterator();

        while(iterator.hasNext())
        {
            if(iterator.next().getTimeStamp()<thresholdTime)
            {
                iterator.remove();
            }
        }
    }


}
