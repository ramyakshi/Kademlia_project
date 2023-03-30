package kademlia;
import java.math.BigInteger;
import java.util.*;

public class NetworkCrawler {

    // K ie the size of k bucket
    public int k;

    public int alpha;

    public Node node;

    Map<Node, String> contactStatusMap;
    List<Node> nearestKNodes;

    int count;

    List<Node> lastCrawled;

    public NetworkCrawler(Node node, int k, int alpha)
    {
        this.node = node;
        this.k = k;
        this.alpha = alpha;
        this.contactStatusMap = new HashMap<>();
        this.nearestKNodes = this.node.getRoutingTable().findNeighbors(this.node, sender);
        this.count = alpha;
        this.lastCrawled = new ArrayList<>();
    }
    public static  boolean areListsEqual(List<Node> l1, List<Node> l2)
    {
        return new HashSet<>(l1).equals(new HashSet<>(l2));
    }
   public List<Node> nodeLookup(Node sender, )
   {
       if( areListsEqual(nearestKNodes, lastCrawled))
       {
           count = nearestKNodes.size();
       }
       lastCrawled = nearestKNodes;

       // Contactstatusmap has 'count' nodes which are closest and are not contacted yet
       HashMap<BigInteger, List<Node>> nearestReturned = new HashMap<>();
       Set<Node> contactedNodes = new HashSet<>();
       int localCount = 0;
       for(Node n : contactStatusMap.keySet())
       {
           localCount++;
           if(localCount< count) {
               if(contactStatusMap.get(n).equals("NOTCONTACTED"))
               {
                   //Send lookup message

                   List<Node> newNodes = n.getRoutingTable().findNeighbors(this.node,n);
                   contactStatusMap.put(n,"CONTACTED");
                   nearestReturned.put(n.getId(),newNodes);
               }
           }
       }
       // Remove nodes which failed to respond
   }

   public List<Node> removeNodes(HashMap<BigInteger, List<Node>> nearestReturned, List<Node> nearestKNodes)
   {
       ArrayList<Node> toRemove = new ArrayList<>();

       for(Map.Entry<BigInteger, List<Node>> entry : nearestReturned.entrySet())
       {
           // If there was no response
              // Add to toRemove
           // else
              // add it to nearestKNodes

           //remove the toRemove from nearestKNodes
           // Call nodeLookup() again
       }
   }
} 
