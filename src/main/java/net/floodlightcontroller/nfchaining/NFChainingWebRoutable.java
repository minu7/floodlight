package net.floodlightcontroller.nfchaining;

import org.restlet.Context;
import org.restlet.Restlet;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.routing.Router;

public class NFChainingWebRoutable implements RestletRoutable {
  /**
   * Create the Restlet router and bind to the proper resources.
   */
  @Override
  public Restlet getRestlet(Context context) {
      Router router = new Router(context);
      router.attach("/nf", NFChainingResource.class);
      router.attach("/define", NFChainingResource.class);
      router.attach("/associate", NFChainingResource.class);
      return router;
  }

  /**
   * Set the base path for the Topology
   */
  @Override
  public String basePath() {
      return "/wm/nfchain";
  }

}