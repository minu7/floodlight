package net.floodlightcontroller.nfchaining;

import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.types.DatapathId;

public class NFChain {
    private String[] chain;
    private String sourceIp;
    private String destIp;
    private List<DatapathId> switchesUsed = null;
    
    public NFChain(String[] chain) {
        this.chain = chain; 
        this.sourceIp = "";
        this.destIp = "";
    }

    public String[] getChain() {
        return this.chain;
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

    public void setSwitchesUsed(List<DatapathId> list) {
        this.switchesUsed = list;
    }

    public List<DatapathId> getSwitchesUsed() {
        return this.switchesUsed;
    }

    public void freePath() {
        this.sourceIp = "";
        this.destIp = "";
        this.switchesUsed = null;
    }


}
