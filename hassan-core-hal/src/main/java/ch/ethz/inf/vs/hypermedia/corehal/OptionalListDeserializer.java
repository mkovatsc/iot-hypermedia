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

import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ynh on 26/11/15.
 */
public class OptionalListDeserializer implements JsonDeserializer<OptionalList>, JsonSerializer<OptionalList> {
	public static JsonElement cleanup(JsonElement serialize) {
		if (serialize.isJsonObject()) {
			serialize.getAsJsonObject().entrySet().removeIf(x -> x.getValue().isJsonNull());
			serialize.getAsJsonObject().entrySet().forEach(x -> cleanup(x.getValue()));
		}
		if (serialize.isJsonArray()) {
			serialize.getAsJsonArray().forEach(OptionalListDeserializer::cleanup);
		}
		return serialize;
	}

	@Override
	public OptionalList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		OptionalList list = new OptionalList();
		Type elementType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
		if (json.isJsonArray()) {
			for (JsonElement el : json.getAsJsonArray()) {
				list.add(context.deserialize(el, elementType));
			}
		} else {
			list.add(context.deserialize(json, elementType));
		}
		return list;
	}

	@Override
	public JsonElement serialize(OptionalList values, Type typeOfSrc, JsonSerializationContext context) {
		if (values.isEmpty()) {
			return null;
		}
		if (values.size() == 1) {
			return cleanup(context.serialize(values.iterator().next()));
		}
		return cleanup(context.serialize(values, ArrayList.class));
	}
}
