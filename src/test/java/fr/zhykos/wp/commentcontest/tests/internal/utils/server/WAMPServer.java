package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import fr.zhykos.wp.commentcontest.tests.internal.utils.os.IOSUtils;

/*
 * Windows implementation for Apache / PHP / MySQL: http://www.wampserver.com
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class WAMPServer implements ITestServer {

	private static final String APACHE_SERVICE = "wampapache64"; //$NON-NLS-1$
	private static final String MYSQL_SERVICE = "wampmysqld64"; //$NON-NLS-1$

	@Override
	public void launch(final int port, final String webappDirectory)
			throws UtilsException {
		if (!IOSUtils.DEFAULT.isWindows()) {
			throw new UtilsException("WAMP server is only for Windows!"); //$NON-NLS-1$
		}
		IOSUtils.DEFAULT.startService(APACHE_SERVICE);
		IOSUtils.DEFAULT.startService(MYSQL_SERVICE);
	}

	@Override
	public void stop() throws UtilsException {
		// DO NOTHING
	}

}
