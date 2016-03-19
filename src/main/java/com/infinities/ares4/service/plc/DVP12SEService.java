package com.infinities.ares4.service.plc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

import com.infinities.ares4.service.plc.entity.ExtendTimerTask;
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
	private int[] invariants = new int[] { 1, 1, 1, 1, 1 };
	private Object lock = new Object();

	public DVP12SEService(String ip) {
		this.ip = ip;

	}

	public void start() {
		this.setStart(true);
		scheduler = Executors.newScheduledThreadPool(3);
		scheduler.scheduleWithFixedDelay(new Worker(), 0, 500,
				TimeUnit.MILLISECONDS);
	}

	public void stop() {
		this.setStart(false);
		scheduler.shutdownNow();

		for (int i = 0; i < 3; i++) {
			try {
				writeCoils(i, false);
			} catch (Exception e) {
				logger.error("there is a problem when stop plc", e);
			}
		}

	}

	public synchronized int[] getInvariants() {
		return this.invariants;
	}

	public synchronized void setInvariants(int[] invariants) {
		this.invariants = invariants;
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

	private class Worker implements Runnable {

		@Override
		public void run() {
			logger.debug("work begin");
			synchronized (lock) {
				if (isStart()) {
					logger.debug("work start");
					List<ExtendTimerTask> tasks = new ArrayList<ExtendTimerTask>();
					try {
						BitVector inputs = readInputDiscretes();
						if (inputs.getBit(0)) {
							petriNet.getUltraviolet_on().setHaveToken(true);
							petriNet.getUltraviolet_off().setHaveToken(false);
						} else {
							petriNet.getUltraviolet_off().setHaveToken(true);
							petriNet.getUltraviolet_on().setHaveToken(false);
						}
					} catch (Exception e) {
						logger.debug("read input failed");
						return;
					}

					updateInvariants();

					if (petriNet.getWater_empty().isHaveToken()
							&& petriNet.getValve_1_closed().isHaveToken()
							&& !petriNet.getOpen_valve_1().isTrigger()) {
						petriNet.getOpen_valve_1().setTrigger(true);
						tasks.add(triggerOpenValve1());
					}

					if (petriNet.getValve_1_opened().isHaveToken()
							&& !petriNet.getClose_valve_1().isTrigger()) {
						petriNet.getClose_valve_1().setTrigger(true);
						tasks.add(triggerCloseValve1());
					}

					if (petriNet.getFertilizer_empty().isHaveToken()
							&& petriNet.getValve_2_closed().isHaveToken()
							&& !petriNet.getOpen_valve_2().isTrigger()) {
						petriNet.getOpen_valve_2().setTrigger(true);
						tasks.add(triggerOpenValve2());
					}

					if (petriNet.getValve_2_opened().isHaveToken()
							&& !petriNet.getClose_valve_2().isTrigger()) {
						petriNet.getClose_valve_2().setTrigger(true);
						tasks.add(triggerCloseValve2());
					}

					if (petriNet.getWater_filled().isHaveToken()
							&& petriNet.getFertilizer_filled().isHaveToken()
							&& petriNet.getUltraviolet_off().isHaveToken()
							&& !petriNet.getOpen_ultraviolet().isTrigger()) {
						petriNet.getOpen_ultraviolet().setTrigger(true);
						tasks.add(triggerOpenUltraviolet());
					}

					if (petriNet.getUltraviolet_on().isHaveToken()
							&& !petriNet.getClose_ultraviolet().isTrigger()) {
						petriNet.getClose_ultraviolet().setTrigger(true);
						tasks.add(triggerCloseUltraviolet());
					}

					for (ExtendTimerTask task : tasks) {
						scheduler.schedule(task, task.getTimed(),
								TimeUnit.SECONDS);
					}
				}
				logger.debug("work end");
			}
		}
	}

	public ExtendTimerTask triggerOpenValve1() {
		return new ExtendTimerTask(0) {

			@Override
			public void run() {
				synchronized (lock) {
					try {
						writeCoils(2, true);
					} catch (Exception e) {
						logger.error("there is error when writing coil", e);
					}
					petriNet.getWater_empty().setHaveToken(false);
					petriNet.getValve_1_closed().setHaveToken(false);
					petriNet.getValve_1_opened().setHaveToken(true);
					petriNet.getOpen_valve_1().setTrigger(false);
				}
			}

		};
	}

	public ExtendTimerTask triggerCloseValve1() {
		return new ExtendTimerTask(4) {

			@Override
			public void run() {
				synchronized (lock) {
					try {
						writeCoils(2, false);
					} catch (Exception e) {
						logger.error("there is error when writing coil", e);
					}
					petriNet.getValve_1_opened().setHaveToken(false);
					petriNet.getValve_1_closed().setHaveToken(true);
					petriNet.getWater_filled().setHaveToken(true);
					petriNet.getClose_valve_1().setTrigger(false);
				}
			}

		};
	}

	public ExtendTimerTask triggerOpenValve2() {
		return new ExtendTimerTask(0) {

			@Override
			public void run() {
				synchronized (lock) {
					try {
						writeCoils(1, true);
					} catch (Exception e) {
						logger.error("there is error when writing coil", e);
					}
					petriNet.getFertilizer_empty().setHaveToken(false);
					petriNet.getValve_2_closed().setHaveToken(false);
					petriNet.getValve_2_opened().setHaveToken(true);
					petriNet.getOpen_valve_2().setTrigger(false);
				}
			}

		};
	}

	public ExtendTimerTask triggerCloseValve2() {
		return new ExtendTimerTask(2) {

			@Override
			public void run() {
				synchronized (lock) {
					try {
						writeCoils(1, false);
					} catch (Exception e) {
						logger.error("there is error when writing coil", e);
					}
					petriNet.getValve_2_opened().setHaveToken(false);
					petriNet.getValve_2_closed().setHaveToken(true);
					petriNet.getFertilizer_filled().setHaveToken(true);
					petriNet.getClose_valve_2().setTrigger(false);
				}
			}

		};
	}

	public ExtendTimerTask triggerOpenUltraviolet() {
		return new ExtendTimerTask(0) {

			@Override
			public void run() {
				synchronized (lock) {
					try {
						writeCoils(0, true);
					} catch (Exception e) {
						logger.error("there is error when writing coil", e);
					}
					petriNet.getFertilizer_filled().setHaveToken(false);
					petriNet.getWater_filled().setHaveToken(false);
					// petriNet.getUltraviolet_off().setHaveToken(false);
					// petriNet.getUltraviolet_on().setHaveToken(true);
					petriNet.getOpen_ultraviolet().setTrigger(false);
				}
			}

		};
	}

	public ExtendTimerTask triggerCloseUltraviolet() {
		return new ExtendTimerTask(10) {

			@Override
			public void run() {
				synchronized (lock) {
					try {
						writeCoils(0, false);
					} catch (Exception e) {
						logger.error("there is error when writing coil", e);
					}
					// petriNet.getUltraviolet_on().setHaveToken(false);
					// petriNet.getUltraviolet_off().setHaveToken(true);
					petriNet.getFertilizer_empty().setHaveToken(true);
					petriNet.getWater_empty().setHaveToken(true);
					petriNet.getClose_ultraviolet().setTrigger(false);
				}
			}

		};
	}

	public void updateInvariants() {
		synchronized (lock) {
			int[] invariants = new int[] { 0, 0, 0, 0, 0 };
			if (petriNet.getWater_empty().isHaveToken()) {
				invariants[0]++;
			}
			if (petriNet.getUltraviolet_on().isHaveToken()) {
				invariants[0]++;
			}
			if (petriNet.getWater_filled().isHaveToken()) {
				invariants[0]++;
			}
			if (petriNet.getValve_1_opened().isHaveToken()) {
				invariants[0]++;
			}

			if (petriNet.getUltraviolet_on().isHaveToken()) {
				invariants[1]++;
			}
			if (petriNet.getUltraviolet_off().isHaveToken()) {
				invariants[1]++;
			}

			if (petriNet.getValve_2_opened().isHaveToken()) {
				invariants[2]++;
			}
			if (petriNet.getValve_2_closed().isHaveToken()) {
				invariants[2]++;
			}

			if (petriNet.getValve_1_opened().isHaveToken()) {
				invariants[3]++;
			}
			if (petriNet.getValve_1_closed().isHaveToken()) {
				invariants[3]++;
			}

			if (petriNet.getFertilizer_empty().isHaveToken()) {
				invariants[4]++;
			}
			if (petriNet.getValve_2_opened().isHaveToken()) {
				invariants[4]++;
			}
			if (petriNet.getFertilizer_filled().isHaveToken()) {
				invariants[4]++;
			}
			if (petriNet.getUltraviolet_on().isHaveToken()) {
				invariants[4]++;
			}
			setInvariants(invariants);
		}
	}

	public static void main(String args[]) {
		DVP12SEService service = new DVP12SEService("192.168.1.5");
		service.start();

		int n;
		Scanner input = new Scanner(System.in);
		while ((n = input.nextInt()) != 0) {
			System.out.println("You entered " + n);
			System.out.println("Input an integer");
		}
		input.close();
		service.stop();
		System.out.println("Out of loop");

	}

}
