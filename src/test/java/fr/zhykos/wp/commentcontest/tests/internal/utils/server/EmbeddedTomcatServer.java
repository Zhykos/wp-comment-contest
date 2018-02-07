package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

/**
 * FIXME This class is not finished at all!
 */
class EmbeddedTomcatServer extends AbstractServer {

	private final Tomcat tomcat;

	public EmbeddedTomcatServer() {
		super();
		this.tomcat = new Tomcat();
	}

	@Override
	public void start() throws UtilsException {
		/*
		 * https://devcenter.heroku.com/articles/create-a-java-web-application-
		 * using-embedded-tomcat
		 */
		try {
			this.tomcat.setPort(getPort());
			this.tomcat.setBaseDir("."); //$NON-NLS-1$
			this.tomcat.addWebapp("", //$NON-NLS-1$
					getWordpressEmbeddedDir().getAbsolutePath());
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
	protected File specificDeployWordPress(final File wordpressDir)
			throws UtilsException {
		return wordpressDir;
	}

	@Override
	public String getVersion(final File installDir) throws UtilsException {
		return null;
	}

}
