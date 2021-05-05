package net.floodlightcontroller.nfchaining;

import java.util.*;

import org.projectfloodlight.openflow.types.DatapathId;
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
   
  @Post("nf")
  public Object nf() {
      return "we need to save and associate the new nf";
  }
  
  @Put("define")
  public Object define() {
      return "we need to define nf chain";
  }
  
  @Post("associate")
  public Object associate() {
      return "we need to associate chain to ip path";
  }
}