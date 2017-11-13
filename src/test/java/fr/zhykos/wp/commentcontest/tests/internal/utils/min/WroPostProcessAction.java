package fr.zhykos.wp.commentcontest.tests.internal.utils.min;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;

class WroPostProcessAction extends AbstractProcessorAction {

	public WroPostProcessAction(final Object[] processors)
			throws UtilsException {
		super();
		for (final Object processor : processors) {
			checkType(processor);
		}
	}

	private static ResourcePostProcessor checkType(final Object processor)
			throws UtilsException {
		if (!(processor instanceof ResourcePostProcessor)) {
			throw new UtilsException(
					String.format("Processor '%s' must be typed as '%s'", //$NON-NLS-1$
							processor.getClass().getName(),
							ResourcePostProcessor.class.getName()));
		}
		return (ResourcePostProcessor) processor;
	}

	@Override
	protected void execute(final Reader reader, final Writer writer,
			final Object processor) throws UtilsException {
		try {
			checkType(processor).process(reader, writer);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
	}

}
