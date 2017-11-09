package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.chrome.ChromeDriver;

import fr.zhykos.wp.commentcontest.tests.internal.utils.server.ITestServer;
import fr.zhykos.wp.commentcontest.tests.internal.utils.server.ITestServerFactory;

public final class Utils {

	private static final Logger LOGGER = Logger
			.getLogger(Utils.class.getName());
	private static final String DONE_STR = "done!"; //$NON-NLS-1$
	private static final String WEBAPP = "webapp"; //$NON-NLS-1$

	private Utils() {
		// Do nothing and must not be called
	}

	public static void installWordPress() throws UtilsException {
		downloadWordPress();
		unzipWordPressInstall();
	}

	private static void unzipWordPressInstall() throws UtilsException {
		LOGGER.info("Unzipping WordPress..."); //$NON-NLS-1$
		final File wordPressFile = getWordPressFile();
		final File webappDirectory = getWebappDirectory();
		unzipFile(wordPressFile, webappDirectory);
		LOGGER.info(DONE_STR);
	}

	private static void unzipFile(final File fileToUnzip,
			final File targetDirectory) throws UtilsException {
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
			throw new UtilsException(e);
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

	private static void downloadWordPress() throws UtilsException {
		LOGGER.info("Downloading latest WordPress version..."); //$NON-NLS-1$
		final File wordpressFile = getWordPressFile();
		// https://wordpress.org/download/
		downloadWebFile("https://wordpress.org/latest.zip", wordpressFile); //$NON-NLS-1$
		LOGGER.info(DONE_STR);
	}

	private static void downloadWebFile(final String distantFile,
			final File localFile) throws UtilsException {
		LOGGER.info(String.format("Downloading '%s'...", distantFile)); //$NON-NLS-1$
		try (FileOutputStream fos = new FileOutputStream(localFile);) {
			final URL website = new URL(distantFile);
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
			LOGGER.info(String.format("Deleting directory '%s'...", //$NON-NLS-1$
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

	public static ITestServer startEmbeddedServer() throws UtilsException {
		// http://localhost:8080/wordpress/index.php
		LOGGER.info("Starting server..."); //$NON-NLS-1$
		final int port = getIntegerSystemProperty(
				Utils.class.getName() + ".serverport", 8080); //$NON-NLS-1$
		final String webappDirectory = getWebappDirectory().getAbsolutePath()
				+ "/wordpress"; //$NON-NLS-1$
		final ITestServer server = ITestServerFactory.DEFAULT.createServer();
		server.launch(port, webappDirectory);
		LOGGER.info(DONE_STR);
		return server;
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
			throws UtilsException {
		/*
		 * https://chromedriver.storage.googleapis.com/index.html?path=2.33/
		 * TODO Gérer le numéro de version et Linux / Mac
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
			throws UtilsException {
		installChromeDriver(installChromeDrv);
		final ChromeDriver driver = new ChromeDriver();
		driver.close();
	}

	public static ICommandExecResult executeCommand(final String command)
			throws UtilsException {
		try {
			LOGGER.info(String.format("Executing command: %s", command)); //$NON-NLS-1$
			final CommandLine commandLine = CommandLine.parse(command);
			final DefaultExecutor executor = new DefaultExecutor();
			final StringWriter stdOut = new StringWriter();
			final StringWriter stdErr = new StringWriter();
			executor.setStreamHandler(new ExecuteStreamHandler() {
				@Override
				public void stop() throws IOException {
					// DO NOTHING
				}

				@Override
				public void start() throws IOException {
					// DO NOTHING
				}

				@Override
				public void setProcessOutputStream(final InputStream is)
						throws IOException {
					IOUtils.copy(is, stdOut, "UTF-8"); //$NON-NLS-1$
				}

				@Override
				public void setProcessInputStream(final OutputStream os)
						throws IOException {
					// DO NOTHING
				}

				@Override
				public void setProcessErrorStream(final InputStream is)
						throws IOException {
					IOUtils.copy(is, stdErr, "UTF-8"); //$NON-NLS-1$
				}
			});
			final ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
			executor.setWatchdog(watchdog);
			final int exitValue = executor.execute(commandLine);
			final ICommandExecResult result = createCommandResult(exitValue,
					stdOut, stdErr);
			LOGGER.info("Done!"); //$NON-NLS-1$
			return result;
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
	}

	private static ICommandExecResult createCommandResult(final int exitValue,
			final Writer output, final Writer errorOutput) {
		return new ICommandExecResult() {
			@Override
			public String getOuput() {
				return output.toString();
			}

			@Override
			public int getExitValue() {
				return exitValue;
			}

			@Override
			public String getErrorOutput() {
				return errorOutput.toString();
			}
		};
	}

}
