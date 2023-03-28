package messageComponents;

import kademlia.Config;
import kademlia.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

public class AckMessage implements Message{

    // THe node from which the message starts/originates
    private Node starter;

    public static Config config;

    private static final messageType msgType = messageType.ACK;

    public AckMessage(Node node)
    {
        this.starter = node;
    }

    // TODO - implement toStream()
    @Override
    public void toStream(DataOutputStream output) throws Exception {

    }

    @Override
    public final void fromStream(DataInputStream input) throws Exception
    {
        byte[] byteInfo = new byte[this.config.bitSpace/8];
        input.readFully(byteInfo);
        this.starter.port = input.readInt();
        // Not sure of below conversion from byte[] to BigInt
        this.starter.id = new BigInteger(byteInfo);
    }

    @Override
    public byte msgCode() {
        return this.msgType.getCode();
    }

}
