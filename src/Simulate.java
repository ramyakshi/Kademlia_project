import simulator.EDSimulator;

public class Simulate {
    public static void main(String[] args) {
        System.out.println("\n=== STARTING SIMULATION");
        EDSimulator.start(5, 4, 3, 2); // to test splitting buckets
        System.out.println("\n=== PRINTING END STATE");
        EDSimulator.printEndState();
    }
}
