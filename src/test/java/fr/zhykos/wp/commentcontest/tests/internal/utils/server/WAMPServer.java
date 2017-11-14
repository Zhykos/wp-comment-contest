package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import fr.zhykos.wp.commentcontest.tests.internal.utils.Utils;
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
class WAMPServer extends AbstractServer {

	private static final Logger LOGGER = Logger
			.getLogger(WAMPServer.class.getName());
	private static final String APACHE_SERVICE = "wampapache64"; //$NON-NLS-1$
	private static final String MYSQL_SERVICE = "wampmysqld64"; //$NON-NLS-1$

	@Override
	public void start() throws UtilsException {
		if (!IOSUtils.DEFAULT.isWindows()) {
			throw new UtilsException("WAMP server is only for Windows!"); //$NON-NLS-1$
		}
		IOSUtils.DEFAULT.startService(APACHE_SERVICE);
		IOSUtils.DEFAULT.startService(MYSQL_SERVICE);
	}

	@Override
	public void stop() throws UtilsException {
		IOSUtils.DEFAULT.stopService(MYSQL_SERVICE);
		IOSUtils.DEFAULT.stopService(APACHE_SERVICE);
	}

	@Override
	protected File specificDeployWordPress(final File wpEmbedDir)
			throws UtilsException {
		// Copy WordPress into WAMP www directory
		final String propertyKey = getClass().getName() + ".wptestdir"; //$NON-NLS-1$
		final String wpTestDirStr = System.getProperty(propertyKey);
		if (wpTestDirStr == null || wpTestDirStr.isEmpty()) {
			throw new UtilsException(String
					.format("VM argument '%s' must be set!", propertyKey)); //$NON-NLS-1$
		}
		final File wpTestDir = new File(wpTestDirStr);
		if (!wpTestDir.getParentFile().exists()) {
			throw new UtilsException(
					String.format("WAMP www directory '%s' does not exist!", //$NON-NLS-1$
							wpTestDir.getParent()));
		}
		Utils.checkDeleteDirectory(wpTestDir);
		if (!wpTestDir.mkdir()) {
			throw new UtilsException(
					String.format("Cannot create directory '%s'", //$NON-NLS-1$
							wpTestDir.getAbsolutePath()));
		}
		LOGGER.info(String.format("Deploying WordPress for WAMP in '%s'...", //$NON-NLS-1$
				wpTestDir.getAbsolutePath()));
		try {
			FileUtils.copyDirectory(wpEmbedDir, wpTestDir);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		LOGGER.info("Done!"); //$NON-NLS-1$
		return wpTestDir;
	}

	@Override
	public String getHomeURL() throws UtilsException {
		final String testUrlBase = System.getProperty(
				getClass().getName() + ".testurlbase", //$NON-NLS-1$
				getWordpressEmbeddedDir().getName());
		return String.format("%s%s", super.getHomeURL(), testUrlBase); //$NON-NLS-1$
	}

}
