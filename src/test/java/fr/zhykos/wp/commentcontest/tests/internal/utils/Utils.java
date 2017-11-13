package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.chrome.ChromeDriver;

import fr.zhykos.wp.commentcontest.tests.internal.utils.min.IMin;
import fr.zhykos.wp.commentcontest.tests.internal.utils.min.IMinFactory;
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

	public static void checkDeleteDirectory(final File directory)
			throws UtilsException {
		try {
			LOGGER.info(String.format("Deleting directory '%s'...", //$NON-NLS-1$
					directory.getAbsolutePath()));
			if (directory.exists()) {
				FileUtils.deleteDirectory(directory);
			}
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

	public static ITestServer startServer(final boolean doInstChrmDrv)
			throws UtilsException {
		// http://localhost:8080/wordpress/index.php
		LOGGER.info("Starting server..."); //$NON-NLS-1$
		final int port = getIntegerSystemProperty(
				Utils.class.getName() + ".serverport", 8080); //$NON-NLS-1$
		final File wpEmbedDir = new File(getWebappDirectory().getAbsolutePath(),
				"wordpress"); //$NON-NLS-1$
		final ITestServer server = ITestServerFactory.DEFAULT.createServer();
		final File wpRunDir = server.deployWordPress(wpEmbedDir);
		deployPlugin(wpRunDir);
		server.launch(port, wpRunDir.getAbsolutePath());
		configureWordpress(doInstChrmDrv);
		LOGGER.info(DONE_STR);
		return server;
	}

	private static void deployPlugin(final File wpRunDir)
			throws UtilsException {
		try {
			final File localPlgDir = getLocalPluginDir();
			final File wpPluginDir = new File(wpRunDir,
					"wp-content/plugins/pluginToTest"); //$NON-NLS-1$
			if (!wpPluginDir.mkdir()) {
				throw new UtilsException(
						String.format("Cannot create directory '%s'", //$NON-NLS-1$
								wpPluginDir.getAbsolutePath()));
			}
			FileUtils.copyDirectory(localPlgDir, wpPluginDir);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
	}

	public static File getLocalPluginDir() {
		final String localPlgDirStr = System.getProperty(
				Utils.class.getName() + ".plugindir", "src/main/wordpress"); //$NON-NLS-1$ //$NON-NLS-2$
		return new File(localPlgDirStr);
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

	private static void configureWordpress(final boolean installChromeDrv)
			throws UtilsException {
		installChromeDriver(installChromeDrv);
		final ChromeDriver driver = new ChromeDriver();
		driver.close();
	}

	public static void packagePlugin(final String[] cssToMini,
			final String[] jsToMini, final String[] filesStrToRemove)
			throws UtilsException {
		LOGGER.info("Packaging plugin..."); //$NON-NLS-1$
		checkDeleteDirectory(getPackageDir());
		try {
			final List<File> filesToRemove = new ArrayList<>();
			for (final String string : filesStrToRemove) {
				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				/*
				 * @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				 * ticognani: it's ok here to instanciate objects in a loop!
				 */
				final File file = new File(
						getLocalPluginDir().getAbsoluteFile(), string);
				filesToRemove.add(file);
			}
			copyAndModifyCode(cssToMini, jsToMini, filesToRemove);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		// try {
		// FileUtils.copyDirectory(localPluginDir, packageDir);
		// minimifiedAndChangeCode(packageDir, cssToMini);
		// } catch (final IOException e) {
		// throw new UtilsException(e);
		// }
		LOGGER.info(DONE_STR);
	}

	private static File getPackageDir() {
		return new File("package"); //$NON-NLS-1$
	}

	private static void copyAndModifyCode(final String[] cssToMini,
			final String[] jsToMini, final List<File> filesToRemove)
			throws IOException, UtilsException {
		final UtilsException[] internEx = new UtilsException[1];
		final File localPluginDir = getLocalPluginDir();
		final Path pathAbsolute = Paths.get(localPluginDir.getAbsolutePath());
		try (final Stream<Path> paths = Files.walk(pathAbsolute)) {
			paths.filter(Files::isRegularFile).forEach(new Consumer<Path>() {
				@Override
				public void accept(final Path path) {
					try {
						final File file = path.toFile();
						if (!modifyFileReferencingMiniIfNecessary(file,
								cssToMini, jsToMini)
								&& !minimifiedFile(file, cssToMini, jsToMini)
								&& !filesToRemove.contains(file)) {
							FileUtils.copyFile(file,
									getPackageFileFromPluginPath(path));
						}
					} catch (final IOException e) {
						internEx[0] = new UtilsException(e);
					} catch (final UtilsException e) {
						internEx[0] = e;
					}
				}
			});
		}
		if (internEx[0] != null) {
			throw internEx[0];
		}
	}

	protected static File getPackageFileFromPluginPath(final Path path) {
		final File packageDir = getPackageDir();
		final File localPluginDir = getLocalPluginDir();
		final Path pathAbsolute = Paths.get(localPluginDir.getAbsolutePath());
		final File relativeFileTgt = pathAbsolute.relativize(path).toFile();
		return new File(packageDir, relativeFileTgt.getPath());
	}

	protected static boolean minimifiedFile(final File file,
			final String[] cssToMini, final String[] jsToMini)
			throws UtilsException {
		boolean result = false;
		final String fileStr = file.toString().replace('\\', '/');
		for (final String pattern : jsToMini) {
			if (fileStr.endsWith(pattern.replace('\\', '/'))) {
				result = true;
				break;
			}
		}
		if (!result) {
			for (final String pattern : cssToMini) {
				if (fileStr.endsWith(pattern.replace('\\', '/'))) {
					result = true;
					break;
				}
			}
		}
		if (result) {
			minimifiedFile(file);
		}
		return result;
	}

	private static void minimifiedFile(final File file) throws UtilsException {
		final File tempFile = getPackageFileFromPluginPath(file.toPath());
		final File parentFile = tempFile.getParentFile();
		if (!parentFile.exists() && !parentFile.mkdirs()) {
			throw new UtilsException(String.format(
					"Cannot create directory '%s'", tempFile.getParent())); //$NON-NLS-1$
		}
		final File targetFile = new File(parentFile,
				addMinToFileName(tempFile.getName()));
		final IMin minProcessor = IMinFactory.DEFAULT.createMinProcessor(file);
		minProcessor.process(targetFile);
	}

	private static String addMinToFileName(final String name) {
		final int lastIndex = name.lastIndexOf('.');
		return name.substring(0, lastIndex) + ".min" //$NON-NLS-1$
				+ name.substring(lastIndex);
	}

	protected static boolean modifyFileReferencingMiniIfNecessary(
			final File file, final String[] cssToMini, final String[] jsToMini)
			throws UtilsException, IOException {
		boolean result = false;
		for (final String pattern : jsToMini) {
			result = GrepUtils.grep(file, pattern);
			if (result) {
				break;
			}
		}
		if (!result) {
			for (final String pattern : cssToMini) {
				result = GrepUtils.grep(file, pattern);
				if (result) {
					break;
				}
			}
		}
		if (result) {
			modifyFileWithMiniReferences(file, cssToMini, jsToMini);
		}
		return result;
	}

	private static void modifyFileWithMiniReferences(final File file,
			final String[] cssToMini, final String[] jsToMini)
			throws IOException, UtilsException {
		final File targetFile = getPackageFileFromPluginPath(file.toPath());
		if (!targetFile.getParentFile().mkdirs()) {
			throw new UtilsException(String.format(
					"Cannot create directory '%s'", targetFile.getParent())); //$NON-NLS-1$
		}
		try (final FileReader fileReader = new FileReader(file);
				final BufferedReader bufferedReader = new BufferedReader(
						fileReader);
				final FileWriter fileWriter = new FileWriter(targetFile);
				final BufferedWriter bufferedWriter = new BufferedWriter(
						fileWriter);) {
			String line = bufferedReader.readLine();
			int lineNumber = 1;
			while (line != null) {
				final String lineToWrite = modifyLineWithMini(line, lineNumber,
						file, cssToMini, jsToMini);
				bufferedWriter.write(lineToWrite);
				bufferedWriter.newLine();
				line = bufferedReader.readLine();
				lineNumber++;
			}
		}
	}

	private static String modifyLineWithMini(final String line,
			final int lineNumber, final File file, final String[] cssToMini,
			final String[] jsToMini) {
		String newLine = line;
		for (final String javascript : jsToMini) {
			if (newLine
					.matches(String.format(".*wp_register_script\\s?\\(.*%s.*", //$NON-NLS-1$
							javascript))
					|| newLine.matches(String.format(
							".*wp_enqueue_script\\s?\\(.*%s.*", javascript))) { //$NON-NLS-1$
				newLine = newLine.replace(javascript,
						javascript.replace(".js", ".min.js")); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.info(String.format(
						"Line number %d from file '%s' has changed: min javascript '%s'", //$NON-NLS-1$
						Integer.valueOf(lineNumber), file.getAbsolutePath(),
						javascript));
			}
		}
		for (final String css : cssToMini) {
			/*
			 * No test on 'wp_register_style' because is not a correct function:
			 * https://codex.wordpress.org/Function_Reference/wp_register_style
			 */
			if (newLine.matches(String.format(".*wp_enqueue_style\\s?\\(.*%s.*", //$NON-NLS-1$
					css))) {
				newLine = newLine.replace(css, css.replace(".css", ".min.css")); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.info(String.format(
						"Line number %d from file '%s' has changed: min CSS '%s'", //$NON-NLS-1$
						Integer.valueOf(lineNumber), file.getAbsolutePath(),
						css));
			}
		}
		return newLine;
	}

}
