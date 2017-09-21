package com.kt.restful.model;

public enum CCSDefine {
	
	CCM_REFERID("CCM_REFERID"),                      
	CCM_TIME("CCM_TIME"),
	CGMDN("CGMDN"),                    
	CCS_TIME("CCS_TIME"),      
	CCM_STATUS("CCM_STATUS"),            
	SMS_TIME("SMS_TIME"),    
	SMS_STATUS("SMS_STATUS"),      
	CCM_REASON("CCM_REASON");
	
	
	
	final private String name;

	private CCSDefine(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
