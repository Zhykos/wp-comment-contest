package fr.zhykos.wp.commentcontest.tests.internal;

import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.IWordPressInformation;
import fr.zhykos.wp.commentcontest.tests.internal.utils.Utils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.IRunnableCondition;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils.Translations;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPlugin;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginCatalog;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTest;
import fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins.IWordPressPluginToTestFactory;

/*
 * http://atatorus.developpez.com/tutoriels/java/test-application-web-avec-selenium/
 */
public class WPCommentContestPluginTest {

	private static final String PAGE_LOAD_TIMEOUT = "30000"; //$NON-NLS-1$
	// XXX Constante INST_CHROME_DRV qui n'a rien à faire là
	private static final boolean INST_CHROME_DRV = Utils
			.getBooleanSystemProperty(WPCommentContestPluginTest.class.getName()
					+ ".installchromedriver", true); //$NON-NLS-1$
	private static final int FAKE_COMMENTS_NB = 5;

	private final IWordPressPlugin wpRssPlg;
	private final IWordPressPlugin fakerPlg;
	private final IWordPressPluginToTest myPlugin;

	private WebDriver driver;
	private Selenium selenium;
	private IWordPressInformation wpInfo;

	public WPCommentContestPluginTest() throws UtilsException {
		this.wpRssPlg = IWordPressPluginCatalog.DEFAULT
				.getPlugin("wp-rss-aggregator"); //$NON-NLS-1$
		this.fakerPlg = IWordPressPluginCatalog.DEFAULT.getPlugin("fakerpress"); //$NON-NLS-1$
		this.myPlugin = IWordPressPluginToTestFactory.DEFAULT.getPlugin(
				"comment-contest", new String[] { "css/comment-contest.css" }, //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "js/OrgZhyweb_WPCommentContest_jQuery.js" }, //$NON-NLS-1$
				new String[] {});
	}

