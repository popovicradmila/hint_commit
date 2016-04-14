package hintcommit.driver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.StringByteIterator;

public class App {


	public static NettyClient nc;
	public static RequestExecution re;
	public static boolean vanillaVers = false;
	
	public static void main(String[] args) {
	    CountDownLatch nettyStartupLatch = new CountDownLatch(1);
		YcsbAdapter ya = new YcsbAdapter();
		
		nc = new NettyClient(nettyStartupLatch, Integer.parseInt(args[0]));
		nc.start();
		re = new RequestExecution(nc);
		
		try {
			ya.init();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			nettyStartupLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<String, ByteIterator> vals = new HashMap<>();
		vals.put("1", new StringByteIterator("a"));
		HashMap<String, ByteIterator> res= new HashMap<String, ByteIterator>(); 
		HashSet<String> fields = new HashSet<>();
		fields.add("value");
		ya.update("", "1", vals);
		System.out.println(ya.read("", "1", fields, res));
		System.out.println(ya.read("", "3", fields, res));
		
		String albumjson = "{\"privacy\":\"a\", \"photos\":[\"1\",\"2\"]}";
		Album a = new Album(albumjson);
		System.out.println(a);
	}
}
