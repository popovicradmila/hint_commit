package main.java.ch.epfl.lpd;

import java.io.Serializable;

public class InOutQueueC implements Serializable{
	public int sender;
	public String key;
	public String value;
	public int[] timestamps;
    private static final long serialVersionUID = 1L;
	
	public InOutQueueC(int sender, String key, String value, int[] timestamps) {
		super();
		this.sender = sender;
		this.key = key;
		this.value = value;
		this.timestamps = timestamps;
	}
}
