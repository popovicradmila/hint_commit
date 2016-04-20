package main.java.hintcommit.driver;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.Gson;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.StringByteIterator;

public class App {

	public static Gson gson = new Gson();

	public static void main(String[] args) {
		HintCommitClient ya = new HintCommitClient();

		try {
			ya.init();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<String, ByteIterator> vals = new HashMap<String, ByteIterator>();
		vals.put("1", new StringByteIterator("a"));
		HashMap<String, ByteIterator> res= new HashMap<String, ByteIterator>();
		HashSet<String> fields = new HashSet<String>();
		fields.add("value");
		ya.read("", "2", fields, res);
		ya.update("", "1", vals);
		ya.read("", "1", fields, res);
		ya.read("", "1", fields, res);


		HashMap<String, ByteIterator> vals1 = new HashMap<String, ByteIterator>();
		vals1.put("2", new StringByteIterator("b"));
		ya.update("", "2", vals1);



		HashMap<String, ByteIterator> vals2 = new HashMap<String, ByteIterator>();
		vals2.put("1", new StringByteIterator("c"));
		ya.update("", "3", vals2);


		ya.read("", "2", fields, res);

		ya.read("", "3", fields, res);

		ya.read("", "1", fields, res);
		ya.read("", "3", fields, res);
		ya.read("", "2", fields, res);
		ya.read("", "1", fields, res);


	}
}
