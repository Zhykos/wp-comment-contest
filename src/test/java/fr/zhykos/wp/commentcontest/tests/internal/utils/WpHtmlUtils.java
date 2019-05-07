package fr.zhykos.wp.commentcontest.tests.internal.utils;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.thoughtworks.selenium.Selenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPlugin;

public final class WpHtmlUtils {

	private static final int INST_PLG_ATTEMPTS = 10;
	/** Number of plug-in to find with a specific search **/
	private static final int INST_PLG_SEARCH = 1;
	// XXX Mutualiser cette variable pour le chargement d'une page
	private static final String PAGE_LOAD_TIMEOUT = "30000"; //$NON-NLS-1$

	// XXX Mettre le système de traduction dans une autre classe
	public enum Translations {
		extensions, addExtensions, activatedPluginOk, comments, editComment,
		commentsOnArticle, articles, users, addUser, editUser, customFields
	}

	public interface IRunnableCondition {
		boolean run();
	}

	private static final Map<Locale, Map<Translations, String>> TRANSLATIONS = new ConcurrentHashMap<>();
	static {
		// XXX Ce serait bien de pouvoir accéder aux fichiers de trad WordPress
		final Map<Translations, String> french = new ConcurrentHashMap<>();
		french.put(Translations.extensions, "Extensions"); //$NON-NLS-1$
		french.put(Translations.addExtensions, "Ajouter des extensions"); //$NON-NLS-1$
		french.put(Translations.activatedPluginOk, "Extension activée."); //$NON-NLS-1$
		french.put(Translations.comments, "Commentaires"); //$NON-NLS-1$
		french.put(Translations.editComment, "Modifier le commentaire"); //$NON-NLS-1$
		french.put(Translations.commentsOnArticle, "Commentaires sur « %s »"); //$NON-NLS-1$
		french.put(Translations.articles, "Articles"); //$NON-NLS-1$
		french.put(Translations.users, "Utilisateurs"); //$NON-NLS-1$
		french.put(Translations.addUser, "Ajouter un utilisateur"); //$NON-NLS-1$
		french.put(Translations.editUser, "Modifier l’utilisateur %s"); //$NON-NLS-1$
		french.put(Translations.customFields, "Champs personnalisés"); //$NON-NLS-1$
		TRANSLATIONS.put(Locale.FRENCH, french);
	}

	private WpHtmlUtils() {
		// DO NOTHING AND MUST NOT BE CALLED
	}

	public static String getTranslation(final Translations word) {
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
		assertH1Tag(driver, Translations.extensions);
	}

	public static void activatePlugins(final Selenium selenium,
			final WebDriver driver, final String homeURL,
			final IWordPressPlugin[] plugins) throws UtilsException {
		for (final IWordPressPlugin plugin : plugins) {
			openExtensionsPage(selenium, driver, homeURL);
			final String xpathExpression = String.format(
					"//form[@id='bulk-action-form']//table//tr[@data-slug='%s']/td//span[@class='activate']/a", //$NON-NLS-1$
					plugin.getId());
			driver.findElement(By.xpath(xpathExpression)).click();
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			plugin.defaultActivationAction(driver);
		}
	}

	public static void assertDefaultPluginActivation(final WebDriver driver) {
		assertHTML(driver, "//div[@id='message']/p", //$NON-NLS-1$
				getTranslation(Translations.activatedPluginOk));
	}

	// XXX Améliorer assertH1Tag et assertH1Tag avec Object...
	public static void assertH1Tag(final WebDriver driver,
			final Translations expected) {
		assertH1Tag(driver, getTranslation(expected));
	}

	// XXX Améliorer assertH1Tag et assertH1Tag avec Object...
	public static void assertH1Tag(final WebDriver driver,
			final Translations expected, final Object... translationArgs) {
		final String translation = String.format(getTranslation(expected),
				translationArgs);
		assertH1Tag(driver, translation);
	}

	// XXX Voir si on peut mieux faire pour les tests sur les balises Hx
	public static void assertH1Tag(final WebDriver driver,
			final String expected) {
		assertHTML(driver, "//h1", expected); //$NON-NLS-1$
	}

	// XXX Voir si on peut mieux faire pour les tests sur les balises Hx
	public static void assertH2Tag(final WebDriver driver,
			final String expected) {
		assertHTML(driver, "//h2", expected); //$NON-NLS-1$
	}

	// XXX Voir si on peut mieux faire pour les tests sur les balises Hx
	public static void assertH3Tag(final WebDriver driver,
			final String expected) {
		assertHTML(driver, "//h3", expected); //$NON-NLS-1$
	}

