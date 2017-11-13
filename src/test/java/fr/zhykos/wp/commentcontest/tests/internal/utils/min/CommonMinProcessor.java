package fr.zhykos.wp.commentcontest.tests.internal.utils.min;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

class CommonMinProcessor implements IMin {
	private final File source;
	private final Object[] processors;

	protected CommonMinProcessor(final File source, final Object processor) {
		this(source, new Object[] { processor });
	}

	protected CommonMinProcessor(final File source, final Object[] processors) {
		this.source = source;
		this.processors = Arrays.copyOf(processors, processors.length);
	}

	@Override
	public final void process(final File target) throws UtilsException {
		try {
			internalProcess(target);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
	}

	private void internalProcess(final File target)
			throws IOException, UtilsException {
		try (final FileReader fileReader = new FileReader(this.source);
				final FileWriter fileWriter = new FileWriter(target);) {
			Reader reader = fileReader;
			final AbstractProcessorAction preAction = new AbstractProcessorAction() {
				@Override
				protected void execute(final Reader absReader,
						final Writer absWriter, final Object object)
						throws UtilsException {
					preProcess(absReader, absWriter);
				}
			};
			final AbstractProcessorAction postAction = new AbstractProcessorAction() {
				@Override
				protected void execute(final Reader absReader,
						final Writer absWriter, final Object object)
						throws UtilsException {
					postProcess(absReader, absWriter);
				}
			};
			final AbstractProcessorAction[] actions = new AbstractProcessorAction[] {
					preAction, new WroPreProcessAction(this.processors, target),
					new WroPostProcessAction(this.processors), postAction };
			for (final AbstractProcessorAction action : actions) {
				for (final Object processor : this.processors) {
					reader = action.execute(reader, processor);
				}
			}
			IOUtils.copy(reader, fileWriter);
		}
	}

	@SuppressWarnings("static-method")
	/*
	 * @SuppressWarnings("static-method") tcicognani: overridable method
	 */
	protected void preProcess(final Reader reader, final Writer writer)
			throws UtilsException {
		try {
			IOUtils.copy(reader, writer);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
	}

	@SuppressWarnings("static-method")
	/*
	 * @SuppressWarnings("static-method") tcicognani: overridable method
	 */
	protected void postProcess(final Reader reader, final Writer writer)
			throws UtilsException {
		try {
			IOUtils.copy(reader, writer);
		} catch (final IOException e) {
			throw new UtilsException(e);
		}
	}

}
