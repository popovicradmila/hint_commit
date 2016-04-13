package main.java.ch.epfl.lpd.store;

import main.java.ch.epfl.lpd.store.StoreMap;
import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.net.ClientThread;

public class ReadCommand extends Command {

	public ReadCommand(StoreMap map, String arg1){
		super(map,arg1,null);
	}
	
	public String execute (){
		/*
		App.response.append(key);
		App.response.append(",");
		App.response.append(map.get(key));
		App.response.append("\n");
		*/
		//System.out.println(key+","+map.get(key));
		String response = key+","+map.get(key);
		App.writer.println(response.toString());
		App.writer.flush();
		return map.get(key);
	}

	public void executeClient (ClientThread c){
		c.key = key;
		c.hint = map.get(key);

		c.hint();
		
		App.store.inQueue.queueLock.lock();
		c.freshPending = App.store.inQueue.getFresh(key);
		App.store.inQueue.queueLock.unlock();
		
		//System.out.println("Read a value : "+key +"," + c.hint);
		/**/
		if (c.freshPending!=null)
		{
			while(c.commit==null)
				try {
					c.lock.lock();
						c.commited.await();
					c.lock.unlock();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			c.commit();
		}
		else
		{
			c.commit = c.hint;
			c.commit();
		}
		System.out.println("Stop waiting");
	}
	
	public String toString()
	{
		return "get:"+"["+key+"]";
	}
}