	// TODO Passer en beforeclass et afterclass !!!!!!!!!!!!!!!!!!!!!!! ca
	// évitera de faire les init pour chaque navigateur
	@Before
	public void before() throws UtilsException {
		// TODO faire une méthode utilitaire pour l'initialisation globale afin
		// de créer un framework générique de tests pour wordpress
		final boolean cleanWorkspace = Utils.getBooleanSystemProperty(
				getClass().getName() + ".cleanworkspace", true); //$NON-NLS-1$
		if (cleanWorkspace) {
			Utils.cleanWorkspace();
		}
		final boolean installWordPress = Utils.getBooleanSystemProperty(
				getClass().getName() + ".installwordpress", true); //$NON-NLS-1$
		if (installWordPress) {
			Utils.installWordPress();
		}
		// XXX startServer c'est nul comme nom...
		this.wpInfo = Utils.startServer(INST_CHROME_DRV, this.myPlugin,
				new IWordPressPlugin[] { this.wpRssPlg, this.fakerPlg });
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
	@Ignore
	@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
	/*
	 * @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert") tcicognani:
	 * Assertions are in another method
	 */
	public void testPluginInstallAndGlobalFeatures() throws UtilsException {
		Utils.installChromeDriver(INST_CHROME_DRV);
		this.driver = new ChromeDriver(); // XXX test other browsers
		this.selenium = new WebDriverBackedSelenium(this.driver,
				this.wpInfo.getTestServer().getHomeURL());
		final String homeURL = this.wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL);
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, this.wpInfo.getWebsiteName());
		WpHtmlUtils.connect((ChromeDriver) this.driver, this.selenium,
				this.wpInfo);
		addFakeComments(); // XXX beforeclass car prérequis pour tous les tests
		testPluginCommentPage();
		testPluginArticlePage();
	}

	private void testPluginArticlePage() throws UtilsException {
		final String homeURL = this.wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL + "/wp-admin/edit.php"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, Translations.articles);
		final String commentsNb = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='comments column-comments']/div/a/span[@class='comment-count-approved']")) //$NON-NLS-1$
				.getText();
		Assert.assertEquals(FAKE_COMMENTS_NB + 1, Integer.parseInt(commentsNb));
		final String contestLinkTxt = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"))
				.getText();
		Assert.assertEquals("Lancer le concours", contestLinkTxt);
		WpHtmlUtils.expandSettingsScreenMenu(this.driver, this.selenium);
		this.selenium.uncheck("id=orgZhyweb-wpCommentContest-hide");
		final List<WebElement> contestColumnElts = this.driver.findElements(By
				.xpath("//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"));
		Assert.assertTrue(contestColumnElts.isEmpty());
		this.selenium.check("id=orgZhyweb-wpCommentContest-hide");
		final WebElement contestLink = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"));
		Assert.assertEquals("Lancer le concours", contestLink.getText());
		final String articleName = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='title column-title has-row-actions column-primary page-title']/strong/a")) //$NON-NLS-1$
				.getText();
		this.selenium.open(contestLink.getAttribute("href")); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
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
		Assert.assertEquals("Comment Contest", link);
		// Check plugin page
		final String homeURL = this.wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL
				+ "/wp-admin/edit-comments.php?page=orgZhyweb-wpCommentContest");
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH2Tag(this.driver, "Comment Contest");
		testReport();
	}

	private void testReport() {
		// TODO Auto-generated method stub
	}

	protected WebDriver getDriver() {
		return this.driver;
	}

	private void addFakeComments() throws UtilsException {
		// Generate fakes
		final String homeURL = this.wpInfo.getTestServer().getHomeURL();
		this.selenium.open(
				homeURL + "/wp-admin/admin.php?page=fakerpress&view=comments"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		this.selenium.type("id=fakerpress-field-qty-min", //$NON-NLS-1$
				String.valueOf(FAKE_COMMENTS_NB));
		this.selenium.uncheck("id=fakerpress-field-use_html-1"); //$NON-NLS-1$
		this.driver
				.findElement(
						By.xpath("//body//form[@class='fp-module-generator']")) //$NON-NLS-1$
				.submit();
		final IRunnableCondition condition = new IRunnableCondition() {
			@Override
			public boolean run() {
				final List<WebElement> foundElements = getDriver()
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
		this.selenium
				.open(homeURL + "/wp-admin/comment.php?action=editcomment&c=2"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, Translations.editComment);
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		this.selenium.type("id=jj", //$NON-NLS-1$
				Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
		this.selenium.select("id=mm", //$NON-NLS-1$
				"index=" + Integer.toString(cal.get(Calendar.MONTH))); //$NON-NLS-1$
		this.selenium.type("id=aa", //$NON-NLS-1$
				Integer.toString(cal.get(Calendar.YEAR)));
		final String articleName = this.driver.findElement(By.xpath(
				"//div[@id='misc-publishing-actions']/div[@class='misc-pub-section misc-pub-response-to']/b/a")) //$NON-NLS-1$
				.getText();
		this.selenium.submit("id=post"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, Translations.commentsOnArticle,
				articleName);
	}

	@Test(timeout = 200000)
	public void testCommentsInTable() throws UtilsException {
		Utils.installChromeDriver(INST_CHROME_DRV);
		this.driver = new ChromeDriver(); // XXX test other browsers
		this.selenium = new WebDriverBackedSelenium(this.driver,
				this.wpInfo.getTestServer().getHomeURL());
		WpHtmlUtils.connect((ChromeDriver) this.driver, this.selenium,
				this.wpInfo);
		addFakeComments(); // XXX beforeclass car prérequis pour tous les tests
		final String homeURL = this.wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL + "/wp-admin/edit.php"); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.assertH1Tag(this.driver, Translations.articles);
		final WebElement contestLink = this.driver.findElement(By.xpath(
				"//tr[@id='post-1']/td[@class='orgZhyweb-wpCommentContest column-orgZhyweb-wpCommentContest']/a"));
		this.selenium.open(contestLink.getAttribute("href")); //$NON-NLS-1$
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		this.driver.findElement(By.xpath("//div[@id='contestForm']/table")); //$NON-NLS-1$
		final List<WebElement> linesElt = this.driver.findElements(
				By.xpath("//div[@id='contestForm']/table/tbody/tr")); //$NON-NLS-1$
		// n fake comments plus one from install and one invisible fake
		Assert.assertEquals(FAKE_COMMENTS_NB + 2, linesElt.size());
		final String firstLineId = linesElt.get(0).getAttribute("id"); //$NON-NLS-1$
		Assert.assertEquals("comment-contest-not-found-tr", firstLineId); //$NON-NLS-1$
		String bckColorAlt1 = null;
		String bckColorAlt2 = null;
		for (int i = 1; i < linesElt.size(); i++) {
			final WebElement lineElt = linesElt.get(i);
			final String eltId = lineElt.getAttribute("id"); //$NON-NLS-1$
			Assert.assertTrue(eltId.matches("comment-contest-\\d")); //$NON-NLS-1$
			// Check alternate colors
			final String bckColor = lineElt.getCssValue("background-color"); //$NON-NLS-1$
			if (i % 2 == 0) {
				if (bckColorAlt1 == null) {
					bckColorAlt1 = bckColor;
				} else {
					Assert.assertEquals(bckColorAlt1, bckColor);
				}
			} else {
				if (bckColorAlt2 == null) {
					bckColorAlt2 = bckColor;
				} else {
					Assert.assertEquals(bckColorAlt2, bckColor);
				}
			}
		}
		// Assert.assertNotEquals(bckColorAlt2, bckColorAlt1); // FIXME
		// Special links in table
		final List<WebElement> actionSpans = this.driver.findElements(By.xpath(
				"//tr[@id='comment-contest-1']/td[@class='comment column-comment']/div[@class='row-actions']/span")); //$NON-NLS-1$
		Assert.assertEquals(4, actionSpans.size());
		final String span1 = actionSpans.get(0).getAttribute("class"); //$NON-NLS-1$
		Assert.assertEquals("delete", span1); //$NON-NLS-1$
		final String span2 = actionSpans.get(1).getAttribute("class"); //$NON-NLS-1$
		Assert.assertEquals("restore", span2); //$NON-NLS-1$
		final String span3 = actionSpans.get(2).getAttribute("class"); //$NON-NLS-1$
		Assert.assertEquals("cheat", span3); //$NON-NLS-1$
		final String span4 = actionSpans.get(3).getAttribute("class"); //$NON-NLS-1$
		Assert.assertEquals("stopcheat", span4); //$NON-NLS-1$
		// Check columns
		this.driver.findElement(By.xpath(
				"//tr[@id='comment-contest-1']/th[@class='check-column']/input[@type='checkbox']")); //$NON-NLS-1$
		final List<WebElement> columns = this.driver
				.findElements(By.xpath("//tr[@id='comment-contest-1']/td")); //$NON-NLS-1$
		Assert.assertEquals(2, columns.size());
	}

	@After
	public void after() throws UtilsException {
		// XXX After class
		if (this.driver != null) {
			this.driver.quit();
		}
		if (this.wpInfo != null) {
			try {
				this.wpInfo.getTestServer().stop();
			} catch (final Exception e) {
				fail(e.getMessage());
			}
		}
		// TODO Utiliser la classe EMF Facet de vérification d'erreur lors du
		// test unitaire pour savoir si on peut packager
		// Utils.packagePlugin(this.myPlugin);
		// fail();
	}

}
