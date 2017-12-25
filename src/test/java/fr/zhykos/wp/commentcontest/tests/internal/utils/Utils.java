package fr.zhykos.wp.commentcontest.tests.internal.utils;

import static org.junit.Assert.fail;

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.min.IMin;
import fr.zhykos.wp.commentcontest.tests.internal.utils.min.IMinFactory;
import fr.zhykos.wp.commentcontest.tests.internal.utils.server.ITestServer;
import fr.zhykos.wp.commentcontest.tests.internal.utils.server.ITestServerFactory;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPlugin;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTest;

/*
 * TODO Découper cette classe !
 * TODO Ajouter une méthode utilitaire qui, en cas de getText(), vérifie que l'élément est visible et si non plante
 */
public final class Utils {

	private interface IDatabase {
		String getAddress();

		String getLogin();

		String getPassword();

		String getBaseName();

		String getDriver(); // XXX Passer une classe spéciale pour gérer les BDD

		int getPort();

		default String print() {
			return String.format(
					"Driver: '%s' / Address: '%s' / Port: %d / Login: '%s' / Password: '%s' / Database: '%s'", //$NON-NLS-1$
					getDriver(), getAddress(), Integer.valueOf(getPort()),
					getLogin(), getPassword(), getBaseName());
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger(Utils.class.getName());
	private static final String DONE_STR = "done!"; //$NON-NLS-1$
	private static final String WEBAPP = "webapp"; //$NON-NLS-1$
	// XXX Mutualiser cette variable pour le chargement d'une page
	private static final String PAGE_LOAD_TIMEOUT = "30000"; //$NON-NLS-1$

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

	protected static void unzipFile(final File fileToUnzip,
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

	protected static void downloadWebFile(final String distantFile,
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

	// XXX pluginToTest: proposer de tester plusieurs plugins
	public static IWordPressInformation startServer(
			final IWordPressPluginToTest pluginToTest,
			final IWordPressPlugin[] otherPlugins) throws UtilsException {
		LOGGER.info("Starting server..."); //$NON-NLS-1$
		final File wpEmbedDir = new File(getWebappDirectory().getAbsolutePath(),
				"wordpress"); //$NON-NLS-1$
		final ITestServer server = ITestServerFactory.DEFAULT.createServer();
		final File wpRunDir = server.deployWordPress(wpEmbedDir);
		deployPluginToTest(wpRunDir, pluginToTest);
		server.start();
		final IWordPressInformation wpInfo = configureWordpress(server,
				pluginToTest, otherPlugins);
		LOGGER.info(DONE_STR);
		return wpInfo;
	}

	private static void deployPluginToTest(final File wpRunDir,
			final IWordPressPluginToTest pluginToTest) throws UtilsException {
		try {
			final File localPlgDir = pluginToTest.getLocalPluginDirectory();
			final File wpPluginDir = new File(
					new File(wpRunDir, "wp-content/plugins"), //$NON-NLS-1$
					pluginToTest.getId());
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

	private static IWordPressInformation configureWordpress(
			final ITestServer server, final IWordPressPluginToTest pluginToTest,
			final IWordPressPlugin[] otherPlugins) throws UtilsException {
		final WebDriver driver = BrowserUtils.createChromeDriver();
		try {
			// TODO Check H1 in each page
			cleanDatabase();
			final String homeURL = server.getHomeURL();
			driver.get(homeURL);
			final Selenium selenium = new WebDriverBackedSelenium(driver,
					homeURL);
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			// XXX select retourne une exception runtime crade s'il ne trouve
			// pas la langue : gérer le cas en sélectionnant l'anglais au cas où
			selenium.select("id=language", //$NON-NLS-1$
					String.format("value=%s", Locale.getDefault().toString())); //$NON-NLS-1$
			selenium.click("id=language-continue"); //$NON-NLS-1$
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			final String step1href = driver
					.findElement(By.xpath("//body//p[@class='step']/a")) //$NON-NLS-1$
					.getAttribute("href"); //$NON-NLS-1$
			selenium.open(step1href);
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			final IDatabase databaseInfo = getDatabaseInfo();
			selenium.type("id=dbname", databaseInfo.getBaseName()); //$NON-NLS-1$
			selenium.type("id=uname", databaseInfo.getLogin()); //$NON-NLS-1$
			selenium.type("id=pwd", databaseInfo.getPassword()); //$NON-NLS-1$
			selenium.type("id=dbhost", databaseInfo.getAddress()); //$NON-NLS-1$
			selenium.type("id=prefix", "wp_"); //$NON-NLS-1$ //$NON-NLS-2$
			driver.findElement(By.xpath("//body//form//p[@class='step']/input")) //$NON-NLS-1$
					.click();
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			final String step2conTest = driver.findElement(By.xpath("//body")) //$NON-NLS-1$
					.getAttribute("id"); //$NON-NLS-1$
			if ("error-page".equals(step2conTest)) { //$NON-NLS-1$
				fail("Cannot connect to database! " + databaseInfo.print()); //$NON-NLS-1$
			}
			final String step2href = driver
					.findElement(By.xpath("//body//p[@class='step']/a")) //$NON-NLS-1$
					.getAttribute("href"); //$NON-NLS-1$
			selenium.open(step2href);
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			final String websiteName = "Zhykos Auto Plugin Tests";
			selenium.type("id=weblog_title", websiteName); //$NON-NLS-1$
			final String wpLogin = "Zhykos";
			selenium.type("id=user_login", wpLogin); //$NON-NLS-1$
			selenium.type("id=admin_email", "zhykos@gmail.com"); //$NON-NLS-1$
			selenium.check("id=blog_public"); //$NON-NLS-1$
			final String wpPassword = selenium.getValue("id=pass1-text"); //$NON-NLS-1$
			selenium.click("id=submit"); //$NON-NLS-1$
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			final List<WebElement> installMessages = driver
					.findElements(By.xpath("//body//p[@class='message']")); //$NON-NLS-1$
			if (!installMessages.isEmpty()) {
				final String message = installMessages.get(0).getText();
				fail(String.format("Cannot install WordPress! Error: '%s'", //$NON-NLS-1$
						message));
			}
			final String loginHref = driver
					.findElement(By.xpath("//body//p[@class='step']/a")) //$NON-NLS-1$
					.getAttribute("href"); //$NON-NLS-1$
			WpHtmlUtils.connect(driver, selenium, wpLogin, wpPassword,
					loginHref);
			WpHtmlUtils.installExternalPlugins(otherPlugins, selenium, driver,
					homeURL);
			final IWordPressPlugin[] allPlugins = new IWordPressPlugin[otherPlugins.length
					+ 1];
			allPlugins[0] = pluginToTest;
			System.arraycopy(otherPlugins, 0, allPlugins, 1,
					otherPlugins.length);
			WpHtmlUtils.activatePlugins(selenium, driver, homeURL, allPlugins);
			return new IWordPressInformation() {
				@Override
				public String getPassword() {
					return wpPassword;
				}

				@Override
				public String getLogin() {
					return wpLogin;
				}

				@Override
				public ITestServer getTestServer() {
					return server;
				}

				@Override
				public String getWebsiteName() {
					return websiteName;
				}
			};
		} finally {
			driver.quit();
		}
	}

	private static void cleanDatabase() throws UtilsException {
		final IDatabase databaseInfo = getDatabaseInfo();
		try {
			Class.forName(databaseInfo.getDriver());
		} catch (final ClassNotFoundException e) {
			throw new UtilsException(e);
		}
		final String url = String.format(
				"jdbc:mysql://%s:%d/?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", //$NON-NLS-1$
				databaseInfo.getAddress(),
				Integer.valueOf(databaseInfo.getPort()));
		try (final Connection connection = DriverManager.getConnection(url,
				databaseInfo.getLogin(), databaseInfo.getPassword());
				final Statement statement = connection.createStatement();) {
			final String dropDatabase = String.format(
					"DROP DATABASE IF EXISTS %s", //$NON-NLS-1$
					databaseInfo.getBaseName());
			LOGGER.info(dropDatabase);
			statement.executeUpdate(dropDatabase);
			final String createDatabase = String.format("CREATE DATABASE %s", //$NON-NLS-1$
					databaseInfo.getBaseName());
			LOGGER.info(createDatabase);
			statement.executeUpdate(createDatabase);
		} catch (final SQLException e) {
			throw new UtilsException(e);
		}
	}

	private static IDatabase getDatabaseInfo() {
		return new IDatabase() {
			@Override
			public String getPassword() {
				return System.getProperty(Utils.class.getName() + ".dbpassword", //$NON-NLS-1$
						""); //$NON-NLS-1$
			}

			@Override
			public String getLogin() {
				return System.getProperty(Utils.class.getName() + ".dblogin", //$NON-NLS-1$
						"root"); //$NON-NLS-1$
			}

			@Override
			public String getAddress() {
				return System.getProperty(Utils.class.getName() + ".dbaddress", //$NON-NLS-1$
						"localhost"); //$NON-NLS-1$
			}

			@Override
			public String getBaseName() {
				return System.getProperty(Utils.class.getName() + ".wpdatabase", //$NON-NLS-1$
						"wptestzf"); //$NON-NLS-1$
			}

			@Override
			public String getDriver() {
				return System.getProperty(Utils.class.getName() + ".dbdriver", //$NON-NLS-1$
						"com.mysql.cj.jdbc.Driver"); //$NON-NLS-1$
			}

			@Override
			public int getPort() {
				return getIntegerSystemProperty(
						Utils.class.getName() + ".dbport", 80); //$NON-NLS-1$
			}
		};
	}

	// XXX pluginToTest: proposer de tester plusieurs plugins
	public static void packagePlugin(final IWordPressPluginToTest pluginToTest)
			throws UtilsException {
		LOGGER.info("Packaging plugin..."); //$NON-NLS-1$
		checkDeleteDirectory(getPackageDir());
		try {
			copyAndModifyCode(pluginToTest);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		LOGGER.info(DONE_STR);
	}

	private static File getPackageDir() {
		return new File("package"); //$NON-NLS-1$
	}

	private static void copyAndModifyCode(
			final IWordPressPluginToTest pluginToTest)
			throws IOException, UtilsException {
		final UtilsException[] internEx = new UtilsException[1];
		final File localPluginDir = pluginToTest.getLocalPluginDirectory();
		final Path pathAbsolute = Paths.get(localPluginDir.getAbsolutePath());
		final List<File> filesToRemove = Arrays
				.asList(pluginToTest.packagingFilesToRemove());
		try (final Stream<Path> paths = Files.walk(pathAbsolute)) {
			paths.filter(Files::isRegularFile).forEach(new Consumer<Path>() {
				@Override
				public void accept(final Path path) {
					try {
						final File file = path.toFile();
						if (!modifyFileReferencingMiniIfNecessary(file,
								pluginToTest)
								&& !minimifiedFileIfNecessary(file,
										pluginToTest)
								&& !filesToRemove.contains(file)) {
							FileUtils.copyFile(file,
									getPackageFileFromPluginPath(path,
											pluginToTest));
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

	protected static File getPackageFileFromPluginPath(final Path path,
			final IWordPressPluginToTest pluginToTest) {
		final File packageDir = getPackageDir();
		final File localPluginDir = pluginToTest.getLocalPluginDirectory();
		final Path pathAbsolute = Paths.get(localPluginDir.getAbsolutePath());
		final File relativeFileTgt = pathAbsolute.relativize(path).toFile();
		return new File(packageDir, relativeFileTgt.getPath());
	}

	protected static boolean minimifiedFileIfNecessary(final File file,
			final IWordPressPluginToTest pluginToTest) throws UtilsException {
		boolean result = false;
		final String fileStr = file.toString().replace('\\', '/');
		for (final String pattern : pluginToTest.packagingJavascriptToMini()) {
			if (fileStr.endsWith(pattern.replace('\\', '/'))) {
				result = true;
				break;
			}
		}
		if (!result) {
			for (final String pattern : pluginToTest.packagingCSSToMini()) {
				if (fileStr.endsWith(pattern.replace('\\', '/'))) {
					result = true;
					break;
				}
			}
		}
		if (result) {
			minimifiedFile(file, pluginToTest);
		}
		return result;
	}

	private static void minimifiedFile(final File file,
			final IWordPressPluginToTest pluginToTest) throws UtilsException {
		final File tempFile = getPackageFileFromPluginPath(file.toPath(),
				pluginToTest);
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
			final File file, final IWordPressPluginToTest pluginToTest)
			throws UtilsException, IOException {
		boolean result = false;
		for (final String pattern : pluginToTest.packagingJavascriptToMini()) {
			result = GrepUtils.grep(file, pattern);
			if (result) {
				break;
			}
		}
		if (!result) {
			for (final String pattern : pluginToTest.packagingCSSToMini()) {
				result = GrepUtils.grep(file, pattern);
				if (result) {
					break;
				}
			}
		}
		if (result) {
			modifyFileWithMiniReferences(file, pluginToTest);
		}
		return result;
	}

	private static void modifyFileWithMiniReferences(final File file,
			final IWordPressPluginToTest pluginToTest)
			throws IOException, UtilsException {
		final File targetFile = getPackageFileFromPluginPath(file.toPath(),
				pluginToTest);
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
						file, pluginToTest);
				bufferedWriter.write(lineToWrite);
				bufferedWriter.newLine();
				line = bufferedReader.readLine();
				lineNumber++;
			}
		}
	}

	private static String modifyLineWithMini(final String line,
			final int lineNumber, final File file,
			final IWordPressPluginToTest pluginToTest) {
		String newLine = line;
		for (final String javascript : pluginToTest
				.packagingJavascriptToMini()) {
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
		for (final String css : pluginToTest.packagingCSSToMini()) {
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
