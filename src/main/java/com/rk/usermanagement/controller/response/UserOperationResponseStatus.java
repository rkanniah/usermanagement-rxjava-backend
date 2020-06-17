package com.rk.usermanagement.controller.response;

import java.io.Serializable;

import org.springframework.http.ResponseEntity;

public class UserOperationResponseStatus implements Serializable {

	private static final long serialVersionUID = 671342367740004969L;

	ResponseEntity<?> responseEntity;

	public ResponseEntity<?> getResponseEntity() {
		return responseEntity;
	}

	public void setResponseEntity(ResponseEntity<?> responseEntity) {
		this.responseEntity = responseEntity;
	}
}