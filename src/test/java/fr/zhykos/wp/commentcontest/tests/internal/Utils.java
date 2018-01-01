package fr.zhykos.wp.commentcontest.tests.internal;

import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.BrowserUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.IWordPressInformation;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.IRunnableCondition;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.Translations;

final class Utils {

	// XXX Mutualiser cette variable pour le chargement d'une page
	public static final String PAGE_LOAD_TIMEOUT = "30000"; //$NON-NLS-1$
	public static final int FAKE_COMMENTS_NB = 5;

	private Utils() {
		// Do nothing and must not be called
	}

	// XXX faire en sorte d'ajouter une API sur le plugin fakerpress
	public static void addFakeComments(final IWordPressInformation wpInfo)
			throws UtilsException {
		final WebDriver currentDriver = BrowserUtils
				.createAllCompatibleDriversAndGetTheBetter();
		try {
			internalAddFakeComments(currentDriver, wpInfo);
		} finally {
			currentDriver.quit();
		}
	}

	private static void internalAddFakeComments(final WebDriver currentDriver,
			final IWordPressInformation wpInfo) throws UtilsException {
		final Selenium currentSelenium = new WebDriverBackedSelenium(
				currentDriver, wpInfo.getTestServer().getHomeURL());
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		currentSelenium.open(homeURL);
		currentSelenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(currentDriver, wpInfo.getWebsiteName());
		WpHtmlUtils.connect(currentDriver, currentSelenium, wpInfo);
		// Generate fakes
		currentSelenium.open(
				homeURL + "/wp-admin/admin.php?page=fakerpress&view=comments"); //$NON-NLS-1$
		currentSelenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		currentSelenium.type("id=fakerpress-field-qty-min", //$NON-NLS-1$
				String.valueOf(FAKE_COMMENTS_NB));
		currentSelenium.uncheck("id=fakerpress-field-use_html-1"); //$NON-NLS-1$
		currentDriver.findElement(By.xpath(
				"//body//form[@class='fp-module-generator']/div[@class='fp-submit']/input")) //$NON-NLS-1$
				.click();
		final IRunnableCondition condition = new IRunnableCondition() {
			@Override
			public boolean run() {
				final List<WebElement> foundElements = currentDriver
						.findElements(By.xpath(
								"//div[@class='fp-response']//div[@class='notice is-dismissible notice-success']")); //$NON-NLS-1$
				return (!foundElements.isEmpty());
			}

			// XXX toString c'est nul
			@Override
			public String toString() {
				return "generate comments"; //$NON-NLS-1$
			}
		};
		WpHtmlUtils.waitUntilCondition(condition, 60000);
		// Modify one comment date (for tests)
		currentSelenium
				.open(homeURL + "/wp-admin/comment.php?action=editcomment&c=2"); //$NON-NLS-1$
		currentSelenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(currentDriver, Translations.editComment);
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		currentSelenium.type("id=jj", //$NON-NLS-1$
				Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
		currentSelenium.select("id=mm", //$NON-NLS-1$
				"index=" + Integer.toString(cal.get(Calendar.MONTH))); //$NON-NLS-1$
		currentSelenium.type("id=aa", //$NON-NLS-1$
				Integer.toString(cal.get(Calendar.YEAR)));
		final String articleName = currentDriver.findElement(By.xpath(
				"//div[@id='misc-publishing-actions']/div[@class='misc-pub-section misc-pub-response-to']/b/a")) //$NON-NLS-1$
				.getText();
		currentSelenium.click("id=save"); //$NON-NLS-1$
		currentSelenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(currentDriver, Translations.commentsOnArticle,
				articleName);
	}

}
