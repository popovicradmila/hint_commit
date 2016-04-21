package main.java.ch.epfl.lpd.store;


import java.util.HashMap;

import main.java.ch.epfl.lpd.InOutQueueC;
import main.java.ch.epfl.lpd.InQueue;
import main.java.ch.epfl.lpd.OutQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreMap
{
    private HashMap<String, String> map;
    public int[] timestamps = {0,0,0};
    public InQueue inQueue;
    public OutQueue outQueue;
    public HashMap<String,InOutQueueC> latestUpdate = new HashMap<>();

    public static Logger logger = LoggerFactory.getLogger(StoreMap.class);

    public StoreMap() {
        this.map = new HashMap<String, String>();
        this.inQueue  = new InQueue(this);
        this.outQueue = new OutQueue(this);
        this.inQueue.start();
        this.outQueue.start();
    }

    public String get(String key) {
        return map.get(key);
    }

    public void put(String key, String value) {
        map.put(key, value);
        logger.info("StoreMap size so far: " + map.size());
    }

    public void remove(String key) {
        map.remove(key);
    }

    public String toString() {
        return map.toString();
    }
}
