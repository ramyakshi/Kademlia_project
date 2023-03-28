package messageComponents;

import kademlia.Config;
import kademlia.Node;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

public class ConnectMessage implements Message{

    public static final messageType msgType = messageType.CONNECT;

    // THe node from which the message starts/originates
    private Node starter;

    public ConnectMessage(Node starter)
    {
        this.starter = starter;
    }

    public static Config config;

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

    public Node getOriginNode()
    {
        return this.starter;
    }
}
