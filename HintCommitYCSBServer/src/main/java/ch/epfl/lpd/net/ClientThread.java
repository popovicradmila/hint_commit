package main.java.ch.epfl.lpd.net;


import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.InOutQueueC;
import main.java.ch.epfl.lpd.store.Command;
import main.java.ch.epfl.lpd.store.ReadCommand;
import main.java.ch.epfl.lpd.store.WriteCommand;


public class ClientThread
{
	public String key;
	public String hint;
	public String commit;
	public volatile ArrayList<InOutQueueC> freshPending;
	public int latency;    
	public Lock lock = new ReentrantLock();
    public Condition commited = lock.newCondition();
    public ChannelHandlerContext ctx;


    public ClientThread() throws Exception {
    }
    
    public void run(String command, ChannelHandlerContext ctx){
    	this.ctx = ctx;
    	String[] tokens = command.split(",");
    	if (tokens[0].equals("put"))
    	{
    		WriteCommand c = new WriteCommand(App.store, tokens[1], tokens[2]);
    	}
    	else
	    	if (tokens[0].equals("get"))
	    	{
	    		ReadCommand c = new ReadCommand(App.store, tokens[1]);
	            c.executeClient(this);
	    	}
    }
    
    public void hint(){
    	ctx.writeAndFlush("h"+hint+"\r\n");
    }

    public void commit(){
    	ctx.writeAndFlush("c"+commit+"\r\n");
		//printResponse();
		reset();
    }

    public void reset() {
    	freshPending=null;
        key = null;
        commit = null;
        hint = null;
    }

    public void printResponse() {
        System.out.println("Key: "+key+", hint: "+hint+", commit: "+commit);
    }
}
