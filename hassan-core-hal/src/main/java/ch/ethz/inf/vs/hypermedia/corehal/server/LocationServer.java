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

import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.block.CoREHalResourceFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.Form;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import ch.ethz.inf.vs.hypermedia.corehal.model.LocationDescription;
import ch.ethz.inf.vs.hypermedia.corehal.model.ThingDescription;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.tools.ResourceDirectory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ynh on 05/11/15.
 */
public class LocationServer extends CoapServer implements RegisteredServer {

	private final LocationDescriptionResource locationDescriptionResource;
	private final String id;
	private final List<String> handles;
	private boolean addLocationToRD;

	public LocationServer(String id, boolean addLocationToRD, boolean test) {
		if (test)
			addEndpoint(TestConnector.getEndpoint(0));
		else
			addEndpoint(new CoapEndpoint());
		this.id = id;
		this.addLocationToRD = addLocationToRD;
		handles = new ArrayList<>();
		locationDescriptionResource = new LocationDescriptionResource(id);
		add(locationDescriptionResource);
	}

	public static void main(String[] args) {
		// create server
		CoapServer server = new LocationServer("/CH/ETH/CAB", false, true);
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

	public class LocationDescriptionResource extends CoapResource {
		private final List<JsonElement> children = new ArrayList<>();
		private final List<JsonElement> things = new ArrayList<>();
		private final String location;
		private final CoapResource childrenResource;
		private final CoapResource thingsResource;

		public LocationDescriptionResource(String location) {
			super("location");
			this.location = location;
			this.childrenResource = new CoapResource("children") {
				@Override
				public void handlePOST(CoapExchange exchange) {
					addChild(exchange);
				}
			};
			this.thingsResource = new CoapResource("things") {
				@Override
				public void handlePOST(CoapExchange exchange) {
					addThing(exchange);
				}

				@Override
				public void handleDELETE(CoapExchange exchange) {
					deleteThing(exchange);
				}
			};

			add(childrenResource);
			add(thingsResource);
			if (addLocationToRD)
				getAttributes().setAttribute("location", location);
			getAttributes().addContentType(Utils.getContentType(LocationDescription.class));
		}

		public LocationDescription getLocationDescription() {
			LocationDescription location = new LocationDescription();
			location.setLocation(this.location);
			location.setSelf(getURI());
			location.addForm("edit", new Form("POST", "", Utils.getMediaType(LocationDescription.class)));
			location.addForm("add-child", new Form("POST", childrenResource.getURI(), Utils.getMediaType(LocationDescription.class)));
			location.addForm("add-thing", new Form("POST", thingsResource.getURI(), Utils.getMediaType(ThingDescription.class)));
			location.addForm("remove-thing", new Form("DELETE", thingsResource.getURI() + "{?_self}", Utils.getMediaType(ThingDescription.class)).setTemplated(true));
			for (JsonElement child : children) {
				String url = child.getAsJsonObject().get("_self").getAsString();
				location.addLink("child", new Link(url, Utils.getMediaType(LocationDescription.class)), child);
			}
			for (JsonElement thing : things) {
				String url = thing.getAsJsonObject().get("_self").getAsString();
				location.addLink("thing", new Link(url, Utils.getMediaType(ThingDescription.class)), thing);
			}
			return location;
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getLocationDescription()), 65022);
		}

		public void addChild(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			JsonElement item = CoREHalResourceFuture.getGson().fromJson(postBody, JsonElement.class);
			children.add(item);
			try {
				exchange.respond(CoAP.ResponseCode.CREATED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

			}
		}

		public void addThing(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			JsonElement item = CoREHalResourceFuture.getGson().fromJson(postBody, JsonElement.class);
			things.add(item);
			try {
				exchange.respond(CoAP.ResponseCode.CREATED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

			}
		}

		public void deleteThing(CoapExchange exchange) {
			try {
				String query = exchange.getRequestOptions().getUriQuery().get(0);
				assert query.startsWith("_self=");
				things.removeIf((x) -> x.getAsJsonObject().get("_self").getAsString().equals(query.substring("_self=".length())));
				exchange.respond(CoAP.ResponseCode.CHANGED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
			}
		}
	}
}