	@SuppressWarnings("PMD.UseLocaleWithCaseConversions")
	/*
	 * @SuppressWarnings("PMD.UseLocaleWithCaseConversions") tcicognani: we
	 * usually use Locale.getDefault() in toLowerCase() parameter so it's
	 * useless to add it
	 */
	private static void assertHTML(final WebDriver driver, final String xpath,
			final String expected) {
		if (expected == null) {
			/*
			 * XXX Eviter de planter car le jour où ce code sera dans un vrai
			 * framework, les gens ne pourront pas facilement changer ce code et
			 * planter les fera chier: avoir une option pour éviter le plantage
			 * ou autre...
			 */
			Assert.fail(String.format(
					"Cannot check HTML value to XPath '%s' because the expected value is unknown!", //$NON-NLS-1$
					xpath));
		} else {
			/*
			 * tcicognani: useless 'else' statement but Eclipse says 'expected'
			 * variable can be null (with is impossible due to the fail
			 * assertion...)
			 */
			final String text = driver.findElement(By.xpath(xpath)).getText();
				/*
			 * In French we add a NBSP (char160) right after a opening guillemet
			 * and right before an ending guillemet: sometimes the browser
			 * transforms spaces into NBSP or not...
			 */
			Assert.assertEquals(expected.toLowerCase().replace((char) 160, ' '),
					text.toLowerCase().replace((char) 160, ' '));
		}
	}

