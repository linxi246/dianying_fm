package com.moviegat.dyfm.exception;

import java.io.IOException;

/**
 * 重复请求次数超过最大
 * @author Administrator
 *
 */
public class RetryOutMaxException extends IOException {


	private static final long serialVersionUID = 3921447129011498971L;
	
	public RetryOutMaxException(String message) {
		super(message);
	}
}
