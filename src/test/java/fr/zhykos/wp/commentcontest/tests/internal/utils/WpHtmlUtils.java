package fr.zhykos.wp.commentcontest.tests.internal.utils;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.thoughtworks.selenium.Selenium;

public final class WpHtmlUtils {

	/** Number of plug-in to find with a specific search **/
	private static final int INST_PLG_SEARCH = 1;
	private static final String PAGE_LOAD_TIMEOUT = "30000"; //$NON-NLS-1$

	private enum Translations {
		extensions, addExtensions
	}

	private interface IRunnableCondition {
		boolean run();
	}

	private static final Map<Locale, Map<Translations, String>> TRANSLATIONS = new ConcurrentHashMap<>();
	static {
		final Map<Translations, String> french = new ConcurrentHashMap<>();
		french.put(Translations.extensions, "Extensions"); //$NON-NLS-1$
		french.put(Translations.addExtensions, "Ajouter des extensions"); //$NON-NLS-1$
		TRANSLATIONS.put(Locale.FRENCH, french);
	}

	private WpHtmlUtils() {
		// DO NOTHING AND MUST NOT BE CALLED
	}

	private static String getTranslation(final Translations word) {
		String translation = null;
		Map<Translations, String> locTranslations = TRANSLATIONS
				.get(Locale.getDefault());
		if (locTranslations == null) {
			locTranslations = TRANSLATIONS.get(
					Locale.forLanguageTag(Locale.getDefault().getLanguage()));
		}
		if (locTranslations != null) {
			translation = locTranslations.get(word);
		}
		return translation;
	}

	public static void openExtensionsPage(final Selenium selenium,
			final WebDriver driver, final String homeURL) {
		selenium.open(homeURL + "/wp-admin/plugins.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		checkH1Tag(driver, Translations.extensions);
	}

	public static void activatePlugin(final Selenium selenium,
			final WebDriver driver, final String homeURL,
			final String pluginId) {
		openExtensionsPage(selenium, driver, homeURL);
		final String xpathExpression = String.format(
				"//form[@id='bulk-action-form']//table//tr[@data-slug='%s']/td//span[@class='activate']/a", //$NON-NLS-1$
				pluginId);
		driver.findElement(By.xpath(xpathExpression)).click();
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// TODO y a t il une facon de vérifier cette activation ?
	}

	private static void checkH1Tag(final WebDriver driver,
			final Translations expected) {
		checkH1Tag(driver, getTranslation(expected));
	}

	@SuppressWarnings("PMD.UseLocaleWithCaseConversions")
	/*
	 * @SuppressWarnings("PMD.UseLocaleWithCaseConversions") tcicognani: we
	 * usually use Locale.getDefault() in toLowerCase() parameter so it's
	 * useless to add it
	 */
	public static void checkH1Tag(final WebDriver driver,
			final String expected) {
		if (expected == null) {
			Assert.fail(
					"Cannot check H1 tag value because the expected value is unknown!"); //$NON-NLS-1$
		} else {
			/*
			 * tcicognani: useless else statement but Eclipse says expected can
			 * be null (with is impossible due to the fail assertion...)
			 */
			final String h1home = driver.findElement(By.xpath("//h1")) //$NON-NLS-1$
					.getText();
			Assert.assertEquals(expected.toLowerCase(), h1home.toLowerCase());
		}
	}

	// XXX ChromeDriver en paramètre ! Passer un driver générique
	public static void connect(final ChromeDriver driver,
			final Selenium selenium, final IWordPressInformation wpInfo)
			throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		connect(driver, selenium, wpInfo.getLogin(), wpInfo.getPassword(),
				homeURL + "/wp-login.php"); //$NON-NLS-1$
	}

