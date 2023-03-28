package kademlia;

import java.util.Random;


public final  class Config {
    public int bitSpace = 32;
    public int k;
    public Random rng;

    public Config(int bitSpace, int k, int rngSeed) {
        this.bitSpace = bitSpace;
        this.k = k;
        this.rng = new Random(rngSeed);
    }
}
