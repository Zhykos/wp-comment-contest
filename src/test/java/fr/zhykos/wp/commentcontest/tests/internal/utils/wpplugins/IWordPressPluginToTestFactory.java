package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

// Factory pattern
public interface IWordPressPluginToTestFactory {

	IWordPressPluginToTestFactory DEFAULT = new WordPressPluginToTestFactory();

	IWordPressPluginToTest getPlugin(String pluginId, String[] cssToMini,
			String[] jsToMini, String[] filesToRemove) throws UtilsException;

	IWordPressPluginToTest getPluginLocalDirParameter(String pluginId,
			String parameter, String[] cssToMini, String[] jsToMini,
			String[] filesToRemove) throws UtilsException;

	IWordPressPluginToTest getPluginLocalDir(String pluginId, File localDir,
			String[] cssToMini, String[] jsToMini, String[] filesToRemove)
			throws UtilsException;

}
