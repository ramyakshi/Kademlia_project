package simulator;

public class CommonState {
    private static long time = 0;

    public static long getTime()
    {
        return time;
    }
    public static void setTime(long t)
    {
        time = t;
    }
}
