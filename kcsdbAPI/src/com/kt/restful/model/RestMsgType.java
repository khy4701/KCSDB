package com.kt.restful.model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RestMsgType {
	
private static Logger logger = LogManager.getLogger(RestMsgType.class);
	
	private String ccmRefId;
	private String ccmTime;
	private String cgmdn;
	private String ccsTime;
	private String ccmStatus;
	private String smsTime;
	private String smsStatus;
	private String ccmReason;
	private String deleteFlag;
	private String data;
		
	public RestMsgType() {
	}
	
	public RestMsgType(String UpdateDelete) {
	}

			
	public String getCcmRefId() {
		return ccmRefId;
	}

	public void setCcmRefId(String ccmRefId) {
		this.ccmRefId = ccmRefId;
	}

	public String getCcmTime() {
		return ccmTime;
	}

	public void setCcmTime(String ccmTime) {
		this.ccmTime = ccmTime;
	}

	public String getCgmdn() {
		return cgmdn;
	}

	public void setCgmdn(String cgmdn) {
		this.cgmdn = cgmdn;
	}

	public String getCcsTime() {
		return ccsTime;
	}

	public void setCcsTime(String ccsTime) {
		this.ccsTime = ccsTime;
	}

	public String getCcmStatus() {
		return ccmStatus;
	}

	public void setCcmStatus(String ccmStatus) {
		this.ccmStatus = ccmStatus;
	}

	public String getSmsTime() {
		return smsTime;
	}

	public void setSmsTime(String smsTime) {
		this.smsTime = smsTime;
	}

	public String getCcmReason() {
		return ccmReason;
	}

	public void setCcmReason(String ccmReason) {
		this.ccmReason = ccmReason;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public String getSmsStatus() {
		return smsStatus;
	}

	public void setSmsStatus(String smsStatus) {
		this.smsStatus = smsStatus;
	}
	
	public String getDeleteFlag() {
		return deleteFlag;
	}
	
	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}


}
