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
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.block.CoREHalResourceFuture;
import ch.ethz.inf.vs.hypermedia.corehal.block.LocationCrawler;
import ch.ethz.inf.vs.hypermedia.corehal.block.LocationDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.corehal.block.ThingDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import ch.ethz.inf.vs.hypermedia.corehal.model.ThingDescription;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.tools.ResourceDirectory;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 05/11/15.
 */
public class DeviceServer extends CoapServer implements RegisteredServer {

	private final ThingDescriptionResource thingDescription;
	private final String location;
	private final String id = UUID.randomUUID().toString();
	private final List<String> handles;
	private final Map<String, Link> links = new HashMap<>();
	private String name;
	private ThingDescription description;
	private LocationDescriptionFuture loc;

	public DeviceServer(String location, boolean test) {
		if (test)
			addEndpoint(TestConnector.getEndpoint(0));
		else
			addEndpoint(new CoapEndpoint());
		this.location = location;
		handles = new ArrayList<>();
		thingDescription = new ThingDescriptionResource(location);
		add(thingDescription);
	}

	public DeviceServer setName(String name) {
		this.name = name;
		return this;
	}

	public static void main(String[] args) {
		// create server
		CoapServer server = new DeviceServer("/CH/ETH/CAB", true);
		server.start();
		System.out.printf(ResourceDirectory.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	@Override
	public String getId() {
		return id.replace("/", "_").substring(1);
	}

	@Override
	public void addRDHandle(String handle) {
		handles.add(handle);
	}

	@Override
	public void updateRegistration() {
		for (String handle : handles) {
			try {
				CoREHALUtils.register(this, handle);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void link(String rel, String url, String type) {
		links.put(rel, new Link(url, type));
	}

	public void register() throws ExecutionException, InterruptedException {
		ThingDescriptionFuture desc = new HypermediaClient(CoREHALUtils.getBaseUri(this)).discover().getByMediaType(ThingDescriptionFuture::new);

		description = desc.get();
		description.setSelf(description.getSelf(desc.getUrl()));
		loc = new HypermediaClient("coap://127.0.0.1:5783").discover().resourceLookup().use(new LocationCrawler()).matchingPrefix(location).first();
		loc.addThing(description).get();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				deregister();
			}
		});
	}

	public void deregister() {
		try {
			loc.removeThing(description).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public class ThingDescriptionResource extends CoapResource {
		private final String location;

		public ThingDescriptionResource(String location) {
			super("thing");
			this.location = location;
			getAttributes().setAttribute("location", location);
			getAttributes().addContentType(Utils.getContentType(ThingDescription.class));
		}

		public ThingDescription getThingDescription() {
			ThingDescription location = new ThingDescription();
			location.setLocation(this.location);
			location.setName(name);
			location.setSelf(getURI());
			links.entrySet().stream().forEach(x -> location.addLink(x.getKey(), x.getValue()));
			return location;
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getThingDescription()), Utils.getContentType(ThingDescription.class));
		}
	}
}
