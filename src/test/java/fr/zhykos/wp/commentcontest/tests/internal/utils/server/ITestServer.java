package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

public interface ITestServer {

	void start() throws UtilsException;

	void stop() throws UtilsException;

	File deployWordPress(File wpEmbedDir) throws UtilsException;

	String getHomeURL() throws UtilsException;

}
