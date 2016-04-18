package main.java.hintcommit.cache;

import java.util.LinkedList;

public class LruList {

	private LinkedList<String> list = new LinkedList<String>();
	
	public void put(String key){
		boolean wasInside = list.removeFirstOccurrence(key);
		list.add(key);
	}
	
	public void update(String key){
		put(key);
	}
	
	public String evictKey(){
		return list.pollFirst();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i=0;i<list.size()-1;i++){
			sb.append(list.get(i).substring(0, list.get(i).length()-2));
			sb.append(",");
		}
		if (list.size()>0)
			sb.append(list.get(list.size()-1).substring(0, list.get(list.size()-1).length()-2));
		sb.append("]");	
		return sb.toString();
	}
}
