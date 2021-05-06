package net.floodlightcontroller.nfchaining;
 
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.MacAddress;
 
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.restserver.IRestApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NFChaining implements IOFMessageListener, IFloodlightModule, INFChainingREST {
   
    protected IFloodlightProviderService floodlightProvider;
    protected IRestApiService restApiService;
    protected Set<Long> macAddresses;
    protected static Logger logger = LoggerFactory.getLogger(NFChaining.class);
    protected ArrayList<NFChain> nfChains = new ArrayList<>();
    protected Map<String, String> nfSwitch = new HashMap<>();
    

    @Override
    public String getName() {
        return NFChaining.class.getSimpleName();
    }
    
    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }
 
    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(INFChainingREST.class);
        return l; 
    }
 
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(INFChainingREST.class , this);
        return m;
    }
 
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
            new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IRestApiService.class);
        l.add(INFChainingREST.class); 
        return l;
    }
 
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        macAddresses = new ConcurrentSkipListSet<Long>();
        restApiService = context.getServiceImpl(IRestApiService.class);
        restApiService.addRestletRoutable(new NFChainingWebRoutable());
    }
 
    
    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        restApiService.addRestletRoutable(new NFChainingWebRoutable());
    }
 
    @Override
    public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        Ethernet eth =
                IFloodlightProviderService.bcStore.get(cntx,
                                            IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
        Long sourceMACHash = eth.getSourceMACAddress().getLong();
        if (!macAddresses.contains(sourceMACHash)) {
            macAddresses.add(sourceMACHash);
            logger.info("MAC Address: {} seen on switch: {}",
                eth.getSourceMACAddress().toString(),
                sw.getId().toString());
        }
        return Command.CONTINUE;
    }

    @Override
    public Map<String, String> associateNfSwitch(String nf, String sw) {
        if (nfSwitch.containsKey(nf)) {
            // per semplicità per adesso i comandi che ritornano vuoto anzichè il contenuto 
            // sono usati per errore
            // non si può sovrascrivere per adesso
            return null;
        }
        nfSwitch.put(nf, sw); // TODO: trovare modo per fare validazione switch (esiste lo switch immesso?)
        return nfSwitch;
    }

    @Override
    public int defineNfChain(String[] nfChain) {
        for (String nf : nfChain) {
            if (!nfSwitch.containsKey(nf)) {
                return -1; // se la nf non è associata a nessuno switch ritorno -1 come errore
            }
        }
        nfChains.add(new NFChain(nfChain));
        return nfChains.size() - 1;
    }

    @Override
    public boolean associatePathToNFChain(String sourceIp, String destIp, int nfChainId) {
        // qui bisogna fare l'effettiva implementazione dei percorsi negli switch
        try {
            NFChain c = nfChains.get(nfChainId);
            logger.info("OK GET");
            return c.setPath(sourceIp, destIp);
        } catch (Exception e) {
            logger.info("EXCEPTION");
            logger.error(e.getMessage());
            return false;
        }
    }
}
