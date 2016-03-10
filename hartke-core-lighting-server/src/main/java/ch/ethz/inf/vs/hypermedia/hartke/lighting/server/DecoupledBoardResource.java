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

import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.BulletinBoard;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.UUID;

/**
 * Created by ynh on 30/09/15.
 */
public class DecoupledBoardResource extends BulletinBoardResource {

	private final CreateItemResource createItemResource;

	public DecoupledBoardResource(String name, BulletinBoardServer srv) {
		super(name, srv);
		createItemResource = new CreateItemResource(UUID.randomUUID().toString());
		add(createItemResource);
	}

	@Override
	public BulletinBoard getBulletinBoard() {
		BulletinBoard bulletins = new BulletinBoard();
		bulletins.setBase(getURI() + "/");
		bulletins.addForm("create-item", new Form("POST", createItemResource.getName(), Utils.getMediaType(ThingDescription.class)));
		bulletins.addEmbedded("item", list.values());
		return bulletins;
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		exchange.respond(ResponseCode.METHOD_NOT_ALLOWED);
	}

	class CreateItemResource extends CoapResource {

		public CreateItemResource(String name) {
			super(name);
		}

		@Override
		public void handlePOST(CoapExchange exchange) {
			createItem(exchange);
		}
	}
}
