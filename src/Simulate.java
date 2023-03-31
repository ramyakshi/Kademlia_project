import simulator.EDSimulator;

public class Simulate {
    public static void main(String[] args) {
        System.out.println("\n=== STARTING SIMULATION");
        EDSimulator.start(20, 19, 6, 3); // to test splitting buckets
        System.out.println("\n=== PRINTING END STATE");
        EDSimulator.printEndState();

        //EDSimulator.checkFindNeighbors();
        /*System.out.println("Second simulation");
        EDSimulator.start(20, 5, 5,3);
        System.out.println("\n=== PRINTING END STATE");
        EDSimulator.printEndState();*/
    }
}
