package fr.zhykos.wp.commentcontest.tests.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

final class Utils {

	private static final Logger LOGGER = Logger
			.getLogger(Utils.class.getName());
	private static final String DONE_STR = "done!"; //$NON-NLS-1$
	private static final String WEBAPP = "webapp"; //$NON-NLS-1$

	private Utils() {
		// Do nothing and must not be called
	}

	public static void installWordPressAndPlugin() throws UtilsException {
		downloadWordPress();
		unzipWordPressInstall();
	}

	private static void unzipWordPressInstall() throws UtilsException {
		LOGGER.info("Unzipping WordPress... "); //$NON-NLS-1$
		final File wordPressFile = getWordPressFile();
		try (final ZipInputStream zis = new ZipInputStream(
				new FileInputStream(wordPressFile));) {
			final File webappDirectory = getWebappDirectory();
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				unzipEntry(zis, zipEntry, webappDirectory);
				zipEntry = zis.getNextEntry();
			}
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		LOGGER.info(DONE_STR);
	}

	private static void unzipEntry(final ZipInputStream zis,
			final ZipEntry zipEntry, final File webappDirectory)
			throws IOException, FileNotFoundException {
		final byte[] buffer = new byte[1024];
		final String fileName = zipEntry.getName();
		final File newFile = new File(webappDirectory, fileName);
		if (zipEntry.isDirectory()) {
			newFile.mkdirs();
		} else {
			try (final FileOutputStream fos = new FileOutputStream(newFile);) {
				int len = zis.read(buffer);
				while (len > 0) {
					fos.write(buffer, 0, len);
					len = zis.read(buffer);
				}
			}
		}
	}

	private static void downloadWordPress() throws UtilsException {
		LOGGER.info("Downloading latest WordPress version... "); //$NON-NLS-1$
		final File wordpressFile = getWordPressFile();
		try (FileOutputStream fos = new FileOutputStream(wordpressFile);) {
			// https://wordpress.org/download/
			final URL website = new URL("https://wordpress.org/latest.zip"); //$NON-NLS-1$
			try (final ReadableByteChannel rbc = Channels
					.newChannel(website.openStream());) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		LOGGER.info(DONE_STR);
	}

	private static File getWordPressFile() {
		final File tempDir = getTempDirectory();
		return new File(tempDir, "wordpress.zip"); //$NON-NLS-1$
	}

	public static File getTempDirectory() {
		final File tempDir = new File("temp"); //$NON-NLS-1$
		tempDir.mkdirs();
		return tempDir;
	}

	public static File getWebappDirectory() {
		final File tempDir = new File(WEBAPP);
		tempDir.mkdirs();
		return tempDir;
	}

	public static void cleanWorkspace() throws UtilsException {
		final File tempDirectory = getTempDirectory();
		checkDeleteDirectory(tempDirectory);
		final File webappDirectory = getWebappDirectory();
		checkDeleteDirectory(webappDirectory);
	}

	private static void checkDeleteDirectory(final File directory)
			throws UtilsException {
		try {
			LOGGER.info(String.format("Deleting directory '%s'... ", //$NON-NLS-1$
					directory.getAbsolutePath()));
			FileUtils.deleteDirectory(directory);
			LOGGER.info(DONE_STR);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		if (directory.exists()) {
			final String message = String.format(
					"Cannot delete temp directory: '%s'", //$NON-NLS-1$
					directory.getAbsolutePath());
			throw new UtilsException(message);
		}
	}

	public static Server startJetty() throws UtilsException {
		/*
		 * http://javaetmoi.com/2015/06/web-app-jetty-standalone/
		 */
		try {
			LOGGER.info("Starting Jerry server... "); //$NON-NLS-1$
			final Server server = new Server(8080);
			final WebAppContext root = new WebAppContext();
			root.setContextPath("/");
			root.setDescriptor("webapp/WEB-INF/web.xml");
			// final URL webAppDir =
			// Thread.currentThread().getContextClassLoader()
			// .getResource("webapp");
			// if (webAppDir == null) {
			// throw new UtilsException(
			// "No webapp directory was found into the JAR file");
			// }
			root.setResourceBase(WEBAPP);
			root.setParentLoaderPriority(true);
			server.setHandler(root);
			server.start();
			// final Monitor monitor = new Monitor(8090, new Server[] { server
			// });
			// monitor.start();
			// server.join();
			// LOGGER.info(DONE_STR);
			return server;
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
	}

	public static String getSystemProperty(final String propertyKey)
			throws UtilsException {
		final String property = System.getProperty(propertyKey);
		if (property == null) {
			final String message = String.format("No property '%s' found!", //$NON-NLS-1$
					propertyKey);
			throw new UtilsException(message);
		}
		return property;
	}

}
