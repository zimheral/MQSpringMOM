package com.zimheral.mqspringmom;

import java.io.Serializable;
import java.util.List;

public class MQMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String message;
	private String id;
	private List<String> params;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getParams() {
		return params;
	}
	public void setParams(List<String> params) {
		this.params = params;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
