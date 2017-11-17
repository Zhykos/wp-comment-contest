package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class WordPressPluginToTestFactory implements IWordPressPluginToTestFactory {

	@Override
	public IWordPressPluginToTest getPlugin(final String pluginId,
			final String[] cssToMini, final String[] jsToMini,
			final String[] filesToRemove) throws UtilsException {
		return getPluginLocalDirParameter(pluginId, null, cssToMini, jsToMini,
				filesToRemove);
	}

	@Override
	public IWordPressPluginToTest getPluginLocalDirParameter(
			final String pluginId, final String parameter,
			final String[] cssToMini, final String[] jsToMini,
			final String[] filesToRemove) throws UtilsException {
		final String localPlgDirStr = System.getProperty(
				IWordPressPluginToTest.class.getName() + '.' + parameter,
				"src/main/wordpress"); //$NON-NLS-1$
		final File localPlgDir = new File(localPlgDirStr);
		if (!localPlgDir.exists()) {
			throw new UtilsException(String.format(
					"Plug-in local directory '%s' does not exist!", //$NON-NLS-1$
					localPlgDir.getAbsolutePath()));
		}
		return getPluginLocalDir(pluginId, localPlgDir, cssToMini, jsToMini,
				filesToRemove);
	}

	@Override
	public IWordPressPluginToTest getPluginLocalDir(final String pluginId,
			final File localDir, final String[] cssToMini,
			final String[] jsToMini, final String[] filesToRemove)
			throws UtilsException {
		final IWordPressPlugin plugin = IWordPressPluginCatalog.DEFAULT
				.getPlugin(pluginId);
		return new IWordPressPluginToTest() {
			@Override
			public String getName() {
				return plugin.getName();
			}

			@Override
			public String getId() {
				return plugin.getId();
			}

			@Override
			public void defaultActivationAction(final WebDriver driver) {
				plugin.defaultActivationAction(driver);
			}

			@Override
			public File getLocalPluginDirectory() {
				return localDir;
			}

			@Override
			public String[] packagingCSSToMini() {
				return cssToMini;
			}

			@Override
			public String[] packagingJavascriptToMini() {
				return jsToMini;
			}

			@Override
			public File[] packagingFilesToRemove() {
				final List<File> result = new ArrayList<>();
				final File localPluginDir = getLocalPluginDirectory();
				for (final String string : filesToRemove) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					/*
					 * @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					 * ticognani: it's ok here to instanciate objects in a loop!
					 */
					final File file = new File(localPluginDir, string);
					result.add(file);
				}
				return result.toArray(new File[result.size()]);
			}
		};
	}

}
