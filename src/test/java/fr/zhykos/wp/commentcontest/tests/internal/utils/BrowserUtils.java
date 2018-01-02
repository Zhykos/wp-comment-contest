package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.safari.SafariDriver;

import fr.zhykos.wp.commentcontest.tests.internal.utils.os.IOSUtils;

// XXX on a le même pattern pour créer un navigateur : tenter de faire mieux
public final class BrowserUtils {

	private static boolean chromeInstalled = false;
	private static boolean geckoInstalled = false;
	private static boolean edgeInstalled = false;
	private static boolean ieInstalled = false;
	private static boolean operaInstalled = false;

	private static final List<Class<? extends WebDriver>> TCI_DRIVER_SORT = new ArrayList<>();
	static {
		TCI_DRIVER_SORT.add(ChromeDriver.class);
		TCI_DRIVER_SORT.add(FirefoxDriver.class);
		TCI_DRIVER_SORT.add(OperaDriver.class);
		TCI_DRIVER_SORT.add(SafariDriver.class);
		TCI_DRIVER_SORT.add(EdgeDriver.class);
		TCI_DRIVER_SORT.add(InternetExplorerDriver.class);
	}

	private BrowserUtils() {
		// Do nothing and must not be called
	}

	public static List<WebDriver> createAllDrivers() {
		final List<WebDriver> drivers = new ArrayList<>();
		final WebDriver chrome = createChromeDriver();
		drivers.add(chrome);
		final WebDriver firefox = createFirefoxDriver();
		drivers.add(firefox);
		final WebDriver edge = createEdgeDriver();
		drivers.add(edge);
		if (edge instanceof ErrorDriver) {
			/*
			 * Only create IE driver if Edge cannot be created (maybe an old
			 * computer / and it's better because IE is a pain in the ass to
			 * deal with)
			 */
			// XXX Ajouter une clé -D pour permettre aux dev de forcer les tests sur IE
			final WebDriver internetExplorer = createInternetExplorerDriver();
			drivers.add(internetExplorer);
		}
		final WebDriver opera = createOperaDriver();
		drivers.add(opera);
		final WebDriver safari = createSafariDriver();
		drivers.add(safari);
		return drivers;
	}

	public static List<WebDriver> createAllCompatibleDrivers()
			throws UtilsException {
		final List<WebDriver> result = new ArrayList<>();
		final List<WebDriver> drivers = createAllDrivers();
		final List<String> messages = new ArrayList<>();
		for (final WebDriver webDriver : drivers) {
			if (webDriver instanceof ErrorDriver) {
				messages.add(webDriver.toString());
			} else {
				result.add(webDriver);
			}
		}
		if (result.isEmpty()) {
			final String messagesStr = messages.stream()
					.collect(Collectors.joining(" / ")); //$NON-NLS-1$
			throw new UtilsException(
					"No WebDriver compatible with your system! All errors: " //$NON-NLS-1$
							+ messagesStr);
		}
		return result;
	}

	protected static List<Class<? extends WebDriver>> getDriverSort() {
		return Collections.unmodifiableList(TCI_DRIVER_SORT);
	}

	public static WebDriver createAllCompatibleDriversAndGetTheBetter()
			throws UtilsException {
		// TODO est-il possible de permettre aux dev de modifier l'ordre de préférence ? (une clé avec la liste des drivers séparés par point virgule ?)
		final List<WebDriver> drivers = createAllCompatibleDrivers();
		final List<Class<? extends WebDriver>> driverSort = getDriverSort();
		Collections.sort(drivers, new Comparator<WebDriver>() {
			@Override
			public int compare(final WebDriver driver1,
					final WebDriver driver2) {
				final int index1 = driverSort.indexOf(driver1.getClass());
				final int index2 = driverSort.indexOf(driver2.getClass());
				return index1 - index2;
			}
		});
		for (int i = 1; i < drivers.size(); i++) {
			drivers.get(i).quit();
		}
		return drivers.get(0);
	}

	private static WebDriver createChromeDriver() {
		/*
		 * https://chromedriver.storage.googleapis.com/index.html?path=2.33/
		 * TODO Gérer le numéro de version et Linux / Mac
		 */
		WebDriver result = null;
		try {
			chromeInstalled = downloadAndInstallDriver(chromeInstalled,
					"webdriver.chrome.driver", //$NON-NLS-1$
					"https://chromedriver.storage.googleapis.com/2.33/chromedriver_win32.zip", //$NON-NLS-1$
					"chromedriver.exe"); //$NON-NLS-1$
			result = new ChromeDriver();
		} catch (final Exception e) {
			if (result != null) {
				result.quit();
			}
			result = new ErrorDriver(e);
		}
		return result;
	}

