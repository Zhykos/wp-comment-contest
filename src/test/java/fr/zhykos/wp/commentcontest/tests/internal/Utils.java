package fr.zhykos.wp.commentcontest.tests.internal;

import java.util.Calendar;
import java.util.GregorianCalendar;
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

	private static void internalAddFakeComments(final WebDriver driver,
			final IWordPressInformation wpInfo) throws UtilsException {
		final Selenium selenium = new WebDriverBackedSelenium(driver,
				wpInfo.getTestServer().getHomeURL());
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL);
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, wpInfo.getWebsiteName());
		WpHtmlUtils.connect(driver, selenium, wpInfo);
		// Generate fakes
		selenium.open(
				homeURL + "/wp-admin/admin.php?page=fakerpress&view=comments"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		selenium.type("id=fakerpress-field-qty-min", //$NON-NLS-1$
				String.valueOf(FAKE_COMMENTS_NB));
		selenium.uncheck("id=fakerpress-field-use_html-1"); //$NON-NLS-1$
		driver.findElement(By.xpath(
				"//body//form[@class='fp-module-generator']/div[@class='fp-submit']/input")) //$NON-NLS-1$
				.click();
		final IRunnableCondition condition = new IRunnableCondition() {
			@Override
			public boolean run() {
				final List<WebElement> foundElements = driver.findElements(By
						.xpath("//div[@class='fp-response']//div[@class='notice is-dismissible notice-success']")); //$NON-NLS-1$
				return (!foundElements.isEmpty());
			}

			// XXX toString c'est nul
			@Override
			public String toString() {
				return "generate comments"; //$NON-NLS-1$
			}
		};
		WpHtmlUtils.waitUntilCondition(condition, 60000);
		final Calendar fakeDate = getDateSecondJanuary2018Noon();
		for (int i = 1; i <= FAKE_COMMENTS_NB; i++) {
			modifyCommentDate(driver, selenium, homeURL, i, fakeDate);
		}
		fakeDate.add(Calendar.DATE, -1);
		modifyCommentDate(driver, selenium, homeURL, FAKE_COMMENTS_NB + 1,
				fakeDate);
		modifyCommentAuthor(driver, selenium, homeURL, 1, "Zhykos"); //$NON-NLS-1$
		modifyCommentAuthor(driver, selenium, homeURL, 2, "Zhykos"); //$NON-NLS-1$
	}

	public static Calendar getDateSecondJanuary2018Noon() {
		return new GregorianCalendar(2018, 0, 2, 12, 0);
	}

	private static void modifyCommentDate(final WebDriver driver,
			final Selenium selenium, final String homeURL, final int commentId,
			final Calendar fakeDate) {
		openCommentPage(driver, selenium, homeURL, commentId);
		driver.findElement(By.xpath(
				"//div[@id='misc-publishing-actions']/div[@class='misc-pub-section curtime misc-pub-curtime']/a[@class='edit-timestamp hide-if-no-js']")) //$NON-NLS-1$
				.click();
		selenium.type("id=jj", //$NON-NLS-1$
				Integer.toString(fakeDate.get(Calendar.DAY_OF_MONTH)));
		selenium.select("id=mm", //$NON-NLS-1$
				"index=" + Integer.toString(fakeDate.get(Calendar.MONTH))); //$NON-NLS-1$
		selenium.type("id=aa", //$NON-NLS-1$
				Integer.toString(fakeDate.get(Calendar.YEAR)));
		selenium.type("id=hh", //$NON-NLS-1$
				Integer.toString(fakeDate.get(Calendar.HOUR_OF_DAY)));
		selenium.type("id=mn", //$NON-NLS-1$
				Integer.toString(fakeDate.get(Calendar.MINUTE)));
		saveCommentPage(driver, selenium);
	}

	private static void modifyCommentAuthor(final WebDriver driver,
			final Selenium selenium, final String homeURL, final int commentId,
			final String newName) {
		openCommentPage(driver, selenium, homeURL, commentId);
		selenium.type("id=name", newName); //$NON-NLS-1$
		saveCommentPage(driver, selenium);
	}

	private static void saveCommentPage(final WebDriver driver,
			final Selenium selenium) {
		final String articleName = driver.findElement(By.xpath(
				"//div[@id='misc-publishing-actions']/div[@class='misc-pub-section misc-pub-response-to']/b/a")) //$NON-NLS-1$
				.getText();
		selenium.click("id=save"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.commentsOnArticle,
				articleName);
	}

	private static void openCommentPage(final WebDriver driver,
			final Selenium selenium, final String homeURL,
			final int commentId) {
		selenium.open(homeURL + "/wp-admin/comment.php?action=editcomment&c=" //$NON-NLS-1$
				+ commentId);
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.editComment);
	}

}
