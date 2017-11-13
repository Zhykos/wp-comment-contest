package fr.zhykos.wp.commentcontest.tests.internal.utils.min;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

abstract class AbstractProcessorAction {

	private final Writer result;

	protected AbstractProcessorAction() {
		this.result = new StringWriter();
	}

	protected abstract void execute(final Reader reader, final Writer writer,
			final Object processor) throws UtilsException;

	protected final Reader execute(final Reader reader, final Object processor)
			throws UtilsException {
		execute(reader, this.result, processor);
		return new StringReader(this.result.toString());
	}

}
