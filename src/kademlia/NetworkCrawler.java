package kademlia;
import simulator.EDSimulator;
import simulator.Event;
import simulator.Payload;

import java.math.BigInteger;
import java.util.*;

public class NetworkCrawler {

    // K ie the size of k bucket
    public int k;

    public int alpha;

    public Node node;

    private static Map<Node, String> contactStatusMap;
    PriorityQueue<Node> nearestKNodes;

    int count;

    List<Node> lastCrawled;

    List<Node> globalList;

    public Protocol protocol;

    HashMap<BigInteger, Protocol> nodeIdToProtocol;

    public static boolean debugMode;

    public static long nowTime = 0;
    public NetworkCrawler(int k, int alpha, Protocol protocol)
    {
        //this.node = node;
        this.k = k;
        this.alpha = alpha;
        contactStatusMap = new HashMap<>();
        this.nearestKNodes = new PriorityQueue<>();
        this.count = alpha;
        this.lastCrawled = new ArrayList<>();
        this.protocol = protocol;
        this.debugMode = false;

    }
    public static  boolean areListsEqual(List<Node> l1, List<Node> l2)
    {
        return new HashSet<>(l1).equals(new HashSet<>(l2));
    }
    public PriorityQueue<Node> getKNearest(List<Node> globalList, Node target)
    {
        Comparator<Node> comparator = (Node n1, Node n2) -> n2.distanceTo(target).compareTo(n1.distanceTo(target));
        PriorityQueue<Node> nodes = new PriorityQueue<>(1, comparator);
        for(Node n : globalList)
        {
            nodes.add(n);
            if(nodes.size()>k)
            {
                Node evict = nodes.poll();
            }
        }
        //return new ArrayList<>(nodes);
        return nodes;
    }

    public List<Node> nodeLookupBegin(Node target, HashMap<BigInteger, Protocol> nodeProtocolMap, long nowTime)
    {
       /* nearestKNodes = getKNearest(globalList, target);
        PriorityQueue<Node> copyQueue = nearestKNodes;
        for(int i =0;i<copyQueue.size();i++)
        {
            contactStatusMap.put(copyQueue.poll(),"NOTCONTACTED");
        }

        // Contactstatusmap has k nodes which are closest and are not contacted yet
        ArrayList<Node> kNearestList = new ArrayList<>(this.nearestKNodes);
        return nodeLookupRecursive(kNearestList,target);

        */
        if(this.nowTime == 0)
            this.nowTime = nowTime;
        if(this.nodeIdToProtocol == null)
            this.nodeIdToProtocol = nodeProtocolMap;

        /*int index = this.protocol.routingTable.getBucketIdxFor(target);
        //System.out.println(this.protocol.node.getId()+" " +target+" " + index);
        KBucket bucket = this.protocol.routingTable.kBuckets.get(index);

        /*for(int i=0; i<this.protocol.routingTable.kBuckets.size();i++)
        {
            System.out.println("Bucket - " + i);
            KBucket buck = this.protocol.routingTable.kBuckets.get(i);
            for(Map.Entry<BigInteger, Node> entry : buck.nodes.entrySet())
            {
                System.out.print(entry.getValue().getId()+" ");
            }
            System.out.println("---------");
        }*/
        List<Node> initialKNearest = this.protocol.getRoutingTable().findNeighbors(target,this.node);
        //Iterator<Node> it = bucket.getNodes();

        /*while(it.hasNext())
        {
            initialKNearest.add(it.next());
        }*/
        for(int i=0; i<initialKNearest.size();i++) {

            contactStatusMap.put(initialKNearest.get(i),"NOTCONTACTED");
        }
        List<Node> result =  nodeLookupRecursive(initialKNearest, target);
        //System.out.println(result.size());
        return result;
    }
    public void printContactMap()
    {
        if(!debugMode)
            return;
        for(Map.Entry<Node,String> entry : contactStatusMap.entrySet())
        {
            System.out.println("Node - " + entry.getKey()+" Status - " + entry.getValue());
        }
        System.out.println("-----------------");
    }

    public static void printKnearestList(List<Node> kNearestList)
    {
        if(!debugMode)
            return;
        System.out.println("K Nearest at this point");
        for(Node n : kNearestList)
        {
            System.out.print(n.getId()+" ");
        }
        System.out.println("-----------------");
    }
   public List<Node> nodeLookupRecursive(List<Node> kNearestList, Node target)
   {
       printContactMap();
       printKnearestList(kNearestList);
       if( areListsEqual(kNearestList, lastCrawled))
       {
           count = kNearestList.size();
       }
       lastCrawled = kNearestList;

       HashMap<Node, List<Node>> nearestReturned = new HashMap<>();
       int localCount = 0;
       for(Node n : kNearestList)
       {
           if(localCount< count) {
               if(contactStatusMap.get(n).equals("NOTCONTACTED"))
               {
                   localCount++;
                   //System.out.println("Local count value - "+ localCount + " Node -" + n.getId());
                   //Send lookup message
                   //List<Node> newNodes = this.nodeIdToProtocol.get(n.getId()).routingTable.findNeighbors(target,null);
                   List<Node> newNodes = this.protocol.callFindNode(nodeIdToProtocol,n,target.getId(),true);
                   //EDSimulator.add(nowTime+1, Event.RPC_FIND_NODE_REQUEST,this.node,n,new Payload(this.node));
                   contactStatusMap.put(n,"CONTACTED");
                   nearestReturned.put(n,newNodes);
               }
           }
           if(localCount>=count)
               break;

       }
       // No candidate node which is still to be contacted exists
       if(localCount == 0)
       {
           /*System.out.println("Print before return");
           for(int i=0;i <kNearestList.size();i++)
           {
               System.out.print(kNearestList.get(i) + " ");
           }*/
           //System.out.println();
           return kNearestList;
       }
       // Remove nodes which failed to respond
       return removeNodes(nearestReturned, kNearestList, target);
   }

   public List<Node> removeNodes(HashMap<Node, List<Node>> nearestReturned, List<Node> nearestKNodes,Node target)
   {

       ArrayList<Node> toRemove = new ArrayList<>();
       //Below block for removing nodes which didn't respond
       List<Node> keysAsArray = new ArrayList<>(nearestReturned.keySet());
       Random r = new Random();

       // For now, 1 random node didn't respond
       //toRemove.add(keysAsArray.get(r.nextInt(keysAsArray.size())));
       //System.out.println("Node did not respond - " + toRemove.get(0).getId());
       for(Map.Entry<Node, List<Node>> entry : nearestReturned.entrySet())
       {

           if(entry.getValue()==null)
           {
               toRemove.add(entry.getKey());
           }
           else{
               nearestKNodes.addAll(entry.getValue());
           }
           // else
              // add it to nearestKNodes
       }
       nearestKNodes.removeAll(toRemove);
       // To prevent nodes being duplicated in k-nearest list
       ArrayList<Node> uniqueNodes = new ArrayList<>(new HashSet<>(nearestKNodes));
       if(debugMode)
       {
           System.out.println("Unique nodes");
           for(Node n : uniqueNodes)
           {
               System.out.print(n.getId()+" ");
           }
           System.out.println("----------");
       }
       PriorityQueue<Node> updatedKNearest = getKNearest(uniqueNodes, target);
       for(Node n : updatedKNearest)
       {
           if(!contactStatusMap.containsKey(n))
               contactStatusMap.put(n,"NOTCONTACTED");
       }
       return nodeLookupRecursive(new ArrayList<>(updatedKNearest),target);
   }
} 
