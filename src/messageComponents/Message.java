package messageComponents;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface Message {

    // For writing stream to output
    public void toStream(DataOutputStream output) throws Exception;

    // For reading input stream
    public void fromStream(DataInputStream input) throws Exception;

    public byte msgCode();

    //Default
    public messageType msgType = messageType.SIMPLE;
}
