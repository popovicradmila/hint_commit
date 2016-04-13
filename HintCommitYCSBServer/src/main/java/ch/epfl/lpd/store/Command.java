package main.java.ch.epfl.lpd.store;

public abstract class Command {
	protected String key;
	protected String value;
	protected StoreMap map;
	
	public Command(StoreMap map, String key, String value){
		this.map = map;
		this.key = key;
		this.value = value;
	}
	
	public String execute(){
		return null;
	};
}
