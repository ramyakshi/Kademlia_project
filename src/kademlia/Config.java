package kademlia;

import java.util.Random;


public class Config {
    public int bitSpace;
    public int k;
    public Random rng;

    public int maxLatency;
    public int alpha;
    public Config(int bitSpace, int k, Random rng, int alpha, int maxLatency) {
        this.bitSpace = bitSpace;
        this.k = k;
        this.rng = rng;
        this.alpha = alpha;
    }
}
