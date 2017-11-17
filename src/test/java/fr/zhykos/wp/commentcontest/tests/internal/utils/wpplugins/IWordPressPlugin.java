package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import org.openqa.selenium.WebDriver;

public interface IWordPressPlugin {

	String getName();

	String getId();

	void defaultActivationAction(WebDriver driver);

}
