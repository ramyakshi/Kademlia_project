package playground;

import java.math.BigInteger;
import java.util.Random;

import components.Content;

class Main {
    public static void main(String[] args) throws Exception{
        BigInteger x = new BigInteger("1");
        BigInteger y = new BigInteger("4");
        System.out.println(x.add(y).divide(new BigInteger("2")));

        Content c1 = new Content(System.nanoTime(),123);

        Thread.sleep(1000);

        Content c2 = new Content(System.nanoTime(),123);
        System.out.println(c2.compareTo(c1));


    }
}

// 1 0 1 - 5
// 0 1 1 - 3

// 1 0 1 - 5
// 1 0 0 - 4

//