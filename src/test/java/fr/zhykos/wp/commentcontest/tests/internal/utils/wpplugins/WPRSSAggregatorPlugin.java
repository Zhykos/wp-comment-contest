package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class WPRSSAggregatorPlugin implements IWordPressPlugin {

	private final static String REGEX = "Welcome to WP RSS Aggregator .*!"; //$NON-NLS-1$

	@Override
	public String getName() {
		return "WP RSS Aggregator"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "wp-rss-aggregator"; //$NON-NLS-1$
	}

	@Override
	public void defaultActivationAction(final WebDriver driver) {
		final String h1tag = driver.findElement(By.xpath("//h1")).getText(); //$NON-NLS-1$
		if (!h1tag.matches(REGEX)) {
			Assert.fail(String.format(
					"Wrong H1 tag '%s'! Maybe '%s' plugin activation failed (expected H1 tag regex: '%s')", //$NON-NLS-1$
					h1tag, getName(), REGEX));
		}
	}

}
