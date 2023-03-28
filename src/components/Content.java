package components;

import java.math.BigInteger;
import java.util.Comparator;

public class Content implements Comparable<Content> {

// Not sure if both creation and update timestamps are needed

    public long timeStamp;

    public Content() {

    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long value;

    public Content(long timeStamp, long value)
    {
        this.timeStamp = timeStamp;
        this.value = value;
    }

    @Override
    public int compareTo(Content c) {
        return Long.compare(this.getTimeStamp(),c.getTimeStamp());
    }
}
