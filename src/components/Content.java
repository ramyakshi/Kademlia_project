package components;

import java.math.BigInteger;
import java.util.Comparator;

public class Content {

// Not sure if both creation and update timestamps are needed

    public long expiration;
    public String value;

    public Content() {

    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean hasExpired()
    {
        return System.nanoTime() > expiration;
    }


    public Content(long expiration, String value)
    {
        this.expiration = expiration;
        this.value = value;
    }

    /*public int compareTo(Content c) {
        return Long.compare(this.getExpiration(),c.getExpiration());
    }*/
}
