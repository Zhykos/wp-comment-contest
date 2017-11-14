package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import java.io.File;

import fr.zhykos.wp.commentcontest.tests.internal.utils.Utils;
import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

abstract class AbstractServer implements ITestServer {

	private final String address;
	private final int port;

	private File wpEmbedDir;

	protected AbstractServer() {
		this.address = System.getProperty(
				ITestServerFactory.class.getName() + ".serveraddress", //$NON-NLS-1$
				"localhost"); //$NON-NLS-1$
		this.port = Utils.getIntegerSystemProperty(
				ITestServerFactory.class.getName() + ".serverport", 80); //$NON-NLS-1$
	}

	protected String getAddress() {
		return this.address;
	}

	protected int getPort() {
		return this.port;
	}

	protected File getWordpressEmbeddedDir() throws UtilsException {
		if (this.wpEmbedDir == null) {
			throw new UtilsException("WordPress is not deployed!"); //$NON-NLS-1$
		}
		return this.wpEmbedDir;
	}

	protected abstract File specificDeployWordPress(final File wordpressDir)
			throws UtilsException;

	@Override
	public final File deployWordPress(final File wordpressDir)
			throws UtilsException {
		final File temp = specificDeployWordPress(wordpressDir);
		this.wpEmbedDir = temp;
		return temp;
	}

	@Override
	public String getHomeURL() throws UtilsException {
		return String.format("http://%s:%d/", this.address, //$NON-NLS-1$
				Integer.valueOf(this.port));
	}

}
