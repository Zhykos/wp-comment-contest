package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class WindowsUtils implements IOSUtils {

	@Override
	public ICommandExecResult startService(final String serviceName)
			throws UtilsException {
		final ICommandExecResult result = executeCommand(
				String.format("net start %s", serviceName)); //$NON-NLS-1$
		checkServiceRunning(serviceName);
		return result;
	}

	@Override
	public void checkServiceRunning(final String serviceName)
			throws UtilsException {
		final String commandLine = String.format("sc query %s", serviceName); //$NON-NLS-1$
		final ICommandExecResult cmdRes = executeCommand(commandLine);
		if (!cmdRes.getOuput().contains("RUNNING")) { //$NON-NLS-1$
			throw new UtilsException(
					String.format("Service '%s' is not running!", serviceName)); //$NON-NLS-1$
		}
	}

	@Override
	public ICommandExecResult executeCommand(final String command)
			throws UtilsException {
		final StringBuilder windowsCommand = new StringBuilder();
		windowsCommand.append("cmd.exe /c "); //$NON-NLS-1$
		windowsCommand.append(command);
		return CommonOSUtils.executeCommand(windowsCommand.toString());
	}

	@Override
	public boolean isWindows() {
		return true;
	}

	@Override
	public ICommandExecResult stopService(final String serviceName)
			throws UtilsException {
		return executeCommand(String.format("net stop %s", serviceName)); //$NON-NLS-1$
	}

}
