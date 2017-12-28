package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

final class CommonOSUtils {

	private static final Logger LOGGER = Logger
			.getLogger(CommonOSUtils.class.getName());

	private CommonOSUtils() {
		// Do nothing and must not be called
	}

	public static ICommandExecResult executeCommand(final String command)
			throws UtilsException {
		ICommandExecResult result = null;
		try {
			LOGGER.info(String.format("Executing command: %s", command)); //$NON-NLS-1$
			final CommandLine commandLine = CommandLine.parse(command);
			final DefaultExecutor executor = new DefaultExecutor();
			final StringWriter stdOut = new StringWriter();
			final StringWriter stdErr = new StringWriter();
			executor.setStreamHandler(new ExecuteStreamHandler() {
				@Override
				public void stop() throws IOException {
					// DO NOTHING
				}

				@Override
				public void start() throws IOException {
					// DO NOTHING
				}

				@Override
				public void setProcessOutputStream(final InputStream is)
						throws IOException {
					IOUtils.copy(is, stdOut, "UTF-8"); //$NON-NLS-1$
				}

				@Override
				public void setProcessInputStream(final OutputStream os)
						throws IOException {
					// DO NOTHING
				}

				@Override
				public void setProcessErrorStream(final InputStream is)
						throws IOException {
					IOUtils.copy(is, stdErr, "UTF-8"); //$NON-NLS-1$
				}
			});
			final ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
			executor.setWatchdog(watchdog);
			final int exitValue = executor.execute(commandLine);
			result = createCommandResult(exitValue, stdOut, stdErr);
			LOGGER.info("Done!"); //$NON-NLS-1$
		} catch (final ExecuteException e) {
			final int exitValue = e.getExitValue();
			LOGGER.warning(String.format(
					"Error while executing command '%s': exiting value %s and error is '%s'", //$NON-NLS-1$
					command, Integer.valueOf(exitValue), e.getMessage()));
			result = createCommandResult(exitValue, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
		return result;
	}

	private static ICommandExecResult createCommandResult(final int exitValue,
			final Writer output, final Writer errorOutput) {
		return createCommandResult(exitValue, output.toString(),
				errorOutput.toString());
	}

	private static ICommandExecResult createCommandResult(final int exitValue,
			final String output, final String errorOutput) {
		return new ICommandExecResult() {
			@Override
			public String getOuput() {
				return output;
			}

			@Override
			public int getExitValue() {
				return exitValue;
			}

			@Override
			public String getErrorOutput() {
				return errorOutput;
			}
		};
	}

	public static IOSUtils createOSUtils() {
		/*
		 * https://www.mkyong.com/java/how-to-detect-os-in-java-
		 * systemgetpropertyosname/
		 */
		IOSUtils result = null;
		@SuppressWarnings("PMD.UseLocaleWithCaseConversions")
		/*
		 * @SuppressWarnings("PMD.UseLocaleWithCaseConversions") tcicognani: we
		 * usually use Locale.getDefault() in toLowerCase() parameter so it's
		 * useless to add it
		 */
		final String currentOS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (isWindows(currentOS)) {
			result = new WindowsUtils();
		} else if (isMac(currentOS)) {
			result = new MacOSUtils();
		} else {
			result = new UnsupportedOS(currentOS);
		}
		return result;
	}

	private static boolean isWindows(final String currentOS) {
		return (currentOS.indexOf("win") >= 0); //$NON-NLS-1$
	}

	private static boolean isMac(final String currentOS) {
		return (currentOS.indexOf("mac") >= 0); //$NON-NLS-1$
	}

	// private static boolean isUnix() {
	// return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0
	// || OS.indexOf("aix") > 0);
	// }
	//
	// private static boolean isSolaris() {
	// return (OS.indexOf("sunos") >= 0);
	// }
}
