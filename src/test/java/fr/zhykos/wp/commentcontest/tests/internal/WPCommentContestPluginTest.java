package fr.zhykos.wp.commentcontest.tests.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.BrowserUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.BrowserUtils.ErrorDriver;
import fr.zhykos.wp.commentcontest.tests.internal.utils.IWordPressInformation;
import fr.zhykos.wp.commentcontest.tests.internal.utils.Utils.IDatabase;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.IRunnableCondition;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.Translations;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPlugin;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginCatalog;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTest;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTestFactory;

// TODO Il faudrait pouvoir lancer les tests sur toutes les versions de wordpress à partir de la version minimale requise ! Peut on donc faire un dynamictest parent qui bouclerait sur les vesions wordpress pour ensuite boucler sur les tests eux memes sur les browsers ?
// TODO Tester toutes les langues disponibles via un test unitaire (pas la peine de tout tester dans toutes les langues !), juste vérifier que les langues se chargent bien
public class WPCommentContestPluginTest {

	private static final String EDIT_PAGE = "/wp-admin/edit.php"; //$NON-NLS-1$
	private static final String ID_DOACTION = "id=doaction"; //$NON-NLS-1$
	private static final String ID_SELECTALL1 = "id=cb-select-all-1"; //$NON-NLS-1$
	private static final String CHEAT_COMMENT = "cheatComment"; //$NON-NLS-1$
	private static final String STYLE_ATTRIBUTE = "style"; //$NON-NLS-1$
	private static final String ID_PREFIX = "id="; //$NON-NLS-1$
	private static final String ALIAS_CONFIG = "aliasConfig"; //$NON-NLS-1$
	private static final String ID_ALIAS_CONFIG = ID_PREFIX + ALIAS_CONFIG;
	private static final String EMAIL_CONFIG = "emailConfig"; //$NON-NLS-1$
	private static final String ID_EMAIL_CONFIG = ID_PREFIX + EMAIL_CONFIG;
	private static final String IPADDRESS_CONFIG = "ipConfig"; //$NON-NLS-1$
	private static final String ID_IPADRS_CONFIG = ID_PREFIX + IPADDRESS_CONFIG;
	private static final String TIMEBTWN_CONFIG = "timeBetween"; //$NON-NLS-1$
	private static final String ID_TMEBTW_CONFIG = ID_PREFIX + TIMEBTWN_CONFIG;
	private static final String TMEBTW_FLNAME = "timeBetweenFilterName"; //$NON-NLS-1$
	private static final String ID_TMEBTW_FLNAME = ID_PREFIX + TMEBTW_FLNAME;
	private static final String TEST_ON_BROWSER = "test on browser '%s'"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String CONTEST_LK_XPATH = "//tr[@id='post-%s']/td[@class='fr-zhykos-wordpress-commentcontest column-fr-zhykos-wordpress-commentcontest']/a"; //$NON-NLS-1$
	private static final String CONTEST_LK1_XPATH = String
			.format(CONTEST_LK_XPATH, "1"); //$NON-NLS-1$
	private static final String STATIC_METHOD = "static-method"; //$NON-NLS-1$
	private static final String BORDER_ERROR = "border: 2px solid red;"; //$NON-NLS-1$
	private static final String ID_CHEAT_LINK_1 = ID_PREFIX + "cheatLink-1"; //$NON-NLS-1$
	private static final String ID_DELETE_LINK_1 = ID_PREFIX + "deleteLink-1"; //$NON-NLS-1$
	private static final String ID_RESTORE_LINK_1 = ID_PREFIX + "restoreLink-1"; //$NON-NLS-1$
	private static final String ID_STOP_CHEAT_1 = ID_PREFIX + "stopCheatLink-1"; //$NON-NLS-1$

	private static IWordPressPlugin wpRssPlg;
	private static IWordPressPlugin fakerPlg;
	private static IWordPressPluginToTest myPlugin;
	private static IWordPressInformation wpInfo;

