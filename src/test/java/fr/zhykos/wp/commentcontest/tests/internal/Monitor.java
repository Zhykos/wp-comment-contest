package fr.zhykos.wp.commentcontest.tests.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;

/*
 * https://github.com/arey/embedded-jetty-webapp/blob/master/src/main/java/com/javaetmoi/jetty/Monitor.java
 */
/**
 * Listens for stop commands and causes jetty to stop by stopping the server
 * instance.
 *
 * @see <a href=
 *      "https://github.com/eclipse/jetty.project/tree/master/jetty-maven-plugin">https://github.com/eclipse/jetty.project/tree/master/jetty-maven-plugin</a>
 */
class Monitor extends Thread {

	private static final Log LOG = LogFactory.getLog(Monitor.class);
	private static final String LOCALHOST = "127.0.0.1"; //$NON-NLS-1$
	private final Server[] servers;
	private ServerSocket serverSocket;

	public Monitor(final int port, final Server[] servers) throws IOException {
		super();
		if (port <= 0) {
			final String message = String.format("Bad stop port %d", //$NON-NLS-1$
					Integer.valueOf(port));
			throw new IllegalStateException(message);
		}
		this.servers = Arrays.copyOf(servers, servers.length);
		setDaemon(true);
		setName("StopJettyMonitor"); //$NON-NLS-1$
		this.serverSocket = new ServerSocket(port, 1,
				InetAddress.getByName(LOCALHOST));
		this.serverSocket.setReuseAddress(true);
	}

	@Override
	public void run() {
		while (this.serverSocket != null) {
			try (Socket socket = this.serverSocket.accept();) {
				runSocket(socket);
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	private void runSocket(final Socket socket)
			throws SocketException, IOException {
		socket.setSoLinger(false, 0);
		final LineNumberReader lin = new LineNumberReader(
				new InputStreamReader(socket.getInputStream()));
		final String cmd = lin.readLine();
		if ("stop".equals(cmd)) {
			try {
				socket.close();
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
			}
			try {
				this.serverSocket.close();
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
			}
			this.serverSocket = null;
			for (int i = 0; this.servers != null
					&& i < this.servers.length; i++) {
				try {
					final Server server = this.servers[i];
					final String message = String.format("Stopping server '%s'", //$NON-NLS-1$
							server);
					LOG.info(message);
					server.stop();
				} catch (final Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		} else {
			final String message = String
					.format("Unsupported monitor operation '%s'", cmd); //$NON-NLS-1$
			LOG.info(message);
		}
	}

}