package fr.zhykos.wp.commentcontest.tests.internal.utils.server;

import fr.zhykos.wp.commentcontest.tests.internal.utils.UtilsException;

@SuppressWarnings("PMD.AtLeastOneConstructor")
/*
 * @SuppressWarnings("PMD.AtLeastOneConstructor") tcicognani: useless default
 * constructor: no need to have one
 */
class TestServerFactory implements ITestServerFactory {

	@Override
	public ITestServer createServer() throws UtilsException {
		final String className = System.getProperty(
				ITestServerFactory.class.getName() + ".server", ""); //$NON-NLS-1$ //$NON-NLS-2$
		Class<?> classs = null;
		try {
			classs = Class.forName(className);
			return (ITestServer) classs.getDeclaredConstructor().newInstance();
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
	}

}
