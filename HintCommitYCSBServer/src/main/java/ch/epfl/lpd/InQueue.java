package main.java.ch.epfl.lpd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.ch.epfl.lpd.store.StoreMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InQueue extends Thread{

	public StoreMap map;
	public ConcurrentLinkedQueue<InOutQueueC> queue = new ConcurrentLinkedQueue<InOutQueueC>();
	public Lock queueLock = new ReentrantLock();
    public HashMap<String, InOutQueueC> newestEntries = new HashMap<String, InOutQueueC>();
	public final Lock lock = new ReentrantLock();
	public final Condition notEmpty = lock.newCondition();

    public static Logger logger = LoggerFactory.getLogger(InQueue.class);

	public InQueue(StoreMap map)
	{
		this.map = map;
	}

	public void put(InOutQueueC c)
	{
		queue.add(c);
		newestEntries.put(c.key, c);
	}

	public void put(int sender, String key, String value, int[] timestamps)
	{
		InOutQueueC entry = new InOutQueueC(sender, key, value, timestamps);
		queue.add(entry);
		newestEntries.put(key, entry);
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
					lock.unlock();
					logger.error("Got exception", e1);
				}

    		for(Iterator<InOutQueueC> it = queue.iterator(); it.hasNext(); ) {
    		      InOutQueueC entry = it.next();//queue.peek(); //OVDE
                //   logger.info("About to send.." + entry.toString());
    		      if (safeToDeliver(entry)){
    		    	  queueLock.lock();
        		      queue.remove(entry);
        		      queueLock.unlock();

        			  map.timestamps[entry.sender] = entry.timestamps[entry.sender];
        		      map.put(entry.key, entry.value);

        				if (isInFreshPending(entry,App.client.freshPending))
						{
							App.client.commit = entry.value;
							App.client.lock.lock();
							App.client.commited.signal();
							App.client.lock.unlock();
						}
    		      }
    		      else{
  					lock.lock();
  						try {
							notEmpty.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							logger.error("Got exception", e);
						}
					lock.unlock();
    		      }
    		}
    	}
    }

    private boolean safeToDeliver(InOutQueueC c){
    	int sender = c.sender;
    	if (c.timestamps[sender]!=map.timestamps[sender]+1)
    		return false;
    	for (int i=0; i<3; i++){
    		if (i!=sender && map.timestamps[i]<c.timestamps[i])
    			return false;
    	}
    	return true;
    }

    public ArrayList<InOutQueueC> getFresh(String key){
    	if (queue.size()==0)
    		return null;
    	ArrayList<InOutQueueC> list =  new ArrayList<>();
    	for(Iterator<InOutQueueC> it = queue.iterator(); it.hasNext(); ) {
		      InOutQueueC entry = it.next();
		      if (entry.key.equals(key)){
			      list.add(entry);
		      }
		    }
    	return list;
    }

    public boolean isInFreshPending(InOutQueueC e, ArrayList<InOutQueueC> list)
    {
    	if (list==null)
    		return false;
    	for (InOutQueueC i:list)
    	{
    		if (i.equals(e))
    			return true;
    	}
    	return false;
    }
}
