package com.moviegat.dyfm.exception;

import java.io.IOException;

/**
 * 资源错误类
 * @author XX
 *
 */
public class ResourceNotFountException extends IOException {
	private static final long serialVersionUID = -6566893213144547898L;
	
	public ResourceNotFountException(String mess){
		super(mess);
	}
	
}
