package com.infinities.ares4.rest;

import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.jboss.logging.Logger;

import com.infinities.ares4.service.plc.DVP12SEService;

@Path("/rest")
public class FrontendRestService {
	
	private Logger LOG = Logger.getLogger(FrontendRestService.class);
	private DVP12SEService dVP12SEService = new DVP12SEService(getSysProp("DVP12SE.ip"));
	
	@PUT
	@Path("/start")
	public void start(){
		LOG.debug("receive start command!");
		dVP12SEService.start();
		LOG.debug("send DVP12SE start command!");
	}

	@PUT
	@Path("/stop")
	public void stop(){
		LOG.debug("receive stop command!");
		dVP12SEService.stop();
		LOG.debug("send DVP12SE stop command!");
	}

	public String getSysProp(String key) {
		Properties prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream("system.properties"));
			return prop.getProperty(key);
		}

		catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;

	}
	
}
