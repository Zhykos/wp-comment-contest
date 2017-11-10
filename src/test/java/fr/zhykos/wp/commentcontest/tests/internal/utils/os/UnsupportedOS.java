package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

class UnsupportedOS implements IOSUtils {

	private final String currentOS;

	public UnsupportedOS(final String currentOS) {
		this.currentOS = currentOS;
	}

	private void throwUnsupported() {
		throw new UnsupportedOperationException(
				String.format("Current operating system '%s' is unsupported!", //$NON-NLS-1$
						this.currentOS));
	}

	@Override
	public void startService(final String serviceName) {
		throwUnsupported();
	}

	@Override
	public void checkServiceRunning(final String serviceName)
			throws UtilsException {
		throwUnsupported();
	}

	@Override
	public ICommandExecResult executeCommand(final String command)
			throws UtilsException {
		throwUnsupported();
		return null;
	}

	@Override
	public boolean isWindows() {
		throwUnsupported();
		return false;
	}

}
