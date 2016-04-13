package main.java.ch.epfl.lpd.net;

import java.io.Serializable;

public class ProtocolMsg implements Serializable{
	private BroadcastMsg msg;
	private int timestamp;
	
	private int senderId;
    	private static final long serialVersionUID = 1L;
	
	public ProtocolMsg(BroadcastMsg msg, int timestamp, int sender) {
		super();
		this.msg = msg;
		this.timestamp = timestamp;
		this.senderId = sender;
	}
	
	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public BroadcastMsg getMsg() {
		return msg;
	}
	public void setMsg(BroadcastMsg msg) {
		this.msg = msg;
	}
	
	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public static class Ack implements Serializable{
		public int senderId;
		public int timestamp;
		public int acker;    
		private static final long serialVersionUID = 1L;
		
		public Ack(ProtocolMsg msg, int acker){
			this.senderId = msg.senderId;
			this.timestamp = msg.timestamp;
			this.acker = acker;
		}
		
		public int getSenderId() {
			return senderId;
		}

		public void setSenderId(int senderId) {
			this.senderId = senderId;
		}

		public int getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(int timestamp) {
			this.timestamp = timestamp;
		}

		public int getAcker() {
			return acker;
		}

		public void setAcker(int acker) {
			this.acker = acker;
		}

	}
	
}
