package main.java.ch.epfl.lpd.store;

import main.java.ch.epfl.lpd.store.StoreMap;
import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.net.ClientThread;

public class ReadCommand extends Command {

	public ReadCommand(StoreMap map, String arg1){
		super(map,arg1,null);
	}

	public void execute (ClientThread c){
		
		c.key = key;
		c.hint = map.get(key);
		
		//if running the vanilla version, send just the value currently in the store and return
		if (App.vanillaVers)
		{
			c.hint();
			return;
		}
		
		App.store.inQueue.queueLock.lock();
		c.freshPending = App.store.inQueue.getFresh(key);
		App.store.inQueue.queueLock.unlock();
		
		//System.out.println("Read a value : "+key +"," + c.hint);
		/**/
		if (c.freshPending!=null)
		{
			c.hint();
			while(c.commit==null)
				try {
					c.lock.lock();
						c.commited.await();
					c.lock.unlock();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			c.commit(false);
		}
		else
		{
			c.commit = c.hint;
			c.commit(true);
		}
	}
	
	public String toString()
	{
		return "get:"+"["+key+"]";
	}
}
