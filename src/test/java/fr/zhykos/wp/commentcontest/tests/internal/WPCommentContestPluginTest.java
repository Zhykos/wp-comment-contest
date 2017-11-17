package fr.zhykos.wp.commentcontest.tests.internal;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import fr.zhykos.wp.commentcontest.tests.internal.utils.IWordPressInformation;
import fr.zhykos.wp.commentcontest.tests.internal.utils.Utils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils;
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

	private final IWordPressPlugin wpRssPlg;
	private final IWordPressPluginToTest myPlugin;

	private WebDriver driver;
	private Selenium selenium;
	private IWordPressInformation wpInfo;

	public WPCommentContestPluginTest() throws UtilsException {
		this.wpRssPlg = IWordPressPluginCatalog.DEFAULT
				.getPlugin("wp-rss-aggregator"); //$NON-NLS-1$
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
		this.wpInfo = Utils.startServer(INST_CHROME_DRV, this.myPlugin,
				new IWordPressPlugin[] { this.wpRssPlg });
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
	@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
	/*
	 * @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert") tcicognani:
	 * Assertions are in another method
	 */
	public void chromeTest() throws UtilsException {
		Utils.installChromeDriver(INST_CHROME_DRV);
		this.driver = new ChromeDriver();
		this.selenium = new WebDriverBackedSelenium(this.driver,
				this.wpInfo.getTestServer().getHomeURL());
		testPlugin();
	}

	private void testPlugin() throws UtilsException {
		final String homeURL = this.wpInfo.getTestServer().getHomeURL();
		this.selenium.open(homeURL);
		this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		WpHtmlUtils.checkH1Tag(this.driver, this.wpInfo.getWebsiteName());
		WpHtmlUtils.connect((ChromeDriver) this.driver, this.selenium,
				this.wpInfo);
		WpHtmlUtils.activatePlugins(this.selenium, this.driver, homeURL,
				new IWordPressPlugin[] { this.myPlugin, this.wpRssPlg });
		// l'activation peut se faire dans l'install maintenant que l'on a une
		// méthode par défaut d'activation !
	}

	@After
	public void after() throws UtilsException {
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
		// test unitaire
		// Utils.packagePlugin(this.myPlugin);
		fail();
	}

}
