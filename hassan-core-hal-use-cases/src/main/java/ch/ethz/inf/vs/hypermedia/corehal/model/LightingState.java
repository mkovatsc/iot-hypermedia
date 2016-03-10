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
package ch.ethz.inf.vs.hypermedia.corehal.model;

import ch.ethz.inf.vs.hypermedia.client.MediaType;
import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.corehal.values.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ynh on 05/11/15.
 */
@MediaType(contentType = 64522, mediaType = "application/x.lighting-state+json")
public class LightingState extends CoREHalBase {

	static Map<String, Class<? extends LightingValue>> valueTypes;

	static {
		valueTypes = new HashMap<>();
		registerValueType("rgb", RGBValue.class);
		registerValueType("temperature", TemperatureValue.class);
		registerValueType("hsv", HSVValue.class);
		registerValueType("brightness", BrightnessValue.class);
		registerValueType("cie", CIEValue.class);
		registerValueType("observe", ObserveValue.class);
	}

	private Object value;
	private String type;

	public static void registerValueType(String typeName, Class<? extends LightingValue> typeClass) {
		valueTypes.put(typeName, typeClass);
	}

	public LightingValue getValue() {
		if (!(value instanceof LightingValue)) {
			convertValue();
		}
		return (LightingValue) value;
	}

	public void setValue(Object value) {
		this.value = value;
		for (Map.Entry<String, Class<? extends LightingValue>> entry : valueTypes.entrySet()) {
			if (entry.getValue().isInstance(value)) {
				setType(entry.getKey());
				return;
			}

		}
		throw new RuntimeException("Unkown value type " + value.getClass().getSimpleName() + "\nPlease register value type using LightingState.registerValueType");
	}

	private void convertValue() {
		if (valueTypes.containsKey(getType())) {
			setValue(ResourceFuture.gson.fromJson(ResourceFuture.gson.toJsonTree(value), valueTypes.get(getType())));
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
