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

import java.awt.Color;

import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Lighting;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.LightingConfig;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Link;

import com.google.gson.JsonParseException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * Created by ynh on 30/09/15.
 */
public class LightingConfigResource extends CoapResource {
	private final LightServer srv;
	Link src;

	private CoapObserveRelation handle;

	public LightingConfigResource(String name, LightServer srv) {
		super(name);
		this.srv = srv;
		setObservable(true);
		getAttributes().addContentType(Utils.getContentType(LightingConfig.class));
	}

	public LightingConfig getConfig() {
		LightingConfig config = new LightingConfig();
		config.addForm("update", new Form("PUT", getUpdateFormURI(), Utils.getMediaType(LightingConfig.class)));
		if (src != null)
			config.setSrc(src);
		return config;
	}

	public String getUpdateFormURI() {
		return "";
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		Response response = new Response(ResponseCode.CONTENT);
		response.setPayload(ResourceFuture.gson.toJson(getConfig()));
		response.getOptions().setContentFormat(Utils.getContentType(LightingConfig.class));
		exchange.respond(response);
	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		update(exchange);
	}

	protected void update(CoapExchange exchange) {
		String postBody = exchange.getRequestText();

		if (postBody == null || postBody.isEmpty()) {
			exchange.respond(ResponseCode.BAD_REQUEST);
		} else {
			try {
				LightingConfig item = ResourceFuture.gson.fromJson(postBody, LightingConfig.class);
				src = item.getSrc();
				if (srv.isInlined()) {
					srv.registerSelf();
				}
				exchange.respond(ResponseCode.CHANGED);

				if (LightServer.test)
					return;

				stop();

				System.out.println("=== Now observing " + src.getHref());

				CoapClient obs = new CoapClient(src.getHref());
				handle = obs.observe(new CoapHandler() {

					@Override
					public void onLoad(CoapResponse response) {

						System.out.println(">>> " + response.getCode());
						System.out.println(response.getResponseText());
						System.out.flush();

						if (response.getCode() == ResponseCode.CONTENT) {
							if (response.getOptions().getContentFormat() != Utils.getContentType(Lighting.class)) {
								System.out.println("!!! Wrong Content-Format: Expected " + Utils.getContentType(Lighting.class) + " but was " + response.getOptions().getContentFormat());
							}

							try {
								Lighting state = ResourceFuture.gson.fromJson(response.getResponseText(), Lighting.class);

								LightServer.setOnOff(state.isOn());
								LightServer.setColor(Color.getHSBColor(state.getHue() / 360f, state.getSaturation(), 1f));
							} catch (Exception e) {
								System.out.println("!!! Malformed Lighting media type: " + e);
							}
						}
					}

					@Override
					public void onError() {
						System.out.println("<<< Observe error");
					}
				}); // , Utils.getContentType(Lighting.class));

			} catch (JsonParseException ex) {
				exchange.respond(ResponseCode.BAD_REQUEST);
			}
		}
	}

	public void stop() {
		if (handle != null) {
			System.out.println("=== Canceling " + handle.toString());
			handle.proactiveCancel();
		}
	}
}
