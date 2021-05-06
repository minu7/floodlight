package net.floodlightcontroller.nfchaining;

import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService; 

public interface INFChainingREST extends IFloodlightService {
    public Map<String, String> associateNfSwitch(String nf, String sw);
    public int defineNfChain(String[] nfChain);
    public boolean associatePathToNFChain(String sourceIp, String destIp, int nfChainId);    
}
