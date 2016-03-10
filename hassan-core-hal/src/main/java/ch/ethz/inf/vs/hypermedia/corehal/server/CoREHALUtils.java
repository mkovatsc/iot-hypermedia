/*******************************************************************************
 * Copyright (c) 2016 Institute for Pervasive Computing, ETH Zurich.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Yassin N. Hassan - architect and implementation
 *******************************************************************************/
package ch.ethz.inf.vs.hypermedia.corehal.server;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.Endpoint;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

/**
 * Created by ynh on 06/11/15.
 */
public class CoREHALUtils {
	public static void register(RegisteredServer server, InetSocketAddress rdAddress) {
		String uri = getBaseUri(rdAddress);
		CoapClient c = createClient(server);
		c.setTimeout(5000);

		c.setURI(uri);
		Set<WebLink> resources = c.discover("rt=core.rd");
		if (resources != null) {
			if (resources.size() > 0) {
				WebLink w = resources.iterator().next();
				register(server, uri, w.getURI());
			}
		} else {
			System.out.println("Discover timeout");
		}
	}

	public static String getBaseUri(InetSocketAddress rdAddress) {
		String host = rdAddress.getHostString();
		if (host.equals("0.0.0.0")) {
			host = "localhost";
		}
		int port = rdAddress.getPort();
		return "coap://" + host + ":" + port;
	}

	private static void register(RegisteredServer server, String baseUri, String uri) {
		String handle = baseUri + uri + "?ep=" + server.getId();
		CoapResponse response = register(server, handle);
		server.addRDHandle(baseUri + "/" + response.getOptions().getLocationPathString());
	}

	private static CoapClient createClient(RegisteredServer server) {
		CoapClient client = new CoapClient();
		List<Endpoint> endpoints = server.getEndpoints();
		client.setExecutor(server.getRoot().getExecutor());
		if (!endpoints.isEmpty()) {
			Endpoint ep = endpoints.get(0);
			client.setEndpoint(ep);
		}
		return client;
	}

	public static CoapResponse register(RegisteredServer server, String handle) {
		CoapClient client = createClient(server);
		client.setTimeout(5000);
		client.setURI(handle);
		return client.post(LinkFormat.serializeTree(server.getRoot()), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}

	public static void register(CoapServer rd, RegisteredServer server) {
		register(server, rd.getEndpoints().get(0).getAddress());
	}

	public static String getBaseUri(CoapServer rd) {
		return getBaseUri(rd.getEndpoints().get(0).getAddress());
	}
}
