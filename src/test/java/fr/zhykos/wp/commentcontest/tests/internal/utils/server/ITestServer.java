package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

public interface ITestServer {

	void launch(int port, String webappDirectory) throws UtilsException;

	void stop() throws UtilsException;

}
