package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

public interface ITestServerFactory {

	ITestServerFactory DEFAULT = new TestServerFactory();

	ITestServer createServer() throws UtilsException;

}
