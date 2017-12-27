package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;

// XXX on a le même pattern pour créer un navigateur : tenter de faire mieux
public final class BrowserUtils {

	private static boolean chromeInstalled = false;
	private static boolean geckoInstalled = false;
	private static boolean edgeInstalled = false;
	private static boolean ieInstalled = false;
	private static boolean operaInstalled = false;

	private BrowserUtils() {
		// Do nothing and must not be called
	}

	/*
	 * XXX Ajouter tous les autres navigateurs mais n'ajouter que ceux
	 * disponibles et faire un assume (ou un truc dans le genre, je ne sais pas
	 * si je veux faire planter le test si le navigateur n'est pas installé) sur
	 * les navigateurs non installés
	 */
	public static List<WebDriver> createAllDriversForTests()
			throws UtilsException {
		final List<WebDriver> drivers = new ArrayList<>();
		final WebDriver chrome = createChromeDriver();
		drivers.add(chrome);
		final WebDriver firefox = createFirefoxDriver();
		drivers.add(firefox);
		final WebDriver edge = createEdgeDriver();
		drivers.add(edge);
		final WebDriver internetExplorer = createInternetExplorerDriver();
		drivers.add(internetExplorer);
		final WebDriver opera = createOperaDriver();
		drivers.add(opera);
		return drivers;
	}

	public static WebDriver createChromeDriver() throws UtilsException {
		/*
		 * https://chromedriver.storage.googleapis.com/index.html?path=2.33/
		 * TODO Gérer le numéro de version et Linux / Mac
		 */
		chromeInstalled = downloadAndInstallDriver(chromeInstalled,
				"webdriver.chrome.driver", //$NON-NLS-1$
				"https://chromedriver.storage.googleapis.com/2.33/chromedriver_win32.zip", //$NON-NLS-1$
				"chromedriver.exe"); //$NON-NLS-1$
		return new ChromeDriver();
	}

	public static WebDriver createFirefoxDriver() throws UtilsException {
		/*
		 * https://github.com/mozilla/geckodriver/releases TODO Gérer le numéro
		 * de version et Linux / Mac
		 */
		geckoInstalled = downloadAndInstallDriver(geckoInstalled,
				"webdriver.gecko.driver", //$NON-NLS-1$
				"https://github.com/mozilla/geckodriver/releases/download/v0.19.1/geckodriver-v0.19.1-win64.zip", //$NON-NLS-1$
				"geckodriver.exe"); //$NON-NLS-1$
		return new FirefoxDriver();
	}

	public static WebDriver createEdgeDriver() throws UtilsException {
		/*
		 * https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
		 * #downloads TODO Gérer le numéro de version XXX Exclusif windows !
		 */
		edgeInstalled = downloadAndInstallDriver(edgeInstalled,
				"webdriver.edge.driver", //$NON-NLS-1$
				"https://download.microsoft.com/download/D/4/1/D417998A-58EE-4EFE-A7CC-39EF9E020768/MicrosoftWebDriver.exe", //$NON-NLS-1$
				"MicrosoftWebDriver.exe"); //$NON-NLS-1$
		return new EdgeDriver();
	}

	public static WebDriver createInternetExplorerDriver()
			throws UtilsException {
		/*
		 * https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver
		 * TODO Gérer le numéro de version XXX Exclusif windows !
		 */
		ieInstalled = downloadAndInstallDriver(ieInstalled,
				"webdriver.ie.driver", //$NON-NLS-1$
				"https://selenium-release.storage.googleapis.com/3.8/IEDriverServer_x64_3.8.0.zip", //$NON-NLS-1$
				"IEDriverServer.exe"); //$NON-NLS-1$
		return new InternetExplorerDriver();
	}

	public static WebDriver createOperaDriver() throws UtilsException {
		/*
		 * https://github.com/operasoftware/operachromiumdriver TODO Gérer le
		 * numéro de version et l'OS
		 */
		operaInstalled = downloadAndInstallDriver(operaInstalled,
				"webdriver.opera.driver", //$NON-NLS-1$
				"https://github.com/operasoftware/operachromiumdriver/releases/download/v.2.32/operadriver_win64.zip", //$NON-NLS-1$
				"operadriver_win64/operadriver.exe"); //$NON-NLS-1$
		// XXX Adapter ce pattern pour tous les autres navigateurs
		OperaDriver driver = null;
		try {
			final OperaOptions operaOptions = new OperaOptions();
			// XXX C'est quoi cette merde d'être obligé d'ajouter le chemin??
			operaOptions.setBinary(
					"C:\\Program Files\\Opera\\49.0.2725.64\\opera.exe"); //$NON-NLS-1$
			driver = new OperaDriver(operaOptions);
			return driver;
		} catch (final Exception e) {
			if (driver != null) {
				driver.quit();
			}
			throw new UtilsException(e);
		}
	}

	private static boolean downloadAndInstallDriver(
			final boolean alreadyInstalled, final String property,
			final String downloadURL, final String exeName)
			throws UtilsException {
		boolean installed = alreadyInstalled;
		try {
			if (!installed) {
				final File tempDir = Utils.getTempDirectory();
				// XXX Faire mieux pour keySuffix car par exemple on "operadriver_win64" dans le nom de la clé
				final String keySuffix = exeName.replace("/", ".").replace("\\", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"."); //$NON-NLS-1$
				final String propertyKey = String.format("%s.download.%s", //$NON-NLS-1$
						BrowserUtils.class.getName(), keySuffix);
				final boolean download = Utils
						.getBooleanSystemProperty(propertyKey, true);
				if (download) {
					if (downloadURL.endsWith(".zip")) { //$NON-NLS-1$
						final File driverZip = File.createTempFile("temp", //$NON-NLS-1$
								"driver.zip", tempDir); //$NON-NLS-1$
						Utils.downloadWebFile(downloadURL, driverZip);
						Utils.unzipFile(driverZip, tempDir);
						driverZip.deleteOnExit();
					} else {
						final File target = new File(tempDir, exeName);
						Utils.downloadWebFile(downloadURL, target);
					}
				}
				final File driverExe = new File(tempDir, exeName);
				System.setProperty(property, driverExe.getAbsolutePath());
				installed = true;
			}
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
		return installed;
	}

}
