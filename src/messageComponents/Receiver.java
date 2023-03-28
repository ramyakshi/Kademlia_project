package messageComponents;

public interface Receiver {

    public void receiveMsg(Message text, int convoId) throws Exception;

    public void timeOut(int convoId) throws Exception;
}
