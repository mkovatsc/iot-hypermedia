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
 *    Klaus Hartke - CoRE Lighting specification
 *******************************************************************************/
package ch.ethz.inf.vs.hypermedia.hartke.lighting.server;

import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.BulletinBoard;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;
import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ynh on 30/09/15.
 */
public class BulletinBoardResource extends CoapResource {
	final Map<String, ThingDescription> list;

	public BulletinBoardResource(String name, BulletinBoardServer srv) {
		super(name);
		getAttributes().addContentType(Utils.getContentType(BulletinBoard.class));
		list = new HashMap<>();
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		BulletinBoard bulletins = getBulletinBoard();
		Response response = new Response(ResponseCode.CONTENT);
		response.setPayload(ResourceFuture.gson.toJson(bulletins));
		response.getOptions().setContentFormat(Utils.getContentType(BulletinBoard.class));
		exchange.respond(response);
	}

	public BulletinBoard getBulletinBoard() {
		BulletinBoard bulletins = new BulletinBoard();
		bulletins.addForm("create-item", new Form("POST", getURI(), Utils.getMediaType(ThingDescription.class)));
		bulletins.addEmbedded("item", list.values());
		return bulletins;
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		createItem(exchange);
	}

	public void createItem(CoapExchange exchange) {
		String postBody = exchange.getRequestText();

		if (postBody == null || postBody.isEmpty()) {
			exchange.respond(ResponseCode.BAD_REQUEST);
		} else {
			try {
				ThingDescription item = ResourceFuture.gson.fromJson(postBody, ThingDescription.class);
				list.put(item.getName(), item);
				exchange.respond(ResponseCode.CREATED);
			} catch (JsonParseException ex) {
				exchange.respond(ResponseCode.BAD_REQUEST);
			}
		}
	}

	public void stop() {
	}
}