	private static WebDriver createFirefoxDriver() {
		/*
		 * https://github.com/mozilla/geckodriver/releases TODO Gérer le numéro
		 * de version et Linux / Mac
		 */
		WebDriver result = null;
		try {
			geckoInstalled = downloadAndInstallDriver(geckoInstalled,
					"webdriver.gecko.driver", //$NON-NLS-1$
					"https://github.com/mozilla/geckodriver/releases/download/v0.19.1/geckodriver-v0.19.1-win64.zip", //$NON-NLS-1$
					"geckodriver.exe"); //$NON-NLS-1$
			result = new FirefoxDriver();
		} catch (final Exception e) {
			if (result != null) {
				result.quit();
			}
			result = new ErrorDriver(e);
		}
		return result;
	}

	private static WebDriver createEdgeDriver() {
		/*
		 * https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
		 * #downloads TODO Gérer le numéro de version XXX Exclusif windows !
		 */
		WebDriver result = null;
		try {
			edgeInstalled = downloadAndInstallDriver(edgeInstalled,
					"webdriver.edge.driver", //$NON-NLS-1$
					"https://download.microsoft.com/download/D/4/1/D417998A-58EE-4EFE-A7CC-39EF9E020768/MicrosoftWebDriver.exe", //$NON-NLS-1$
					"MicrosoftWebDriver.exe"); //$NON-NLS-1$
			result = new EdgeDriver();
		} catch (final Exception e) {
			if (result != null) {
				result.quit();
			}
			result = new ErrorDriver(e);
		}
		return result;
	}

	private static WebDriver createInternetExplorerDriver() {
		/*
		 * https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver
		 * TODO Gérer le numéro de version XXX Exclusif windows !
		 */
		WebDriver result = null;
		try {
			ieInstalled = downloadAndInstallDriver(ieInstalled,
					"webdriver.ie.driver", //$NON-NLS-1$
					"https://selenium-release.storage.googleapis.com/3.8/IEDriverServer_x64_3.8.0.zip", //$NON-NLS-1$
					"IEDriverServer.exe"); //$NON-NLS-1$
			result = new InternetExplorerDriver();
		} catch (final Exception e) {
			if (result != null) {
				result.quit();
			}
			result = new ErrorDriver(e);
		}
		return result;
	}

	private static WebDriver createOperaDriver() {
		/*
		 * https://github.com/operasoftware/operachromiumdriver TODO Gérer le
		 * numéro de version et l'OS
		 */
		WebDriver result = null;
		try {
			operaInstalled = downloadAndInstallDriver(operaInstalled,
					"webdriver.opera.driver", //$NON-NLS-1$
					"https://github.com/operasoftware/operachromiumdriver/releases/download/v.2.32/operadriver_win64.zip", //$NON-NLS-1$
					"operadriver_win64/operadriver.exe"); //$NON-NLS-1$
			final OperaOptions operaOptions = new OperaOptions();
			// XXX C'est quoi cette merde d'être obligé d'ajouter le chemin??
			operaOptions.setBinary(
					"C:\\Program Files\\Opera\\49.0.2725.64\\opera.exe"); //$NON-NLS-1$
			result = new OperaDriver(operaOptions);
		} catch (final Exception e) {
			if (result != null) {
				result.quit();
			}
			result = new ErrorDriver(e);
		}
		return result;
	}

	// XXX tcicognani: Never tested (I don't have any MacOS device)
	private static WebDriver createSafariDriver() {
		WebDriver result = null;
		try {
			if (IOSUtils.DEFAULT.isMacOS()) {
				result = new SafariDriver();
			} else {
				result = new ErrorDriver(
						"Safari driver is only compatible with MacOS"); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			if (result != null) {
				result.quit();
			}
			result = new ErrorDriver(e);
		}
		return result;
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
				// XXX Au lieu de mettre une clé pour savoir si on doit télécharger, ce serait mieux de mettre une clé pour spécifier le chemin du driver et s'il est spécifié et présent, on n'a pas à le télécharger
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

	public static class ErrorDriver implements WebDriver {
		private final String errorMessage;

		public ErrorDriver(final String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public ErrorDriver(final Throwable throwable) {
			this(throwable.getMessage());
		}

		@Override
		public String toString() {
			return this.errorMessage;
		}

		@Override
		public void get(final String url) {
			// Do nothing
		}

		@Override
		public String getCurrentUrl() {
			return null;
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public List<WebElement> findElements(final By byLocator) {
			return null;
		}

		@Override
		public WebElement findElement(final By byLocator) {
			return null;
		}

		@Override
		public String getPageSource() {
			return null;
		}

		@Override
		public void close() {
			// Do nothing
		}

		@Override
		public void quit() {
			// Do nothing
		}

		@Override
		public Set<String> getWindowHandles() {
			return null;
		}

		@Override
		public String getWindowHandle() {
			return null;
		}

		@Override
		public TargetLocator switchTo() {
			return null;
		}

		@Override
		public Navigation navigate() {
			return null;
		}

		@Override
		public Options manage() {
			return null;
		}
	}

}
