package main.java.ch.epfl.lpd.store;

import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.net.PointToPointLink;
import main.java.ch.epfl.lpd.net.ClientThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WriteCommand extends Command {
    public static Logger logger = LoggerFactory.getLogger(WriteCommand.class);

    public WriteCommand(StoreMap map, String arg1, String arg2)
    {
        super(map, arg1, arg2);
    }

    public String execute(ClientThread clientThread)
    {
        int senderId = App.rb.me.getId();

        map.timestamps[senderId]++;
        map.put(key, value);

        int[] tsmps = { map.timestamps[0], map.timestamps[1], map.timestamps[2] };
        map.outQueue.put(senderId, key, value, tsmps);
        map.outQueue.lock.lock();
        map.outQueue.notEmpty.signal();
        map.outQueue.lock.unlock();

        int c = 0;
        for (PointToPointLink ptpl: App.rb.ptpLinks.values())
        {
            int left = ptpl.getUnAcknowledged();
            try {
                if (left > 500) {
                    logger.info("PTPlink: Backpressure");
                    Thread.sleep(300);
                }
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error("Got exception", e);
            }
        }

        clientThread.writeOk();

        return null;
    }

    public String toString()
    {
        return "put:" + "[" + key + "]" + ",[" + value + "]";
    }
}
