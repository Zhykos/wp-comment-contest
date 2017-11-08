package fr.zhykos.wp.commentcontest.tests.internal;

public class UtilsException extends Exception {

	private static final long serialVersionUID = 4044127237742659292L;

	public UtilsException(final Throwable throwable) {
		super(throwable);
	}

	public UtilsException(final String message) {
		super(message);
	}

}
