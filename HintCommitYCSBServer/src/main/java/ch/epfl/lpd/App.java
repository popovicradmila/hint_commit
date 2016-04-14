package main.java.ch.epfl.lpd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import main.java.ch.epfl.lpd.net.ClientThread;
import main.java.ch.epfl.lpd.net.PointToPointLink;
import main.java.ch.epfl.lpd.net.PortListener;
import main.java.ch.epfl.lpd.net.ReliableBroadcast;
import main.java.ch.epfl.lpd.store.Command;
import main.java.ch.epfl.lpd.store.ReadCommand;
import main.java.ch.epfl.lpd.store.StoreMap;
import main.java.ch.epfl.lpd.store.WriteCommand;
import sun.misc.Signal;
import sun.misc.SignalHandler;


public class App {

	public static ReliableBroadcast rb;
	public static ServerThread st;
	public static StoreMap store;
	public static LinkedList<Command> commands = new LinkedList<Command>();
	public static LinkedList<Command> readCommands = new LinkedList<Command>();
    public static PrintWriter writer;
    public static ClientThread client;


    public static void main(String args[]) throws Exception {

        /**
         * Parse command line arguments.
         */

        List<NodeInfo> nodes = parseCmdNodesInfo(args);
        for (int i = 0; i < 3; i++)
            System.out.println("Node [" + i + "] info " + nodes.get(i).toString());

        int thisNodeIndex = Integer.parseInt(args[6]);
        if (thisNodeIndex > 2 || thisNodeIndex < 0)
            throw new Exception("Invalid node index (" + thisNodeIndex + "); must be >= 0 and <= 2!");
        System.out.println("This node has index: " + thisNodeIndex);

        String nettyPort = args[7];
        System.out.println("Netty listening on: " + nettyPort);

        /**
         * Instantiate the backend, i.e., the local in-memory store.
         */

        store = new StoreMap();

        /**
         * Instantiate point-to-point links to the other nodes.
         */

        DatagramSocket mySocket = null;
        try {
			mySocket = new DatagramSocket(nodes.get(thisNodeIndex).getPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        NodeInfo me = nodes.get(thisNodeIndex);
        rb = new ReliableBroadcast(mySocket, null, me);

        rb.ptpLinks = establishPTPLinks(nodes, thisNodeIndex);

        writer = new PrintWriter("server.log", "UTF-8");

	    /**
         * Now wait for the starting signal...
         *
         * This flag tells the App when it can start executing the trace, i.e.,
         * when SIGINT was received.
         */

        CountDownLatch startSignal = new CountDownLatch(1);
        registerSignalHandler(startSignal);

        st = new ServerThread(nettyPort);
        st.start();

        System.out.println("Awaiting for SIGINT... please send the signal using: kill -INT PID");
        startSignal.await();
        System.out.println("Got SIGINT. Starting...");


        PortListener pl = new PortListener();
		client = new ClientThread();
        pl.start();

        for (PointToPointLink ptpl:rb.ptpLinks.values())
        	ptpl.start();


        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                public void run() {
                    System.out.println("Shutdown Hook is running !");
                }});

        /*
        for (int i = 0; i < commands.size(); i++) {
            commands.get(i).execute();
        }
        */

        writer.close();
    }


    private static HashMap<Integer, PointToPointLink> establishPTPLinks(List<NodeInfo> nodes, int index) throws Exception {
        HashMap<Integer, PointToPointLink> links = new HashMap<Integer, PointToPointLink>();

        for (int i = 0; i < 3; i++) {
            if (i == index)
            {
                continue;
            }
            links.put(i,new PointToPointLink(nodes.get(i), nodes.get(index)));
        }
        return links;
    }

    private static void parseInputFile(String path) throws Exception {
    	try {
        	BufferedReader br = new BufferedReader(new FileReader(path));
    	    for(String line; (line = br.readLine()) != null; ) {
    	    	String[] tokens = line.split(",");
    	    	if (tokens[0].equals("put"))
    	    	{
    	    		WriteCommand c = new WriteCommand(store, tokens[1], tokens[2]);
    	    		commands.add(c);
    	    	}
    	    	else
	    	    	if (tokens[0].equals("get"))
	    	    	{
	    	    		ReadCommand c = new ReadCommand(store, tokens[1]);
	    	    		readCommands.add(c);
	    	    	}
	    	    	else
	    	    	{

	    	    	}
    	    }
    	}
    	    catch (Exception e){
				e.printStackTrace();
    	    }
    	    finally{ }

    }

    private static List<NodeInfo> parseCmdNodesInfo(String args[]) throws Exception {
        if (args.length < 8)
            throw new Exception("Insuffient command line arguments!");

        List<NodeInfo> nInfo = new ArrayList<NodeInfo>();
        for (int i = 0; i < 3; i++)
        {
            int pos = i*2;

            String ip = args[pos];
            int port = Integer.parseInt(args[pos + 1]);
            NodeInfo n = new NodeInfo(ip, port,i);

            nInfo.add(n);
        }
        return nInfo;
    }

    private static void registerSignalHandler(final CountDownLatch signalLatch)
    {
        Signal.handle(new Signal("INT"),
            new SignalHandler() {
                public void handle(Signal sig) {
                    // decrement the counter to signal that the App can start
                    signalLatch.countDown();
                }
            }
        );
    }
}
