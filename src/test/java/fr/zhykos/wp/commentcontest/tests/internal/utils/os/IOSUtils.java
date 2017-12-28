package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

// XXX Renommer cette interface si c'est bien une interface
// XXX Et si c'était une classe utilitaire plutôt qu'une interface ?
public interface IOSUtils {

	IOSUtils DEFAULT = CommonOSUtils.createOSUtils();

	/*
	 * XXX Not sure but is a service only a Windows thing?????
	 */
	ICommandExecResult startService(String serviceName) throws UtilsException;

	/*
	 * XXX Not sure but is a service only a Windows thing?????
	 */
	ICommandExecResult stopService(String serviceName) throws UtilsException;

	ICommandExecResult executeCommand(String command) throws UtilsException;

	void checkServiceRunning(final String serviceName) throws UtilsException;

	default boolean isWindows() {
		return false;
	}

	default boolean isMacOS() {
		return false;
	}

}
