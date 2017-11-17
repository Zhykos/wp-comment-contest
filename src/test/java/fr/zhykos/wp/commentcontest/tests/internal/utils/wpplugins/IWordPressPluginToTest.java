package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import java.io.File;

public interface IWordPressPluginToTest extends IWordPressPlugin {

	File getLocalPluginDirectory();

	String[] packagingCSSToMini();

	String[] packagingJavascriptToMini();

	File[] packagingFilesToRemove();

}
