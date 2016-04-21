package main.java.ch.epfl.lpd;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.ch.epfl.lpd.net.BroadcastMsg;
import main.java.ch.epfl.lpd.store.StoreMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutQueue extends Thread{

	public StoreMap map;
	public ConcurrentLinkedQueue<InOutQueueC> queue = new ConcurrentLinkedQueue<InOutQueueC>();
	public final Lock lock = new ReentrantLock();
	public final Condition notEmpty = lock.newCondition();

    public static Logger logger = LoggerFactory.getLogger(OutQueue.class);

	public OutQueue(StoreMap map)
	{
		this.map = map;
	}

	public void put(InOutQueueC c)
	{
		queue.add(c);
	}

	public void put(int sender, String key, String value, int[] timestamps)
	{
		queue.add(new InOutQueueC(sender, key, value, timestamps));
	}

	public InOutQueueC get()
	{
		return queue.poll();
	}


    public void run(){
    	while (true){
    		while(queue.size()==0)
				try {
					lock.lock();
					notEmpty.await();
					lock.unlock();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					logger.error("Got exception", e1);
				}
    		for(Iterator<InOutQueueC> it = queue.iterator(); it.hasNext(); ) {
    		      InOutQueueC entry = it.next();
    		      queue.remove(entry);
    		      BroadcastMsg msg = new BroadcastMsg(entry, entry.timestamps[entry.sender], entry.sender);
                //   logger.info("Sending out broadcast with TS: " + entry.timestamps[entry.sender]);
    		      App.rb.broadcast(msg);
    		     }
    	}
    }
}
