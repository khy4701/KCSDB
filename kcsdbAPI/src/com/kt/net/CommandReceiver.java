package com.kt.net;

public interface CommandReceiver {

	public void receiveMessage(String command, String imsi, String ipAddress, String name, int period);
	
}