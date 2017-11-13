package fr.zhykos.wp.commentcontest.tests.internal.utils.min;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class MinFactory implements IMinFactory {

	@Override
	public IMin createMinProcessor(final File source) throws UtilsException {
		IMin result = null;
		// https://github.com/wro4j/wro4j.git
		if (source.getName().endsWith(".css")) { //$NON-NLS-1$
			result = new CommonMinProcessor(source, new CssMinProcessor());
		} else if (source.getName().endsWith(".js")) { //$NON-NLS-1$
			result = new CommonMinProcessor(source, new JSMinProcessor());
		} else {
			throw new UtilsException(String.format(
					"Cannot create minimified processor for file '%s'", //$NON-NLS-1$
					source.getName()));
		}
		return result;
	}

}
