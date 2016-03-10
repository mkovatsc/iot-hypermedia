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
import ch.ethz.inf.vs.hypermedia.corehal.model.LightingState;
import ch.ethz.inf.vs.hypermedia.corehal.model.PowerConsumptionState;
import ch.ethz.inf.vs.hypermedia.corehal.services.lifx.LIFXBulb;
import ch.ethz.inf.vs.hypermedia.corehal.values.RGBValue;
import org.eclipse.californium.core.CoapServer;

import java.awt.*;

/**
 * Created by ynh on 11/01/16.
 */
public class LifxServer extends LightBulbServer {
	private final LIFXBulb bulb;

	public LifxServer(String mac) {
		super(false);
		bulb = new LIFXBulb(mac, "192.168.0.255");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				setColor(new RGBValue(0, 0, 0));
			}
		});
	}

	public static void main(String[] args) throws Exception {
		DeviceServer thingserver = new DeviceServer("/CH/ETH/CAB/51", false);
		thingserver.setName("LIFX Bulb");
		thingserver.start();
		thingserver.register();
		// create servr
		CoapServer server = new LifxServer("D0:73:D5:00:CC:57");
		server.start();
		int port = server.getEndpoints().get(0).getAddress().getPort();
		thingserver.link("lighting-state", "coap://localhost:" + port + "/light", Utils.getMediaType(LightingState.class));
		thingserver.link("power-consumption", "coap://localhost:" + port + "/power", Utils.getMediaType(PowerConsumptionState.class));
		System.out.printf(LightBulbServer.class.getSimpleName() + " listening on port %d.\n", port);
	}

	@Override
	public void setColor(RGBValue color) {
		super.setColor(color);
		if (bulb == null)
			return;
		if (color == null) {
			bulb.setColor(Color.BLACK);
		} else {
			System.out.println("Set COLOR");
			bulb.setColor(new Color(color.getR(), color.getG(), color.getB()));
		}
	}
}
