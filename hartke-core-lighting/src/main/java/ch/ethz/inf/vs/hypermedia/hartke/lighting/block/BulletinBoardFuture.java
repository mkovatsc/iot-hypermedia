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
package ch.ethz.inf.vs.hypermedia.hartke.lighting.block;

import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.BulletinBoard;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.ThingDescription;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 26/09/15.
 */
public class BulletinBoardFuture extends CoREAppResourceFuture<BulletinBoard> {

	public Collection<ThingDescription> getThingDescriptions() throws ExecutionException, InterruptedException {
		return get().getEmbeddedStream("item", ThingDescription.class);
	}

	public ThingDescriptionFuture getThingByName(String name) {
		ThingDescriptionFuture thing = new ThingDescriptionFuture();
		thing.addParent(this);
		thing.setPreProcess(() -> {
			ThingDescription item = getThingDescriptions()
					.stream()
					.sequential()
					.filter(x -> x.getName().equals(name))
					.findFirst()
					.get();
			thing.setFromSource(item, getUrl(), this);
		});
		return thing;
	}
}
