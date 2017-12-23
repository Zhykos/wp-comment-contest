package fr.zhykos.wp.commentcontest.tests.internal.utils.wpplugins;

import java.util.ArrayList;
import java.util.List;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

class WordPressPluginCatalog implements IWordPressPluginCatalog {

	private final List<IWordPressPlugin> plugins = new ArrayList<>();

	public WordPressPluginCatalog() {
		/*
		 * XXX tcicognani: I wanted to have all classes through reflection but
		 * it doesn't work this way and I had to do so with external librairies:
		 * for now list all classes like a noob
		 */
		this.plugins.add(new PluginCommentContest());
		this.plugins.add(new PluginWPRSSAggregator());
	}

	@Override
	public IWordPressPlugin getPlugin(final String pluginId)
			throws UtilsException {
		IWordPressPlugin result = null;
		for (final IWordPressPlugin plugin : this.plugins) {
			if (plugin.getId().equals(pluginId)) {
				result = plugin;
				break;
			}
		}
		if (result == null) {
			throw new UtilsException(String.format(
					"Cannot find plugin with id '%s' in the internal catalog! Make your own and/or contribute it to this framework", //$NON-NLS-1$
					pluginId));
		}
		return result;
	}

}
