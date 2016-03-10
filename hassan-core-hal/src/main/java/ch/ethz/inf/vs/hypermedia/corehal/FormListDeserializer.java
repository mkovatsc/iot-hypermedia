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

import ch.ethz.inf.vs.hypermedia.corehal.model.Form;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ynh on 26/11/15.
 */
public class FormListDeserializer implements JsonDeserializer<FormList>, JsonSerializer<FormList> {
	@Override
	public FormList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		FormList list = new FormList();
		if (json.isJsonArray()) {
			for (JsonElement el : json.getAsJsonArray()) {
				list.add(context.deserialize(el, Form.class));
			}
		} else {
			list.add(context.deserialize(json, Form.class));
		}
		return list;
	}

	@Override
	public JsonElement serialize(FormList src, Type typeOfSrc, JsonSerializationContext context) {
		Set<Form> values = new HashSet<>(src.values());
		if (values.isEmpty()) {
			return null;
		}
		if (values.size() == 1) {
			return OptionalListDeserializer.cleanup(context.serialize(values.iterator().next()));
		}
		return OptionalListDeserializer.cleanup(context.serialize(values));
	}

}
