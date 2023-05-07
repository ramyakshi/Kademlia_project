package kademlia;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.math.BigInteger;

public class Util {

    public static BigInteger getRandomBigInteger(BigInteger x, BigInteger y, Random rnd) {
        if (x.compareTo(y) > 0) {
            getRandomBigInteger(y,x,rnd);
        }

        BigInteger range = y.subtract(x).add(BigInteger.ONE);
        int bitLength = range.bitLength();

        BigInteger randomBigInt;
        do {
            randomBigInt = new BigInteger(bitLength, rnd);
        } while (randomBigInt.compareTo(range) >= 0);

        return randomBigInt.add(x);
    }
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
        //return input;
        try{
            byte[] inputBytes = input.toByteArray();
            return new BigInteger(1,MessageDigest.getInstance("SHA1").digest(inputBytes));
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA Alogrithm does not exist");
            //System.out.println(e.toString());
        }
    }
}
