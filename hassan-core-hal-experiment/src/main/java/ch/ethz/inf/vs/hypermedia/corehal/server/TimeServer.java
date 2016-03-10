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

import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.model.Time;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ynh on 05/11/15.
 */
public class TimeServer extends CoapServer {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final ThingDescriptionResource thingDescription;

	public TimeServer(boolean test) {
		if (test)
			addEndpoint(TestConnector.getEndpoint(0));
		else
			addEndpoint(new CoapEndpoint());
		thingDescription = new ThingDescriptionResource();
		add(thingDescription);
	}

	public static void main(String[] args) throws Exception {

		DeviceServer thingserver = new DeviceServer("/CH/ETH/CAB/51", false);
		thingserver.setName("Atomic Clock");
		thingserver.start();
		thingserver.register();
		// create server
		CoapServer server = new TimeServer(false);
		server.start();
		int port = server.getEndpoints().get(0).getAddress().getPort();
		thingserver.link("time", "coap://localhost:" + port + "/time", Utils.getMediaType(Time.class));
		System.out.printf(TimeServer.class.getSimpleName() + " listening on port %d.\n", port);

	}

	public class ThingDescriptionResource extends CoapResource {
		public ThingDescriptionResource() {
			super("time");
			setObservable(true);
			getAttributes().addContentType(Utils.getContentType(Time.class));
			schedule();
		}

		void schedule() {
			scheduler.schedule(() -> {
				changed();
				schedule();
			} , 1000, TimeUnit.MILLISECONDS);
		}

		public Time getTime() {
			Time time = new Time();
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
			String text = date.format(formatter);
			time.setTime(text);
			time.setSelf(getURI());
			return time;
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, ResourceFuture.gson.toJson(getTime()), Utils.getContentType(Time.class));
		}
	}
}
