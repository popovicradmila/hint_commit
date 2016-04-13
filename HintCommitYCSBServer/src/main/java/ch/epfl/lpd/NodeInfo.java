package main.java.ch.epfl.lpd;

import java.net.DatagramSocket;
import java.net.SocketException;


public class NodeInfo
{
    private String ip;
    private int port;
    private int id;
    private DatagramSocket socket;


    public NodeInfo(String nodeIP, int nodePort, int id) {
        this.ip = nodeIP;
        this.port = nodePort;
        this.id = id;
    }

    public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPort() {
        return this.port;
    }

    public String getIP() {
        return this.ip;
    }

    public String toString() {
        return "["+this.id+"] "+ this.ip + ":" + this.port;
    }
}
