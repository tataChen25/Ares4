package com.infinities.ares4.service.plc;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.util.BitVector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infinities.ares4.service.plc.entity.PetriNet;

public class DVP12SEService {

	private static final Logger logger = LoggerFactory
			.getLogger(DVP12SEService.class);
	private final int INPUT_START = 1024;
	private final int OUTPUT_START = 1280;
	private final String ip;
	private final PetriNet petriNet = new PetriNet();
	private boolean start = false;
	private ScheduledExecutorService scheduler;

	public DVP12SEService(String ip) {
		this.ip = ip;

	}

	public void start() {
		this.setStart(true);
		scheduler = Executors.newScheduledThreadPool(1);
	}

	public void stop() {
		this.setStart(false);
	}

	private BitVector readInputDiscretes() throws Exception {
		TCPMasterConnection con = null;
		ModbusTCPTransaction trans = null;
		ReadInputDiscretesRequest req = null;
		ReadInputDiscretesResponse res = null;

		int ref = INPUT_START;
		int count = 8;

		try {
			// 2. Open the connection
			con = getConnection(ip);

			logger.debug("Connected to {}:{}", new Object[] {
					con.getAddress().getHostAddress(), con.getPort() });

			// 3. Prepare the request
			req = new ReadInputDiscretesRequest(ref, count);
			req.setUnitID(0);
			logger.debug("Request: {}", req.getHexMessage());

			// 4. Prepare the transaction
			trans = new ModbusTCPTransaction(con);
			trans.setRequest(req);
			trans.setReconnecting(false);
			// 5. Execute the transaction
			trans.execute();

			res = (ReadInputDiscretesResponse) trans.getResponse();
			logger.debug("Response: {}", res.getHexMessage());
			return res.getDiscretes();
		} finally {
			// 6. Close the connection
			if (con != null) {
				con.close();
			}
		}
	}

	private void writeCoils(int ref, boolean status) throws Exception {
		TCPMasterConnection con = null;
		ModbusRequest req = null;
		ModbusTransaction trans = null;
		ref = OUTPUT_START + ref;

		try {
			// 2. Open the connection
			con = getConnection(ip);
			logger.debug("Connected to {}:{}", new Object[] {
					con.getAddress().getHostAddress(), con.getPort() });

			// 3. Prepare the request
			req = new WriteCoilRequest(ref, status);
			req.setUnitID(0);
			logger.debug("Request: {}", req.getHexMessage());

			// 4. Prepare the transaction
			trans = new ModbusTCPTransaction(con);
			trans.setRequest(req);

			// 5. Execute the transaction repeat times
			trans.execute();
			logger.debug("Response: {}", trans.getResponse().getHexMessage());
		} finally {
			// 6. Close the connection
			if (con != null) {
				con.close();
			}
		}
	}

	private TCPMasterConnection getConnection(String ip) throws Exception {
		// 1. Set Address
		TCPMasterConnection con = null;
		InetAddress addr = InetAddress.getByName(ip);
		int port = Modbus.DEFAULT_PORT;

		// 2. Open the connection
		con = new TCPMasterConnection(addr);
		con.setPort(port);
		con.connect();
		return con;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public PetriNet getPetriNet() {
		return petriNet;
	}

	private class Worker implements Callable<BitVector> {

		@Override
		public BitVector call() throws Exception {
			if (isStart()) {
				if (petriNet.getWater_empty().isHaveToken()
						&& petriNet.getFertilizer_empty().isHaveToken()
						&& petriNet.getValve_1_closed().isHaveToken()) {
					petriNet.getValve_1_opened().setHaveToken(true);
					petriNet.getWater_empty().setHaveToken(false);
				}
			}
			return null;
		}
	}

}
