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
import ch.ethz.inf.vs.hypermedia.corehal.model.Form;
import ch.ethz.inf.vs.hypermedia.corehal.model.HeatingState;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import ch.ethz.inf.vs.hypermedia.corehal.model.PowerConsumptionState;
import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ynh on 05/11/15.
 */
public class ThermostatServer extends CoapServer {

	private final CelsiusState celsiusState;
	private final PowerConsumptionResource powerConsumptionResource;
	private final Timer timer;
	private double outsideTemperature = 10;
	private double lasttemperature = 21;
	private double temperature = 21;
	private double targetTemperature = 21;
	boolean heating = false;

	public ThermostatServer() {
		addEndpoint(new CoapEndpoint());
		celsiusState = new CelsiusState();
		powerConsumptionResource = new PowerConsumptionResource();
		add(powerConsumptionResource);
		add(celsiusState);
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				temperature = temperature * 0.998 + outsideTemperature * 0.002;
				double abs = Math.abs(targetTemperature - temperature);
				if (abs > 1) {
					heating = true;
					powerConsumptionResource.getPower().setValue(1000);
				}
				if (abs < 0.1) {
					heating = false;
					powerConsumptionResource.getPower().setValue(100);
				}
				if (heating) {
					temperature = temperature + 0.3 * Math.signum(targetTemperature - temperature);
				}
				if (Math.abs(lasttemperature - temperature) > 1) {
					lasttemperature = temperature;
					celsiusState.changed();
				}
			}
		}, 1000, 1000);

	}

	public double convertToFahrenheit(double celsius) {
		return (9.0 / 5.0) * celsius + 32;
	}

	public double fahrenheitToCelsius(double fahrenheit) {
		return (fahrenheit - 32) / 9.0 * 5.0;
	}

	public static void main(String[] args) throws Exception {
		DeviceServer thingserver = new DeviceServer("/CH/ETH/CAB/51", false);
		thingserver.setName("ThermostatServer");
		thingserver.start();
		thingserver.register();
		// create servr
		CoapServer server = new ThermostatServer();
		server.start();
		int port = server.getEndpoints().get(0).getAddress().getPort();
		thingserver.link("heating-state", "coap://localhost:" + port + "/heating", Utils.getMediaType(HeatingState.class));
		thingserver.link("power-consumption", "coap://localhost:" + port + "/power", Utils.getMediaType(PowerConsumptionState.class));
		System.out.printf(ThermostatServer.class.getSimpleName() + " listening on port %d.\n", port);
	}

	public class CelsiusState extends CoapResource {

		private final FahrenheitState fahrenheitState;

		public CelsiusState() {
			super("heating");
			fahrenheitState = new FahrenheitState();
			add(fahrenheitState);
			setObservable(true);
			getAttributes().addContentType(Utils.getContentType(HeatingState.class));
		}

		public HeatingState getHeatingState() {
			HeatingState heatingState = new HeatingState();
			heatingState.setTemperature(temperature);
			heatingState.setTargetTemperature(targetTemperature);
			heatingState.setUnit("celsius");
			heatingState.setSelf(getURI());
			heatingState.addLink("same-as", new Link(fahrenheitState.getURI(), Utils.getMediaType(HeatingState.class)).setNames("fahrenheit"));
			heatingState.addForm("edit", new Form("POST", "", Utils.getMediaType(HeatingState.class)).setNames("celsius"));
			heatingState.addForm("edit", new Form("POST", fahrenheitState.getURI(), Utils.getMediaType(HeatingState.class)).setNames("fahrenheit"));
			return heatingState;
		}

		@Override
		public void changed() {
			System.out.println("Changed");
			super.changed();
			fahrenheitState.changed();
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getHeatingState()), Utils.getContentType(HeatingState.class));
		}

		@Override
		public void handlePOST(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			try {
				HeatingState item = CoREHalResourceFuture.getGson().fromJson(postBody, HeatingState.class);
				assert item.getUnit().equals("celsius");
				double celsius = item.getTargetTemperature();
				assert celsius > 12 && celsius < 30;
				targetTemperature = celsius;
				celsiusState.changed();
				exchange.respond(CoAP.ResponseCode.CHANGED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
			}
		}

	}

	public class FahrenheitState extends CoapResource {

		public FahrenheitState() {
			super("fahrenheit");
			setObservable(true);
			getAttributes().addContentType(Utils.getContentType(HeatingState.class));
		}

		public HeatingState getHeatingState() {
			HeatingState heatingState = new HeatingState();
			heatingState.setTemperature(convertToFahrenheit(temperature));
			heatingState.setTargetTemperature(convertToFahrenheit(targetTemperature));
			heatingState.addLink("same-as", new Link(celsiusState.getURI(), Utils.getMediaType(HeatingState.class)).setNames("celsius"));
			heatingState.setUnit("fahrenheit");
			heatingState.addForm("edit", new Form("POST", celsiusState.getURI(), Utils.getMediaType(HeatingState.class)).setNames("celsius"));
			heatingState.addForm("edit", new Form("POST", "", Utils.getMediaType(HeatingState.class)).setNames("fahrenheit"));
			heatingState.setSelf(getURI());
			return heatingState;
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getHeatingState()), Utils.getContentType(HeatingState.class));
		}

		@Override
		public void handlePOST(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			try {
				HeatingState item = CoREHalResourceFuture.getGson().fromJson(postBody, HeatingState.class);
				assert item.getUnit().equals("fahrenheit");
				double celsius = fahrenheitToCelsius(item.getTargetTemperature());
				assert celsius > 12 && celsius < 30;
				targetTemperature = celsius;
				celsiusState.changed();
				exchange.respond(CoAP.ResponseCode.CHANGED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
			}
		}
	}
}
