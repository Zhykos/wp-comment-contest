package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

/**
 * FIXME This class is not finished at all!
 */
class EmbeddedTomcatServer implements ITestServer {

	private final Tomcat tomcat;

	public EmbeddedTomcatServer() {
		this.tomcat = new Tomcat();
	}

	@Override
	public void launch(final int port, final String wpRunDir)
			throws UtilsException {
		/*
		 * https://devcenter.heroku.com/articles/create-a-java-web-application-
		 * using-embedded-tomcat
		 */
		try {
			this.tomcat.setPort(port);
			this.tomcat.setBaseDir("."); //$NON-NLS-1$
			this.tomcat.addWebapp("", wpRunDir); //$NON-NLS-1$
			this.tomcat.init();
			this.tomcat.start();
			// tomcat.getServer().await();
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
	}

	@Override
	public void stop() throws UtilsException {
		try {
			this.tomcat.stop();
		} catch (final LifecycleException e) {
			throw new UtilsException(e);
		}
	}

	@Override
	public File deployWordPress(final File wpEmbedDir) throws UtilsException {
		return wpEmbedDir;
	}

}
