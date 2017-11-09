package fr.zhykos.wp.commentcontest.tests.internal.utils;

public class UtilsException extends Exception {

	private static final long serialVersionUID = -1692931636898369523L;

	public UtilsException(final Throwable throwable) {
		super(throwable);
	}

	public UtilsException(final String message) {
		super(message);
	}

}
