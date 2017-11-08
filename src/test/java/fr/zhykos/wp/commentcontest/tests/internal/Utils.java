package fr.zhykos.wp.commentcontest.tests.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

final class Utils {

	private Utils() {
		// Do nothing and must not be called
	}

	public static void installWordPressAndPlugin() throws UtilsException {
		downloadWordPress();
		unzipWordPressInstall();
	}

	private static void unzipWordPressInstall() throws UtilsException {
		System.out.print("Unzipping WordPress... ");
		ZipInputStream zis = null;
		try {
			final File wordPressFile = getWordPressFile();
			zis = new ZipInputStream(new FileInputStream(wordPressFile));
			final byte[] buffer = new byte[1024];
			ZipEntry zipEntry = zis.getNextEntry();
			final File webappDirectory = getWebappDirectory();
			while (zipEntry != null) {
				final String fileName = zipEntry.getName();
				final File newFile = new File(webappDirectory, fileName);
				if (zipEntry.isDirectory()) {
					newFile.mkdirs();
				} else {
					final FileOutputStream fos = new FileOutputStream(newFile);
					int len = zis.read(buffer);
					while (len > 0) {
						fos.write(buffer, 0, len);
						len = zis.read(buffer);
					}
					fos.close();
				}
				zipEntry = zis.getNextEntry();
			}
		} catch (final IOException e) {
			throw new UtilsException(e);
		} finally {
			if (zis != null) {
				try {
					zis.closeEntry();
					zis.close();
				} catch (final IOException e) {
					throw new UtilsException(e);
				}
			}
		}
		System.out.println("done!");
	}

	private static void downloadWordPress() throws UtilsException {
		System.out.print("Downloading latest WordPress version... ");
		FileOutputStream fos = null;
		try {
			// https://wordpress.org/download/
			final URL website = new URL("https://wordpress.org/latest.zip");
			final ReadableByteChannel rbc = Channels
					.newChannel(website.openStream());
			final File wordpressFile = getWordPressFile();
			fos = new FileOutputStream(wordpressFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (final IOException e) {
			throw new UtilsException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (final IOException e) {
					throw new UtilsException(e);
				}
			}
		}
		System.out.println("done!");
	}

	private static File getWordPressFile() {
		final File tempDir = getTempDirectory();
		final File wordpressFile = new File(tempDir, "wordpress.zip");
		return wordpressFile;
	}

	public static File getTempDirectory() {
		final File tempDir = new File("temp");
		tempDir.mkdirs();
		return tempDir;
	}

	public static File getWebappDirectory() {
		final File tempDir = new File("webapp");
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
			System.out.print(String.format("Deleting directory '%s'... ",
					directory.getAbsolutePath()));
			FileUtils.deleteDirectory(directory);
			System.out.println("done!");
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		if (directory.exists()) {
			final String message = String.format(
					"Cannot delete temp directory: '%s'",
					directory.getAbsolutePath());
			throw new UtilsException(message);
		}
	}

	public static Monitor startJetty() throws Exception {
		System.out.print("Starting Jerry server... ");
		final Server server = new Server(8080);
		final WebAppContext root = new WebAppContext();
		root.setContextPath("/");
		root.setDescriptor("webapp/WEB-INF/web.xml");
		final URL webAppDir = Thread.currentThread().getContextClassLoader()
				.getResource("webapp");
		if (webAppDir == null) {
			throw new RuntimeException(
					"No webapp directory was found into the JAR file");
		}
		root.setResourceBase(webAppDir.toURI().toString());
		root.setParentLoaderPriority(true);
		server.setHandler(root);
		server.start();
		final Monitor monitor = new Monitor(8090, new Server[] { server });
		monitor.start();
		server.join();
		System.out.println("done!");
		return monitor;
	}

	public static String getSystemProperty(final String propertyKey)
			throws UtilsException {
		final String property = System.getProperty(propertyKey);
		if (property == null) {
			final String message = String.format("No property '%s' found!",
					propertyKey);
			throw new UtilsException(message);
		}
		return property;
	}

}
