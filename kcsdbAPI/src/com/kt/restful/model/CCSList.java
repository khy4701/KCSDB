package com.kt.restful.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

//
@JsonPropertyOrder(alphabetic=true) 
public class CCSList {
	
	private int TOTAL_ROW;
	public int getTOTAL_ROW() {
		return TOTAL_ROW;
	}

	public void setTOTAL_ROW(int tOTAL_ROW) {
		TOTAL_ROW = tOTAL_ROW;
	}

	List<CCSInfo> LIST = new ArrayList<CCSInfo>();
		
	 public List<CCSInfo> getCcs() {
		return LIST;
	}

	public void setCcs(List<CCSInfo> ccs) {
		this.LIST = ccs;
	}

	@JsonPropertyOrder({ "CCM_REFERID", "CCM_TIME", "CGMDN", "CCS_TIME", "CCM_STATUS","CCM_STATUS","SMS_TIME","SMS_STATUS","CCM_REASON" })
	public static class CCSInfo {
			private String CCM_REFERID;
			private String CCM_TIME;
			private String CGMDN;
			private String CCS_TIME;
			private String CCM_STATUS;
			private String SMS_TIME;
			private String SMS_STATUS;
			private String CCM_REASON;
	 
		
			@JsonProperty("CCM_REFERID")
			public String getCCM_REFERID() {
				return CCM_REFERID;
			}

			public void setCCM_REFERID(String cCM_REFERID) {
				CCM_REFERID = cCM_REFERID;
			}
			
			@JsonProperty("CCM_TIME")
			public String getCCM_TIME() {
				return CCM_TIME;
			}

			public void setCCM_TIME(String cCM_TIME) {
				CCM_TIME = cCM_TIME;
			}
			
			@JsonProperty("CGMDN")
			public String getCGMDN() {
				return CGMDN;
			}

			public void setCGMDN(String cGMDN) {
				CGMDN = cGMDN;
			}

			@JsonProperty("CCS_TIME")
			public String getCCS_TIME() {
				return CCS_TIME;
			}
			
			public void setCCS_TIME(String cCS_TIME) {
				CCS_TIME = cCS_TIME;
			}

			@JsonProperty("CCM_STATUS")
			public String getCCM_STATUS() {
				return CCM_STATUS;
			}

			@JsonProperty("SMS_TIME")
			public String getSMS_TIME() {
				return SMS_TIME;
			}

			@JsonProperty("SMS_STATUS")
			public String getSMS_STATUS() {
				return SMS_STATUS;
			}

			@JsonProperty("CCM_REASON")

			public String getCCM_REASON() {
				return CCM_REASON;
			}

			
			public void setData(String name, String value){
				
				switch(name){
				case "CCM_REFERID":
					CCM_REFERID = value;
					
				case "CCM_TIME":
					CCM_TIME = value;
					
				case "CGMDN":
					CGMDN = value;
					
				case "CCS_TIME":
					CCS_TIME = value;
					
				case "CCM_STATUS":
					CCM_STATUS = value;
					
				case "SMS_TIME":
					SMS_TIME = value;
					
				case "SMS_STATUS":
					SMS_STATUS = value;
					
				case "CCM_REASON":
					CCM_REASON = value;					
				}
			}
	}
		
	public CCSList() {
		
	}
	
	public void addCCSInfo(CCSInfo ccsInfo){
		LIST.add(ccsInfo);
	}
}
