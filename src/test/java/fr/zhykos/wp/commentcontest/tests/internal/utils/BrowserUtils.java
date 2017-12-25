package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.File;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public final class BrowserUtils {

	private static boolean chromeInstalled = false;

	private BrowserUtils() {
		// Do nothing and must not be called
	}

	private static void downloadAndInstallChromeDriver() throws UtilsException {
		/*
		 * https://chromedriver.storage.googleapis.com/index.html?path=2.33/
		 * TODO Gérer le numéro de version et Linux / Mac
		 */
		if (!chromeInstalled) {
			final File tempDir = Utils.getTempDirectory();
			final boolean download = Utils.getBooleanSystemProperty(
					BrowserUtils.class.getName() + ".downloadchromedriver", //$NON-NLS-1$
					true);
			if (download) {
				final File chromeDriverZip = new File(tempDir,
						"chromedriver.zip"); //$NON-NLS-1$
				Utils.downloadWebFile(
						"https://chromedriver.storage.googleapis.com/2.33/chromedriver_win32.zip", //$NON-NLS-1$
						chromeDriverZip);
				Utils.unzipFile(chromeDriverZip, tempDir);
			}
			final File chromeDriverExe = new File(tempDir, "chromedriver.exe"); //$NON-NLS-1$
			System.setProperty("webdriver.chrome.driver", //$NON-NLS-1$
					chromeDriverExe.getAbsolutePath());
			chromeInstalled = true;
		}
	}

	public static WebDriver createChromeDriver() throws UtilsException {
		downloadAndInstallChromeDriver();
		return new ChromeDriver();
	}

}
