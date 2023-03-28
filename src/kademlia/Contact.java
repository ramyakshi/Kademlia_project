package kademlia;

// Need a wrapper for Node class because we need more info ie when the node was last seen, if it's stale
public class Contact implements Comparable<Contact>{

    private final Node node;
    private long lastWhenSeen;

    public Contact(Node node)
    {
        this.node = node;
        this.lastWhenSeen = System.currentTimeMillis() / 1000;
    }

    public Node getNode()
    {
        return this.node;
    }

    public void setLastWhenSeen()
    {
        this.lastWhenSeen = System.currentTimeMillis()/1000;
    }

    public long getLastWhenSeen()
    {
        return this.lastWhenSeen;
    }

    public boolean isEqual(Contact c)
    {
        return c.getNode().equals(this.getNode());
    }

    public int compareTo(Contact c)
    {
        if(this.isEqual(c))
            return 0;
        else if(this.getLastWhenSeen() > c.getLastWhenSeen())
            return 1;
        else
            return -1;
    }
}
