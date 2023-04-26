package kademlia;

import components.Content;

import java.math.BigInteger;
import java.util.*;

public class NodeStorage {

    public long ttl = 604800;

    private final HashMap<BigInteger, Content> nodeStorageTable;

    public NodeStorage()
    {
        this.nodeStorageTable =  new HashMap<>();
    }

    public HashMap<BigInteger, Content> getNodeStorageTable()
    {
        return this.nodeStorageTable;
    }
    public void setValue(BigInteger key, String value, long timeStamp)
    {
        long expiryTime = System.nanoTime() + ttl;
        expiryTime -= timeStamp;

        Content val = new Content(expiryTime, value);
        this.nodeStorageTable.put(key,val);
        deleteOlderThan(key, this.ttl);
    }

    public String getContentValue(BigInteger key)
    {
        deleteOlderThan(key, this.ttl);
        Content result = nodeStorageTable.getOrDefault(key,null);
        if(result==null)
            return null;
        return result.getValue();
    }
    public void deleteOlderThan(BigInteger key, long seconds)
    {
        long thresholdTime = System.nanoTime() - seconds;

        Iterator<HashMap.Entry<BigInteger,Content>> iterator = nodeStorageTable.entrySet().iterator();

        while(iterator.hasNext())
        {
            HashMap.Entry<BigInteger,Content> entry = iterator.next();
            if(entry.getValue().getExpiration()<thresholdTime)
            {
                iterator.remove();
            }
        }
    }


}
