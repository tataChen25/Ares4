package com.infinities.ares4.service.plc.entity;

import java.io.Serializable;
import java.util.Calendar;

public class Transition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int workTime;
	private Calendar startTime;
	private boolean status = false;

	public Transition(int workTime) {
		this.workTime = workTime;
	}

	public int getWorkTime() {
		return workTime;
	}

	public void setWorkTime(int workTime) {
		this.workTime = workTime;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public void trigger() {

	}
}
