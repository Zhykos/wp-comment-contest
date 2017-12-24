package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import org.openqa.selenium.WebDriver;

// TODO Pouvoir �tendre cette interface pour ajouter du comportement sp�cifique pour certains plugins (ex: ajouter des commentaires pour faker press)
public interface IWordPressPlugin {

	String getName();

	String getId();

	void defaultActivationAction(WebDriver driver);

}
