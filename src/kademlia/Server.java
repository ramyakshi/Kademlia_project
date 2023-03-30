package kademlia;

import messageComponents.ConnectReceiveHandler;
import messageComponents.Message;
import messageComponents.Receiver;
import messageComponents.messageType;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class Server {
    /*
    //TODO - Finish Implementation
    private static final int DATAGRAM_PACKET_SIZE = 64 * 1024;

    private final DatagramSocket socket;

    private transient boolean isRunning;

    private final Timer timerValue;

    private final HashMap<Integer, Receiver> receivers;

    private final HashMap<Integer, TimerTask> scheduledTasks;

    private final Node localNode;

    public static Map<BigInteger, Node> centralNodeList;

    public Server(int udpPort, Node localNode) throws SocketException
    {
        this.socket = new DatagramSocket(udpPort);
        this.localNode = localNode;
        this.isRunning = true;
        this.receivers = new HashMap<>();
        this.scheduledTasks = new HashMap<>();
        this.timerValue = new Timer();
        this.centralNodeList = new HashMap<>();
    }

    public void begin()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                listenForRequest();
            }
        }.start();
    }

    public void listenForRequest()
    {
        try
        {
            while(isRunning)
            {
                byte[] byteArray = new byte[DATAGRAM_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);
                socket.receive(packet);

                try{
                    DataInputStream packetDataStream = new DataInputStream(new ByteArrayInputStream(packet.getData(),packet.getOffset(),packet.getLength()));

                    //int id = packetDataStream.readBigInteger();
                    byte[] buffer = new byte[DATAGRAM_PACKET_SIZE];
                    packetDataStream.readFully(buffer);
                    BigInteger id = new BigInteger(1, buffer);
                    byte msgCode = packetDataStream.readByte();

                    packetDataStream.close();

                    Receiver receiver;
                    if(this.receivers.containsKey(id))
                    {
                        synchronized (this)
                        {
                            receiver = this.receivers.get(id);
                            this.receivers.remove(id);
                            TimerTask task = this.scheduledTasks.get(id);
                            this.scheduledTasks.remove(id);

                            if(task!= null)
                            {
                                task.cancel();
                            }
                        }
                    }
                    else
                    {
                        if(msgCode == messageType.CONNECT.getCode( ))
                        {
                            receiver = (Receiver) new ConnectReceiveHandler(this,centralNodeList.get(id));
                        }
                    }

                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Exception while listening");
        }
    }
    */
}
