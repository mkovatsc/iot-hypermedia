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

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.BulletinBoardFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.LightingConfig;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Link;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.utils.devices.DevicePanel;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Created by ynh on 24/09/15.
 */
public class LightServer extends CoapServer {

	static final String ENTRYPOINT = "coap://localhost:5082/";

	public static boolean test;

	private final String name;
	private final String purpose;
	private final LightingConfigResource config;
	private final boolean inline;

	private final String entrypoint;

	private static DevicePanel led;
	private static Color color = Color.white;
	private static boolean on = false;

	@SuppressWarnings("serial")
	public LightServer(int port, String name, String purpose, String configName, Class<? extends LightingConfigResource> config_type, boolean inline, String entrypoint, boolean test) throws Exception {
		LightServer.test = test;
		if (test) {
			addEndpoint(TestConnector.getEndpoint(port));
		} else {
			for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
				if (addr instanceof Inet6Address) {
					continue;
				}
				if (!addr.isLoopbackAddress() &&
					!addr.isLinkLocalAddress() &&
					!addr.isSiteLocalAddress() &&
					!addr.isAnyLocalAddress()) {

					addEndpoint(new CoapEndpoint(new InetSocketAddress(addr, port)));
				}
			}
		}
		this.inline = inline;
		this.name = name;
		this.purpose = purpose;
		this.entrypoint = entrypoint;

		config = config_type.getDeclaredConstructor(String.class, LightServer.class).newInstance(configName, this);

		add(config);
		add(new CoapResource("monitor") {
			@Override
			public void handleGET(CoapExchange exchange) {
				exchange.respond(ResponseCode.CONTENT, color.toString(), MediaTypeRegistry.TEXT_PLAIN);
			}
		});

		if (!test) {
			led = new DevicePanel(getClass().getResourceAsStream("candle_400.png"), 240, 400) {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(super.img, 0, 0, getWidth(), getHeight(), this);

					if (on) {
						Graphics2D g2 = (Graphics2D) g;

						Color[] gradient = { new Color(color.getRed(), color.getGreen(), color.getBlue(), 240), new Color(color.getRed(), color.getGreen(), color.getBlue(), 200), new Color(color.getRed(), color.getGreen(), color.getBlue(), 0) };
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

	public static void setColor(Color c) {
		color = c;
		led.repaint();
	}

	public static void setOnOff(boolean b) {
		on = b;
	}

	public static void main(String[] args) throws Exception {
		int port = 0;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		Scanner scanner = new Scanner(System.in);
		System.out.print("Thing name: ");
		String name = scanner.nextLine().trim();
		System.out.print("Thing purpose: ");
		String purpose = scanner.nextLine().trim();
		System.out.flush();

		InlineConfig inlineConfig = Utils.selectFromEnum(InlineConfig.class, scanner, "Inline");
		ConfigURL configURLMode = Utils.selectFromEnum(ConfigURL.class, scanner, "Config URL");
		UpdateType updateMode = Utils.selectFromEnum(UpdateType.class, scanner, "Update Type");
		LightServer server = start(port, name, purpose, inlineConfig, configURLMode, updateMode, ENTRYPOINT, false);
		System.out.printf(LightServer.class.getSimpleName() + " " + name + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
		scanner.close();
	}

	public static LightServer start(int port, String name, String purpose, InlineConfig inlineConfig, ConfigURL configURLMode, UpdateType updateMode, String entrypoint, boolean test) throws Exception {
		String configURL = null;
		boolean inline = inlineConfig == InlineConfig.YES;
		switch (configURLMode) {
		case CONFIG:
			configURL = "config";
			break;
		case RANDOM:
			configURL = UUID.randomUUID().toString();
			break;
		}

		Class<? extends LightingConfigResource> configType = null;
		switch (updateMode) {
		case NORMAL:
			configType = LightingConfigResource.class;
			break;
		case SEPARATE_RESOURCE:
			configType = RandomLightingConfig.class;
			break;
		case OTHER_SERVER:
			configType = DecoupledLightingConfig.class;
			break;
		case OTHER_SERVER2:
			configType = DecoupledLightingConfig2.class;
			break;
		}

		LightServer server = new LightServer(port, name, purpose, configURL, configType, inline, entrypoint, test);
		server.start();
		return server;
	}

	@Override
	public void start() {
		super.start();
		registerSelf();
	}

	@Override
	public void stop() {
		config.stop();
		super.stop();
	}

	public ThingDescription getThingDescription() throws URISyntaxException {
		ThingDescription item = new ThingDescription();
		item.setBase("coap://" + getEndpoints().get(0).getAddress().getAddress().getHostAddress() + ":" + getEndpoints().get(0).getAddress().getPort());
		item.setName(name);
		item.setPurpose(purpose);
		if (isInlined()) {
			LightingConfig cfg = config.getConfig();
			cfg.setBase(Utils.resolve(config.getURI(), cfg.getBase()));
			item.addEmbedded("config", cfg);
		}
		item.addLink("config", new Link(config.getURI(), "application/lighting-config+json"));
		return item;
	}

	public void registerSelf() {
		try {
			System.out.println("Registry entry point " + entrypoint);
			new HypermediaClient(entrypoint).discover().getByMediaType(BulletinBoardFuture::new).getFormRequest("create-item", getThingDescription(), false).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isInlined() {
		return inline;
	}

	public enum ConfigURL {
		CONFIG, RANDOM
	}

	public enum UpdateType {
		NORMAL, SEPARATE_RESOURCE, OTHER_SERVER, OTHER_SERVER2
	}

	public enum InlineConfig {
		NO, YES
	}

}
