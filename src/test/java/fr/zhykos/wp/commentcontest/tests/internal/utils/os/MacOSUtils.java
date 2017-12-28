package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class MacOSUtils implements IOSUtils {

	private static final String NOT_IMPL_YET = "Not implemented yet"; //$NON-NLS-1$

	@Override
	public ICommandExecResult startService(final String serviceName)
			throws UtilsException {
		throw new UnsupportedOperationException(NOT_IMPL_YET); // FIXME
	}

	@Override
	public void checkServiceRunning(final String serviceName)
			throws UtilsException {
		throw new UnsupportedOperationException(NOT_IMPL_YET); // FIXME
	}

	@Override
	public ICommandExecResult executeCommand(final String command)
			throws UtilsException {
		throw new UnsupportedOperationException(NOT_IMPL_YET); // FIXME
	}

	@Override
	public ICommandExecResult stopService(final String serviceName)
			throws UtilsException {
		throw new UnsupportedOperationException(NOT_IMPL_YET); // FIXME
	}

	@Override
	public boolean isMacOS() {
		return true;
	}

}
