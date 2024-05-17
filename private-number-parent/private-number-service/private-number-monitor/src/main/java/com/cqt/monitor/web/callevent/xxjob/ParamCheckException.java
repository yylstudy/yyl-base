package com.cqt.monitor.web.callevent.xxjob;

/**
 * @author CQT
 */
public class ParamCheckException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ParamCheckException(String message) {
		super(message);
	}

	public ParamCheckException(Throwable cause) {
		super(cause);
	}

	public ParamCheckException(String message, Throwable cause) {
		super(message, cause);
	}
}
