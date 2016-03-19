package com.infinities.ares4.service.plc.entity;

import java.io.Serializable;

public class Place implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean haveToken;

	public Place(boolean initialMarking) {
		super();
		this.haveToken = initialMarking;
	}

	public boolean isHaveToken() {
		return haveToken;
	}

	public void setHaveToken(boolean haveToken) {
		this.haveToken = haveToken;
	}

}
