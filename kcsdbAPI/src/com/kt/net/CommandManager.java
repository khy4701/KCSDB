package com.kt.net;

import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.AllowIpProperty;
import com.kt.restful.constants.LogFlagProperty;
import com.kt.restful.constants.OverloadControlProperty;
import com.kt.restful.model.StatisticsModel;

public class CommandManager implements CommandReceiver{
	private static Logger logger = LogManager.getLogger(CommandManager.class);
	
	private static CommandManager commandManager;
	
	private HashMap<String,String> allowIpList = new HashMap<String,String>();
	private List<String> traceImsiList = new ArrayList<String>();
	
	private boolean logFlag = false;
		
	public static void main(String[] args) {
		CommandConnector.getInstance();
	}
	
	private CommandManager() {
		
		try {
			for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
				if(!ip.trim().equals("")) {
					String []ipInfo = ip.split(":");
					allowIpList.put(ipInfo[0], ipInfo[1]);
				}
			}
			logger.info("allow_ip_list : " + AllowIpProperty.getPropPath("allow_ip_list"));
		} catch (Exception ex) {
			logger.error("AllowIpProperty Load Excetpion Occured : " + ex.getMessage());
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
	
	public static CommandManager getInstance() {
		if(commandManager == null) {
			commandManager = new CommandManager();
		}
		
		return commandManager;
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
	
	public synchronized static void sendMessage(String command, String imsi, String ipAddress, String name ,String sendMsg, int jobNo) {
		CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, sendMsg, jobNo);
	}
	
	public synchronized void receiveMessage(String command, String imsi, String ipAddress, String name, int jobNo) {

		String result = "";

		switch (command.toUpperCase()) {
		case "REG-MESSAGE-TRC" :
			StringBuffer regTrcSB  = new StringBuffer();
			if(traceImsiList.size() >= 40) {
				regTrcSB.append("FAIL REASON = TRACE LIST IS FULL");
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
				break;
			}
			if(traceImsiList.contains(imsi)) {
				regTrcSB.append("FAIL REASON = ALREADY EXIST IMSI");
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
				break;
			} else {
				traceImsiList.add(imsi);
				regTrcSB.append("IMSI = ");
				regTrcSB.append(imsi);
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
				break;
			}
		case "CANC-MESSAGE-TRC" :
			StringBuffer cancTrcSB  = new StringBuffer();
			if(traceImsiList.contains(imsi)) {
				traceImsiList.remove(imsi);
				cancTrcSB.append("IMSI = ");
				cancTrcSB.append(imsi);
				cancTrcSB.append(System.getProperty("line.separator"));
				cancTrcSB.append("DELETED");
				cancTrcSB.append(System.getProperty("line.separator"));
				result = cancTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			} else {
				cancTrcSB.append("FAIL REASON = NOT EXIST IMSI");
				cancTrcSB.append(System.getProperty("line.separator"));
				result = cancTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name,result, jobNo);
			}
			break;
		case "DIS-MESSAGE-TRC" :
			StringBuffer disTrcSB  = new StringBuffer();
			
			System.out.println("dis-message-trc:");
			if(traceImsiList.size() == 0) {
				disTrcSB.append("FAIL REASON = NOT EXIST TRACE INFOMATION");
				disTrcSB.append(System.getProperty("line.separator"));
			} else {
				disTrcSB.append("TRACE NUM");
				disTrcSB.append(System.getProperty("line.separator"));
				disTrcSB.append("--------------------");
				disTrcSB.append(System.getProperty("line.separator"));
				for(String imsilist : traceImsiList){
					disTrcSB.append(imsilist);
					disTrcSB.append(System.getProperty("line.separator"));
				}
				disTrcSB.append("--------------------");
				disTrcSB.append(System.getProperty("line.separator"));
				disTrcSB.append("TOTAL : ");
				disTrcSB.append(traceImsiList.size());
				disTrcSB.append(System.getProperty("line.separator"));
			}
			result = disTrcSB.toString();
			CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			break;
			
		case "CRTE-API-INFO" :	
						
			if(allowIpList.size() >= 40) {
				result = "  ALREADY EXIST MAX ALLOW IP";
				break;
			}
			
			if(allowIpList.containsKey(ipAddress)) {
				result = "  ALREADY EXIST IPADDRESS";
			} else {
				allowIpList.put(ipAddress,name);
				result = "  ADD ALLOW IP : " + ipAddress+"\n";
				result += "      NAME     : " + name;
			}

			try {
				AllowIpProperty.setProperty(allowIpList);
				allowIpList.clear();
				for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						String []ipInfo = ip.split(":");						
						allowIpList.put(ipInfo[0], ipInfo[1]);
					}
				}
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;
			
			
		case "DEL-API-INFO" :
			if(allowIpList.containsKey(ipAddress)) {
				allowIpList.remove(ipAddress);
				result = "  DELETE ALLOW IP : " + ipAddress;
			} else {
				result = "  NOT EXIST IP ADDRESS";
			}
			try {
				AllowIpProperty.setProperty(allowIpList);
				allowIpList.clear();
				for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						String []ipInfo = ip.split(":");
						allowIpList.put(ipInfo[0], ipInfo[1]);
					}
				}
				
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			
			break;
			
		case "DIS-API-INFO" :
			StringBuffer disAllowIpSB  = new StringBuffer();
			disAllowIpSB.append("  --------------------------------\n");
			//disAllowIpSB.append(System.getProperty("line.separator"));
			disAllowIpSB.append("  ID IPADDR		NAME\n");
			//disAllowIpSB.append(System.getProperty("line.separator"));
			disAllowIpSB.append("  --------------------------------\n");
			//disAllowIpSB.append(System.getProperty("line.separator"));
			int idNum = 1;
			
			String disIp, disName;
			Iterator<String> iterator = allowIpList.keySet().iterator();
		    while (iterator.hasNext()) {
		        disIp = (String) iterator.next();
		        disName  = allowIpList.get(disIp);
		        
				disAllowIpSB.append("  "+idNum+"  "+disIp+"	"+disName+"\n");
		        idNum++;
		    }
		    
			disAllowIpSB.append("  --------------------------------\n");
			//disAllowIpSB.append(System.getProperty("line.separator"));
			disAllowIpSB.append("  TOTAL : ");
			disAllowIpSB.append(allowIpList.size()+"\n");
			//disAllowIpSB.append(System.getProperty("line.separator"));
			result = disAllowIpSB.toString();
			CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			break;
			
		case "RELOAD-CONFIG-DATA": 
			logger.info(command + "  "+ imsi);
			if(imsi.equals("PROVS")) {
				if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
					logFlag = true;
				} else {
					logFlag = false;
				}
				result = "";
				
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			}
			break;
			
		case "DIS-CONFIG-DATA": 
			logger.info(command + "  "+ imsi);
			if(imsi.equals("PROVS")) {
				if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
					logFlag = true;
				} else {
					logFlag = false;
				}
				result = "LOG FLAG = " + logFlag;
				result = result.toUpperCase();
				
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, name, result, jobNo);
			}
			break;
		}
	}
		
	public synchronized HashMap<String,String> getAllowIpList() {
		return allowIpList;
	}

	public void setAllowIpList(HashMap<String,String> allowIpList) {
		this.allowIpList = allowIpList;
	}

	public synchronized List<String> getTraceImsiList() {
		return traceImsiList;
	}

	public void setTraceImsiList(List<String> traceImsiList) {
		this.traceImsiList = traceImsiList;
	}

	public synchronized boolean isLogFlag() {
		return logFlag;
	}

	public void setLogFlag(boolean logFlag) {
		this.logFlag = logFlag;
	}	
}

