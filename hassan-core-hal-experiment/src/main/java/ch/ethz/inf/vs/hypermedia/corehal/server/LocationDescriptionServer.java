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
import ch.ethz.inf.vs.hypermedia.corehal.block.LocationCrawler;
import ch.ethz.inf.vs.hypermedia.corehal.block.LocationDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.LocationDescription;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.tools.ResourceDirectory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 05/11/15.
 */
public class LocationDescriptionServer extends CoapServer {
	protected static ResourceDirectory rootRD;
	private static Set<String> locationServers = new HashSet<>();

	public static void main(String[] args) throws Exception {
		rootRD = new ResourceDirectory();
		rootRD.addEndpoint(new CoapEndpoint(5783));
		rootRD.start();
		Thread.sleep(100);
		buildPath("/CH/ETH/CAB/50");
		buildPath("/CH/ETH/CAB/51");
		buildPath("/CH/ETH/CAB/52");
		buildPath("/CH/ETH/HG/1");
		buildPath("/CH/UZH/HG");
		buildPath("/CH/ETH/HG/3");
	}

	public static void buildPath(String pathName) throws IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
		String[] path = pathName.substring(1).split("/");
		String id = "";
		for (int i = 0; i < path.length; i++) {
			id += "/" + path[i];
			if (locationServers.add(id)) {
				LocationServer location = new LocationServer(id, true, false);
				location.start();
				if (i == 0) {
					CoREHALUtils.register(rootRD, location);
				} else {
					registerLocation(id, location);
				}
				Thread.sleep(100);
			}
		}
	}

	public static void registerLocation(String path, CoapServer server) throws IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
		LocationDescriptionFuture desc = new HypermediaClient(CoREHALUtils.getBaseUri(server)).discover().getByMediaType(LocationDescriptionFuture::new);
		LocationDescription description = desc.get();
		description.setSelf(description.getSelf(desc.getUrl()));

		new HypermediaClient(CoREHALUtils.getBaseUri(rootRD)).discover().resourceLookup().use(new LocationCrawler()).matchingPrefix(path).first().addChildLocation(description).get();
	}
}
