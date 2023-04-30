import simulator.EDSimulator;
import kademlia.Util;
import java.math.BigInteger;
import java.util.Base64;

public class Simulate {
    public static void main(String[] args) {
        System.out.println("\n=== STARTING SIMULATION");
        EDSimulator.start(5, 3, 3, 1); // to test splitting buckets
        System.out.println("\n=== PRINTING END STATE");
        EDSimulator.printEndState();
        //System.out.print(Util.digest(BigInteger.valueOf(1)));

        //EDSimulator.checkFindNeighbors();
        /*System.out.println("Second simulation");
        EDSimulator.start(20, 5, 5,3);
        System.out.println("\n=== PRINTING END STATE");
        EDSimulator.printEndState();*/
    }
}
