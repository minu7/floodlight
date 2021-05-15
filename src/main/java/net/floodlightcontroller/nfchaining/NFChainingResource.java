package net.floodlightcontroller.nfchaining;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.projectfloodlight.openflow.types.DatapathId;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.staticentry.IStaticEntryPusherService;
import net.floodlightcontroller.staticentry.web.SFPEntryMap;
import org.restlet.data.Status;

public class NFChainingResource extends ServerResource {
  protected static Logger log = LoggerFactory.getLogger(NFChainingResource.class);

  @Post("define")
  public Object define(String fmJson) {
      // we need to define nf chain
      log.info("DEFINE");
      INFChainingREST nfChainingService = (INFChainingREST)getContext().getAttributes().get(INFChainingREST.class.getCanonicalName());
      
      ObjectMapper mapper = new ObjectMapper();
      try {
            ObjectNode root = (ObjectNode)mapper.readTree(fmJson);
            JsonNode arrayNode = root.get("chain");

            String[] chain = mapper.readValue(arrayNode.traverse(), new TypeReference<String[]>(){});
            return nfChainingService.defineNfChain(chain);
        } catch (IOException e) {
            e.printStackTrace();
        }           

      return null;
  }
  
  @Put("associate")
  public Object associate(String fmJson) {
      log.info("ASSOCIATE");
      // we need to associate chain to ip path
      INFChainingREST nfChainingService = (INFChainingREST)getContext().getAttributes().get(INFChainingREST.class.getCanonicalName());
      
      ObjectMapper mapper = new ObjectMapper();
      try {
          JsonNode root = mapper.readTree(fmJson);

          String sourceIp = root.get("sourceIp").asText();
          String destIp = root.get("destIp").asText();
          int nfChainId = Integer.parseInt(root.get("nfChainId").asText());
          
          return nfChainingService.associatePathToNFChain(sourceIp, destIp, nfChainId);
        } catch (IOException e) {
            e.printStackTrace();
        }           

      return null;
  }
  
  @Delete("delete")
  public Object delete(String fmJson) {
      log.info("ASSOCIATE");
      // we need to associate chain to ip path
      INFChainingREST nfChainingService = (INFChainingREST)getContext().getAttributes().get(INFChainingREST.class.getCanonicalName());
      
      ObjectMapper mapper = new ObjectMapper();
      try {
          JsonNode root = mapper.readTree(fmJson);

          String sourceIp = root.get("sourceIp").asText();
          String destIp = root.get("destIp").asText();
          
          return nfChainingService.deletePath(sourceIp, destIp);
        } catch (IOException e) {
            e.printStackTrace();
        }           

      return null;
  }

}