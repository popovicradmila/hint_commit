package main.java.ch.epfl.lpd.store;

import main.java.ch.epfl.lpd.App;

public class WriteCommand extends Command {
	
	public WriteCommand(StoreMap map, String arg1, String arg2){
		super(map,arg1,arg2);
	}
	
	public String execute(){
		int senderId = App.rb.me.getId();
		map.timestamps[senderId]++;
		map.put(key, value);

		int[] tsmps = {map.timestamps[0], map.timestamps[1], map.timestamps[2]};
		map.outQueue.put(senderId, key, value, tsmps);
		map.outQueue.lock.lock();
		map.outQueue.notEmpty.signal();
		map.outQueue.lock.unlock();
		
		if (App.rb.me.getId()==1)
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
	public String toString()
	{
		return "get:"+"["+key+"]"+",["+value+"]";
	}
}
