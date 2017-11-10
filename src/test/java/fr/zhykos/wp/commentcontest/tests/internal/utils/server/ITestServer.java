package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

public interface ITestServer {

	void launch(int port, String wpRunDir) throws UtilsException;

	File deployWordPress(File wpEmbedDir) throws UtilsException;

	void stop() throws UtilsException;

}
