package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import org.openqa.selenium.WebDriver;

import fr.zhykos.wp.commentcontest.tests.internal.utils.WpHtmlUtils;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class PluginFakerPress implements IWordPressPlugin {

	@Override
	public String getName() {
		return "FakerPress"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "fakerpress"; //$NON-NLS-1$
	}

	@Override
	public void defaultActivationAction(final WebDriver driver) {
		WpHtmlUtils.assertDefaultPluginActivation(driver);
	}

}
