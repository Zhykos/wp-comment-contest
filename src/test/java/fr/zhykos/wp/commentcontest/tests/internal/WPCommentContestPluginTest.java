package fr.zhykos.wp.commentcontest.tests.internal;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.opera.OperaDriver;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.BrowserUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.BrowserUtils.ErrorDriver;
import fr.zhykos.wp.commentcontest.tests.internal.utils.IWordPressInformation;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.Translations;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPlugin;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginCatalog;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTest;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTestFactory;

/*
 * http://atatorus.developpez.com/tutoriels/java/test-application-web-avec-selenium/
 */
public class WPCommentContestPluginTest {

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
	private static final String TEST_ON_BROWSER = "test on browser '%s'"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String CONTEST_LK_XPATH = "//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"; //$NON-NLS-1$
	private static final String STATIC_METHOD = "static-method"; //$NON-NLS-1$
	private static final String BORDER_ERROR = "border: 2px solid red;"; //$NON-NLS-1$
	private static IWordPressPlugin wpRssPlg;
	private static IWordPressPlugin fakerPlg;
	private static IWordPressPluginToTest myPlugin;
	private static IWordPressInformation wpInfo;

	@BeforeAll
	public static void beforeAll() throws UtilsException {
		wpRssPlg = IWordPressPluginCatalog.DEFAULT
				.getPlugin("wp-rss-aggregator"); //$NON-NLS-1$
		fakerPlg = IWordPressPluginCatalog.DEFAULT.getPlugin("fakerpress"); //$NON-NLS-1$
		myPlugin = IWordPressPluginToTestFactory.DEFAULT.getPlugin(
				"comment-contest", new String[] { "css/comment-contest.css" }, //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "js/OrgZhyweb_WPCommentContest_jQuery.js" }, //$NON-NLS-1$
				new String[] {});

		// TODO faire une méthode utilitaire pour l'initialisation globale afin
		// de créer un framework générique de tests pour wordpress
		final boolean cleanWorkspace = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.getBooleanSystemProperty(
						WPCommentContestPluginTest.class.getName()
								+ ".cleanworkspace", //$NON-NLS-1$
						true);
		if (cleanWorkspace) {
			fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
					.cleanWorkspace();
		}
		final boolean installWordPress = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.getBooleanSystemProperty(
						WPCommentContestPluginTest.class.getName()
								+ ".installwordpress", //$NON-NLS-1$
						true);
		if (installWordPress) {
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
		selenium.open(homeURL + "/wp-admin/edit.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(driver, Translations.articles);
		final String commentsNb = driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='comments column-comments']/div/a/span[@class='comment-count-approved']")) //$NON-NLS-1$
				.getText();
		Assertions.assertEquals(Utils.FAKE_COMMENTS_NB + 1,
				Integer.parseInt(commentsNb));
		final String contestLinkTxt = driver
				.findElement(By.xpath(CONTEST_LK_XPATH)).getText();
		Assertions.assertEquals("Lancer le concours", contestLinkTxt);
		WpHtmlUtils.expandSettingsScreenMenu(driver, selenium);
		selenium.uncheck("id=orgZhyweb-wpCommentContest-hide"); //$NON-NLS-1$
		selenium.click("id=screen-options-apply"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final List<WebElement> contestColumnElts = driver
				.findElements(By.xpath(CONTEST_LK_XPATH));
		Assertions.assertTrue(contestColumnElts.isEmpty());
		WpHtmlUtils.expandSettingsScreenMenu(driver, selenium);
		selenium.check("id=orgZhyweb-wpCommentContest-hide"); //$NON-NLS-1$
		final WebElement contestLink = driver
				.findElement(By.xpath(CONTEST_LK_XPATH));
		Assertions.assertEquals("Lancer le concours", contestLink.getText());
		final String articleName = driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='title column-title has-row-actions column-primary page-title']/strong/a")) //$NON-NLS-1$
				.getText();
		selenium.click("id=screen-options-apply"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final WebElement contestLink2 = driver
				.findElement(By.xpath(CONTEST_LK_XPATH));
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
				"//li[@id='menu-comments']/ul/li/a[@href='edit-comments.php?page=orgZhyweb-wpCommentContest']")); //$NON-NLS-1$
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
				+ "/wp-admin/edit-comments.php?page=orgZhyweb-wpCommentContest"); //$NON-NLS-1$
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
		openCommentContestPluginOnArticleNumber1(driver);
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
		// Assert.assertNotEquals(bckColorAlt2, bckColorAlt1); // FIXME
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
				driver);
		final String nbWinners = selenium.getValue("id=zwpcc_nb_winners"); //$NON-NLS-1$
		Assertions.assertEquals(1, Integer.parseInt(nbWinners));
		Assertions.assertFalse(selenium.isVisible("id=dialog-modal-winners")); //$NON-NLS-1$
		launchContestThenAssertNbWinners(selenium, driver,
				Integer.parseInt(nbWinners));
		driver.findElement(By.xpath(
				"//div[@class='ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix ui-draggable-handle']/button")) //$NON-NLS-1$
				.click();
		Assertions.assertFalse(selenium.isVisible("id=dialog-modal-winners")); //$NON-NLS-1$
	}

	private static void launchContestThenAssertNbWinners(
			final Selenium selenium, final WebDriver driver,
			final int nbWinners) {
		scrollToY(driver, 0);
		driver.findElement(By.id("zwpcc_form_submit")).click(); //$NON-NLS-1$
		Assertions.assertTrue(selenium.isVisible("id=dialog-modal-winners")); //$NON-NLS-1$
		final List<WebElement> winnerLines = driver.findElements(By.xpath(
				"//div[@id='dialog-modal-winners']/table/tbody[@id='the-list-contest']/tr")); //$NON-NLS-1$
		int nbLinesVisible = 0;
		for (final WebElement line : winnerLines) {
			if (WpHtmlUtils.isVisible(line)) {
				nbLinesVisible++;
			}
		}
		Assertions.assertEquals(nbWinners, nbLinesVisible);
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
				driver);
		expandFilters(driver, selenium);
		Assertions.assertFalse(
				selenium.isVisible("id=zwpcc_dateFilter_error_message")); //$NON-NLS-1$
		submitThenAssertDateFieldsStyle(true, true, selenium, driver);
		selenium.click("id=datepicker"); //$NON-NLS-1$
		WpHtmlUtils.waitUntilVisibleStateByElementId(selenium, driver,
				"ui-datepicker-div", true, 10000); //$NON-NLS-1$
		final Calendar fakeDate = Utils.getDateSecondJanuary2018Noon();
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
		assertCommentsTable(driver, Utils.FAKE_COMMENTS_NB);
		launchContestThenAssertNbWinners(selenium, driver, 1);
		// XXX Je n'ai pas ajouté de tests en mettant des valeurs fausses...
	}

	private static void assertCommentsTable(final WebDriver driver,
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
		WpHtmlUtils.elementVisibilityBlock(driver, "filters"); //$NON-NLS-1$
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
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
		uncheckAllTable(selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 2);
		uncheckAllTable(selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "2"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 0);
		uncheckAllTable(selenium, driver);
		selenium.type(ID_ALIAS_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertAliasFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 1);
		Assertions.assertTrue(
				driver.findElement(By.id("cb-select-2")).isSelected()); //$NON-NLS-1$
		Assertions.assertEquals(Utils.getZhykosName(), driver.findElement(By
				.xpath("//tr[@id='comment-contest-2']/td[@class='author column-author has-row-actions column-primary']/strong")) //$NON-NLS-1$
				.getText());
		launchContestThenAssertNbWinners(selenium, driver, 1);
	}

	private static void uncheckAllTable(final Selenium selenium,
			final WebDriver driver)
			throws UtilsException {
		scrollToY(driver, 600);
		selenium.click("id=cb-select-all-1"); //$NON-NLS-1$
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			throw new UtilsException(e);
		}
		if (selenium.isChecked("id=cb-select-all-1")) { //$NON-NLS-1$
			selenium.click("id=cb-select-all-1"); //$NON-NLS-1$
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
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
		uncheckAllTable(selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 2);
		uncheckAllTable(selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "2"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 0);
		uncheckAllTable(selenium, driver);
		selenium.type(ID_EMAIL_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertEmailFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 1);
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
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
		uncheckAllTable(selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "0"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 2);
		uncheckAllTable(selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "2"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 0);
		uncheckAllTable(selenium, driver);
		selenium.type(ID_IPADRS_CONFIG, "1"); //$NON-NLS-1$
		submitThenAssertIpAddressFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 1);
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
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
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
		assertCommentsTable(driver, nbResult);
		uncheckAllTable(selenium, driver);
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
	public Collection<DynamicTest> testZZZ() {
		final Collection<DynamicTest> dynamicTests = new ArrayList<>();
		final List<WebDriver> allDrivers = BrowserUtils.createAllDrivers();
		for (final WebDriver webDriver : allDrivers) {
			final Executable exec = () -> initTestZZZ(webDriver);
			final String testName = String.format(TEST_ON_BROWSER, webDriver);
			final DynamicTest test = DynamicTest.dynamicTest(testName, exec);
			dynamicTests.add(test);
		}
		return dynamicTests;
	}

	private static void initTestZZZ(final WebDriver driver) {
		try {
			if (!(driver instanceof ErrorDriver)) {
				assertTestZZZ(driver);
			}
		} catch (final UtilsException e) {
			Assertions.fail(e);
		} finally {
			driver.quit();
		}
	}

	private static void assertTestZZZ(final WebDriver driver)
			throws UtilsException {
		final Selenium selenium = openCommentContestPluginOnArticleNumber1(
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
		selenium.uncheck("id=timeBetweenFilterName"); //$NON-NLS-1$
		selenium.uncheck("id=timeBetweenFilterEmail"); //$NON-NLS-1$
		selenium.uncheck("id=timeBetweenFilterIP"); //$NON-NLS-1$
		selenium.click(ID_PREFIX + "timeBetweenFilter"); //$NON-NLS-1$
		final boolean visible = WpHtmlUtils.isVisible(
				"zwpcc_timeBetweenFilter_error_message", selenium, driver); //$NON-NLS-1$
		Assertions.assertTrue(visible);
		uncheckAllTable(selenium, driver);
		submitThenAssertTimeBetweenField(selenium, driver, 0,
				"timeBetweenFilterName"); //$NON-NLS-1$
		submitThenAssertTimeBetweenField(selenium, driver, 1,
				"timeBetweenFilterEmail"); //$NON-NLS-1$
		submitThenAssertTimeBetweenField(selenium, driver, 1,
				"timeBetweenFilterIP"); //$NON-NLS-1$
		selenium.check("id=timeBetweenFilterName"); //$NON-NLS-1$
		selenium.check("id=timeBetweenFilterEmail"); //$NON-NLS-1$
		selenium.check("id=timeBetweenFilterIP"); //$NON-NLS-1$
		assertCommentsTable(driver, 0);
		uncheckAllTable(selenium, driver);
		selenium.uncheck("id=timeBetweenFilterEmail"); //$NON-NLS-1$
		selenium.uncheck("id=timeBetweenFilterIP"); //$NON-NLS-1$
		selenium.type(ID_TMEBTW_CONFIG, "1440"); //$NON-NLS-1$
		submitThenAssertTimeBetweenField(selenium, driver, 0,
				"timeBetweenFilterName"); //$NON-NLS-1$
		selenium.type(ID_TMEBTW_CONFIG, "1441"); //$NON-NLS-1$
		selenium.check("id=timeBetweenFilterName"); //$NON-NLS-1$
		selenium.check("id=timeBetweenFilterName"); //$NON-NLS-1$ A second check for fu***** Edge
		submitThenAssertTimeBetweenFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, 1);
		Assertions.assertTrue(selenium.isChecked("id=cb-select-2")); //$NON-NLS-1$
	}

	private static void submitThenAssertTimeBetweenField(
			final Selenium selenium, final WebDriver driver, final int nbResult,
			final String checkId) throws UtilsException {
		selenium.check("id=" + checkId); //$NON-NLS-1$
		submitThenAssertTimeBetweenFieldStyle(false, selenium, driver);
		assertCommentsTable(driver, nbResult);
		uncheckAllTable(selenium, driver);
		selenium.uncheck("id=" + checkId); //$NON-NLS-1$
	}

	private static void submitThenAssertTimeBetweenFieldStyle(
			final boolean mustHaveError, final Selenium selenium,
			final WebDriver driver) {
		submitThenAssertFieldStyle(mustHaveError, selenium, driver,
				"timeBetweenFilter", "zwpcc_timeBetweenFilter_error_message", //$NON-NLS-1$ //$NON-NLS-2$
				TIMEBTWN_CONFIG);
	}

	/*
	 * TODO Tests:
	 * - vérifier les tooltips
	 * - créer un deuxième article avec des commentaires et bien vérifier si on a les bons commentaires
	 * - lancer un concours de base plusieurs fois et voir si on a bien un commentaire de l'article et que le random fonctionne
	 * - tests en changeant les valeurs par défaut des configurations
	 * - test qui (dé)plie les filtres et vérifie qu'ils sont affichés
	 * - test pour vérifier que seuls les commentaires cochés sont bien mis en dans les résultats
	 */

	private static Selenium openCommentContestPluginOnArticleNumber1(
			final WebDriver driver) throws UtilsException {
		final Selenium selenium = new WebDriverBackedSelenium(driver,
				wpInfo.getTestServer().getHomeURL());
		WpHtmlUtils.connect(driver, selenium, wpInfo);
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		selenium.open(homeURL + "/wp-admin/edit.php"); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final WebElement contestLink = driver
				.findElement(By.xpath(CONTEST_LK_XPATH));
		selenium.open(contestLink.getAttribute("href")); //$NON-NLS-1$
		selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		return selenium;
	}

	@AfterAll
	public static void afterAll() {
		if (wpInfo != null) {
			try {
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

}
