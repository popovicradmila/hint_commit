package main.java.hintcommit.driver;

import com.google.gson.Gson;

public class Post {
	
	enum PostType{PHOTO, MESSAGE};
	

	private String id;
	private PostType type;
	private String content;
	private String timestamp;
	private String author;
	
	
	public Post(String json, Gson gson) {
		super();
		Post a = gson.fromJson(json, Post.class);
		this.author = a.author;
		this.id = a.id;
		this.type = a.type;
		this.timestamp = a.timestamp;
		this.content = a.content;
	}
	
	public String toJson(Gson gson){
		return gson.toJson(this);
	}

	@Override
	public String toString() {
		return "Post [id=" + id + ", type=" + type + ", content="+content+", timestamp="+timestamp+", author="+author+"]";
	}
}
