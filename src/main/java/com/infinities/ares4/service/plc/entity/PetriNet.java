package com.infinities.ares4.service.plc.entity;

import java.io.Serializable;

public class PetriNet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Place valve_1_opened = new Place(false);
	private Place valve_1_closed = new Place(true);
	private Place valve_2_opened = new Place(false);
	private Place valve_2_closed = new Place(true);
	private Place water_filled = new Place(false);
	private Place fertilizer_filled = new Place(false);
	private Place ultraviolet_on = new Place(false);
	private Place ultraviolet_off = new Place(true);
	private Place water_empty = new Place(true);
	private Place fertilizer_empty = new Place(true);

	private Transition open_valve_1 = new Transition(0);
	private Transition close_valve_1 = new Transition(0);
	private Transition open_valve_2 = new Transition(0);
	private Transition close_valve_2 = new Transition(0);
	private Transition open_ultraviolet = new Transition(0);
	private Transition close_ultraviolet = new Transition(0);

	public PetriNet() {

	}

	public Place getValve_1_opened() {
		return valve_1_opened;
	}

	public void setValve_1_opened(Place valve_1_opened) {
		this.valve_1_opened = valve_1_opened;
	}

	public Place getValve_1_closed() {
		return valve_1_closed;
	}

	public void setValve_1_closed(Place valve_1_closed) {
		this.valve_1_closed = valve_1_closed;
	}

	public Place getValve_2_opened() {
		return valve_2_opened;
	}

	public void setValve_2_opened(Place valve_2_opened) {
		this.valve_2_opened = valve_2_opened;
	}

	public Place getValve_2_closed() {
		return valve_2_closed;
	}

	public void setValve_2_closed(Place valve_2_closed) {
		this.valve_2_closed = valve_2_closed;
	}

	public Place getWater_filled() {
		return water_filled;
	}

	public void setWater_filled(Place water_filled) {
		this.water_filled = water_filled;
	}

	public Place getFertilizer_filled() {
		return fertilizer_filled;
	}

	public void setFertilizer_filled(Place fertilizer_filled) {
		this.fertilizer_filled = fertilizer_filled;
	}

	public Place getUltraviolet_on() {
		return ultraviolet_on;
	}

	public void setUltraviolet_on(Place ultraviolet_on) {
		this.ultraviolet_on = ultraviolet_on;
	}

	public Place getUltraviolet_off() {
		return ultraviolet_off;
	}

	public void setUltraviolet_off(Place ultraviolet_off) {
		this.ultraviolet_off = ultraviolet_off;
	}

	public Place getWater_empty() {
		return water_empty;
	}

	public void setWater_empty(Place water_empty) {
		this.water_empty = water_empty;
	}

	public Place getFertilizer_empty() {
		return fertilizer_empty;
	}

	public void setFertilizer_empty(Place fertilizer_empty) {
		this.fertilizer_empty = fertilizer_empty;
	}

	public Transition getOpen_valve_1() {
		return open_valve_1;
	}

	public void setOpen_valve_1(Transition open_valve_1) {
		this.open_valve_1 = open_valve_1;
	}

	public Transition getClose_valve_1() {
		return close_valve_1;
	}

	public void setClose_valve_1(Transition close_valve_1) {
		this.close_valve_1 = close_valve_1;
	}

	public Transition getOpen_valve_2() {
		return open_valve_2;
	}

	public void setOpen_valve_2(Transition open_valve_2) {
		this.open_valve_2 = open_valve_2;
	}

	public Transition getClose_valve_2() {
		return close_valve_2;
	}

	public void setClose_valve_2(Transition close_valve_2) {
		this.close_valve_2 = close_valve_2;
	}

	public Transition getOpen_ultraviolet() {
		return open_ultraviolet;
	}

	public void setOpen_ultraviolet(Transition open_ultraviolet) {
		this.open_ultraviolet = open_ultraviolet;
	}

	public Transition getClose_ultraviolet() {
		return close_ultraviolet;
	}

	public void setClose_ultraviolet(Transition close_ultraviolet) {
		this.close_ultraviolet = close_ultraviolet;
	}

}
