package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

// Factory pattern
public interface IWordPressPluginCatalog {

	IWordPressPluginCatalog DEFAULT = new WordPressPluginCatalog();

	IWordPressPlugin getPlugin(String pluginId) throws UtilsException;

}
