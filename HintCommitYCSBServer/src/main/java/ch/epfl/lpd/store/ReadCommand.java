package main.java.ch.epfl.lpd.store;

import main.java.ch.epfl.lpd.store.StoreMap;
import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.net.ClientThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReadCommand extends Command {
    public static Logger logger = LoggerFactory.getLogger(ReadCommand.class);

    public ReadCommand(StoreMap map, String arg1)
    {
        super(map, arg1, null);
    }

    public void execute(ClientThread clientThread)
    {
        clientThread.key  = key;
        clientThread.hint = map.get(key);

        logger.info("Attempted to read key " + key + " with value: " + clientThread.hint);

        //if running the vanilla version, send just the value currently in the store and return
        if (App.vanillaVers)
        {
            clientThread.hint();
            return;
        }

        App.store.inQueue.queueLock.lock();
        clientThread.freshPending = App.store.inQueue.getFresh(key);
        App.store.inQueue.queueLock.unlock();

        logger.info("Read a value : " + key + "," + clientThread.hint);
        /**/
        if (clientThread.freshPending != null)
        {
            clientThread.hint();
            while (clientThread.commit == null)
            {
                try {
                    clientThread.lock.lock();
                    clientThread.commited.await();
                    clientThread.lock.unlock();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    logger.error("Got exception", e1);
                }
            }
            clientThread.commit(false);
        }
        else
        {
            clientThread.commit = clientThread.hint;
            clientThread.commit(true);
        }
    }

    public String toString()
    {
        return "get:" + "[" + key + "]";
    }
}
