package com.infinities.ares4.service.plc.entity;

import java.util.TimerTask;

public abstract class ExtendTimerTask extends TimerTask {

	private long timed;

	public ExtendTimerTask(long timed) {
		this.timed = timed;
	}

	public long getTimed() {
		return timed;
	}

	public void setTimed(long timed) {
		this.timed = timed;
	}

}
