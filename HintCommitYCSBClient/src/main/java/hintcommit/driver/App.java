package main.java.hintcommit.driver;

import com.google.gson.Gson;

public class App {

	public static Gson gson = new Gson();
	
	public static void main(String[] args) {
		/*YcsbAdapter ya = new YcsbAdapter();
		
		try {
			ya.init();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		String albumjson = "{\"privacy\":\"a\", \"photos\":[\"1\",\"2\"]}";
		Album a = new Album(albumjson, gson);
		System.out.println(a);
		System.out.println(a.toJson(gson));
	}
}
