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

import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.BulletinBoard;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.UUID;

/**
 * Created by ynh on 30/09/15.
 */
public class DecoupledBoardResource2 extends BulletinBoardResource {

	private final CreateItemResource createItemResource;
	private final CoapServer createItemServer;

	public DecoupledBoardResource2(String name, BulletinBoardServer srv) {
		super(name, srv);
		createItemServer = new CoapServer();
		if (srv.test) {
			createItemServer.addEndpoint(TestConnector.getEndpoint(0));
		} else {
			createItemServer.addEndpoint(new CoapEndpoint(0));
		}
		createItemResource = new CreateItemResource(UUID.randomUUID().toString());
		createItemServer.add(createItemResource);
		createItemServer.start();
	}

	@Override
	public void stop() {
		createItemServer.stop();
		createItemServer.destroy();
	}

	public String getInsertURI() {
		int port = createItemServer.getEndpoints().get(0).getAddress().getPort();
		return String.format("coap://localhost:%d%s", port, createItemResource.getURI());
	}

	@Override
	public BulletinBoard getBulletinBoard() {
		BulletinBoard bulletins = new BulletinBoard();
		bulletins.setBase(getURI() + "/");
		bulletins.addForm("create-item", new Form("POST", getInsertURI(), Utils.getMediaType(ThingDescription.class)));
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
