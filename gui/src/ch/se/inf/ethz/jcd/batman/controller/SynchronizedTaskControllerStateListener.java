package ch.se.inf.ethz.jcd.batman.controller;

public interface SynchronizedTaskControllerStateListener {

	void stateChanged(SynchronizedTaskControllerState oldState,
			SynchronizedTaskControllerState newState);

}
