package messageComponents;

public enum messageType
{
    CONNECT("Connect", (byte)1),
    CONTENTLOOKUP("ContentLookup", (byte)2),
    NODELOOKUP("NodeLookup", (byte)3),
    ACK("Ack",(byte)4),
    CONTENT("Content",(byte)5),
    STORE("Store",(byte)6),
    REPLY("Reply",(byte)7),
    SIMPLE("Simple",(byte)8);

    public String getType() {
        return type;
    }

    public byte getCode() {
        return code;
    }

    private final String type;
    private final byte code;

    private messageType(String type, byte code)
    {
        this.type = type;
        this.code = code;
    }
};