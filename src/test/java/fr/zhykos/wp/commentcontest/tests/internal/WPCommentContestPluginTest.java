package fr.zhykos.wp.commentcontest.tests.internal;

import static org.junit.Assert.fail;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/*
 * http://atatorus.developpez.com/tutoriels/java/test-application-web-avec-selenium/
 */
public class WPCommentContestPluginTest {

	// private static final String PAGE_LOAD_TIMEOUT = "100";
	// private static final int HTML_UNIT_DRIVER = 0;
	// private static final int FIREFOX_DRIVER = 1;
	// private static final int OPERA_DRIVER = 2;
	// private static final int CHROME_DRIVER = 3;

	private final String chromeDriverPath;
	// private final StringBuilder errorsBuffer = new StringBuilder();

	private final boolean hasError = false;
	// private int currentDriver;
	private WebDriver driver;
	// private String baseUrl;
	// private Selenium selenium;
	// private Monitor monitor;
	private Server server;

	public WPCommentContestPluginTest() throws UtilsException {
		this.chromeDriverPath = Utils
				.getSystemProperty(getClass().getName() + ".chromedriverpath"); //$NON-NLS-1$
	}

	@Before
	public void before() throws TestException {
		try {
			Utils.cleanWorkspace();
			Utils.installWordPressAndPlugin();
			this.server = Utils.startJetty();
			// final Properties properties = System.getProperties();
			// this.baseUrl = properties.getProperty("base.url",
			// "http://127.0.0.1:8080/tutoselenium/");
		} catch (final UtilsException e) {
			throw new TestException(e);
		}
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

	@Test
	@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
	/*
	 * @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert") tcicognani:
	 * Assertions are in another method
	 */
	public void chromeTest() {
		System.setProperty("webdriver.chrome.driver", this.chromeDriverPath); //$NON-NLS-1$
		this.driver = new ChromeDriver();
		// this.currentDriver = CHROME_DRIVER;
		// this.selenium = new WebDriverBackedSelenium(this.driver,
		// this.baseUrl);
		testSelenium();
	}

	private void testSelenium() {
		// // Connection
		// this.driver.get(this.baseUrl + "faces/page1.xhtml");
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// // Vérification
		// checkHeader(Locale.FRENCH);
		// checkPageUne(false, Locale.FRENCH);
		// checkFooter(); // On vient de se connecter, on n'affiche donc pas le
		// // numéro de la page
		// // précédente
		//
		// // Avant d'aller page 2, on provoque une erreur
		// this.driver.findElement(By.id("contentForm:pageText")).clear();
		// this.driver.findElement(By.id("contentForm:pageText")).sendKeys("4");
		// this.driver.findElement(By.id("contentForm:nextPage")).click();
		//
		// checkPageUne(true, Locale.FRENCH);
		//
		// // On va page 2
		// this.driver.findElement(By.id("contentForm:pageText")).clear();
		// this.driver.findElement(By.id("contentForm:pageText")).sendKeys("2");
		// this.driver.findElement(By.id("contentForm:nextPage")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// // vérification
		// checkHeader(Locale.FRENCH);
		// checkPageDeux(Locale.FRENCH);
		// checkFooter("page une", Locale.FRENCH); // On vient de la page 1 en
		// // français
		//
		// // On va page 3
		// this.driver.findElement(By.id("contentForm:page3Button")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// // vérification
		// checkHeader(Locale.FRENCH);
		// checkPageTrois(Locale.FRENCH);
		// checkFooter("page deux", Locale.FRENCH);
		//
		// // on retourne page 1
		// new
		// Select(this.driver.findElement(By.id("contentForm:pageList_input")))
		// .selectByValue("1");
		// this.driver.findElement(By.id("contentForm:nextPageButton")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		//
		// checkFooter("page trois", Locale.FRENCH);
		// // On passe en anglais
		// this.driver.findElement(By.id("headerForm:english_button")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// // Vérification
		// checkHeader(Locale.ENGLISH);
		// checkPageUne(false, Locale.ENGLISH);
		// checkFooter("page three", Locale.ENGLISH);
		//
		// // On va page 2
		// this.driver.findElement(By.id("contentForm:pageText")).clear();
		// this.driver.findElement(By.id("contentForm:pageText")).sendKeys("2");
		// this.driver.findElement(By.id("contentForm:nextPage")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// // vérification
		// checkHeader(Locale.ENGLISH);
		// checkPageDeux(Locale.ENGLISH);
		// checkFooter("page one", Locale.ENGLISH);
		//
		// // On va page 3
		// this.driver.findElement(By.id("contentForm:page3Button")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// // vérification
		// checkHeader(Locale.ENGLISH);
		// checkPageTrois(Locale.ENGLISH);
		// checkFooter("page two", Locale.ENGLISH);
		//
		// // Retour vers la page 1
		// new
		// Select(this.driver.findElement(By.id("contentForm:pageList_input")))
		// .selectByVisibleText("page1");
		// this.driver.findElement(By.id("contentForm:nextPageButton")).click();
		// this.selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
		// checkFooter("page three", Locale.ENGLISH);
	}

	// private void checkPageTrois(final Locale locale) {
	// checkElement("contentForm", "pageTitle",
	// locale == Locale.FRENCH ? "Page trois" : "Page three");
	// checkPanelTitle("contentForm", "panel", "ui-panelgrid-header",
	// locale == Locale.FRENCH ? "Choix de la nouvelle page"
	// : "Select the new page");
	//
	// checkElement("contentForm", "label",
	// locale == Locale.FRENCH ? "Choisissez la nouvelle page :"
	// : "Select the page :");
	// checkElement("contentForm", "nextPageButton",
	// locale == Locale.FRENCH ? "Page suivante" : "Next page");
	// final Select select = new Select(
	// this.driver.findElement(By.id("contentForm:pageList_input")));
	// final List<WebElement> options = select.getOptions();
	// assertThat(options.get(0).getText(), is("page1"));
	// assertThat(options.get(1).getText(), is("page2"));
	// assertThat(options.get(2).getText(), is("page3"));
	// }
	//
	// private void checkFooter() {
	// assertThat(isElementPresent(By.id("previousPage")), is(false));
	// }
	//
	// private void checkFooter(final String fromPage, final Locale locale) {
	// if (locale == Locale.FRENCH) {
	// checkElement("previousPage", "Vous venez de la " + fromPage);
	// } else {
	// checkElement("previousPage", "You are coming from " + fromPage);
	// }
	// }
	//
	// private void checkElement(final String elementId, final String expected)
	// {
	// assertThat(this.driver.findElement(By.id(elementId)).getText(),
	// is(expected));
	// }
	//
	// private void checkElement(final String parentId, final String elementId,
	// final String expected) {
	// checkElement(parentId + ":" + elementId, expected);
	// }
	//
	// private void checkPageDeux(final Locale locale) {
	// checkElement("contentForm", "pageTitle",
	// locale == Locale.FRENCH ? "Page deux" : "Page two");
	// checkPanelTitle("contentForm", "panel", "ui-panel-title",
	// locale == Locale.FRENCH ? "Choix de la prochaine page"
	// : "Select the new page");
	//
	// checkElement("contentForm", "page1Button",
	// locale == Locale.FRENCH ? "Page une" : "Page one");
	// checkElement("contentForm", "page2Button",
	// locale == Locale.FRENCH ? "Page deux" : "Page two");
	// checkElement("contentForm", "page3Button",
	// locale == Locale.FRENCH ? "Page trois" : "Page three");
	// }
	//
	// private void checkPageUne(final boolean errorMessage, final Locale
	// locale) {
	// checkElement("contentForm", "pageTitle",
	// locale == Locale.FRENCH ? "Page une" : "Page one");
	// checkPanelTitle("contentForm", "panel", "ui-panel-title",
	// locale == Locale.FRENCH ? "Choix de la prochaine page"
	// : "Select the new page");
	//
	// checkElement("contentForm", "label",
	// locale == Locale.FRENCH ? "Numéro de la prochaine page"
	// : "Number of next page :");
	// checkElement("contentForm", "nextPage",
	// locale == Locale.FRENCH ? "Page suivante" : "Next page");
	//
	// if (errorMessage) {
	// checkElement("contentForm", "pageError",
	// locale == Locale.FRENCH
	// ? "Vous devez entrer une valeur entre un et trois."
	// : "You must enter a number between one and three.");
	// final String color = this.driver
	// .findElement(By.id("contentForm:pageError"))
	// .getCssValue("color");
	// switch (this.currentDriver) {
	// case CHROME_DRIVER:
	// case FIREFOX_DRIVER:
	// case OPERA_DRIVER:
	// assertThat(color, is("rgba(255, 0, 0, 1)"));
	// break;
	// default:
	// assertThat(color, is("red"));
	// break;
	// }
	// } else {
	// switch (this.currentDriver) {
	// case FIREFOX_DRIVER:
	// case OPERA_DRIVER:
	// case CHROME_DRIVER:
	// assertThat(
	// this.driver.findElement(By.id("contentForm:pageError"))
	// .isDisplayed(),
	// is(false));
	// break;
	// default:
	// checkElement("contentForm", "pageError", "");
	// }
	// }
	// }
	//
	// private void checkPanelTitle(final String parentId, final String panelId,
	// final String titleClass, final String expectedTitle) {
	// final WebElement panel = this.driver
	// .findElement(By.id(parentId + ":" + panelId));
	// final WebElement panelTitle = panel
	// .findElement(By.className(titleClass));
	// assertThat(panelTitle.getText(), is(expectedTitle));
	// }
	//
	// private void checkHeader(final Locale locale) {
	// final WebElement title = this.driver.findElement(By.tagName("h1"));
	// assertThat(title.getText(),
	// is(locale == Locale.FRENCH ? "En tête" : "Header"));
	// String drapeau = this.driver
	// .findElement(By.id("headerForm:english_button"))
	// .getCssValue("background-image");
	// assertThat(drapeau, is(buildUrl("drapeau_anglais.png")));
	// drapeau = this.driver.findElement(By.id("headerForm:french_button"))
	// .getCssValue("background-image");
	// assertThat(drapeau, is(buildUrl("drapeau_francais.png")));
	// }
	//
	// private String buildUrl(final String resource) {
	// switch (this.currentDriver) {
	// case CHROME_DRIVER:
	// return "url(" + this.baseUrl + "faces/javax.faces.resource/"
	// + resource + "?ln=images)";
	// case FIREFOX_DRIVER:
	// case OPERA_DRIVER:
	// return "url(\"" + this.baseUrl + "faces/javax.faces.resource/"
	// + resource + "?ln=images\")";
	// default:
	// final String[] split = this.baseUrl.split("/");
	// String root = "";
	// if (split.length == 4) {
	// root = "/" + split[3];
	// }
	// return "url(" + root + "/faces/javax.faces.resource/" + resource
	// + "?ln=images)";
	// }
	// }

	@After
	public void after() throws TestException {
		if (this.driver != null) {
			this.driver.quit();
		}
		// final String error = this.errorsBuffer.toString();
		// if (!error.isEmpty()) {
		if (this.hasError) {
			fail("FAIL");
		}
		if (this.server != null) {
			try {
				this.server.stop();
			} catch (final Exception e) {
				fail(e.getMessage());
				throw new TestException(e);
			}
		}
	}

	// private boolean isElementPresent(final By byElement) {
	// boolean result = true;
	// try {
	// this.driver.findElement(byElement);
	// } catch (final NoSuchElementException e) {
	// result = false;
	// }
	// return result;
	// }

}
