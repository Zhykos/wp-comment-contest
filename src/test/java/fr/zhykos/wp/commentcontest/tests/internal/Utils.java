package fr.zhykos.wp.commentcontest.tests.internal;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
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
		// XXX Pourquoi �tre oblig� d'instancier un nouveau driver et un nouveau selenium ? Voir si on peut s'en passer
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
		fakeDate.add(Calendar.DATE, 2);
		modifyCommentDate(driver, selenium, homeURL, 1, fakeDate);
		modifyCommentAuthor(driver, selenium, homeURL, 1);
		modifyCommentAuthor(driver, selenium, homeURL, 2);
		modifyCommentEmail(driver, selenium, homeURL, 2);
		modifyCommentEmail(driver, selenium, homeURL, 3);
		modifyCommentIPAddress(3);
		modifyCommentIPAddress(4);
		modifyCommentText(driver, selenium, homeURL, 4,
				"coucou texte commentaire4"); //$NON-NLS-1$
		modifyCommentText(driver, selenium, homeURL, 5,
				"coucou commentaire5 texte salut"); //$NON-NLS-1$
	}

	private static void modifyCommentIPAddress(final int commentId)
			throws UtilsException {
		fr.zhykos.wp.commentcontest.tests.internal.utils.Utils.executeSQL(String
				.format("UPDATE %scomments SET comment_author_IP = '%s' WHERE wp_comments.comment_ID = %d;", //$NON-NLS-1$
						fr.zhykos.wp.commentcontest.tests.internal.utils.Utils.TABLE_PREFIX,
						getZhykosIPAddress(), Integer.valueOf(commentId)));
	}

	@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
	/*
	 * @SuppressWarnings("PMD.AvoidUsingHardCodedIP") tcicognani: it's a fake IP
	 * Address PMD!
	 */
	public static String getZhykosIPAddress() {
		return "123.234.198.0"; //$NON-NLS-1$
	}

	public static String getZhykosName() {
		return "Zhykos"; //$NON-NLS-1$
	}

	public static String getZhykosEmail() {
		return "zhykos@gmail.com"; //$NON-NLS-1$
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
			final Selenium selenium, final String homeURL,
			final int commentId) {
		openCommentPage(driver, selenium, homeURL, commentId);
		selenium.type("id=name", getZhykosName()); //$NON-NLS-1$
		saveCommentPage(driver, selenium);
	}

	private static void modifyCommentEmail(final WebDriver driver,
			final Selenium selenium, final String homeURL,
			final int commentId) {
		openCommentPage(driver, selenium, homeURL, commentId);
		selenium.type("id=email", getZhykosEmail()); //$NON-NLS-1$
		saveCommentPage(driver, selenium);
	}

	private static void modifyCommentText(final WebDriver driver,
			final Selenium selenium, final String homeURL, final int commentId,
			final String text) {
		openCommentPage(driver, selenium, homeURL, commentId);
		selenium.type("id=content", text); //$NON-NLS-1$
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

	public static void changeRole(final WebDriver driver,
			final Selenium selenium, final IWordPressInformation wpInfo,
			final Collection<String> allAliases, final Collection<String> roles)
			throws UtilsException {
		Assertions.assertTrue(allAliases.size() >= roles.size());
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + "/wp-admin/users.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.users);
		final Iterator<String> aliasIterator = allAliases.iterator();
		final Iterator<String> roleIterator = roles.iterator();
		while (aliasIterator.hasNext() && roleIterator.hasNext()) {
			final String userName = aliasIterator.next();
			final String role = roleIterator.next();
			changeRole(selenium, driver, wpInfo, userName, role);
		}
	}

	private static void changeRole(final Selenium selenium,
			final WebDriver driver, final IWordPressInformation wpInfo,
			final String userName, final String role) throws UtilsException {
		selenium.type("id=user-search-input", userName); //$NON-NLS-1$
		selenium.click("id=search-submit"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.users);
		final List<WebElement> noItemsElt = driver.findElements(
				By.xpath("//tbody[@id='the-list']/tr[@class='no-items']")); //$NON-NLS-1$
		if (noItemsElt.isEmpty()) {
			final WebElement editLink = driver.findElement(
					By.xpath("//tbody[@id='the-list']//span[@class='edit']/a")); //$NON-NLS-1$
			final String href = editLink.getAttribute("href"); //$NON-NLS-1$
			selenium.open(href);
			editUserRole(selenium, driver, userName, role);
			driver.navigate().back();
			driver.navigate().back();
		} else {
			createUser(selenium, driver, wpInfo, userName, role);
		}
	}

	private static void editUserRole(final Selenium selenium,
			final WebDriver driver, final String userName, final String role) {
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		selenium.select("id=role", String.format("value=%s", role)); //$NON-NLS-1$ //$NON-NLS-2$
		selenium.click("id=submit"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.editUser, userName);
	}

	private static void createUser(final Selenium selenium,
			final WebDriver driver, final IWordPressInformation wpInfo,
			final String userName, final String role) throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + "/wp-admin/user-new.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.addUser);
		selenium.type("id=user_login", userName); //$NON-NLS-1$
		selenium.type("id=email", //$NON-NLS-1$
				Math.abs(userName.hashCode()) + "@" //$NON-NLS-1$
						+ Math.abs(userName.hashCode()) + "zhykos.fr"); //$NON-NLS-1$
		selenium.select("id=role", String.format("value=%s", role)); //$NON-NLS-1$ //$NON-NLS-2$
		selenium.uncheck("id=send_user_notification"); //$NON-NLS-1$
		selenium.click("id=createusersub"); //$NON-NLS-1$
		selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.users);
	}

}
