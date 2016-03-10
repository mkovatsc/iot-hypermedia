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
/**
 * Created by ynh on 24/09/15.
 */
package ch.ethz.inf.vs.hypermedia.hartke.lighting.client;

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.client.LinkListFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.BulletinBoardFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.CoREAppResourceFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.LightingConfigFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.ThingDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.CoREAppBase;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.LightingConfig;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Link;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;

public class LightRemoteControl extends CoapServer {
	static final String QUIT_COMMAND = "x|q|e|exit|quit";

	static String name = "LRC";
	static String purpose = "";

	static String entrypoint = "coap://localhost:5082/";

	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	static LRCStateResource state;
	static String baseURI;

	public static void main(String[] args) throws ExecutionException, InterruptedException, InstantiationException, IllegalAccessException, IOException {

		Scanner scanner = new Scanner(System.in);
		System.out.print("Thing name: ");
		name = scanner.nextLine().trim();
		System.out.print("Thing purpose: ");
		purpose = scanner.nextLine().trim();

		HypermediaClient client = new HypermediaClient("coap://127.0.0.1:5082/");
		client.discover().getByMediaType(BulletinBoardFuture::new);
		LinkListFuture discover = client.discover();

		// Read LightingConfig
		BulletinBoardFuture board = discover.getByMediaType(BulletinBoardFuture::new);
		ThingDescriptionFuture test = board.getThingByName("test");
		LightingConfigFuture config = test.getLightingConfig();
		System.out.println("=== Initial config ===");
		debug(config);

		LightRemoteControl lrc = new LightRemoteControl();

		for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
			if (addr instanceof Inet6Address) {
				continue;
			}
			if (!addr.isLoopbackAddress() &&
				!addr.isLinkLocalAddress() &&
//				!addr.isSiteLocalAddress() &&
				!addr.isAnyLocalAddress()) {
				lrc.addEndpoint(new CoapEndpoint(new InetSocketAddress(addr, 0)));
			}
		}

		state = new LRCStateResource("state");
		lrc.add(state);
		lrc.start();

		baseURI = "coap://" + lrc.getEndpoints().get(0).getAddress().getAddress().getHostAddress() + ":" + lrc.getEndpoints().get(0).getAddress().getPort();

		// Update Lighting getLightingConfig
		LightingConfig cfg = new LightingConfig();

		cfg.setSrc(new Link(baseURI + "/state"));
		config.update(cfg);
		Thread.sleep(1000);
		// Reload getLightingConfig from thing
		config.reset(false);
		System.out.println("=== New config ===");
		debug(config);

		System.out.println("=== Control ===");
		String input = "";
		scanner.reset();
		while (true) {
			try {
				System.out.print("Hue [float]: ");
				input = scanner.next().trim();
				if (input.matches(QUIT_COMMAND))
					break;
				float h = Float.parseFloat(input);

				System.out.print("Saturation [float]: ");
				input = scanner.next().trim();
				if (input.matches(QUIT_COMMAND))
					break;
				float s = Float.parseFloat(input);

				System.out.println(h);
				System.out.println(s);

				state.controlHS(h, s);
			} catch (NumberFormatException e) {
				System.out.println("Please enter a float.");
			}
		}
		scanner.close();
		lrc.destroy();
		System.exit(0);
	}

	public void registerSelf() {
		try {
			new HypermediaClient(entrypoint).discover().getByMediaType(BulletinBoardFuture::new).getFormRequest("create-item", getThingDescription(), false).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ThingDescription getThingDescription() throws URISyntaxException {
		ThingDescription item = new ThingDescription();
		item.setBase("coap://" + getEndpoints().get(0).getAddress().getAddress().getHostAddress() + ":" + getEndpoints().get(0).getAddress().getPort());
		item.setName(name);
		item.setPurpose(purpose);
		item.addLink("about", new Link(state.getURI(), "application/lighting+json"));
		return item;
	}

	public <V> Supplier<V> cached(Supplier<V> fn) {
		AtomicReference<V> cache = new AtomicReference<>();
		return () -> {
			if (cache.get() == null) {
				cache.set(fn.get());
			}
			return cache.get();
		};
	}

	private static <V extends CoREAppBase> void debug(CoREAppResourceFuture<V> b) throws ExecutionException, InterruptedException {
		V item = b.get();
		System.err.flush();
		System.out.println(">>> " + b.getUrl());
		System.out.println(gson.toJson(item));
		System.out.flush();
	}

	@Override
	public void start() {
		super.start();
		registerSelf();
	}
}