	// XXX ChromeDriver en paramètre ! Passer un driver générique
	public static void connect(final ChromeDriver driver,
			final Selenium selenium, final String wpLogin,
			final String wpPassword, final String loginHref) {
		// TODO Add check h1
		selenium.open(loginHref);
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		selenium.type("id=user_login", wpLogin); //$NON-NLS-1$
		selenium.type("id=user_pass", wpPassword); //$NON-NLS-1$
		selenium.check("id=rememberme"); //$NON-NLS-1$
		selenium.click("id=wp-submit"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		final List<WebElement> loginMessages = driver
				.findElementsById("login_error"); //$NON-NLS-1$
		if (!loginMessages.isEmpty()) {
			final String message = loginMessages.get(0).getText();
			fail(String.format(
					"Cannot login to WordPress! Wrong configuration? Error: '%s'", //$NON-NLS-1$
					message));
		}
	}

	public static void installPlugins(final IWordPressPlugin[] otherPlugins,
			final Selenium selenium, final WebDriver driver,
			final String homeURL) throws UtilsException {
		selenium.open(homeURL + "/wp-admin/plugin-install.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		checkH1Tag(driver, Translations.addExtensions);
		for (final IWordPressPlugin plugin : otherPlugins) {
			installPlugin(selenium, driver, plugin);
		}
	}

	private static void installPlugin(final Selenium selenium,
			final WebDriver driver, final IWordPressPlugin plugin)
			throws UtilsException {
		final String name = plugin.getName();
		selenium.type(
				"//div[@id='wpbody-content']//div[@class='wp-filter']/form/label/input[@class='wp-filter-search']", //$NON-NLS-1$
				name);
		selenium.submit(
				"//div[@id='wpbody-content']//div[@class='wp-filter']/form"); //$NON-NLS-1$
		waitUntilVisibleState(selenium,
				"//div[@id='wpbody-content']/div[@class='wrap plugin-install-tab-featured']/span[@class='spinner']", //$NON-NLS-1$
				false, 10000);
		final String pluginId = plugin.getId();
		final List<WebElement> pluginLinkTags = driver.findElements(By.xpath(
				String.format("//div[@id='the-list']//a[@data-slug='%s']", //$NON-NLS-1$
						pluginId)));
		if (pluginLinkTags.size() == INST_PLG_SEARCH) {
			final WebElement linkTag = pluginLinkTags.get(0);
			linkTag.click();
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT); // XXX Necessary?
			final IRunnableCondition condition = new IRunnableCondition() {
				@Override
				public boolean run() {
					final String classAttr = linkTag.getAttribute("class"); //$NON-NLS-1$
					return classAttr.contains("activate-now"); //$NON-NLS-1$
				}

				@Override
				public String toString() {
					return String.format(
							"activate button after installating plugin '%s'", //$NON-NLS-1$
							name);
				}
			};
			waitUntilCondition(condition, 60000);
			/*
			 * tcicognani: then I don't activate the plugin because I don't know
			 * how it reacts (something to configure afterwards... so I let
			 * tests developers to deal with it)
			 */
		} else {
			fail(String.format(
					"Cannot find a reference to plugin '%s' and id '%s' (maybe on another page or too much references)", //$NON-NLS-1$
					name, pluginId));
		}
	}

	private static void waitUntilVisibleState(final Selenium selenium,
			final String locator, final boolean visible, final int maxTimeMilli)
			throws UtilsException {
		final IRunnableCondition condition = new IRunnableCondition() {
			@Override
			public boolean run() {
				return selenium.isVisible(locator) == visible;
			}

			@Override
			public String toString() {
				return String.format("state 'visible = %s' for locator '%s'", //$NON-NLS-1$
						Boolean.toString(visible), locator);
			}
		};
		waitUntilCondition(condition, maxTimeMilli);
	}

	private static void waitUntilCondition(final IRunnableCondition condition,
			final int maxTimeMilli) throws UtilsException {
		waitMilli(1000);
		boolean catchState = false;
		final long end = System.currentTimeMillis() + maxTimeMilli;
		while (System.currentTimeMillis() < end) {
			if (condition.run()) {
				catchState = true;
				break;
			}
			waitMilli(1000);
		}
		if (!catchState) {
			throw new UtilsException(
					String.format("Cannot get %s in max time %d milliseconds", //$NON-NLS-1$
							condition, Integer.valueOf(maxTimeMilli)));
		}
	}

	private static void waitMilli(final long time) throws UtilsException {
		try {
			Thread.sleep(time);
		} catch (final InterruptedException e) {
			throw new UtilsException(e);
		}
	}

}
