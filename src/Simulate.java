import simulator.EDSimulator;
import kademlia.Util;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Base64;

public class Simulate {
    public static void main(String[] args) throws Exception{
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(out);
        System.out.println("\n=== STARTING SIMULATION");
        EDSimulator.start(20, 2, 6, 2, 1); // to test splitting buckets
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
