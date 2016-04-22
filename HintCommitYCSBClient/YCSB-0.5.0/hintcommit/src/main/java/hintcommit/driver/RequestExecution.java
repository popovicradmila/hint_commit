package main.java.hintcommit.driver;

import java.util.concurrent.Callable;

import main.java.hintcommit.cache.CacheStore;
import main.java.hintcommit.driver.HintCommitClient.Version;

public class RequestExecution {
    private NettyClient           nc;
    private String                command;
    public HintRequestExecution   hintRequest;
    public CommitRequestExecution commitRequest;
    public CacheRequestExecution  cacheRequest;
    public UpdateRequestExecution updateRequest;
    public CacheStore             cache;
    public Version                vers;

    public RequestExecution(NettyClient nc, CacheStore cache, Version vers
                            )
    {
        this.nc       = nc;
        hintRequest   = new HintRequestExecution();
        commitRequest = new CommitRequestExecution();
        cacheRequest  = new CacheRequestExecution();
        updateRequest = new UpdateRequestExecution();
        this.cache    = cache;
        this.vers     = vers;
    }

    public void setCommand(String c)
    {
        this.command = c;
    }

    public class HintRequestExecution implements Callable<String> {
        @Override
        public String call() throws Exception
        {
            nc.hcInstance.key = command.split(",")[1];
            nc.serverChannel.writeAndFlush(command);
            while (nc.hcInstance.hint == null)
            {
                try {
                    nc.hlock.lock();
                    nc.hinted.await();
                    nc.hlock.unlock();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            //nc.hcInstance.latency = nc.endTime - nc.startTime;
            // nc.hcInstance.printResponse();
            // nc.hcInstance.commit = null;
            // nc.hcInstance.hint = null;
            // if (vers.equals(Version.HINT_CACHE))
            // {
            //     cache.put(nc.hcInstance.key, nc.hcInstance.hint);
            // }
            String newHint = nc.hcInstance.hint;
            if (vers.equals(Version.HINT_CACHE) || vers.equals(Version.JUST_HINT))
            {
                nc.hcInstance.commit = null;
                nc.hcInstance.hint   = null;
            }
            return newHint;
        }
    }

    public class CommitRequestExecution implements Callable<String> {
        @Override
        public String call() throws Exception
        {
            while (nc.hcInstance.commit == null)
            {
                try {
                    nc.clock.lock();
                    nc.commited.await();
                    nc.clock.unlock();
                }
                catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            String newCommit = nc.hcInstance.commit;
            nc.hcInstance.commit = null;
            nc.hcInstance.hint   = null;

            return newCommit;
        }
    }

    public class CacheRequestExecution implements Callable<String> {
        @Override
        public String call() throws Exception
        {
            String key = command.split(",")[1].trim();

            String res = cache.get(key);
            if (res == null) {
                // Cache miss: we have to go to the backend
                // res =
                return res;
            } else {
                return res;
            }
        }
    }

    public class UpdateRequestExecution implements Callable<String> {
        @Override
        public String call() throws Exception
        {
            nc.serverChannel.writeAndFlush(command);
            nc.updateBarrier.await();
            nc.updateBarrier.reset();
            return "ok";
        }
    }
}
