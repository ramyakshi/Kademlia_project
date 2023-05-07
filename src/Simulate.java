import simulator.EDSimulator;
import kademlia.Util;

import java.io.*;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class Simulate {
    public static void main(String[] args) throws Exception{
        //System.out.println("Please provide arguments in order number of nodes, nBootstraps, bitspace, k, alpha, maxLatency, number of get reqs, number of set reqs, seed, churn rate");

        try(BufferedReader br = new BufferedReader(new FileReader("runConfigs.txt"))) {
            String line;
            int lineNum = 1;
            while((line =br.readLine())!=null)
            {
                Scanner scanner = new Scanner(line);
                int[] arg = new int[9];
                float churnFraction = 0;
                for (int i = 0; i < 9; i++) {
                    arg[i] = scanner.nextInt();
                }
                if(scanner.hasNext())
                {
                    churnFraction = scanner.nextFloat();
                }
                System.out.println("Simulation " + lineNum + " started");
                // Save the original System.out stream
                PrintStream originalOut = System.out;
                // Redirect output to the file using a PrintStream with a PrintWriter
                try (PrintStream ps = new PrintStream(new FileOutputStream("output" + lineNum + ".txt"))) {
                    System.setOut(ps);
                    EDSimulator.start(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5], arg[6], arg[7], arg[8], churnFraction);
                }
                System.setOut(originalOut);
                System.out.println("Simulation " + lineNum + " ended");
                lineNum++;
            }
        }

        // Arguments in order - number of nodes, nBootstraps, bitspace, k, alpha, maxLatency, number of get reqs, number of set reqs, seed
        //System.out.print(Util.digest(BigInteger.valueOf(1)));

        //EDSimulator.checkFindNeighbors();
        /*System.out.println("Second simulation");
        EDSimulator.start(20, 5, 5,3);
        System.out.println("\n=== PRINTING END STATE");
        EDSimulator.printEndState();*/
    }
}
