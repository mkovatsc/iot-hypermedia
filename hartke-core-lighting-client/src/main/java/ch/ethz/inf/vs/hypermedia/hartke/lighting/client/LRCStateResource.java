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
package ch.ethz.inf.vs.hypermedia.hartke.lighting.client;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Lighting;

public class LRCStateResource extends CoapResource implements Resource {

	private float h = 0f;
	private float s = 0f;

	public LRCStateResource(String name) {
		super(name);

		setObservable(true);
		getAttributes().addContentType(Utils.getContentType(Lighting.class));
	}

	public void controlHS(float hue, float saturation) {
		this.h = hue;
		this.s = saturation;
		changed();
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		Response response = new Response(ResponseCode.CONTENT);
		response.setPayload(ResourceFuture.gson.toJson(getState()));
		response.getOptions().setContentFormat(Utils.getContentType(Lighting.class));
		exchange.respond(response);
	}

	private Lighting getState() {
		Lighting state = new Lighting();
		state.setColorMode(Lighting.COLOR_MODE_HS);
		if (this.s == 0f) {
			state.setOn(false);
		} else {
			state.setOn(true);
			state.setHue(this.h);
			state.setSaturation(this.s);
		}
		return state;
	}
}
