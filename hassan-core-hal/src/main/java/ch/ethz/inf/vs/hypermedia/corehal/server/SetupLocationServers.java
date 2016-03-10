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

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.TestUtils;
import ch.ethz.inf.vs.hypermedia.client.inspector.HypermediaClientInspector;
import ch.ethz.inf.vs.hypermedia.client.inspector.RequestCounter;
import ch.ethz.inf.vs.hypermedia.corehal.block.LocationDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.corehal.block.ThingDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.LocationDescription;
import ch.ethz.inf.vs.hypermedia.corehal.model.ThingDescription;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.tools.ResourceDirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 09/11/15.
 */
public class SetupLocationServers {
	public final List<Integer> connections;
	protected final String[] path;
	private final boolean test;
	private final boolean addLocationToRD;
	public CoapServer[] rds;
	public RequestCounter rc;
	protected ResourceDirectory rootRD;
	protected ArrayList<CoapServer> servers;

	public SetupLocationServers(String[] path, boolean addLocationToRD, List<Integer> connections, boolean test) {
		this.path = path;
		this.addLocationToRD = addLocationToRD;
		this.connections = connections;
		this.test = test;
		if (test)
			TestUtils.setupEnviroment();
	}

	public void setUp() throws Exception {
		servers = new ArrayList<>();
		rds = new CoapServer[path.length + 1];
		rootRD = new ResourceDirectory();
		if (test)
			rootRD.addEndpoint(TestConnector.getEndpoint(0));
		else
			rootRD.addEndpoint(new CoapEndpoint(5683));
		rootRD.start();
		rds[0] = rootRD;
		String id = "";
		for (int i = 1; i < rds.length; i++) {
			id += "/" + path[i - 1];
			rds[i] = new LocationServer(id, addLocationToRD, test);
			addServer(rds[i]);
		}
		for (int i = 1; i < rds.length; i++) {
			int j = connections.get(i - 1);
			CoapServer server = rds[i];
			CoapServer rd = rds[j];
			if (j == 0) {
				CoREHALUtils.register(rd, (RegisteredServer) server);
			} else {
				registerLocation(rd, server);
			}
		}
		LocationServer item = new LocationServer(id + "_2", addLocationToRD, test);
		addServer(item);
		registerLocation(rds[rds.length - 2], item);
		System.err.println("???????????????????????????????????");
		rc = new RequestCounter();
		HypermediaClientInspector.add(rc);
	}

	public static void registerLocation(CoapServer rd, CoapServer server) throws IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
		LocationDescriptionFuture desc = new HypermediaClient(CoREHALUtils.getBaseUri(server)).discover().getByMediaType(LocationDescriptionFuture::new);
		LocationDescription description = desc.get();
		description.setSelf(description.getSelf(desc.getUrl()));
		new HypermediaClient(CoREHALUtils.getBaseUri(rd)).discover().getByMediaType(LocationDescriptionFuture::new).addChildLocation(description).get();
	}

	public static void registerThing(CoapServer rd, CoapServer server) throws IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
		ThingDescriptionFuture desc = new HypermediaClient(CoREHALUtils.getBaseUri(server)).discover().getByMediaType(ThingDescriptionFuture::new);
		ThingDescription description = desc.get();
		description.setSelf(description.getSelf(desc.getUrl()));
		new HypermediaClient(CoREHALUtils.getBaseUri(rd)).discover().getByMediaType(LocationDescriptionFuture::new).addThing(description).get();
	}

	public void addServer(CoapServer cab) {
		servers.add(cab);
		cab.start();
	}

	public void tearDown() throws InterruptedException {
		System.err.flush();
		System.err.println("-----------------------------------------");
		System.err.println("-----------------------------------------");
		System.err.println("----------------\t" + rc.getRequests() + "\t-----------------");
		System.err.println("-----------------------------------------");
		System.err.println("-----------------------------------------");
		System.err.println("???????????????????????????????????");
		System.err.flush();
		for (CoapServer s : servers) {
			s.stop();
			s.destroy();
		}
		rootRD.stop();
		rootRD.destroy();
		servers.clear();
		System.gc();

	}

	public String getEndpoint() {
		return String.format("coap://localhost:%d/", rootRD.getEndpoints().get(0).getAddress().getPort());
	}

	public String getLocation(LocationDescriptionFuture x) {
		try {
			return x.get().getLocation();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
