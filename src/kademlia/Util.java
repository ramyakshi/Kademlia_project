package kademlia;

import java.math.BigInteger;

public class Util {
    public static int prefixLen(BigInteger b1, BigInteger b2, Config config) {
        // start from most significant bit
        int c = 0;
        for (int n = config.bitSpace - 1; n >= 0; n--) {
            if (b1.testBit(n) != b2.testBit(n)) {
                break;
            }
            c++;
        }
        return c;
    }
}
