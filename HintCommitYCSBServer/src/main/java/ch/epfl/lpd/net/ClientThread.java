package main.java.ch.epfl.lpd.net;


import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import main.java.ch.epfl.lpd.App;
import main.java.ch.epfl.lpd.InOutQueueC;
import main.java.ch.epfl.lpd.store.Command;
import main.java.ch.epfl.lpd.store.ReadCommand;
import main.java.ch.epfl.lpd.store.WriteCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientThread
{
    public String key;
    public String hint;
    public String commit;
    public volatile ArrayList<InOutQueueC> freshPending;
    public int                   latency;
    public Lock                  lock     = new ReentrantLock();
    public Condition             commited = lock.newCondition();
    public ChannelHandlerContext ctx;

    public static Logger logger = LoggerFactory.getLogger(ClientThread.class);

    public ClientThread() throws Exception
    {
    }

    public void run(String command, ChannelHandlerContext ctx)
    {
        logger.info("Command received: " + command);
        this.ctx = ctx;

        String[] tokens = command.split(",", 3);

        if (tokens[0].equals("put"))
        {
            logger.info("Handling a <put> command for key: " + tokens[1]);
            if (tokens.length == 3)
            {
                WriteCommand c = new WriteCommand(App.store, tokens[1], tokens[2]);
                c.execute();
            }
            else
            {
                sendError("Badly formed command");
            }
        }
        else
        if (tokens[0].equals("get"))
        {
            logger.info("Handling a <get> command for key: " + tokens[1]);
            if (tokens.length >= 2)
            {
                ReadCommand c = new ReadCommand(App.store, tokens[1]);
                c.execute(this);
            }
            else
            {
                sendError("Badly formed command");
            }
        }
        else
        {
            sendError("Unknown command");
        }
    }

    public void hint()
    {
        logger.info("Replying to <get> with HINT result: " + hint);
        ctx.writeAndFlush("h" + hint + "\r\n");
    }

    public void sendError(String err)
    {
        logger.warn("Sending error to client: " + err);
        ctx.writeAndFlush("e" + err + "\r\n");
    }

    public void commit(boolean equalsHint)
    {
        if (!equalsHint)
        {
            ctx.writeAndFlush("c" + commit + "\r\n");
        }
        else
        {
            ctx.writeAndFlush("b" + commit + "\r\n");
        }
        //printResponse();
        reset();
    }

    public void reset()
    {
        freshPending = null;
        key          = null;
        commit       = null;
        hint         = null;
    }

    public void printResponse()
    {
        logger.info("Key: " + key + ", hint: " + hint + ", commit: " + commit);
    }
}
