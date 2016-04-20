package main.java.ch.epfl.lpd.net;


import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.NodeInfo;
import main.java.ch.epfl.lpd.net.ProtocolMsg.Ack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortListener extends Thread
{
    private DatagramSocket socket;
    private DatagramSocket clientSocket;
    private InetAddress    receiverIP;
    public NodeInfo        theOther;
    private int            receiverPort;

    public static Logger logger = LoggerFactory.getLogger(PortListener.class);

    public PortListener() throws Exception
    {
        socket = App.rb.mySocket;
    }

    public void run()
    {
        while (true)
        {
            try {
                receiveOnce();
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error("Exception raised.. ", e);
            }
        }
    }

    public ProtocolMsg receiveOnce() throws Exception
    {
        byte[]         receiveData   = new byte[10000];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        byte[]               data = receivePacket.getData();
        ByteArrayInputStream in   = new ByteArrayInputStream(data);
        ObjectInputStream    is   = new ObjectInputStream(in);
        //ProtocolMsg msg = null;
        Serializable content = null;

        if (is.available() <= 0) {
            // Back-off for a short while

        }

        try {
            content = (Serializable)is.readObject();
        }
        catch (ClassNotFoundException e) {
            logger.error("Exception raised.. ", e);
        }
        if (content instanceof ProtocolMsg)
        {
            ProtocolMsg msg    = (ProtocolMsg)content;
            int         sender = msg.getSenderId();
            App.rb.ptpLinks.get(sender).deliver(msg);
        }
        else
        {
            Ack ack   = (Ack)content;
            int acker = ack.getAcker();
            App.rb.ptpLinks.get(acker).deliverAck(ack);
        }
        return null;
    }

    public void shutdownLink()
    {
        logger.info("Closing link");
        socket.close();
    }

    public String toString()
    {
        return receiverIP.toString() + ":" + Integer.toString(receiverPort);
    }
}
