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

import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.LightingConfig;

/**
 * Created by ynh on 30/09/15.
 */
public class DecoupledLightingConfig2 extends DecoupledLightingConfig {
	public DecoupledLightingConfig2(String name, LightServer srv) {
		super(name, srv);
	}

	@Override
	public LightingConfig getConfig() {
		int port = updateServer.getEndpoints().get(0).getAddress().getPort();
		LightingConfig config = new LightingConfig();
		config.setBase(String.format("coap://localhost:%d", port));
		config.addForm("update", new Form("PUT", updateForm.getURI(), Utils.getMediaType(LightingConfig.class)));
		if (src != null)
			config.setSrc(src);
		return config;
	}
}
