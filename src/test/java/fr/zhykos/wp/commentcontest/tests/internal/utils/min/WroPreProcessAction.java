package fr.zhykos.wp.commentcontest.tests.internal.utils.min;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

class WroPreProcessAction extends AbstractProcessorAction {

	private final String sourceURIStr;

	public WroPreProcessAction(final Object[] processors, final File source)
			throws UtilsException {
		super();
		for (final Object processor : processors) {
			checkType(processor);
		}
		this.sourceURIStr = source.toURI().toString();
	}

	private static ResourcePreProcessor checkType(final Object processor)
			throws UtilsException {
		if (!(processor instanceof ResourcePreProcessor)) {
			throw new UtilsException(
					String.format("Processor '%s' must be typed as '%s'", //$NON-NLS-1$
							processor.getClass().getName(),
							ResourcePreProcessor.class.getName()));
		}
		return (ResourcePreProcessor) processor;
	}

	@Override
	protected void execute(final Reader reader, final Writer writer,
			final Object processor) throws UtilsException {
		try {
			final Resource resource = Resource.create(this.sourceURIStr);
			checkType(processor).process(resource, reader, writer);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
	}

}
