package fr.zhykos.wp.commentcontest.tests.internal.utils;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.server.ITestServer;

public interface IWordPressInformation {

	String getLogin();

	String getPassword();

	ITestServer getTestServer();

	String getWebsiteName();

	File getInstallDir();

}
