package net.floodlightcontroller.nfchaining;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.projectfloodlight.openflow.types.DatapathId;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.staticentry.IStaticEntryPusherService;
import net.floodlightcontroller.staticentry.web.SFPEntryMap;
import org.restlet.data.Status;

public class NFResource extends ServerResource {
  protected static Logger log = LoggerFactory.getLogger(NFResource.class);
   
  @Post("nf")
  public Object nf(String fmJson) {
      // we need to save and associate the new nf to switch
      log.info("NF");
      INFChainingREST nfChainingService = (INFChainingREST)getContext().getAttributes().get(INFChainingREST.class.getCanonicalName());
      
      ObjectMapper mapper = new ObjectMapper();
      try {
            JsonNode root = mapper.readTree(fmJson);
            
            String nf = root.get("nf").asText();
            String sw = root.get("sw").asText();
            
            // non fa un bel to string in automatico, così viene il dpid in modo capibile, direttamente no
            Map<String, DatapathId> map = nfChainingService.associateNfSwitch(nf, sw);
            Map<String, String> res = new HashMap<>();

            for (Map.Entry<String, DatapathId> m : map.entrySet()) {
              res.put(m.getKey(), m.getValue().toString());
            }
            return res;

        } catch (IOException e) {
            e.printStackTrace();
        }           

      return null;
  }
}