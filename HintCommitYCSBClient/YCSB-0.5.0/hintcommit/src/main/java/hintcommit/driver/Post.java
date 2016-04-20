package main.java.hintcommit.driver;

import com.google.gson.Gson;

public class Post {
	
	private String id;
	private String type;
	private String content;
	private String timestamp;
	private String author;
	
	public Post(){
		
	}
	
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
	
	public boolean equals(Post other)
    {
        boolean result = this.timestamp.equals(other.timestamp) &&
                         this.content.equals(other.content);

        String id = "";
        if (this.id != null) {
            id = this.id;
        } else if (other.id != null) {
            id = other.id;
        }

        if (result != true) {
            System.out.println("*      Diverged!!!!!!" + " || " + id);
            System.out.println(this.content + " != \n" + other.content + "\n");
            System.out.println(this.timestamp + " != \n" + other.timestamp);
        }

        return result;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
