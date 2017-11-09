package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import fr.zhykos.wp.commentcontest.tests.internal.utils.ICommandExecResult;
import fr.zhykos.wp.commentcontest.tests.internal.utils.Utils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

/*
 * Windows implementation for Apache / PHP / MySQL http://www.wampserver.com
 */
class WAMPServer implements ITestServer {

	private static final String APACHE_SERVICE = "wampapache64"; //$NON-NLS-1$
	private static final String MYSQL_SERVICE = "wampmysqld64"; //$NON-NLS-1$

	@Override
	public void launch(final int port, final String webappDirectory)
			throws UtilsException {
		checkRunningService(APACHE_SERVICE);
		checkRunningService(MYSQL_SERVICE);
	}

	private static void checkRunningService(final String serviceName)
			throws UtilsException {
		final String commandLine = String.format("cmd.exe /c sc query %s", //$NON-NLS-1$
				serviceName);
		final ICommandExecResult cmdRes = Utils.executeCommand(commandLine);
		if (!cmdRes.getOuput().contains("RUNNING")) { //$NON-NLS-1$
			throw new UtilsException(String
					.format("WAMP service '%s' is not running!", serviceName)); //$NON-NLS-1$
		}
	}

	@Override
	public void stop() throws UtilsException {
		// DO NOTHING
	}

}
