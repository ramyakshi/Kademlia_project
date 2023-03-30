package kademlia;

import java.util.Random;


public class Config {
    public int bitSpace;
    public int k;
    public Random rng;

    public Config(int bitSpace, int k, Random rng) {
        this.bitSpace = bitSpace;
        this.k = k;
        this.rng = rng;
    }
}