	// TODO faire une méthode utilitaire pour l'initialisation globale afin de créer un framework générique de tests pour wordpress
	@BeforeAll
	public static void beforeAll() throws UtilsException {
		wpRssPlg = IWordPressPluginCatalog.DEFAULT
				.getPlugin("wp-rss-aggregator"); //$NON-NLS-1$
		fakerPlg = IWordPressPluginCatalog.DEFAULT.getPlugin("fakerpress"); //$NON-NLS-1$
		myPlugin = IWordPressPluginToTestFactory.DEFAULT.getPlugin(
				"comment-contest", new String[] { "css/fr.zhykos.wordpress.commentcontest" }, //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "js/fr.zhykos.wordpress.commentcontest.js" }, //$NON-NLS-1$
				new String[] {});

		final boolean cleanWorkspace = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.hasToCleanWorkspace();
		if (cleanWorkspace) {
			// XXX Mettre le if dans cleanWorkspace() ????
			fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
					.cleanWorkspace();
		}
		final boolean installWordPress = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.getBooleanSystemProperty(
						WPCommentContestPluginTest.class.getName()
								+ ".installwordpress", //$NON-NLS-1$
						true);
		if (installWordPress) {
			// XXX Mettre le if dans installWordPress() ???
			fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
					.installWordPress();
		}
		// XXX startServer c'est nul comme nom...
		// TODO Avoir la possibilité de lancer les tests avec le code minimisé
		// et les fichiers supprimés (filesToRemove)
		wpInfo = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.startServer(myPlugin,
						new IWordPressPlugin[] { wpRssPlg, fakerPlg });
		Utils.addFakeComments(wpInfo);
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" }) // NOPMD tcicognani: cannot create constant for PMD SuppressWarnings
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testPluginInstallAndGlobalFeatures() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestPluginInstallAndGlobalFeatures(
					webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestPluginInstallAndGlobalFeatures(
			final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestPluginInstallAndGlobalFeatures(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestPluginInstallAndGlobalFeatures(
			final WebDriver driver) throws UtilsException {
		final Selenium selenium = new WebDriverBackedSelenium(driver,
				wpInfo.getTestServer().getHomeURL());
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL);
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, wpInfo.getWebsiteName());
		WpHtmlUtils.connect(driver, selenium, wpInfo);
		internalTestPluginCommentPage(driver, selenium);
		internalTestPluginArticlePage(driver, selenium);
	}

	private static void internalTestPluginArticlePage(final WebDriver driver,
			final Selenium selenium) throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + EDIT_PAGE);
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.articles);
		final String commentsNb = driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='comments column-comments']/div/a/span[@class='comment-count-approved']")) //$NON-NLS-1$
				.getText();
		Assertions.assertEquals(Utils.FAKE_COMMENTS_NB + 1,
				Integer.parseInt(commentsNb));
		final String contestLinkTxt = driver
				.findElement(By.xpath(CONTEST_LK1_XPATH)).getText();
		Assertions.assertEquals("Lancer le concours", contestLinkTxt);
		WpHtmlUtils.expandSettingsScreenMenu(driver, selenium);
		selenium.uncheck("id=fr-zhykos-wordpress-commentcontest-hide"); //$NON-NLS-1$
		selenium.click("id=screen-options-apply"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final List<WebElement> contestColumnElts = driver
				.findElements(By.xpath(CONTEST_LK1_XPATH));
		Assertions.assertTrue(contestColumnElts.isEmpty());
		WpHtmlUtils.expandSettingsScreenMenu(driver, selenium);
		selenium.check("id=fr-zhykos-wordpress-commentcontest-hide"); //$NON-NLS-1$
		final WebElement contestLink = driver
				.findElement(By.xpath(CONTEST_LK1_XPATH));
		Assertions.assertEquals("Lancer le concours", contestLink.getText());
		final String articleName = driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='title column-title has-row-actions column-primary page-title']/strong/a")) //$NON-NLS-1$
				.getText();
		selenium.click("id=screen-options-apply"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final WebElement contestLink2 = driver
				.findElement(By.xpath(CONTEST_LK1_XPATH));
		selenium.open(contestLink2.getAttribute("href")); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH2Tag(driver, "Comment Contest");
		WpHtmlUtils.assertH3Tag(driver,
				String.format("Concours pour l'article \"%s\"", articleName));
	}

	private static void internalTestPluginCommentPage(final WebDriver driver,
			final Selenium selenium) throws UtilsException {
		// Test if plugin link is a comment sub menu
		WpHtmlUtils.expandAdminMenu(driver, selenium);
		final WebElement plgCommentMenu = driver.findElement(By.xpath(
				"//li[@id='menu-comments']/ul/li/a[@href='edit-comments.php?page=fr.zhykos.wordpress.commentcontest']")); //$NON-NLS-1$
		final Actions action = new Actions(driver);
		final WebElement element = driver.findElement(By.id("menu-comments")); //$NON-NLS-1$
		action.moveToElement(element).build().perform();
		action.moveToElement(element).build().perform(); // XXX Edge specific hack. Please die!!!!!!!!
		WpHtmlUtils.waitUntilVisibleStateByElement(plgCommentMenu, true, 10000);
		final String linkStr = plgCommentMenu.getText();
		Assertions.assertEquals("Comment Contest", linkStr);
		// Check plugin page
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL
				+ "/wp-admin/edit-comments.php?page=fr.zhykos.wordpress.commentcontest"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH2Tag(driver, "Comment Contest");
		internalTestReport();
	}

	private static void internalTestReport() {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testCommentsInTable() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestCommentsInTable(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestCommentsInTable(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestCommentsInTable(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestCommentsInTable(final WebDriver driver)
			throws UtilsException {
		connectThenOpenCommentContestPluginOnArticleNumber1(driver);
		driver.findElement(By.xpath("//div[@id='contestForm']/table")); //$NON-NLS-1$
		final List<WebElement> linesElt = driver.findElements(
				By.xpath("//div[@id='contestForm']/table/tbody/tr")); //$NON-NLS-1$
		// n fake comments plus one from install and one invisible fake
		Assertions.assertEquals(Utils.FAKE_COMMENTS_NB + 2, linesElt.size());
		final String firstLineId = linesElt.get(0).getAttribute("id"); //$NON-NLS-1$
		Assertions.assertEquals("comment-contest-not-found-tr", firstLineId); //$NON-NLS-1$
		String bckColorAlt1 = null;
		String bckColorAlt2 = null;
		for (int i = 1; i < linesElt.size(); i++) {
			final WebElement lineElt = linesElt.get(i);
			final String eltId = lineElt.getAttribute("id"); //$NON-NLS-1$
			Assertions.assertTrue(eltId.matches("comment-contest-\\d")); //$NON-NLS-1$
			// Check alternate colors
			final String bckColor = lineElt.getCssValue("background-color"); //$NON-NLS-1$
			if (i % 2 == 0) {
				if (bckColorAlt1 == null) {
					bckColorAlt1 = bckColor;
				} else {
					Assertions.assertEquals(bckColorAlt1, bckColor);
				}
			} else {
				if (bckColorAlt2 == null) {
					bckColorAlt2 = bckColor;
				} else {
					Assertions.assertEquals(bckColorAlt2, bckColor);
				}
			}
		}
		Assertions.assertNotEquals(bckColorAlt2, bckColorAlt1);
		// Special links in table
		final List<WebElement> actionSpans = driver.findElements(By.xpath(
				"//tr[@id='comment-contest-1']/td[@class='comment column-comment']/div[@class='row-actions']/span")); //$NON-NLS-1$
		Assertions.assertEquals(4, actionSpans.size());
		final String span1 = actionSpans.get(0).getAttribute(CLASS_ATTRIBUTE);
		Assertions.assertEquals("delete", span1); //$NON-NLS-1$
		final String span2 = actionSpans.get(1).getAttribute(CLASS_ATTRIBUTE);
		Assertions.assertEquals("restore", span2); //$NON-NLS-1$
		final String span3 = actionSpans.get(2).getAttribute(CLASS_ATTRIBUTE);
		Assertions.assertEquals("cheat", span3); //$NON-NLS-1$
		final String span4 = actionSpans.get(3).getAttribute(CLASS_ATTRIBUTE);
		Assertions.assertEquals("stopcheat", span4); //$NON-NLS-1$
		// Check columns
		driver.findElement(By.xpath(
				"//tr[@id='comment-contest-1']/th[@class='check-column']/input[@type='checkbox']")); //$NON-NLS-1$
		final List<WebElement> columns = driver
				.findElements(By.xpath("//tr[@id='comment-contest-1']/td")); //$NON-NLS-1$
		Assertions.assertEquals(2, columns.size());
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testJustDraw() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestJustDraw(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestJustDraw(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestJustDraw(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestJustDraw(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		final String nbWinners = selenium.getValue("id=zwpcc_nb_winners"); //$NON-NLS-1$
		Assertions.assertEquals(1, Integer.parseInt(nbWinners));
		Assertions.assertFalse(selenium.isVisible("id=dialog-modal-winners")); //$NON-NLS-1$
		launchContestThenAssertNbWinners(selenium, driver,
				Integer.parseInt(nbWinners));
		closeResultDialog(driver);
		Assertions.assertFalse(selenium.isVisible("id=dialog-modal-winners")); //$NON-NLS-1$
	}

	private static void closeResultDialog(final WebDriver driver) {
		driver.findElement(By.xpath(
				"//div[@class='ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix ui-draggable-handle']/button")) //$NON-NLS-1$
				.click();
	}

	private static List<WebElement> launchContestThenAssertNbWinners(
			final Selenium selenium, final WebDriver driver,
			final int nbWinners) {
		final List<WebElement> winnerLines = new ArrayList<>();
		scrollToY(driver, 0);
		driver.findElement(By.id("zwpcc_form_submit")).click(); //$NON-NLS-1$
		Assertions.assertTrue(selenium.isVisible("id=dialog-modal-winners")); //$NON-NLS-1$
		final List<WebElement> resultLines = driver.findElements(By.xpath(
				"//div[@id='dialog-modal-winners']/table/tbody[@id='the-list-contest']/tr")); //$NON-NLS-1$
		for (final WebElement line : resultLines) {
			if (WpHtmlUtils.isVisible(line)) {
				winnerLines.add(line);
			}
		}
		Assertions.assertEquals(nbWinners, winnerLines.size());
		return winnerLines;
	}

	private static void scrollToY(final WebDriver driver, final int yPosition) {
		((JavascriptExecutor) driver).executeScript(String
				.format("window.scrollTo(0, %d);", Integer.valueOf(yPosition))); //$NON-NLS-1$
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testDrawTwoComments() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestDrawTwoComments(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestDrawTwoComments(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestDrawTwoComments(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestDrawTwoComments(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		selenium.type("id=zwpcc_nb_winners", "2"); //$NON-NLS-1$ //$NON-NLS-2$
		launchContestThenAssertNbWinners(selenium, driver, 2);
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testDateSelection() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestDateSelection(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestDateSelection(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				// XXX Un peu nul la condition pour opera mais on est obligé...
				if (!(driver instanceof OperaDriver)) {
					/*
					 * tcicognani: Mandatory maximize to prevent some bugs with
					 * Firefox which detect some visible elements as not
					 * visible. I maximize all windows just to have the same
					 * behavior for each browser
					 */
					driver.manage().window().maximize();
				}
				assertTestDateSelection(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestDateSelection(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		Assertions.assertFalse(
				selenium.isVisible("id=zwpcc_dateFilter_error_message")); //$NON-NLS-1$
		submitThenAssertDateFieldsStyle(true, true, selenium, driver);
		selenium.click("id=datepicker"); //$NON-NLS-1$
		WpHtmlUtils.waitUntilVisibleStateByElementId(selenium, driver,
				"ui-datepicker-div", true, 10000); //$NON-NLS-1$
		final Calendar fakeDate = Utils.getDateSecondDayOfCurrentMonth();
		WebElement dateMonthElt = driver.findElement(By.xpath(
				"//div[@id='ui-datepicker-div']//span[@class='ui-datepicker-month']")); //$NON-NLS-1$
		WebElement dateYearElt = driver.findElement(By.xpath(
				"//div[@id='ui-datepicker-div']//span[@class='ui-datepicker-year']")); //$NON-NLS-1$
		final String targetYear = Integer.toString(fakeDate.get(Calendar.YEAR));
		final String targetMonth = fakeDate.getDisplayName(Calendar.MONTH,
				Calendar.LONG, Locale.getDefault());
		while (!targetYear.equals(dateYearElt.getText())
				&& !targetMonth.equals(dateMonthElt.getText())) {
			driver.findElement(By.xpath(
					"//div[@id='ui-datepicker-div']//a[@data-handler='prev']")) //$NON-NLS-1$
					.click();
			dateMonthElt = driver.findElement(By.xpath(
					"//div[@id='ui-datepicker-div']//span[@class='ui-datepicker-month']")); //$NON-NLS-1$
			dateYearElt = driver.findElement(By.xpath(
					"//div[@id='ui-datepicker-div']//span[@class='ui-datepicker-year']")); //$NON-NLS-1$
		}
		final List<WebElement> dayLinks = driver.findElements(By.xpath(
				"//div[@id='ui-datepicker-div']//table[@class='ui-datepicker-calendar']//td/a")); //$NON-NLS-1$
		final YearMonth yearMonthObject = YearMonth.of(
				fakeDate.get(Calendar.YEAR), fakeDate.get(Calendar.MONTH) + 1);
		Assertions.assertEquals(yearMonthObject.lengthOfMonth(),
				dayLinks.size());
		dayLinks.get(0).click();
		WpHtmlUtils.waitUntilVisibleStateByElementId(selenium, driver,
				"ui-datepicker-div", false, 10000); //$NON-NLS-1$
		submitThenAssertDateFieldsStyle(true, false, selenium, driver);
		selenium.type("id=dateHours", //$NON-NLS-1$
				Integer.toString(fakeDate.get(Calendar.HOUR_OF_DAY)));
		selenium.type("id=dateMinutes", //$NON-NLS-1$
				Integer.toString(fakeDate.get(Calendar.MINUTE) + 1));
		submitThenAssertDateFieldsStyle(false, false, selenium, driver);
		assertCommentsCheckInTable(driver, Utils.FAKE_COMMENTS_NB);
		launchContestThenAssertNbWinners(selenium, driver, 1);
		// XXX Je n'ai pas ajouté de tests en mettant des valeurs fausses...
	}

	private static void assertCommentsCheckInTable(final WebDriver driver,
			final int expectedChecked) {
		final List<WebElement> checkCols = driver.findElements(
				By.xpath("//div[@id='contestForm']/table/tbody/tr/th/input")); //$NON-NLS-1$
		Assertions.assertEquals(Utils.FAKE_COMMENTS_NB + 1, checkCols.size());
		int nbChecked = 0;
		for (final WebElement column : checkCols) {
			if (column.isSelected()) {
				nbChecked++;
			}
		}
		Assertions.assertEquals(expectedChecked, nbChecked);
	}

	public static void expandFilters(final WebDriver driver,
			final Selenium selenium) {
		WpHtmlUtils.setDisplayBlockById(driver, "filters"); //$NON-NLS-1$
		Assertions.assertTrue(selenium.isVisible("id=filters")); //$NON-NLS-1$
	}

	private static void submitThenAssertDateFieldsStyle(
			final boolean mustHaveError, final boolean errorOnDate,
			final Selenium selenium, final WebDriver driver) {
		selenium.click("id=dateSubmit"); //$NON-NLS-1$
		final boolean visible = WpHtmlUtils
				.isVisible("zwpcc_dateFilter_error_message", selenium, driver); //$NON-NLS-1$
		Assertions.assertEquals(Boolean.valueOf(mustHaveError),
				Boolean.valueOf(visible));
		final String dateCssStyle = driver.findElement(By.id("datepicker")) //$NON-NLS-1$
				.getAttribute(STYLE_ATTRIBUTE);
		Assertions.assertEquals(Boolean.valueOf(mustHaveError && errorOnDate),
				Boolean.valueOf(dateCssStyle.contains(BORDER_ERROR)));
		final String hoursCssStyle = driver.findElement(By.id("dateHours")) //$NON-NLS-1$
				.getAttribute(STYLE_ATTRIBUTE);
		Assertions.assertEquals(Boolean.valueOf(mustHaveError && !errorOnDate),
				Boolean.valueOf(hoursCssStyle.contains(BORDER_ERROR)));
		final String minCssStyle = driver.findElement(By.id("dateMinutes")) //$NON-NLS-1$
				.getAttribute(STYLE_ATTRIBUTE);
		Assertions.assertEquals(Boolean.valueOf(mustHaveError && !errorOnDate),
				Boolean.valueOf(minCssStyle.contains(BORDER_ERROR)));
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testAliasLimitation() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestAliasLimitation(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestAliasLimitation(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestAliasLimitation(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestAliasLimitation(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		Assertions.assertFalse(
				selenium.isVisible("id=zwpcc_aliasFilter_error_message")); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, ""); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(true, selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "a"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(true, selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "-1"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(true, selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, ".1"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(true, selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 2);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "2"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 0);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 1);
		Assertions.assertTrue(
				driver.findElement(By.id("cb-select-2")).isSelected()); //$NON-NLS-1$
		Assertions.assertEquals(Utils.getZhykosName(), driver.findElement(By
				.xpath("//tr[@id='comment-contest-2']/td[@class='author column-author has-row-actions column-primary']/strong")) //$NON-NLS-1$
				.getText());
		launchContestThenAssertNbWinners(selenium, driver, 1);
	}

	private static void uncheckAllTableWhenFiltersAreExpanded(
			final Selenium selenium, final WebDriver driver)
			throws UtilsException {
		scrollToY(driver, 600);
		uncheckAllTable(selenium);
	}

	private static void uncheckAllTable(final Selenium selenium)
			throws UtilsException {
		selenium.click(ID_SELECTALL1);
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			throw new UtilsException(e);
		}
		if (selenium.isChecked(ID_SELECTALL1)) {
			selenium.click(ID_SELECTALL1);
		}
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			throw new UtilsException(e);
		}
	}

	private static void checkAllTable(final Selenium selenium)
			throws UtilsException {
		if (!selenium.isChecked(ID_SELECTALL1)) {
			selenium.click(ID_SELECTALL1);
		}
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			throw new UtilsException(e);
		}
	}

	private static void submitThenAssertAliasFieldStyle(
			final boolean mustHaveError, final Selenium selenium,
			final WebDriver driver) {
		submitThenAssertFieldStyle(mustHaveError, selenium, driver,
				"aliasAddressFilter", "zwpcc_aliasFilter_error_message", //$NON-NLS-1$ //$NON-NLS-2$
				ALIAS_CONFIG);
	}

	private static void submitThenAssertFieldStyle(final boolean mustHaveError,
			final Selenium selenium, final WebDriver driver,
			final String buttonId, final String errorId, final String fieldId) {
		scrollToY(driver, 200);
		selenium.click(ID_PREFIX + buttonId);
		final boolean visible = WpHtmlUtils.isVisible(errorId, selenium,
				driver);
		Assertions.assertEquals(Boolean.valueOf(mustHaveError),
				Boolean.valueOf(visible));
		final String cssStyle = driver.findElement(By.id(fieldId))
				.getAttribute(STYLE_ATTRIBUTE);
		Assertions.assertEquals(Boolean.valueOf(mustHaveError),
				Boolean.valueOf(cssStyle.contains(BORDER_ERROR)));
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testEmailLimitation() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestEmailLimitation(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestEmailLimitation(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestEmailLimitation(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestEmailLimitation(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		Assertions.assertFalse(
				selenium.isVisible("id=zwpcc_emailFilter_error_message")); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, ""); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(true, selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "a"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(true, selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "-1"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(true, selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, ".1"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(true, selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 2);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "2"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 0);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 1);
		Assertions.assertTrue(
				driver.findElement(By.id("cb-select-3")).isSelected()); //$NON-NLS-1$
		Assertions.assertTrue(driver.findElement(By.xpath(
				"//tr[@id='comment-contest-3']/td[@class='author column-author has-row-actions column-primary']")) //$NON-NLS-1$
				.getText().contains(Utils.getZhykosEmail()));
		launchContestThenAssertNbWinners(selenium, driver, 1);
	}

	private static void submitThenAssertEmailFieldStyle(
			final boolean mustHaveError, final Selenium selenium,
			final WebDriver driver) {
		submitThenAssertFieldStyle(mustHaveError, selenium, driver,
				"emailAddressFilter", "zwpcc_emailFilter_error_message", //$NON-NLS-1$ //$NON-NLS-2$
				EMAIL_CONFIG);
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testIPAddressLimitation() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestIPAddressLimitation(
					webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestIPAddressLimitation(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestIPAddressLimitation(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestIPAddressLimitation(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		Assertions.assertFalse(
				selenium.isVisible("id=zwpcc_ipFilter_error_message")); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, ""); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(true, selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "a"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(true, selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "-1"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(true, selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, ".1"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(true, selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 2);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "2"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 0);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 1);
		Assertions.assertTrue(
				driver.findElement(By.id("cb-select-4")).isSelected()); //$NON-NLS-1$
		Assertions.assertTrue(driver.findElement(By.xpath(
				"//tr[@id='comment-contest-4']/td[@class='author column-author has-row-actions column-primary']")) //$NON-NLS-1$
				.getText().contains(Utils.getZhykosIPAddress()));
		launchContestThenAssertNbWinners(selenium, driver, 1);
	}

	private static void submitThenAssertIpAddressFieldStyle(
			final boolean mustHaveError, final Selenium selenium,
			final WebDriver driver) {
		submitThenAssertFieldStyle(mustHaveError, selenium, driver,
				"ipAddressFilter", "zwpcc_ipFilter_error_message", //$NON-NLS-1$ //$NON-NLS-2$
				IPADDRESS_CONFIG);
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testWords() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestWords(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestWords(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestWords(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestWords(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		submitThenAssertCommentTextOneWord(selenium, driver, "", 0); //$NON-NLS-1$
		submitThenAssertCommentTextOneWord(selenium, driver, "zhykos", //$NON-NLS-1$
				Utils.FAKE_COMMENTS_NB + 1);
		submitThenAssertCommentTextOneWord(selenium, driver, "coucou", 4); //$NON-NLS-1$
		submitThenAssertCommentTextOneWord(selenium, driver, "commentaire", 3); //$NON-NLS-1$
		submitThenAssertCommentTextOneWord(selenium, driver, "commentaire4", 5); //$NON-NLS-1$
		submitThenAssertCommentTextOneWord(selenium, driver, " commentaire4 ", //$NON-NLS-1$
				5);
		submitThenAssertCommentTextOneWord(selenium, driver, "commentaire5", 5); //$NON-NLS-1$
		submitThenAssertCommentTextOneWord(selenium, driver, "commen", 3); //$NON-NLS-1$
		submitThenAssertCommentTextAllWords(selenium, driver, "coucou, texte", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextAllWords(selenium, driver, "coucou,texte", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextAllWords(selenium, driver, "coucou , texte", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextAllWords(selenium, driver, "coucou ,texte", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextAllWords(selenium, driver, "coucou ,texte,", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextAllWords(selenium, driver, ",coucou ,texte", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextAllWords(selenium, driver, ",coucou,texte,", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextOneWord(selenium, driver, "coucou, texte", //$NON-NLS-1$
				4);
		submitThenAssertCommentTextOneWord(selenium, driver,
				"commentaire4, commentaire5", 4); //$NON-NLS-1$
		submitThenAssertCommentTextOneWord(selenium, driver, "zhykos, salut", //$NON-NLS-1$
				5);
	}

	private static void submitThenAssertCommentTextOneWord(
			final Selenium selenium, final WebDriver driver, final String words,
			final int nbResult) throws UtilsException {
		submitThenAssertCommentText(selenium, driver, "wordsFilter", words, //$NON-NLS-1$
				nbResult);
	}

	private static void submitThenAssertCommentTextAllWords(
			final Selenium selenium, final WebDriver driver, final String words,
			final int nbResult) throws UtilsException {
		submitThenAssertCommentText(selenium, driver, "allWordsFilter", words, //$NON-NLS-1$
				nbResult);
	}

	private static void submitThenAssertCommentText(final Selenium selenium,
			final WebDriver driver, final String buttonId, final String words,
			final int nbResult) throws UtilsException {
		scrollToY(driver, 400);
		selenium.type(ID_PREFIX + "words", words); //$NON-NLS-1$
		selenium.click(ID_PREFIX + buttonId);
		assertCommentsCheckInTable(driver, nbResult);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testTimeBetween() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestTimeBetween(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestTimeBetween(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestTimeBetween(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestTimeBetween(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		Assertions.assertFalse(
				selenium.isVisible("id=zwpcc_timeBetweenFilter_error_message")); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(true, selenium, driver);
		selenium.type(ID_TMEBTW_CONFIG, "a"); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(true, selenium, driver);
		selenium.type(ID_TMEBTW_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(true, selenium, driver);
		selenium.type(ID_TMEBTW_CONFIG, "-1"); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(true, selenium, driver);
		selenium.type(ID_TMEBTW_CONFIG, ".1"); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(true, selenium, driver);
		selenium.type(ID_TMEBTW_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(false, selenium, driver);
		selenium.uncheck(ID_TMEBTW_FLNAME);
		selenium.uncheck("id=timeBetweenFilterEmail"); //$NON-NLS-1$
		selenium.uncheck("id=timeBetweenFilterIP"); //$NON-NLS-1$
		selenium.click(ID_PREFIX + "timeBetweenFilter"); //$NON-NLS-1$
		final boolean visible = WpHtmlUtils.isVisible(
				"zwpcc_timeBetweenFilter_error_message", selenium, driver); //$NON-NLS-1$
		Assertions.assertTrue(visible);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		submitThenAssertTimeBetweenField(selenium, driver, 0, TMEBTW_FLNAME);
		submitThenAssertTimeBetweenField(selenium, driver, 1,
				"timeBetweenFilterEmail"); //$NON-NLS-1$
		submitThenAssertTimeBetweenField(selenium, driver, 1,
				"timeBetweenFilterIP"); //$NON-NLS-1$
		selenium.check(ID_TMEBTW_FLNAME);
		selenium.check("id=timeBetweenFilterEmail"); //$NON-NLS-1$
		selenium.check("id=timeBetweenFilterIP"); //$NON-NLS-1$
		assertCommentsCheckInTable(driver, 0);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.uncheck("id=timeBetweenFilterEmail"); //$NON-NLS-1$
		selenium.uncheck("id=timeBetweenFilterIP"); //$NON-NLS-1$
		selenium.type(ID_TMEBTW_CONFIG, "1440"); //$NON-NLS-1$
		submitThenAssertTimeBetweenField(selenium, driver, 0, TMEBTW_FLNAME);
		selenium.type(ID_TMEBTW_CONFIG, "1441"); //$NON-NLS-1$
		selenium.check(ID_TMEBTW_FLNAME);
		selenium.check(ID_TMEBTW_FLNAME); // A second check for fu***** Edge
		submitThenAssertTimeBetweenFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, 1);
		Assertions.assertTrue(selenium.isChecked("id=cb-select-2")); //$NON-NLS-1$
	}

	private static void submitThenAssertTimeBetweenField(
			final Selenium selenium, final WebDriver driver, final int nbResult,
			final String checkId) throws UtilsException {
		selenium.check(ID_PREFIX + checkId);
		submitThenAssertTimeBetweenFieldStyle(false, selenium, driver);
		assertCommentsCheckInTable(driver, nbResult);
		uncheckAllTableWhenFiltersAreExpanded(selenium, driver);
		selenium.uncheck(ID_PREFIX + checkId);
	}

	private static void submitThenAssertTimeBetweenFieldStyle(
			final boolean mustHaveError, final Selenium selenium,
			final WebDriver driver) {
		submitThenAssertFieldStyle(mustHaveError, selenium, driver,
				"timeBetweenFilter", "zwpcc_timeBetweenFilter_error_message", //$NON-NLS-1$ //$NON-NLS-2$
				TIMEBTWN_CONFIG);
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testUserRoles() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestUserRoles(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestUserRoles(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestUserRoles(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestUserRoles(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		final List<WebElement> allAliasesElt = driver.findElements(By.xpath(
				"//tbody[@id='the-list-contest']//span[@class='zwpcc_alias']")); //$NON-NLS-1$
		final Set<String> allAliases = new HashSet<>();
		for (final WebElement webElement : allAliasesElt) {
			WpHtmlUtils.setDisplay(driver, webElement, "block"); //$NON-NLS-1$
			final String text = webElement.getText();
			if (!text.isEmpty()) {
				allAliases.add(text);
			}
		}
		allAliases.remove(wpInfo.getLogin());
		final Set<String> allRoles = new HashSet<>();
		final List<WebElement> allRoleElt = driver
				.findElements(By.xpath("//div[@id='contestForm']//li")); //$NON-NLS-1$
		for (final WebElement webElement : allRoleElt) {
			final String classValue = webElement.getAttribute(CLASS_ATTRIBUTE);
			allRoles.add(classValue);
		}
		allRoles.remove("administrator"); //$NON-NLS-1$
		Utils.changeRole(driver, selenium, wpInfo, allAliases, allRoles);
		openCommentContestPluginOnArticleNumber1(driver, selenium);
		final List<WebElement> allRoleElt2 = driver
				.findElements(By.xpath("//div[@id='contestForm']//li")); //$NON-NLS-1$
		for (final WebElement webElement : allRoleElt2) {
			webElement.click();
			final String classValue = webElement.getAttribute(CLASS_ATTRIBUTE);
			if ("administrator".equals(classValue)) { //$NON-NLS-1$
				assertCommentsCheckInTable(driver, 2);
			} else {
				assertCommentsCheckInTable(driver, 1);
			}
			scrollToY(driver, 200);
			uncheckAllTable(selenium);
		}
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testExpandFilters() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestExpandFilters(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestExpandFilters(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestExpandFilters(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestExpandFilters(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		// Just some random ids (all are not mandatory)
		final String[] idsToCheck = new String[] { "datepicker", "aliasConfig", //$NON-NLS-1$ //$NON-NLS-2$
				"timeBetween", "emailAddressFilter" }; //$NON-NLS-1$ //$NON-NLS-2$
		for (final String idToCheck : idsToCheck) {
			Assertions.assertFalse(selenium.isVisible(ID_PREFIX + idToCheck));
		}
		expandFilters(driver, selenium);
		for (final String idToCheck : idsToCheck) {
			Assertions.assertTrue(selenium.isVisible(ID_PREFIX + idToCheck));
		}
		WpHtmlUtils.setDisplayNone(driver, By.id("filters")); //$NON-NLS-1$
		Assertions.assertFalse(selenium.isVisible("id=filters")); //$NON-NLS-1$
		for (final String idToCheck : idsToCheck) {
			Assertions.assertFalse(selenium.isVisible(ID_PREFIX + idToCheck));
		}
		selenium.click("id=filtersImg"); //$NON-NLS-1$
		WpHtmlUtils.waitUntilVisibleStateByElementId(selenium, driver,
				"filters", true, 10000); //$NON-NLS-1$
		Assertions.assertTrue(selenium.isVisible("id=filters")); //$NON-NLS-1$
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testTooltip() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestTooltip(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestTooltip(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestTooltip(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestTooltip(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		Assertions.assertFalse(selenium.isVisible("id=tiptip_holder")); //$NON-NLS-1$
		final List<WebElement> allHelpsElt = driver
				.findElements(By.xpath("//img[@class='help']")); //$NON-NLS-1$
		final Set<String> margins = new HashSet<>();
		final List<WebElement> displayedElt = new ArrayList<>();
		for (final WebElement webElement : allHelpsElt) {
			if (!webElement.isDisplayed()) {
				continue;
			}
			displayedElt.add(webElement);
			@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
			final Actions action = new Actions(driver);
			action.moveToElement(webElement).build().perform();
			final WebElement tiptip = driver
					.findElement(By.id("tiptip_holder")); //$NON-NLS-1$
			WpHtmlUtils.waitUntilVisibleStateByElement(tiptip, true, 10000);
			final String marginTop = tiptip.getCssValue("margin-top"); //$NON-NLS-1$
			final String marginBottom = tiptip.getCssValue("margin-bottom"); //$NON-NLS-1$
			final String marginLeft = tiptip.getCssValue("margin-left"); //$NON-NLS-1$
			final String marginRight = tiptip.getCssValue("margin-right"); //$NON-NLS-1$
			margins.add(marginTop + '/' + marginBottom + '/' + marginLeft + '/'
					+ marginRight);
			final String helpText = driver.findElement(By.id("tiptip_content")) //$NON-NLS-1$
					.getText();
			Assertions.assertFalse(helpText.isEmpty());
		}
		Assertions.assertEquals(displayedElt.size(), margins.size());
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testCheatOrDelete() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestCheatOrDelete(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestCheatOrDelete(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestCheatOrDelete(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestCheatOrDelete(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		scrollToY(driver, 400);
		final WebElement cheat1 = driver.findElement(By
				.xpath("//tr[@id='comment-contest-1']//span[@class='cheat']")); //$NON-NLS-1$
		Assertions.assertTrue(WpHtmlUtils.isVisible(cheat1));
		displayAllRowActions(driver);
		selenium.click(ID_CHEAT_LINK_1);
		Assertions.assertFalse(selenium.isVisible(ID_CHEAT_LINK_1));
		for (int i = 0; i < 10; i++) {
			final List<WebElement> winners = launchContestThenAssertNbWinners(
					selenium, driver, 1);
			final WebElement alias = winners.get(0)
					.findElement(By.xpath("td/strong")); //$NON-NLS-1$
			Assertions.assertEquals(wpInfo.getLogin(), alias.getText());
			closeResultDialog(driver);
		}
		selenium.click(ID_STOP_CHEAT_1);
		for (int lineId = 2; lineId <= Utils.FAKE_COMMENTS_NB + 1; lineId++) {
			selenium.click("id=deleteLink-" + lineId); //$NON-NLS-1$
		}
		for (int i = 0; i < 10; i++) {
			final List<WebElement> winners = launchContestThenAssertNbWinners(
					selenium, driver, 1);
			final WebElement alias = winners.get(0)
					.findElement(By.xpath("td/strong")); //$NON-NLS-1$
			Assertions.assertEquals(wpInfo.getLogin(), alias.getText());
			closeResultDialog(driver);
		}
		Assertions.assertTrue(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_CHEAT_LINK_1);
		Assertions.assertTrue(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_STOP_CHEAT_1);
		Assertions.assertTrue(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_DELETE_LINK_1);
		Assertions.assertFalse(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_RESTORE_LINK_1);
		Assertions.assertTrue(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_DELETE_LINK_1);
		Assertions.assertFalse(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_CHEAT_LINK_1);
		Assertions.assertTrue(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_DELETE_LINK_1);
		Assertions.assertFalse(selenium.isVisible(ID_DELETE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_RESTORE_LINK_1));
		Assertions.assertTrue(selenium.isVisible(ID_CHEAT_LINK_1));
		Assertions.assertFalse(selenium.isVisible(ID_STOP_CHEAT_1));
		selenium.click(ID_RESTORE_LINK_1);
		selenium.click(ID_CHEAT_LINK_1);
		final WebElement cc1Elt = driver
				.findElement(By.xpath("//tr[@id='comment-contest-1']/th")); //$NON-NLS-1$
		final String classCC1 = cc1Elt.getAttribute(CLASS_ATTRIBUTE);
		Assertions.assertTrue(classCC1.contains(CHEAT_COMMENT));
		final String bkgCC1 = cc1Elt.getCssValue("background-color"); //$NON-NLS-1$
		selenium.click("id=cheatLink-2"); //$NON-NLS-1$
		final WebElement cc2Elt = driver
				.findElement(By.xpath("//tr[@id='comment-contest-2']/th")); //$NON-NLS-1$
		final String classCC2 = cc2Elt.getAttribute(CLASS_ATTRIBUTE);
		Assertions.assertTrue(classCC2.contains(CHEAT_COMMENT));
		final String bkgCC2 = cc2Elt.getCssValue("background-color"); //$NON-NLS-1$
		Assertions.assertEquals(bkgCC1, bkgCC2);
	}

	private static void displayAllRowActions(final WebDriver driver) {
		for (int lineId = 1; lineId <= Utils.FAKE_COMMENTS_NB + 1; lineId++) {
			final WebElement rowActions = driver.findElement(By.xpath(String
					.format("//tr[@id='comment-contest-%d']//div[@class='row-actions']", //$NON-NLS-1$
							Integer.valueOf(lineId))));
			((JavascriptExecutor) driver)
					.executeScript("arguments[0].style.left='0';", rowActions); //$NON-NLS-1$
		}
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testContestRandom() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestContestRandom(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestContestRandom(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestContestRandom(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestContestRandom(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		final Set<String> allWinners = new HashSet<>();
		int attempts = 0;
		final int maxAttempts = (Utils.FAKE_COMMENTS_NB + 1) * 10;
		while (attempts < maxAttempts
				&& allWinners.size() != Utils.FAKE_COMMENTS_NB + 1) {
			final List<WebElement> winners = launchContestThenAssertNbWinners(
					selenium, driver, 1);
			final String lineId = winners.get(0).getAttribute("id"); //$NON-NLS-1$
			allWinners.add(lineId);
			closeResultDialog(driver);
			attempts++;
		}
		Assertions.assertTrue(allWinners.size() == Utils.FAKE_COMMENTS_NB + 1,
				"All winners could not be drawn, is there any but in the random system?"); //$NON-NLS-1$
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testComboActions() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestComboActions(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestComboActions(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestComboActions(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestComboActions(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		selenium.click(ID_DOACTION);
		assertCommentsCheckInTable(driver, 0);
		checkAllTable(selenium);
		final int nbComments = Utils.FAKE_COMMENTS_NB + 1;
		assertCommentsCheckInTable(driver, nbComments);
		selenium.click(ID_DOACTION);
		for (int i = 1; i <= nbComments; i++) {
			final WebElement lineElt = driver
					.findElement(By.id("comment-contest-" + i)); //$NON-NLS-1$
			final String classLine = lineElt.getAttribute(CLASS_ATTRIBUTE);
			Assertions.assertFalse(classLine.contains("removedComment"), //$NON-NLS-1$
					String.format(
							"Line %d must not have 'removedComment' class", //$NON-NLS-1$
							Integer.valueOf(i)));
			Assertions.assertFalse(classLine.contains(CHEAT_COMMENT),
					String.format("Line %d must not have 'cheatComment' class", //$NON-NLS-1$
							Integer.valueOf(i)));
		}
		selenium.select("id=bulk-action-selector-top", "value=delete"); //$NON-NLS-1$ //$NON-NLS-2$
		selenium.click(ID_DOACTION);
		for (int i = 1; i <= nbComments; i++) {
			final WebElement lineElt = driver
					.findElement(By.id("comment-contest-" + i)); //$NON-NLS-1$
			final String classLine = lineElt.getAttribute(CLASS_ATTRIBUTE);
			Assertions.assertTrue(classLine.contains("removedComment"), //$NON-NLS-1$
					String.format("Line %d must have 'removedComment' class", //$NON-NLS-1$
							Integer.valueOf(i)));
			Assertions.assertFalse(classLine.contains(CHEAT_COMMENT),
					String.format("Line %d must not have 'cheatComment' class", //$NON-NLS-1$
							Integer.valueOf(i)));
		}
		selenium.select("id=bulk-action-selector-top", "value=restore"); //$NON-NLS-1$ //$NON-NLS-2$
		selenium.click(ID_DOACTION);
		for (int i = 1; i <= nbComments; i++) {
			final WebElement lineElt = driver
					.findElement(By.id("comment-contest-" + i)); //$NON-NLS-1$
			final String classLine = lineElt.getAttribute(CLASS_ATTRIBUTE);
			Assertions.assertFalse(classLine.contains("removedComment"), //$NON-NLS-1$
					String.format(
							"Line %d must not have 'removedComment' class", //$NON-NLS-1$
							Integer.valueOf(i)));
			Assertions.assertFalse(classLine.contains(CHEAT_COMMENT),
					String.format("Line %d must not have 'cheatComment' class", //$NON-NLS-1$
							Integer.valueOf(i)));
		}
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testOnPage() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestOnPage(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestOnPage(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestOnPage(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestOnPage(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		final List<String> commentsArticle = new ArrayList<>();
		final List<WebElement> commentColumns = driver.findElements(By.xpath(
				"//tbody[@id='the-list-contest']//td[@class='comment column-comment']")); //$NON-NLS-1$
		for (final WebElement webElement : commentColumns) {
			final String text = webElement.getText();
			if (!text.isEmpty()) {
				commentsArticle.add(text);
			}
		}
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + EDIT_PAGE + "?post_type=page"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final List<WebElement> pageContest = driver
				.findElements(By.xpath(String.format(CONTEST_LK_XPATH, "2"))); //$NON-NLS-1$
		if (pageContest.isEmpty()) {
			Utils.generateFakeCommentsOnPages(driver, selenium, homeURL);
		}
		selenium.open(homeURL + EDIT_PAGE + "?post_type=page"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final List<WebElement> pageContest2 = driver
				.findElements(By.xpath(String.format(CONTEST_LK_XPATH, "2"))); //$NON-NLS-1$
		Assertions.assertTrue(pageContest2.size() == 1);
		openCommentContestPluginOnPostNumber(driver, selenium, 2,
				EDIT_PAGE + "?post_type=page"); //$NON-NLS-1$
		final List<String> commentsPage = new ArrayList<>();
		final List<WebElement> commentColumns2 = driver.findElements(By.xpath(
				"//tbody[@id='the-list-contest']//td[@class='comment column-comment']")); //$NON-NLS-1$
		for (final WebElement webElement : commentColumns2) {
			final String text = webElement.getText();
			if (!text.isEmpty()) {
				commentsPage.add(text);
			}
		}
		Assertions.assertTrue(
				Collections.disjoint(commentsArticle, commentsPage));
	}

	@SuppressWarnings({ STATIC_METHOD,
			"PMD.JUnit4TestShouldUseTestAnnotation" })
	/*
	 * @SuppressWarnings("static-method") tcicognani: TestFactory cannot be
	 * static
	 */
	/*
	 * @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation") tcicognani:
	 * It's not a JUnit 4 method, it's JUnit 5...
	 */
	// XXX On a toujours le même pattern pour tester les méthodes sur tous les
	// navigateurs
	// XXX Rajouter timeout
	@TestFactory
	public Collection<DynamicTest> testNoCommentArticle() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestNoCommentArticle(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestNoCommentArticle(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestNoCommentArticle(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestNoCommentArticle(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = connectThenOpenCommentContestPluginOnArticleNumber1(
				driver);
		final String newArticleId = generateOtherArticle(selenium, driver);
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + EDIT_PAGE);
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		driver.findElement(By.xpath(String.format(CONTEST_LK_XPATH, "1"))); //$NON-NLS-1$
		final List<WebElement> contestLinks = driver.findElements(
				By.xpath(String.format(CONTEST_LK_XPATH, newArticleId)));
		Assertions.assertTrue(contestLinks.isEmpty());
		selenium.open(homeURL
				+ "/wp-admin/edit-comments.php?page=fr.zhykos.wordpress.commentcontest&postID=" //$NON-NLS-1$
				+ newArticleId);
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH2Tag(driver, "Comment Contest");
		final String pageContents = selenium.getText("id=zwpcc_form"); //$NON-NLS-1$
		Assertions.assertTrue(pageContents.contains(
				"Debug : Le paramètre 'post' dans l'URL n'est pas valide"));
		selenium.open(homeURL
				+ "/wp-admin/edit-comments.php?page=fr.zhykos.wordpress.commentcontest&postID=" //$NON-NLS-1$
				+ "aaa"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		Assertions.assertTrue(pageContents.contains(
				"Debug : Le paramètre 'post' dans l'URL n'est pas valide"));
	}

	private static String generateOtherArticle(final Selenium selenium,
			final WebDriver driver) throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(
				homeURL + "/wp-admin/admin.php?page=fakerpress&view=posts"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		selenium.type("id=fakerpress-field-qty-min", "1"); //$NON-NLS-1$ //$NON-NLS-2$
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
		return driver.findElement(By.xpath(
				"//div[@class='fp-response']//div[@class='notice is-dismissible notice-success']/p/a")) //$NON-NLS-1$
				.getText();
	}

	private static Selenium connectThenOpenCommentContestPluginOnArticleNumber1(
			final WebDriver driver) throws UtilsException {
		final Selenium selenium = new WebDriverBackedSelenium(driver,
				wpInfo.getTestServer().getHomeURL());
		WpHtmlUtils.connect(driver, selenium, wpInfo);
		openCommentContestPluginOnArticleNumber1(driver, selenium);
		return selenium;
	}

	private static void openCommentContestPluginOnArticleNumber1(
			final WebDriver driver, final Selenium selenium)
			throws UtilsException {
		openCommentContestPluginOnArticleNumber(driver, selenium, 1);
	}

	private static void openCommentContestPluginOnArticleNumber(
			final WebDriver driver, final Selenium selenium, final int number)
			throws UtilsException {
		openCommentContestPluginOnPostNumber(driver, selenium, number,
				EDIT_PAGE);
	}

	private static void openCommentContestPluginOnPostNumber(
			final WebDriver driver, final Selenium selenium, final int number,
			final String pageURL) throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + pageURL);
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final WebElement contestLink = driver.findElement(By.xpath(
				String.format(CONTEST_LK_XPATH, Integer.toString(number))));
		selenium.open(contestLink.getAttribute("href")); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
	}

	@AfterAll
	public static void afterAll() throws IOException, UtilsException {
		if (wpInfo != null) {
			reportTests();
			try {
				System.out.println("PASSWORD = (" + wpInfo.getPassword() + ")");
				wpInfo.getTestServer().stop();
			} catch (final Exception e) {
				Assertions.fail(e.getMessage());
			}
		}
		// TODO Utiliser la classe EMF Facet de vérification d'erreur lors du
		// test unitaire pour savoir si on peut packager
		// Utils.packagePlugin(this.myPlugin);
		// fail();
		// TODO Lister les navigateurs testés avec des détails genre le numéro de version
		// TODO Afficher le mot de passe en debug (avec un "-D")
	}

	@SuppressWarnings("PMD.SystemPrintln")
	/*
	 * @SuppressWarnings("PMD.SystemPrintln") tcicognani: report method in console
	 */
	private static void reportTests() throws IOException, UtilsException {
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		final String serverVersion = wpInfo.getTestServer()
				.getVersion(wpInfo.getInstallDir());
		System.out.println("-------------------------------------------------"); //$NON-NLS-1$
		System.out.println("Tests launched by Zhykos WordPress automatic tests 0.1.0"); //$NON-NLS-1$
		System.out.println("WordPress version " + getWordPressVersion()); //$NON-NLS-1$
		System.out.println("DBMS: " + getDBVersion()); //$NON-NLS-1$
		System.out.println("Server: " + serverVersion); //$NON-NLS-1$
		System.out.println(String.format("OS: %s version %s %s", //$NON-NLS-1$
				System.getProperty("os.name"), System.getProperty("os.version"), //$NON-NLS-1$ //$NON-NLS-2$
				System.getProperty("os.arch"))); //$NON-NLS-1$
		System.out.println("Browsers:"); //$NON-NLS-1$
		for (final WebDriver webDriver : allDrivers) {
			if (!(webDriver instanceof ErrorDriver)) {
				final Capabilities cap = ((RemoteWebDriver) webDriver)
						.getCapabilities();
				System.out.println(
						String.format(" - Driver %s based on %s version %s", //$NON-NLS-1$
								webDriver.getClass().getSimpleName(),
								cap.getBrowserName(), cap.getVersion()));
			}
		}
		System.out.println("-------------------------------------------------"); //$NON-NLS-1$
		for (final WebDriver webDriver : allDrivers) {
			if (!(webDriver instanceof ErrorDriver)) {
				webDriver.quit();
			}
		}
	}

	private static String getWordPressVersion() throws IOException {
		final File dir = wpInfo.getInstallDir();
		final File versionFile = new File(dir, "wp-includes/version.php"); //$NON-NLS-1$
		final List<String> lines = FileUtils.readLines(versionFile,
				Charset.defaultCharset());
		String version = "UNKNOWN"; //$NON-NLS-1$
		for (final String line : lines) {
			if (line.startsWith("$wp_version")) { //$NON-NLS-1$
				version = line.replaceAll(
						"\\$wp_version = '(\\d+\\.\\d+\\.\\d+)';", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
		}
		return version;
	}

	private static String getDBVersion() throws UtilsException {
		String result = null;
		final IDatabase databaseInfo = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.getDatabaseInfo();
		final String url = String.format(
				"jdbc:mysql://%s:%d/%s?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", //$NON-NLS-1$
				databaseInfo.getAddress(),
				Integer.valueOf(databaseInfo.getPort()),
				databaseInfo.getBaseName());
		try (final Connection connection = DriverManager.getConnection(url,
				databaseInfo.getLogin(), databaseInfo.getPassword());) {
			final DatabaseMetaData meta = connection.getMetaData();
			result = String.format("%s version %s with JDBC %s", //$NON-NLS-1$
					meta.getDatabaseProductName(),
					meta.getDatabaseProductVersion(), meta.getDriverVersion());
		} catch (final SQLException e) {
			throw new UtilsException(e);
		}
		return result;
	}

}
