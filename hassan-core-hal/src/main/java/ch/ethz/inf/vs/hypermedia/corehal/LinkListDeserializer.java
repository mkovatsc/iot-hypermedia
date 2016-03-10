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
package ch.ethz.inf.vs.hypermedia.corehal;

import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ynh on 26/11/15.
 */
public class LinkListDeserializer implements JsonDeserializer<LinkList>, JsonSerializer<LinkList> {
	@Override
	public LinkList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		LinkList list = new LinkList();
		if (json.isJsonArray()) {
			for (JsonElement el : json.getAsJsonArray()) {
				list.add(context.deserialize(el, Link.class));
			}
		} else {
			list.add(context.deserialize(json, Link.class));
		}
		return list;
	}

	@Override
	public JsonElement serialize(LinkList src, Type typeOfSrc, JsonSerializationContext context) {
		Set<Link> values = new HashSet<Link>();
		for (Link item : src.linkValues()) {
			values.add(item);
		}
		if (values.isEmpty()) {
			return null;
		}
		if (values.size() == 1) {
			return OptionalListDeserializer.cleanup(context.serialize(values.iterator().next()));
		}
		return OptionalListDeserializer.cleanup(context.serialize(values));
	}
}
