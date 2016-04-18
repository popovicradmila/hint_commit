package main.java.hintcommit.cache;

import java.util.HashMap;

public class CacheStore {
	
	private HashMap<String, String> map = new HashMap<>();
	private LruList lruList = new LruList();
	private int capacity;
	
	public CacheStore(int capacity){
		this.capacity = capacity;
	}
	
	public void put(String key, String value){
		if (map.size()<capacity || map.containsKey(key)){
			map.put(key, value);
			lruList.put(key);
		}
		else{
			mapEvict();
			map.put(key,value);
			lruList.put(key);
		}
	}
	
	public String get(String key){
		//not needed when hint read updates its position in the lru list
		/*
		if (map.get(key)!=null)
			lruList.update(key);
			*/ 
		return map.get(key);
	}
	
	private void mapEvict(){
		String lruKey = lruList.evictKey();
		map.remove(lruKey);
	}
}
