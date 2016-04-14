package hintcommit.driver;

import java.util.ArrayList;

import com.google.gson.Gson;

public class Album {
	
	private String privacy;
	private ArrayList<String> photos;
	private Gson gson = new Gson();
	
	public Album(String privacy, ArrayList<String> photos) {
		super();
		this.privacy = privacy;
		this.photos = photos;
	}
	
	public Album(String json) {
		super();
		Album a = gson.fromJson(json, Album.class);
		this.photos = a.photos;
		this.privacy = a.privacy;
	}
	
	public String getPrivacy() {
		return privacy;
	}
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}
	public ArrayList<String> getPhotos() {
		return photos;
	}
	public void setPhotos(ArrayList<String> photos) {
		this.photos = photos;
	}

	@Override
	public String toString() {
		return "Album [privacy=" + privacy + ", photos=" + photos + "]";
	}
}
