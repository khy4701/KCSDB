package com.kt.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.AllowIpProperty;
import com.kt.restful.constants.LogFlagProperty;
import com.kt.restful.constants.OverloadControlProperty;
import com.kt.restful.model.StatisticsModel;

public class OverloadManager implements CommandReceiver{
	private static Logger logger = LogManager.getLogger(OverloadManager.class);
	
	private static OverloadManager ovldManager;
	
	private List<String> allowIpList = new ArrayList<String>();
	
	private boolean overloadControlFlag = false;
	private boolean logFlag = false;
	
	private int overloadTps = 0;
	
	public static void main(String[] args) {
		OverloadConnector.getInstance();
	}
	
	private OverloadManager() {
		
		try {
			for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
				if(!ip.trim().equals("")) {
					allowIpList.add(ip);
				}
			}
			logger.info("allow_ip_list : " + AllowIpProperty.getPropPath("allow_ip_list"));
		} catch (Exception ex) {
			logger.error("AllowIpProperty Load Excetpion Occured : " + ex.getMessage());
		}
		
		try {
			
//			if(OverloadControlProperty.getPropPath("overloadControlFlag").equals("ON")) {
//				overloadControlFlag = true;
//			} else {
//				overloadControlFlag = false;
//			}
			overloadTps = Integer.parseInt(OverloadControlProperty.getPropPath("overloadControlTPS"));
			logger.info("overloadTps : " + overloadTps);
		} catch (Exception ex) {
			logger.error("OverloadControlProperty Load Excetpion Occured : " + ex.getMessage());
		}
		
		try {
			
			if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
				logFlag = true;
			} else {
				logFlag = false;
			}
			logger.info("logFlag : " + logFlag);
		} catch (Exception ex) {
			logger.error("LogFlagProperty Load Excetpion Occured : " + ex.getMessage());
		}		
	}
	
	public static OverloadManager getInstance() {
		if(ovldManager == null) {
			ovldManager = new OverloadManager();
		}
		
		return ovldManager;
	}
	
	private int clientReqID = 0;
	
	public synchronized int getClientReqID(){
		clientReqID++;
		if(clientReqID > 10000){
			clientReqID = 0;
		}
		
		return clientReqID;
	}

	public synchronized static void sendCommand(HashMap<String, StatisticsModel> statistics) {

	}
	
	public synchronized static void sendMessage(String command, String imsi, String ipAddress, String sendMsg, int jobNo) {
		OverloadConnector.getInstance().sendMessage(command, imsi, ipAddress, sendMsg, jobNo);
	}
	
	public synchronized void receiveMessage(String command, String tps, String ipAddress, String name ,int jobNo) {

		String result = "";
		switch (command.toUpperCase()) {		
		case "OVERLOAD_MODE" :
			logger.error("OVERLOAD_MODE TPS : " + Integer.parseInt(tps));
			result = "PROVS OverloadControl Flag : On";
			try {
				overloadTps = Integer.parseInt(tps);
				overloadControlFlag = true;
				OverloadControlProperty.setProperty(overloadTps);
				logger.error("overloadTps : " + overloadTps);
				
				System.out.println("[DEBUG] Result - " + result + ", overloadTps - " + overloadTps);
			} catch (Exception ex) {
				logger.error("OverloadControlProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;
						
		case "NORMAL_MODE" :
			logger.error("NORMAL_MODE : " + Integer.parseInt(tps));
			result = "PROVS OverloadControl Flag : Off";
			try {
				overloadTps = Integer.parseInt(tps);
				overloadControlFlag = false;
				OverloadControlProperty.setProperty(overloadTps);
				logger.error("overloadTps : " + overloadTps);
				
				System.out.println("[DEBUG] Result - " + result + ", overloadTps - " + overloadTps);
			} catch (Exception ex) {
				logger.error("OverloadControlProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;		
		}
	}	
	
	public synchronized List<String> getAllowIpList() {
		return allowIpList;
	}

	public void setAllowIpList(List<String> allowIpList) {
		this.allowIpList = allowIpList;
	}

	public synchronized boolean isOverloadControlFlag() {
		return overloadControlFlag;
	}

	public synchronized void setOverloadControlFlag(boolean overloadControlFlag) {
		this.overloadControlFlag = overloadControlFlag;
	}

	public synchronized int getOverloadTps() {
		return overloadTps;
	}

	public void setOverloadTps(int overloadTps) {
		this.overloadTps = overloadTps;
	}

	public synchronized boolean isLogFlag() {
		return logFlag;
	}

	public void setLogFlag(boolean logFlag) {
		this.logFlag = logFlag;
	}	
}

