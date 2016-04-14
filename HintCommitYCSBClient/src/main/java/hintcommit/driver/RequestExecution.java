package main.java.hintcommit.driver;

import java.util.concurrent.Callable;

public class RequestExecution {

	private NettyClient nc;
	private String command;
	public HintRequestExecution hre;
	public CommitRequestExecution cre;

	public RequestExecution(NettyClient nc) {
		this.nc = nc;
		hre = new HintRequestExecution(this);
		cre = new CommitRequestExecution(this);
	}

	public void setCommand(String c) {
		this.command = c;
	}

	public class HintRequestExecution implements Callable<String> {

		private RequestExecution re;
		
		public HintRequestExecution(RequestExecution re){
			this.re = re;
		}
		
		@Override
		public String call() throws Exception {

			nc.hcInstance.key = command.split(",")[1];
			nc.serverChannel.writeAndFlush(command);
			while (nc.hcInstance.hint == null)
				try {
					nc.hlock.lock();
					nc.hinted.await();
					nc.hlock.unlock();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			//nc.hcInstance.latency = nc.endTime - nc.startTime;
			// nc.hcInstance.printResponse();
			// nc.hcInstance.commit = null;
			// nc.hcInstance.hint = null;
			return nc.hcInstance.hint;
		}
	}

   public class CommitRequestExecution implements Callable<String> {

		private RequestExecution re;
		
		public CommitRequestExecution(RequestExecution re){
			this.re = re;
		}
	   
		@Override
		public String call() throws Exception {
			while (nc.hcInstance.commit == null)
				try {
					nc.clock.lock();
					nc.commited.await();
					nc.clock.unlock();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			String newCommit = nc.hcInstance.commit;
			nc.hcInstance.commit = null;
			nc.hcInstance.hint = null;

			return newCommit;
		}
	}

}