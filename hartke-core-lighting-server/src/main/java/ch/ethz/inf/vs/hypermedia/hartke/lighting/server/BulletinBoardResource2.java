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
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.BulletinBoard;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;

/**
 * Created by ynh on 30/09/15.
 */
public class BulletinBoardResource2 extends BulletinBoardResource {

	public BulletinBoardResource2(String name, BulletinBoardServer srv) {
		super(name, srv);
	}

	@Override
	public BulletinBoard getBulletinBoard() {
		BulletinBoard bulletins = new BulletinBoard();
		bulletins.setBase(getURI());
		bulletins.addForm("create-item", new Form("POST", "", Utils.getMediaType(ThingDescription.class)));
		bulletins.addEmbedded("item", list.values());
		return bulletins;
	}
}
