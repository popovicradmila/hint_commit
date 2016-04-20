package main.java.ch.epfl.lpd.net;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.NodeInfo;
import main.java.ch.epfl.lpd.net.ProtocolMsg.Ack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PointToPointLink extends Thread
{
    private DatagramSocket socket;
    private DatagramSocket clientSocket;
    private InetAddress    receiverIP;
    public NodeInfo        theOther;
    private NodeInfo       me;
    private int            receiverPort;
    public long            lastAcked     = 0;
    private long           nextToDeliver = 1;
    private ConcurrentHashMap<Integer, DatagramPacket> toSend = new ConcurrentHashMap<Integer, DatagramPacket>();
    private final Lock      lock     = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    public static Logger logger = LoggerFactory.getLogger(PointToPointLink.class);

    public PointToPointLink(NodeInfo nInfo, NodeInfo m) throws Exception
    {
        logger.info("Establishing point-to-point link to " + nInfo.toString());
        clientSocket = new DatagramSocket();
        socket       = App.rb.mySocket;
        receiverIP   = InetAddress.getByName(nInfo.getIP());
        receiverPort = nInfo.getPort();
        theOther     = nInfo;
        me           = m;
    }

    public void run()
    {
        while (true)
        {
            while (toSend.size() == 0)
            {
                try {
                    lock.lock();
                    notEmpty.await();
                    lock.unlock();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    logger.error("Got exception", e1);
                }
            }
            for (Iterator<Map.Entry<Integer, DatagramPacket> > it = toSend.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry<Integer, DatagramPacket> entry = it.next();
                if (entry.getKey() <= lastAcked)
                {
                    it.remove();
                }
                else
                {
                    try {
                        // logger.info("Sending a packet.. " + entry.getValue().getLength());
                        clientSocket.send(entry.getValue());
                    }
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("Got exception", e);
                    }
                }
            }
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error("Got exception", e);
            }
        }
    }

    public void sendAck(ProtocolMsg message) throws Exception
    {
        ProtocolMsg.Ack       ack          = new Ack(message, me.getId());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream    os           = new ObjectOutputStream(outputStream);
        os.writeObject(ack);
        byte[]         data   = outputStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(data, data.length, receiverIP, receiverPort);
        clientSocket.send(packet);
    }

    public void stubbornSend(ProtocolMsg message) throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream    os           = new ObjectOutputStream(outputStream);

        os.writeObject(message);
        byte[] data = outputStream.toByteArray();

        DatagramPacket packet = new DatagramPacket(data, data.length, receiverIP, receiverPort);

        toSend.put(message.getTimestamp(), packet);
        lock.lock();
        notEmpty.signal();
        lock.unlock();
    }

    public void deliver(ProtocolMsg msg) throws Exception
    {
        if (msg.getTimestamp() == nextToDeliver)
        {
            nextToDeliver++;
            App.rb.deliver(msg);
            sendAck(msg);
        }
    }

    public void deliverAck(Ack ack) throws Exception
    {
        if (ack.getTimestamp() > lastAcked)
        {
            lastAcked = ack.getTimestamp();
        }
    }

    public void shutdownLink()
    {
        logger.warn("Closing link");
        socket.close();
    }

    public String toString()
    {
        return receiverIP.toString() + ":" + Integer.toString(receiverPort);
    }
}
