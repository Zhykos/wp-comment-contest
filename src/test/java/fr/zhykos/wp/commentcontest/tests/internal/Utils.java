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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.chrome.ChromeDriver;

final class Utils {

	private static final Logger LOGGER = Logger
			.getLogger(Utils.class.getName());
	private static final String DONE_STR = "done!"; //$NON-NLS-1$
	private static final String WEBAPP = "webapp"; //$NON-NLS-1$

	private Utils() {
		// Do nothing and must not be called
	}

	public static void installWordPress() throws TestException {
		downloadWordPress();
		unzipWordPressInstall();
	}

	private static void unzipWordPressInstall() throws TestException {
		LOGGER.info("Unzipping WordPress..."); //$NON-NLS-1$
		final File wordPressFile = getWordPressFile();
		final File webappDirectory = getWebappDirectory();
		unzipFile(wordPressFile, webappDirectory);
		LOGGER.info(DONE_STR);
	}

	private static void unzipFile(final File fileToUnzip,
			final File targetDirectory) throws TestException {
		LOGGER.info(String.format("Unzipping file '%s'...", //$NON-NLS-1$
				fileToUnzip.getAbsolutePath()));
		try (final ZipInputStream zis = new ZipInputStream(
				new FileInputStream(fileToUnzip));) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				unzipEntry(zis, zipEntry, targetDirectory);
				zipEntry = zis.getNextEntry();
			}
		} catch (final IOException e) {
			throw new TestException(e);
		}
		LOGGER.info(DONE_STR);
	}

	private static void unzipEntry(final ZipInputStream zis,
			final ZipEntry zipEntry, final File targetDirectory)
			throws IOException, FileNotFoundException {
		final byte[] buffer = new byte[1024];
		final String fileName = zipEntry.getName();
		final File newFile = new File(targetDirectory, fileName);
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

	private static void downloadWordPress() throws TestException {
		LOGGER.info("Downloading latest WordPress version..."); //$NON-NLS-1$
		final File wordpressFile = getWordPressFile();
		// https://wordpress.org/download/
		downloadWebFile("https://wordpress.org/latest.zip", wordpressFile); //$NON-NLS-1$
		LOGGER.info(DONE_STR);
	}

	private static void downloadWebFile(final String distantFile,
			final File localFile) throws TestException {
		LOGGER.info(String.format("Downloading '%s'...", distantFile)); //$NON-NLS-1$
		try (FileOutputStream fos = new FileOutputStream(localFile);) {
			final URL website = new URL(distantFile);
			try (final ReadableByteChannel rbc = Channels
					.newChannel(website.openStream());) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
		} catch (final IOException e) {
			throw new TestException(e);
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

	public static void cleanWorkspace() throws TestException {
		final File tempDirectory = getTempDirectory();
		checkDeleteDirectory(tempDirectory);
		final File webappDirectory = getWebappDirectory();
		checkDeleteDirectory(webappDirectory);
	}

	private static void checkDeleteDirectory(final File directory)
			throws TestException {
		try {
			LOGGER.info(String.format("Deleting directory '%s'...", //$NON-NLS-1$
					directory.getAbsolutePath()));
			FileUtils.deleteDirectory(directory);
			LOGGER.info(DONE_STR);
		} catch (final IOException e) {
			throw new TestException(e);
		}
		if (directory.exists()) {
			final String message = String.format(
					"Cannot delete temp directory: '%s'", //$NON-NLS-1$
					directory.getAbsolutePath());
			throw new TestException(message);
		}
	}

	public static EmbeddedServer startEmbeddedServer() throws TestException {
		/*
		 * https://devcenter.heroku.com/articles/create-a-java-web-application-
		 * using-embedded-tomcat
		 */
		try {
			LOGGER.info("Starting server..."); //$NON-NLS-1$
			final Tomcat tomcat = new Tomcat();
			final int port = getIntegerSystemProperty(
					Utils.class.getName() + ".serverport", 8080); //$NON-NLS-1$
			tomcat.setPort(port);
			tomcat.setBaseDir("."); //$NON-NLS-1$
			tomcat.addWebapp("", //$NON-NLS-1$
					getWebappDirectory().getAbsolutePath() + "/wordpress"); //$NON-NLS-1$
			tomcat.init();
			// http://localhost:8080/wordpress/index.php
			tomcat.start();
			// tomcat.getServer().await();
			LOGGER.info(DONE_STR);
			return new EmbeddedServer() {
				@Override
				public void stop() throws TestException {
					try {
						tomcat.stop();
					} catch (final LifecycleException e) {
						throw new TestException(e);
					}
				}
			};
		} catch (final Exception e) {
			throw new TestException(e);
		}
	}

	public static boolean getBooleanSystemProperty(final String propertyKey,
			final boolean defaultBool) {
		final String systemProperty = System.getProperty(propertyKey,
				Boolean.toString(defaultBool));
		return Boolean.parseBoolean(systemProperty);
	}

	public static int getIntegerSystemProperty(final String propertyKey,
			final int defaultInt) {
		final String systemProperty = System.getProperty(propertyKey,
				Integer.toString(defaultInt));
		return Integer.parseInt(systemProperty);
	}

	public static void installChromeDriver(final boolean install)
			throws TestException {
		/*
		 * https://chromedriver.storage.googleapis.com/index.html?path=2.33/
		 * TODO G�rer le num�ro de version et Linux / Mac
		 */
		final File tempDir = getTempDirectory();
		if (install) {
			final File chromeDriverZip = new File(tempDir, "chromedriver.zip"); //$NON-NLS-1$
			downloadWebFile(
					"https://chromedriver.storage.googleapis.com/2.33/chromedriver_win32.zip", //$NON-NLS-1$
					chromeDriverZip);
			unzipFile(chromeDriverZip, tempDir);
		}
		final File chromeDriverExe = new File(tempDir, "chromedriver.exe"); //$NON-NLS-1$
		System.setProperty("webdriver.chrome.driver", //$NON-NLS-1$
				chromeDriverExe.getAbsolutePath());
	}

	public static void configureWordpress(final boolean installChromeDrv)
			throws TestException {
		installChromeDriver(installChromeDrv);
		final ChromeDriver driver = new ChromeDriver();
		driver.close();
	}

}
