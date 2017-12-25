package fr.zhykos.wp.commentcontest.tests.internal;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.BrowserUtils;
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

	private static IWordPressPlugin wpRssPlg;
	private static IWordPressPlugin fakerPlg;
	private static IWordPressPluginToTest myPlugin;
	private static IWordPressInformation wpInfo;

	private WebDriver driver;
	private Selenium selenium;

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
		wpInfo = fr.zhykos.wp.commentcontest.tests.internal.utils.Utils
				.startServer(myPlugin,
						new IWordPressPlugin[] { wpRssPlg, fakerPlg });
		Utils.addFakeComments(wpInfo);
	}

	// @Test
	// public void firefoxTest() throws Exception {
	// driver = new FirefoxDriver();
	// currentDriver = FIREFOX_DRIVER;
	// selenium = new WebDriverBackedSelenium(driver, baseUrl);
	// testSelenium();
	// }

	// @Test
	// public void htmlUnitTest() throws Exception {
	// driver = new HtmlUnitDriver(true);
	// currentDriver = HTML_UNIT_DRIVER;
	// selenium = new WebDriverBackedSelenium(driver, baseUrl);
	// testSelenium();
	// }

	// @Test
	// public void operaTest() throws Exception {
	// driver = new OperaDriver();
	// currentDriver = OPERA_DRIVER;
	// selenium = new WebDriverBackedSelenium(driver, baseUrl);
	// testSelenium();
	// }

	@Test // (timeout = 60000) XXX
	@Disabled
	@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
	/*
	 * @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert") tcicognani:
	 * Assertions are in another method
	 */
	public void testPluginInstallAndGlobalFeatures() throws UtilsException {
		this.driver = BrowserUtils.createChromeDriver(); // XXX test other browsers
		this.selenium = new WebDriverBackedSelenium(this.driver,
				wpInfo.getTestServer().getHomeURL());
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL);
		this.selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, wpInfo.getWebsiteName());
		WpHtmlUtils.connect(this.driver, this.selenium, wpInfo);
		testPluginCommentPage();
		testPluginArticlePage();
	}

	private void testPluginArticlePage() throws UtilsException {
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL + "/wp-admin/edit.php"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, Translations.articles);
		final String commentsNb = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='comments column-comments']/div/a/span[@class='comment-count-approved']")) //$NON-NLS-1$
				.getText();
		Assertions.assertEquals(Utils.FAKE_COMMENTS_NB + 1,
				Integer.parseInt(commentsNb));
		final String contestLinkTxt = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"))
				.getText();
		Assertions.assertEquals("Lancer le concours", contestLinkTxt);
		WpHtmlUtils.expandSettingsScreenMenu(this.driver, this.selenium);
		this.selenium.uncheck("id=orgZhyweb-wpCommentContest-hide");
		final List<WebElement> contestColumnElts = this.driver.findElements(By
				.xpath("//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"));
		Assertions.assertTrue(contestColumnElts.isEmpty());
		this.selenium.check("id=orgZhyweb-wpCommentContest-hide");
		final WebElement contestLink = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"));
		Assertions.assertEquals("Lancer le concours", contestLink.getText());
		final String articleName = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='title column-title has-row-actions column-primary page-title']/strong/a")) //$NON-NLS-1$
				.getText();
		this.selenium.open(contestLink.getAttribute("href")); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH2Tag(this.driver, "Comment Contest");
		WpHtmlUtils.assertH3Tag(this.driver,
				String.format("Concours pour l'article \"%s\"", articleName));
	}

	private void testPluginCommentPage() throws UtilsException {
		// Test if plugin link if a comment sub menu
		WpHtmlUtils.expandAdminMenu(this.driver, this.selenium);
		final String link = this.driver.findElement(By.xpath(
				"//li[@id='menu-comments']/ul/li/a[@href='edit-comments.php?page=orgZhyweb-wpCommentContest']"))
				.getText();
		Assertions.assertEquals("Comment Contest", link);
		// Check plugin page
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL
				+ "/wp-admin/edit-comments.php?page=orgZhyweb-wpCommentContest");
		this.selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH2Tag(this.driver, "Comment Contest");
		testReport();
	}

	private void testReport() {
		// TODO Auto-generated method stub
	}

	// @Disabled
	@Test // (timeout = 200000) XXX
	@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
	/*
	 * @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert") tcicognani: There
	 * are JUnit 5 assertions!
	 */
	public void testCommentsInTable() throws UtilsException {
		this.driver = BrowserUtils.createChromeDriver(); // XXX test other browsers
		this.selenium = new WebDriverBackedSelenium(this.driver,
				wpInfo.getTestServer().getHomeURL());
		WpHtmlUtils.connect(this.driver, this.selenium, wpInfo);
		final String homeURL = wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL + "/wp-admin/edit.php"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		final WebElement contestLink = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"));
		this.selenium.open(contestLink.getAttribute("href")); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(Utils.PAGE_LOAD_TIMEOUT);
		this.driver.findElement(By.xpath("//div[@id='contestForm']/table")); //$NON-NLS-1$
		final List<WebElement> linesElt = this.driver.findElements(
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
		final List<WebElement> actionSpans = this.driver.findElements(By.xpath(
				"//tr[@id='comment-contest-1']/td[@class='comment column-comment']/div[@class='row-actions']/span")); //$NON-NLS-1$
		Assertions.assertEquals(4, actionSpans.size());
		final String span1 = actionSpans.get(0).getAttribute("class"); //$NON-NLS-1$
		Assertions.assertEquals("delete", span1); //$NON-NLS-1$
		final String span2 = actionSpans.get(1).getAttribute("class"); //$NON-NLS-1$
		Assertions.assertEquals("restore", span2); //$NON-NLS-1$
		final String span3 = actionSpans.get(2).getAttribute("class"); //$NON-NLS-1$
		Assertions.assertEquals("cheat", span3); //$NON-NLS-1$
		final String span4 = actionSpans.get(3).getAttribute("class"); //$NON-NLS-1$
		Assertions.assertEquals("stopcheat", span4); //$NON-NLS-1$
		// Check columns
		this.driver.findElement(By.xpath(
				"//tr[@id='comment-contest-1']/th[@class='check-column']/input[@type='checkbox']")); //$NON-NLS-1$
		final List<WebElement> columns = this.driver
				.findElements(By.xpath("//tr[@id='comment-contest-1']/td")); //$NON-NLS-1$
		Assertions.assertEquals(2, columns.size());
	}

	@AfterEach
	public void afterEach() {
		if (this.driver != null) {
			this.driver.quit();
		}
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
	}

}
