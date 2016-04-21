package main.java.ch.epfl.lpd.net;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.InOutQueueC;
import main.java.ch.epfl.lpd.NodeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReliableBroadcast {
    public DatagramSocket mySocket;
    public HashMap<Integer, PointToPointLink> ptpLinks;
    public NodeInfo me;
    public int      protocolTsp  = 0;
    public int[]    protocolTsps = { 0, 0, 0 };

    public static Logger logger = LoggerFactory.getLogger(ReliableBroadcast.class);

    private int   noDeliveredUntilMax = 0;
    private int   b   = 10000;
    private int[] max = { 1, 1, 1 };

    ConcurrentHashMap<HMKey, BroadcastMsg> delivered = new ConcurrentHashMap<HMKey, BroadcastMsg>();


    public ReliableBroadcast(DatagramSocket mySocket,
                             HashMap<Integer, PointToPointLink> ptpLinks, NodeInfo me)
    {
        super();
        this.mySocket = mySocket;
        this.ptpLinks = ptpLinks;
        this.me       = me;
    }

    public void broadcast(BroadcastMsg msg)
    {
        HMKey key = new HMKey(msg.getTimestamp(), msg.getSenderId());

        delivered.put(key, msg);
        noDeliveredUntilMax++;
        if (noDeliveredUntilMax >= b)
        {
            manageDelivered(msg);
        }

        // logger.info("rb: broadcast >>> " + msg.getTimestamp());

        for (PointToPointLink ptpl:ptpLinks.values())
        {
            try {
                protocolTsps[ptpl.theOther.getId()]++;
                ProtocolMsg pm = new ProtocolMsg(msg, protocolTsps[ptpl.theOther.getId()], me.getId());
                ptpl.stubbornSend(pm);
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error("Got exception", e);
            }
        }
    }

    public ProtocolMsg deliver(ProtocolMsg m)
    {
        BroadcastMsg msg = m.getMsg();

        // logger.info("rb: <<< deliver" + msg.getTimestamp());

        HMKey        key   = new HMKey(msg.getTimestamp(), msg.getSenderId());
        BroadcastMsg value = delivered.get(key);
        if ((value != null) || (msg.getTimestamp() < max[msg.getSenderId()]))
        {
        }
        else
        {
            BroadcastMsg bmsg = msg;
            App.store.inQueue.put(bmsg.getMsg());
            App.store.inQueue.lock.lock();
            App.store.inQueue.notEmpty.signal();
            App.store.inQueue.lock.unlock();
            InOutQueueC entry = bmsg.getMsg();
            delivered.put(key, msg);

            // for (PointToPointLink ptpl:ptpLinks.values())
            //      try {
            //          if (m.getSenderId()!=ptpl.theOther.getId() && msg.getSenderId()!=ptpl.theOther.getId())
            //          {
            //              protocolTsps[ptpl.theOther.getId()]++;
            //              ProtocolMsg pm = new ProtocolMsg(msg,protocolTsps[ptpl.theOther.getId()],me.getId());
            //              ptpl.stubbornSend(pm);
            //          }
            //      } catch (Exception e) {
            //          // TODO Auto-generated catch block
            //          logger.error("Got exception", e);
            //      }
            noDeliveredUntilMax++;
            if (noDeliveredUntilMax >= b)
            {
                manageDelivered(msg);
            }
            //return msg;
        }
        return null;
    }

    private void manageDelivered(BroadcastMsg msg)
    {
        boolean allIn = true;

        for (int i = max[msg.getSenderId()]; i < max[msg.getSenderId()] + b; i++)
        {
            HMKey        k = new HMKey(i, msg.getSenderId());
            BroadcastMsg v = delivered.get(k);
            if (v == null)
            {
                allIn = false;
                break;
            }
        }
        if (allIn)
        {
            int oldMax = max[msg.getSenderId()];
            max[msg.getSenderId()] += b;
            for (int i = oldMax; i < max[msg.getSenderId()]; i++)
            {
                HMKey k = new HMKey(i, msg.getSenderId());
                delivered.remove(k);
            }
            noDeliveredUntilMax = 0;
        }
    }

    private class HMKey {
        private final int flag1;
        private final int flag2;

        public HMKey(int flag1, int flag2)
        {
            this.flag1 = flag1;
            this.flag2 = flag2;
        }

        @Override
        public boolean equals(Object object)
        {
            if (!(object instanceof HMKey))
            {
                return false;
            }

            HMKey otherKey = (HMKey)object;
            return this.flag1 == otherKey.flag1 && this.flag2 == otherKey.flag2;
        }

        @Override
        public int hashCode()
        {
            int result = 17;     // any prime number

            result = 31 * result + Integer.valueOf(this.flag1).hashCode();
            result = 31 * result + Integer.valueOf(this.flag2).hashCode();
            return result;
        }
    }
}
