package com.infinities.ares4.rest;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.jboss.logging.Logger;

@Path("/")
public class FrontendRestService {
	
	private Logger LOG = Logger.getLogger(FrontendRestService.class);
	
	@PUT
	@Path("start")
	public void start(){
		LOG.debug("receive start command!");
	}

	@PUT
	@Path("stop")
	public void stop(){
		LOG.debug("receive stop command!");
	}
	
}
