package kademlia;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static BigInteger digest(BigInteger input)
    {
        return input;
        /*try{
            byte[] inputBytes = input.toByteArray();
            return new BigInteger(1,MessageDigest.getInstance("SHA1").digest(inputBytes));
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA Alogrithm does not exist");
            //System.out.println(e.toString());
        }*/
    }
}
