package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class PluginWPRSSAggregator implements IWordPressPlugin {

	private final static String REGEX = "Welcome to WP RSS Aggregator.*"; //$NON-NLS-1$

	@Override
	public String getName() {
		return "WP RSS Aggregator"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "wp-rss-aggregator"; //$NON-NLS-1$
	}

	@Override
	public void defaultActivationAction(final WebDriver driver)
			throws UtilsException {
		final String h1tag = driver.findElement(By.xpath("//body//div[@id='wpbody-content']//div[@class='wpra-wizard-head__title']")).getText(); //$NON-NLS-1$
		if (!h1tag.matches(REGEX)) {
			throw new UtilsException(String.format(
					"Wrong title tag '%s'! Maybe '%s' plugin activation failed (expected H1 tag regex: '%s')", //$NON-NLS-1$
					h1tag, getName(), REGEX));
		}
	}

}
