package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public final class BrowserUtils {

	private static boolean chromeInstalled = false;
	private static boolean geckoInstalled = false;

	private BrowserUtils() {
		// Do nothing and must not be called
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

	private static boolean downloadAndInstallDriver(
			final boolean alreadyInstalled, final String property,
			final String downloadURL, final String exeName)
			throws UtilsException {
		boolean installed = alreadyInstalled;
		try {
			if (!installed) {
				final File tempDir = Utils.getTempDirectory();
				final String propertyKey = String.format("%s.download.%s", //$NON-NLS-1$
						BrowserUtils.class.getName(), exeName);
				final boolean download = Utils
						.getBooleanSystemProperty(propertyKey, true);
				if (download) {
					final File driverZip = File.createTempFile("temp", //$NON-NLS-1$
							"driver.zip", //$NON-NLS-1$
							tempDir);
					Utils.downloadWebFile(downloadURL, driverZip);
					Utils.unzipFile(driverZip, tempDir);
					driverZip.deleteOnExit();
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
