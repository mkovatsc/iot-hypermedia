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

import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.block.CoREHalResourceFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.LightingState;
import ch.ethz.inf.vs.hypermedia.corehal.model.PowerConsumptionState;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * Created by ynh on 05/11/15.
 */
public class PowerConsumptionResource extends CoapResource {

	private final PowerConsumptionState power;

	public PowerConsumptionResource() {
		super("power");
		power = new PowerConsumptionState();
		power.setUnit("watt");
		getAttributes().addContentType(Utils.getContentType(LightingState.class));
	}

	public PowerConsumptionState getPower() {
		power.setSelf(getURI());
		return power;
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getPower()), Utils.getContentType(PowerConsumptionState.class));
	}
}