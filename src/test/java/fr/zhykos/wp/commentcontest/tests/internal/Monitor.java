package fr.zhykos.wp.commentcontest.tests.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
public class Monitor extends Thread {

	private final Server[] servers;
	private ServerSocket serverSocket;

	public Monitor(final int port, final Server[] servers) throws IOException {
		if (port <= 0) {
			throw new IllegalStateException("Bad stop PORT");
		}
		this.servers = servers;
		setDaemon(true);
		setName("StopJettyMonitor");
		this.serverSocket = new ServerSocket(port, 1,
				InetAddress.getByName("127.0.0.1"));
		this.serverSocket.setReuseAddress(true);
	}

	@Override
	public void run() {
		while (this.serverSocket != null) {
			Socket socket = null;
			try {
				socket = this.serverSocket.accept();
				socket.setSoLinger(false, 0);
				final LineNumberReader lin = new LineNumberReader(
						new InputStreamReader(socket.getInputStream()));
				final String cmd = lin.readLine();
				if ("stop".equals(cmd)) {
					try {
						socket.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
					try {
						this.serverSocket.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
					this.serverSocket = null;
					for (int i = 0; this.servers != null
							&& i < this.servers.length; i++) {
						try {
							System.out.println("Stopping server " + i);
							this.servers[i].stop();
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("Unsupported monitor operation " + cmd);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}