	public static void connect(final WebDriver driver, final Selenium selenium,
			final IWordPressInformation wpInfo) throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		connect(driver, selenium, wpInfo.getLogin(), wpInfo.getPassword(),
				homeURL + "/wp-login.php"); //$NON-NLS-1$
	}

	public static void connect(final WebDriver driver, final Selenium selenium,
			final String wpLogin, final String wpPassword,
			final String loginHref) {
		// TODO Add check h1
		selenium.open(loginHref);
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		selenium.type("id=user_login", wpLogin); //$NON-NLS-1$
		selenium.type("id=user_pass", wpPassword); //$NON-NLS-1$
		selenium.check("id=rememberme"); //$NON-NLS-1$
		selenium.click("id=wp-submit"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		final List<WebElement> loginMessages = ((RemoteWebDriver) driver)
				.findElementsById("login_error"); //$NON-NLS-1$
		if (!loginMessages.isEmpty()) {
			final String message = loginMessages.get(0).getText();
			fail(String.format(
					"Cannot login to WordPress! Wrong configuration? Error: '%s'", //$NON-NLS-1$
					message));
		}
	}

	public static void installExternalPlugins(final IWordPressPlugin[] plugins,
			final Selenium selenium, final WebDriver driver,
			final String homeURL) throws UtilsException {
		installExternalPlugins(plugins, selenium, driver, homeURL, 1);
	}

	private static void installExternalPlugins(final IWordPressPlugin[] plugins,
			final Selenium selenium, final WebDriver driver,
			final String homeURL, final int attempt) throws UtilsException {
		if (attempt >= INST_PLG_ATTEMPTS) {
			throw new UtilsException(
					"Too much attempts to install external plugins"); //$NON-NLS-1$
		}
		if (plugins.length > 0) {
			selenium.open(homeURL + "/wp-admin/plugin-install.php"); //$NON-NLS-1$
			selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
			final String h1text = driver.findElement(By.xpath("//h1")) //$NON-NLS-1$
					.getText();
			if ("Briefly unavailable for scheduled maintenance. Check back in a minute." //$NON-NLS-1$
					.equals(h1text)) {
				waitMilli(1000);
				installExternalPlugins(plugins, selenium, driver, homeURL,
						attempt + 1);
			} else {
				assertH1Tag(driver, Translations.addExtensions);
				for (final IWordPressPlugin plugin : plugins) {
					installExternalPlugin(selenium, driver, plugin);
				}
			}
		}
	}

	private static void installExternalPlugin(final Selenium selenium,
			final WebDriver driver, final IWordPressPlugin plugin)
			throws UtilsException {
		final String name = plugin.getName();
		selenium.type(
				"//div[@id='wpbody-content']//div[@class='wp-filter']/form/label/input[@class='wp-filter-search']", //$NON-NLS-1$
				""); //$NON-NLS-1$
		driver.findElement(By.xpath(
				"//div[@id='wpbody-content']//div[@class='wp-filter']/form/label/input[@class='wp-filter-search']")) //$NON-NLS-1$
				.sendKeys(name);
		waitUntilVisibleStateByXPath(driver,
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

				// XXX toString c'est nul
				@Override
				public String toString() {
					return String.format(
							"activated class after installating plugin '%s'", //$NON-NLS-1$
							name);
				}
			};
			waitUntilCondition(condition, 60000);
		} else {
			fail(String.format(
					"Cannot find a reference to plugin '%s' and id '%s' (maybe on another page or too much references)", //$NON-NLS-1$
					name, pluginId));
		}
	}

	public static void waitUntilVisibleStateByXPath(final WebDriver driver,
			final String xpath, final boolean visible, final int maxTimeMilli)
			throws UtilsException {
		final WebElement element = driver.findElement(By.xpath(xpath));
		waitUntilVisibleStateByElement(element, visible, maxTimeMilli);
	}

	public static void waitUntilVisibleStateByElementId(final Selenium selenium,
			final WebDriver driver, final String elementId,
			final boolean visible, final int maxTimeMilli)
			throws UtilsException {
		final IRunnableCondition condition = new IRunnableCondition() {
			@Override
			public boolean run() {
				return isVisible(elementId, selenium, driver) == visible;
			}

			// XXX toString c'est nul
			@Override
			public String toString() {
				return String.format("state 'visible = %s' for locator '%s'", //$NON-NLS-1$
						Boolean.toString(visible), elementId);
			}
		};
		waitUntilCondition(condition, maxTimeMilli);
	}

	public static void waitUntilVisibleStateByElement(final WebElement element,
			final boolean visible, final int maxTimeMilli)
			throws UtilsException {
		final IRunnableCondition condition = new IRunnableCondition() {
			@Override
			public boolean run() {
				return isVisible(element) == visible;
			}

			// XXX toString c'est nul
			@Override
			public String toString() {
				return String.format("state 'visible = %s' for locator '%s'", //$NON-NLS-1$
						Boolean.toString(visible), element);
			}
		};
		waitUntilCondition(condition, maxTimeMilli);
	}

	/*
	 * XXX Methode un peu nulle car on attend 1000ms dès le début (car certains
	 * bugs apparaissent si non) / que se passe t il si le temps max est
	 * inférieur à 1000 ? / message d'exception mal formatté
	 */
	public static void waitUntilCondition(final IRunnableCondition condition,
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

	// XXX Mutualiser avec expandSettingsScreenMenu
	public static void expandAdminMenu(final WebDriver driver,
			final Selenium selenium) throws UtilsException {
		final List<WebElement> buttons = driver
				.findElements(By.xpath("//button[@id='collapse-button']")); //$NON-NLS-1$
		if (buttons.isEmpty()) {
			throw new NoSuchElementException("Cannot get button to expand"); //$NON-NLS-1$
		}
		final WebElement button = buttons.get(0);
		final boolean visible = selenium.isVisible("id=collapse-button"); //$NON-NLS-1$
		if (visible) {
			expandFrom(button);
		} else {
			throw new NoSuchElementException(
					"Cannot get button to expand (doesn't work on mobile responsive UI)"); //$NON-NLS-1$
		}
	}

	private static void expandFrom(final WebElement element)
			throws UtilsException {
		if (!isExpandedElement(element)) {
			element.click();
			final IRunnableCondition condition = new IRunnableCondition() {
				@Override
				public boolean run() {
					return isExpandedElement(element);
				}

				// XXX toString c'est nul
				@Override
				public String toString() {
					return String.format("state expanded for button '%s'", //$NON-NLS-1$
							element.toString());
				}
			};
			waitUntilCondition(condition, 10000);
		}
	}

	protected static boolean isExpandedElement(final WebElement element) {
		final String expandedState = element.getAttribute("aria-expanded"); //$NON-NLS-1$
		return Boolean.TRUE.equals(Boolean.valueOf(expandedState));
	}

	// XXX Mutualiser avec expandAdminMenu
	public static void expandSettingsScreenMenu(final WebDriver driver,
			final Selenium selenium) throws UtilsException {
		final List<WebElement> buttons = driver
				.findElements(By.xpath("//button[@id='show-settings-link']")); //$NON-NLS-1$
		if (buttons.isEmpty()) {
			throw new NoSuchElementException("Cannot get button to expand"); //$NON-NLS-1$
		}
		final WebElement button = buttons.get(0);
		final boolean visible = selenium.isVisible("id=show-settings-link"); //$NON-NLS-1$
		if (visible) {
			expandFrom(button);
		} else {
			throw new NoSuchElementException(
					"Cannot get button to expand (doesn't work on mobile responsive UI)"); //$NON-NLS-1$
		}
	}

	public static void setDisplayBlockById(final WebDriver driver,
			final String elementId) {
		final By byElt = By.id(elementId);
		setDisplayBlock(driver, byElt);
	}

	public static void setDisplayBlock(final WebDriver driver, final By byElt) {
		setDisplay(driver, byElt, "block"); //$NON-NLS-1$
	}

	public static void setDisplayNone(final WebDriver driver, final By byElt) {
		setDisplay(driver, byElt, "none"); //$NON-NLS-1$
	}

	private static void setDisplay(final WebDriver driver, final By byElt,
			final String display) {
		final WebElement element = driver.findElement(byElt);
		setDisplay(driver, element, display);
	}

	public static void setDisplay(final WebDriver driver,
			final WebElement element, final String display) {
		((JavascriptExecutor) driver).executeScript(
				String.format("arguments[0].style.display='%s';", display), //$NON-NLS-1$
				element);
	}

	public static boolean isVisible(final String elementId,
			final Selenium selenium, final WebDriver driver) {
		final WebElement element = driver.findElement(By.id(elementId));
		return selenium.isVisible("id=" + elementId) || isVisible(element); //$NON-NLS-1$
	}

	public static boolean isVisible(final WebElement element) {
		final String cssDisplay = element.getCssValue("display"); //$NON-NLS-1$
		final String cssVisibility = element.getCssValue("visibility"); //$NON-NLS-1$
		return element.isDisplayed() || (!"none".equals(cssDisplay) //$NON-NLS-1$
				&& !"hidden".equals(cssVisibility)); //$NON-NLS-1$
	}

}
