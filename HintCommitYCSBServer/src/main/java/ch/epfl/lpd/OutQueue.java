package main.java.ch.epfl.lpd;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.ch.epfl.lpd.net.BroadcastMsg;
import main.java.ch.epfl.lpd.store.StoreMap;

public class OutQueue extends Thread{
	
	public StoreMap map;
	public ConcurrentLinkedQueue<InOutQueueC> queue = new ConcurrentLinkedQueue<InOutQueueC>();
	public final Lock lock = new ReentrantLock();
	public final Condition notEmpty = lock.newCondition();

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
					e1.printStackTrace();
				}
    		for(Iterator<InOutQueueC> it = queue.iterator(); it.hasNext(); ) {
    		      InOutQueueC entry = it.next();
    		      queue.remove(entry);
    		      BroadcastMsg msg = new BroadcastMsg(entry, entry.timestamps[entry.sender], entry.sender);
    		      App.rb.broadcast(msg);
    		     }
    	}
    }
}
