package net.floodlightcontroller.nfchaining;
 
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.VlanVid;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.devicemanager.internal.DeviceManagerImpl;
import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.staticentry.IStaticEntryPusherService;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.util.FlowModUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NFChaining implements IOFMessageListener, IFloodlightModule, INFChainingREST {
   
    protected IFloodlightProviderService floodlightProvider;
    protected IRestApiService restApiService;
    protected IOFSwitchService switchService;
    protected ITopologyService topology;
    protected IRoutingService routing;
    protected IDeviceService deviceManager;

    protected static Logger logger = LoggerFactory.getLogger(NFChaining.class);
    
    protected ArrayList<String> switches = new ArrayList<>();
    protected ArrayList<NFChain> nfChains = new ArrayList<>();
    protected Map<String, DatapathId> nfSwitch = new HashMap<>();

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
        l.add(IOFSwitchService.class);
        l.add(ITopologyService.class); // topology forse non serve
        l.add(IRoutingService.class);
        l.add(IDeviceService.class);
        l.add(IStaticEntryPusherService.class);
        return l;
    }
 
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        topology = context.getServiceImpl(ITopologyService.class);
        routing = context.getServiceImpl(IRoutingService.class);
        deviceManager = context.getServiceImpl(IDeviceService.class);
        
        restApiService.addRestletRoutable(new NFChainingWebRoutable());
    }
 
    
    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        restApiService.addRestletRoutable(new NFChainingWebRoutable());
    }
 
    @Override
    public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
     
        // per adesso non ci serve fare niente qua
        // valutare se togliere metodo e interfaccia
        return Command.CONTINUE;
    }

    @Override
    public Map<String, DatapathId> associateNfSwitch(String nf, String sw) {
        if (nfSwitch.containsKey(nf)) {
            // per semplicità per adesso i comandi che ritornano vuoto anzichè il contenuto 
            // sono usati per errore
            // non si può sovrascrivere per adesso
            return null;
        }
        DatapathId dpid = DatapathId.of(sw);
        if (switchService.getSwitch(dpid) == null) {
            return null;
        }
        nfSwitch.put(nf, dpid);
        return nfSwitch;
    }

    @Override
    public int defineNfChain(String[] nfChain) {
        for (String nf : nfChain) {
            if (!nfSwitch.containsKey(nf)) {
                return -1; // se la nf non è associata a nessuno switch ritorno -1 come errore
            }
        }
        if (nfChain.length == 0) {
            return -1;
        }
        nfChains.add(new NFChain(nfChain));
        return nfChains.size() - 1;
    }

    private IDevice getDeviceByIp(IPv4Address ip) {
        Collection<? extends IDevice> allDevices = deviceManager.getAllDevices();

        for (IDevice d : allDevices) {
            for (int j = 0; j < d.getIPv4Addresses().length; j++) {
                if (d.getIPv4Addresses()[j].equals(ip)) {
                    return d;
                }
            }
        } 
        return null;
    }

    private void setFlowToSwitch(DatapathId nodeId, OFPort portNumberOutput, IPv4Address srcIp, IPv4Address destIp) {
        IOFSwitch sw = switchService.getActiveSwitch(nodeId);
        OFFlowAdd.Builder fmb = sw.getOFFactory().buildFlowAdd();
        fmb.setBufferId(OFBufferId.NO_BUFFER)
            .setPriority(FlowModUtils.PRIORITY_MAX);
        
        Match.Builder mb = sw.getOFFactory().buildMatch();
        mb.setExact(MatchField.ETH_TYPE, EthType.IPv4) // senza questo da errore
            .setExact(MatchField.IPV4_SRC, srcIp)
            .setExact(MatchField.IPV4_DST, destIp);
            
        
        OFActionOutput.Builder actionBuilder = sw.getOFFactory().actions().buildOutput();

        actionBuilder.setPort(portNumberOutput);

        fmb.setActions(Collections.singletonList((OFAction) actionBuilder.build()));
        fmb.setMatch(mb.build());

        sw.write(fmb.build());
    }

    // l'editor vorrebbe che spezzassi questa funzione
    @Override
    public boolean associatePathToNFChain(String sourceIp, String destIp, int nfChainId) {
        // qui bisogna fare l'effettiva implementazione dei percorsi negli switch
        try { // no check in ip for now, perchè il controller per vedere tutti gli ip la cosa più veloce è fare un pingall
            NFChain c = nfChains.get(nfChainId);
            if (!c.getSouceIp().isEmpty()) {
                // questa path è stata già configurata oppuew
                return false;
            }
            
            // IDevice source = deviceManager.findDevice(MacAddress.NONE, VlanVid.ZERO, IPv4Address.of(sourceIp), IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO);
            // IDevice dest = deviceManager.findDevice(MacAddress.NONE, VlanVid.ZERO, IPv4Address.of(destIp), IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO);
            // la funzione sopra non va, forse vuole più parametri (anche se nel codice direbbe di no, comunque anche gli altri moduli non la usano)
            
            // IMPORTANT: ricordarsi di fare un pingall (magari qui da codice per sicurezza)

            IDevice source = this.getDeviceByIp(IPv4Address.of(sourceIp));
            IDevice dest = this.getDeviceByIp(IPv4Address.of(destIp));

            if (source == null) {
                logger.info("SOURCE DEVICE NOT FOUND");
                return false;
            }

            if (dest == null) {
                logger.info("DEST DEVICE NOT FOUND");
                return false;
            }

            SwitchPort srcSwitch = null;
            SwitchPort destSwitch = null;
            Integer tmpPathLength = null;

            // scegliere dal primo host il miglior switch verso la prima nf
            DatapathId firstNf = nfSwitch.get(c.getChain()[0]);
            for (SwitchPort  s : source.getAttachmentPoints()) {
                Path p = routing.getPath(s.getNodeId(), firstNf); // vedere dove è localizzata la prima nf rispetto src
                if (tmpPathLength == null || p.getHopCount() < tmpPathLength) {
                    tmpPathLength = p.getHopCount();
                    srcSwitch = s;
                }
            }
            if (srcSwitch == null) {
                return false;
            }
            
            tmpPathLength = null;
            DatapathId lastNf = nfSwitch.get(c.getChain()[c.getChain().length - 1]); // last NF
            for (SwitchPort  s : dest.getAttachmentPoints()) {
                Path p = routing.getPath(s.getNodeId(), lastNf); // vedere dove è localizzata l'ultima nf rispetto dest
                if (tmpPathLength == null || p.getHopCount() < tmpPathLength) {
                    tmpPathLength = p.getHopCount();
                    destSwitch = s;
                }
            }
            if (destSwitch == null) {
                return false;
            }
            
            DatapathId switchBefore = srcSwitch.getNodeId();
            for (String nf : c.getChain()) {
                DatapathId sw = nfSwitch.get(nf);
                Path p = routing.getPath(switchBefore, sw);
                logger.info("path " + nf);
                List<NodePortTuple> path = p.getPath();
                for (int i = 0; i < path.size(); i+=2) {
                    // vado di 2 in 2 perchè il primo è da dove parte e il secondo è dove arriva
                    // noi dobbiamo modificare la table di partenza
                    NodePortTuple node = path.get(i);
                    logger.info(node.toString());
                    this.setFlowToSwitch(node.getNodeId(), node.getPortId(), IPv4Address.of(sourceIp), IPv4Address.of(destIp));
                }
                switchBefore = sw;
            }
            Path p = routing.getPath(switchBefore, destSwitch.getNodeId());
            logger.info("path 'finale'");
            List<NodePortTuple> path = p.getPath();
            for (int i = 0; i < path.size(); i+=2) {
                // vado di 2 in 2 perchè il primo è da dove parte e il secondo è dove arriva
                // noi dobbiamo modificare la table di partenza
                NodePortTuple node = path.get(i);
                logger.info(node.toString());
                this.setFlowToSwitch(node.getNodeId(), node.getPortId(), IPv4Address.of(sourceIp), IPv4Address.of(destIp));
            }

            c.setPath(sourceIp, destIp);
            return true;
        } catch (Exception e) {
            logger.info("EXCEPTION");
            logger.error(e.getMessage());
            return false;
        }
    }
}
