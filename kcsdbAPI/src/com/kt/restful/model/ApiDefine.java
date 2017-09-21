package com.kt.restful.model;

public enum ApiDefine {
	QUERY("QUERY"),
	UPDATE("UPDATE"),
	API_TEST("apitest");
		
	final private String name;

	private ApiDefine(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
