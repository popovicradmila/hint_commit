package main.java.hintcommit.driver;

public class HintCommit {
    public String key;
    public String hint;
    public String commit;
    public long   latency;

    public void printResponse()
    {
        System.out.println("Key: " + key + ", hint: " + hint + ", commit: " + commit + ", latency [nano]: " + latency);
    }
}
