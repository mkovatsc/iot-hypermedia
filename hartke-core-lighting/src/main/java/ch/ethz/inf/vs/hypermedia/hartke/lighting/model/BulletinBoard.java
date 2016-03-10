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
package ch.ethz.inf.vs.hypermedia.hartke.lighting.model;

import ch.ethz.inf.vs.hypermedia.client.MediaType;
import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by ynh on 25/09/15.
 */
@MediaType(contentType = 65201, mediaType = "application/bulletin-board+json")
public class BulletinBoard extends CoREAppBase {

	public <V> List<V> getEmbeddedStream(String key, Class<V> type) {
		Iterable<Object> items = (Iterable<Object>) getEmbedded(key);

		return StreamSupport.stream(items.spliterator(), false)
				.map(item -> ResourceFuture.gson.fromJson(ResourceFuture.gson.toJson(item), type))
				.collect(Collectors.toList());
	}

	private Object getEmbedded(String key) {
		return getEmbedded().get(key);
	}
}
