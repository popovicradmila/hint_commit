package main.java.ch.epfl.lpd.store;

import main.java.ch.epfl.lpd.App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WriteCommand extends Command {
    public static Logger logger = LoggerFactory.getLogger(WriteCommand.class);

    public WriteCommand(StoreMap map, String arg1, String arg2)
    {
        super(map, arg1, arg2);
    }

    public String execute()
    {
        int senderId = App.rb.me.getId();

        map.timestamps[senderId]++;
        map.put(key, value);

        int[] tsmps = { map.timestamps[0], map.timestamps[1], map.timestamps[2] };
        map.outQueue.put(senderId, key, value, tsmps);
        map.outQueue.lock.lock();
        map.outQueue.notEmpty.signal();
        map.outQueue.lock.unlock();

        if (App.rb.me.getId() == 1)
        {
            try {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error("Got exception", e);
            }
        }
        return null;
    }

    public String toString()
    {
        return "put:" + "[" + key + "]" + ",[" + value + "]";
    }
}
