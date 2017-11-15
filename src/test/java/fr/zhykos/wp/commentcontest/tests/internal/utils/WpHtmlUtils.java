package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.thoughtworks.selenium.Selenium;

public final class WpHtmlUtils {

	private static final String PAGE_LOAD_TIMEOUT = "30000"; //$NON-NLS-1$

	private enum Translations {
		extensions
	}

	private static final Map<Locale, Map<Translations, String>> TRANSLATIONS = new ConcurrentHashMap<>();
	static {
		final Map<Translations, String> french = new ConcurrentHashMap<>();
		french.put(Translations.extensions, "extensions"); //$NON-NLS-1$
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

	public static void wpHtmlOpenExtensionsPage(final Selenium selenium,
			final WebDriver driver, final String homeURL) {
		selenium.open(homeURL + "/wp-admin/plugins.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		checkH1Tag(driver, getTranslation(Translations.extensions));
	}

	public static void wpHtmlActivatePlugin(final Selenium selenium,
			final WebDriver driver, final String homeURL,
			final String pluginId) {
		wpHtmlOpenExtensionsPage(selenium, driver, homeURL);
		final String xpathExpression = String.format(
				"//form[@id='bulk-action-form']//table//tr[@data-slug='%s']/td//span[@class='activate']/a", //$NON-NLS-1$
				pluginId);
		driver.findElement(By.xpath(xpathExpression)).click();
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
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

}
