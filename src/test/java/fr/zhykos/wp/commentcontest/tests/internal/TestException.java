package fr.zhykos.wp.commentcontest.tests.internal;

public class TestException extends Exception {

	private static final long serialVersionUID = 6764851866607582751L;

	public TestException(final Throwable throwable) {
		super(throwable);
	}

	public TestException(final String message) {
		super(message);
	}

}
