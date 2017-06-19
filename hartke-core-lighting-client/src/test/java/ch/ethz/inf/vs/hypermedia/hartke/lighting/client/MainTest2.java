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

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.client.LinkListFuture;
import ch.ethz.inf.vs.hypermedia.client.TestUtils;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.BulletinBoardFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.LightingConfigFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.ThingDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.LightingConfig;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Link;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.server.BulletinBoardServer;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.server.BulletinBoardServer.BoardType;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.server.LightServer;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.server.LightServer.ConfigURL;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.server.LightServer.InlineConfig;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.server.LightServer.UpdateType;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class MainTest2 {

	private final InlineConfig inlineConfig;
	private final ConfigURL configURL;
	private final UpdateType updateMode;
	private final BoardType boardType;

	public MainTest2() {
		inlineConfig = InlineConfig.NO;
		configURL = ConfigURL.RANDOM;
		updateMode = UpdateType.NORMAL;
		boardType = BoardType.BOARD1;
		TestUtils.setupEnviroment();
	}

	@Ignore
	@Test
	public void testUpdateAlt() throws Exception {
		String src = UUID.randomUUID().toString();

		BulletinBoardServer bulletinServer = BulletinBoardServer.start(0, boardType, true);
		String endpoint = String.format("coap://localhost:%d/", bulletinServer.getPort());
		LightServer light = LightServer.start(0, "test", "test", inlineConfig, configURL, updateMode, endpoint, true);

		HypermediaClient client = new HypermediaClient(endpoint);
		LinkListFuture discover = client.discover();
		// Read LightingConfig
		ThingDescriptionFuture thing = Utils.or(ThingDescriptionFuture::new, FailFuture.fail(ThingDescriptionFuture::new), discover.getByMediaType(BulletinBoardFuture::new).getThingByName("test"));
		LightingConfigFuture config = thing.getLightingConfig();

		// Update Lighting getLightingConfig
		LightingConfig cfg = new LightingConfig();
		cfg.setSrc(new Link(src));
		config.update(cfg);

		// Reload getLightingConfig from thing
		config.reset(false);
		String current_src = config.get().getSrc().getHref();
		assertTrue(Objects.equals(current_src, src));

		bulletinServer.stop();
		bulletinServer.destroy();
		light.stop();
		light.destroy();
	}

	@Ignore
	@Test
	public void testUpdateAlt2() throws Exception {
		String src = UUID.randomUUID().toString();

		BulletinBoardServer bulletinServer = BulletinBoardServer.start(0, boardType, true);
		String endpoint = String.format("coap://localhost:%d/", bulletinServer.getPort());
		LightServer light = LightServer.start(0, "test", "test", inlineConfig, configURL, updateMode, endpoint, true);

		HypermediaClient client = new HypermediaClient(endpoint);
		LinkListFuture discover = client.discover();
		// Read LightingConfig
		ThingDescriptionFuture thing = Utils.or(ThingDescriptionFuture::new, FailFuture.fail(ThingDescriptionFuture::new), discover.getByMediaType(BulletinBoardFuture::new).getThingByName("test"));
		LightingConfigFuture config = thing.getLightingConfig();

		// Update Lighting getLightingConfig
		LightingConfig cfg = new LightingConfig();
		cfg.setSrc(new Link(src));
		config.update(cfg);

		// Reload getLightingConfig from thing
		config.reset(false);
		String current_src = config.get().getSrc().getHref();
		assertTrue(Objects.equals(current_src, src));

		bulletinServer.stop();
		light.stop();
	}
}