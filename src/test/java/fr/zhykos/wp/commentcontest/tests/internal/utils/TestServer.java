package fr.zhykos.wp.commentcontest.tests.internal.utils;

public interface TestServer {

	void launch(int port, String webappDirectory) throws UtilsException;

	void stop() throws UtilsException;

}
