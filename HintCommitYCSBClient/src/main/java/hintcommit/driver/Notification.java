package main.java.hintcommit.driver;


import java.util.Set;
import java.util.HashSet;


public class Notification {
    private String id;
    private String timestamp;
    private String content;

    public Notification(String nid, String ntimestamp, String ncontent)
    {
        this.id        = nid;
        this.timestamp = ntimestamp;
        this.content   = ncontent;
    }

    public String getId()
    {
        return this.id;
    }

    public String getTimestamp()
    {
        return this.timestamp;
    }

    public String getContent()
    {
        return this.content;
    }

    public static Set<String> getValueColumns()
    {
        Set<String> result = new HashSet<String>();
        result.add("timestamp");
        result.add("content");

        return result;
    }

    public static Set<String> getAllColumns()
    {
        Set<String> result = getValueColumns();
        result.add("n_id");

        return result;
    }

    public boolean equals(Notification other)
    {
        // System.out.println("Comparing timestamps.. " + this.timestamp + " vs. " + other.timestamp);
        // System.out.println("Comparing contents.. " + this.content + " vs. " + other.content);

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
}