package net.floodlightcontroller.nfchaining;

import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.module.IFloodlightService; 

public interface INFChainingREST extends IFloodlightService {
    public Map<String, DatapathId> associateNfSwitch(String nf, String sw);
    public int defineNfChain(String[] nfChain);
    public boolean associatePathToNFChain(String sourceIp, String destIp, int nfChainId);    
}
