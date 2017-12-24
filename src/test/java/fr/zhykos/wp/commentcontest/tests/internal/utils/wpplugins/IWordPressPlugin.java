package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import org.openqa.selenium.WebDriver;

// TODO Pouvoir étendre cette interface pour ajouter du comportement spécifique pour certains plugins (ex: ajouter des commentaires pour faker press)
public interface IWordPressPlugin {

	String getName();

	String getId();

	void defaultActivationAction(WebDriver driver);

}
