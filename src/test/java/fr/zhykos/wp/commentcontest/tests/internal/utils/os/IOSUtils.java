package fr.zhykos.wp.commentcontest.tests.internal.utils.os;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

public interface IOSUtils {

	IOSUtils DEFAULT = CommonOSUtils.createOSUtils();

	/*
	 * XXX Not sure but is a service only a Windows thing?????
	 */
	void startService(String serviceName) throws UtilsException;

	ICommandExecResult executeCommand(String command) throws UtilsException;

	void checkServiceRunning(final String serviceName) throws UtilsException;

	boolean isWindows();

}
