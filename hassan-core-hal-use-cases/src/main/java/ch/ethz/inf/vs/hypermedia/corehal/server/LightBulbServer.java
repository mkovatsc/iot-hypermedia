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
import ch.ethz.inf.vs.hypermedia.corehal.block.LightingStateFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.*;
import ch.ethz.inf.vs.hypermedia.corehal.values.BrightnessValue;
import ch.ethz.inf.vs.hypermedia.corehal.values.HSVValue;
import ch.ethz.inf.vs.hypermedia.corehal.values.RGBValue;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.devices.utils.DevicePanel;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.awt.*;

/**
 * Created by ynh on 05/11/15.
 */
public class LightBulbServer extends CoapServer {

	public static final String LIGHTING_CONTEXT = "light";
	public static final String POWER_CONTEXT = "power";

	private final RGBLightState rgbLightState;
	private final HSVLightState hsvLightState;

	private final PowerConsumptionResource powerConsumptionResource;
	private final BrightnessLightState brightnessLightState;
	private RGBValue color = new RGBValue();

	private static DevicePanel led;
	private static boolean on = true;

	@SuppressWarnings("serial")
	public LightBulbServer(boolean test) {
		if (test) {
			addEndpoint(TestConnector.getEndpoint(0));
		} else {
			addEndpoint(new CoapEndpoint());
		}
		rgbLightState = new RGBLightState();
		hsvLightState = new HSVLightState();
		brightnessLightState = new BrightnessLightState();
		powerConsumptionResource = new PowerConsumptionResource();
		add(powerConsumptionResource);
		add(rgbLightState);
		add(hsvLightState);
		add(brightnessLightState);

		if (!test) {
			led = new DevicePanel(getClass().getResourceAsStream("candle_400.png"), 240, 400) {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(super.img, 0, 0, getWidth(), getHeight(), this);

					if (on) {
						Graphics2D g2 = (Graphics2D) g;

						Color[] gradient = { new Color(color.getR(), color.getG(), color.getB(), 240), new Color(color.getR(), color.getG(), color.getB(), 200), new Color(color.getR(), color.getG(), color.getB(), 0) };
						float[] fraction = { 0.0f, 0.5f, 1.0f };
						RadialGradientPaint p = new RadialGradientPaint(120, 120, 120, fraction, gradient);
						g2.setPaint(p);
						g2.fillOval(0, 0, 240, 240);
					}
				}
			};

			new DeviceFrame(led).setVisible(true);
		}
	}

	public static void main(String[] args) throws Exception {
		DeviceServer thingserver = new DeviceServer("/CH/ETH/CAB/51", false);
		thingserver.setName("LED Candle");
		thingserver.start();
		thingserver.register();
		// create server
		CoapServer server = new LightBulbServer(false);
		server.start();
		int port = server.getEndpoints().get(0).getAddress().getPort();
		thingserver.link("lighting-state", "coap://localhost:" + port + "/" + LIGHTING_CONTEXT, Utils.getMediaType(LightingState.class));
		thingserver.link("power-consumption", "coap://localhost:" + port + "/" + POWER_CONTEXT, Utils.getMediaType(PowerConsumptionState.class));
		System.out.printf(LightBulbServer.class.getSimpleName() + " listening on port %d.\n", port);
	}

	public static void setOnOff(boolean b) {
		on = b;
		if (led != null)
			led.repaint();
	}

	public RGBValue getColor() {
		return color;
	}

	public void setColor(RGBValue color) {
		this.color = color;
		if (led != null)
			led.repaint();
	}

	public class RGBLightState extends CoapResource {

		public RGBLightState() {
			super(LIGHTING_CONTEXT);
			setColor(new RGBValue(255, 0, 0));
			getAttributes().addContentType(Utils.getContentType(LightingState.class));
		}

		public LightingState getRGBState() {
			LightingState lightingState = getLightingState();
			lightingState.setValue(getColor());
			lightingState.setSelf(getURI());
			return lightingState;
		}

		@Override
		public void handlePUT(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			try {
				LightingState item = LightingStateFuture.getGson().fromJson(postBody, LightingState.class);
				assert item.getType().equals("rgb");
				setColor((RGBValue) item.getValue());
				exchange.respond(CoAP.ResponseCode.CHANGED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
			}
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getRGBState()), Utils.getContentType(LightingState.class));
		}
	}

	public HSVValue getHSVColor() {
		float[] hsv = new float[3];
		Color.RGBtoHSB(getColor().getR(), getColor().getG(), getColor().getB(), hsv);
		return new HSVValue((int) (hsv[0] * 360.0), hsv[1], hsv[2]);
	}

	public class HSVLightState extends CoapResource {

		public HSVLightState() {
			super("hsv");
			getAttributes().addContentType(Utils.getContentType(LightingState.class));
		}

		public LightingState getHSVState() {
			LightingState lightingState = getLightingState();
			lightingState.setValue(getHSVColor());
			lightingState.setSelf(getURI());
			return lightingState;
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getHSVState()), Utils.getContentType(LightingState.class));
		}

		@Override
		public void handlePUT(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			try {
				LightingState item = LightingStateFuture.getGson().fromJson(postBody, LightingState.class);
				assert item.getType().equals("hsv");
				HSVValue hsvColor = (HSVValue) item.getValue();
				setColor(hsvColor);
				exchange.respond(CoAP.ResponseCode.CHANGED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
			}
		}

	}

	public void setColor(HSVValue hsvColor) {
		int rgb = Color.HSBtoRGB(hsvColor.getHue() / 360.0f, (float) hsvColor.getSaturation(), (float) hsvColor.getBrightness());
		setColor(new RGBValue(rgb));
	}

	public class BrightnessLightState extends CoapResource {

		public BrightnessLightState() {
			super("brightness");
			getAttributes().addContentType(Utils.getContentType(LightingState.class));
		}

		public LightingState getBrightnessState() {
			LightingState lightingState = getLightingState();
			lightingState.setValue(getBrightness());
			lightingState.setSelf(getURI());
			return lightingState;
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(getBrightnessState()), Utils.getContentType(LightingState.class));
		}

		public BrightnessValue getBrightness() {
			float[] hsv = new float[3];
			Color.RGBtoHSB(getColor().getR(), getColor().getG(), getColor().getB(), hsv);
			return new BrightnessValue(hsv[2]);
		}

		@Override
		public void handlePUT(CoapExchange exchange) {
			String postBody = exchange.getRequestText();
			try {
				LightingState item = LightingStateFuture.getGson().fromJson(postBody, LightingState.class);
				assert item.getType().equals("brightness");
				BrightnessValue value = (BrightnessValue) item.getValue();
				HSVValue hsvColor = getHSVColor();
				hsvColor.setBrightness(value.getBrightness());
				setColor(hsvColor);
				exchange.respond(CoAP.ResponseCode.CHANGED);
			} catch (JsonParseException ex) {
				exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
			}
		}
	}

	public class PowerConsumptionResource extends CoapResource {

		private final PowerConsumptionState power;

		public PowerConsumptionResource() {
			super(POWER_CONTEXT);
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

	public LightingState getLightingState() {
		LightingState lightingState = new LightingState();
		lightingState.addLink("same-as", new Link(rgbLightState.getURI(), Utils.getMediaType(LightingState.class)).setNames("rgb"));
		lightingState.addLink("same-as", new Link(hsvLightState.getURI(), Utils.getMediaType(LightingState.class)).setNames("brightness"));
		lightingState.addLink("same-as", new Link(brightnessLightState.getURI(), Utils.getMediaType(LightingState.class)).setNames("hsv"));
		lightingState.addForm("edit", new Form("PUT", rgbLightState.getURI(), Utils.getMediaType(LightingState.class)).setNames("rgb"));
		lightingState.addForm("edit", new Form("PUT", hsvLightState.getURI(), Utils.getMediaType(LightingState.class)).setNames("hsv"));
		lightingState.addForm("edit", new Form("PUT", brightnessLightState.getURI(), Utils.getMediaType(LightingState.class)).setNames("brightness"));
		return lightingState;
	}
}
