package net.floodlightcontroller.nfchaining;

public class NFChain {
    private String[] chain;
    private String sourceIp;
    private String destIp;
    
    public NFChain(String[] chain) {
        this.chain = chain; 
        this.sourceIp = "";
        this.destIp = "";
    }

    public boolean setPath(String sourceIp, String destIp) {
        if (!this.sourceIp.isEmpty()) {
           return false;
        }
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        return true;
    }

    public String getSouceIp() {
        return sourceIp;
    }

    public String getDestIp() {
        return destIp;
    }

    public String[] getNFChain() {
        return chain;
    }

}
