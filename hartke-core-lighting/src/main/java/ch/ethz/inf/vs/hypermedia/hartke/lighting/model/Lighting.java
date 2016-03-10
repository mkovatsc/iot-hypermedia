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

/**
 * Created by ynh on 25/09/15.
 */
@MediaType(contentType = 65203, mediaType = "application/lighting+json")
public class Lighting {

	public static final String COLOR_MODE_HS = "hs";
	public static final String COLOR_MODE_XY = "xy";
	public static final String COLOR_MODE_CT = "ct";

	private boolean on;
	private float brightness;
	private float hue;
	private float saturation;
	private float x;
	private float y;
	private float colorTemperature;
	private String colorMode;

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public float getHue() {
		return hue;
	}

	public void setHue(float hue) {
		this.hue = hue;
	}

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getColorTemperature() {
		return colorTemperature;
	}

	public void setColorTemperature(float colorTemperature) {
		this.colorTemperature = colorTemperature;
	}

	public String getColorMode() {
		return colorMode;
	}

	public void setColorMode(String colorMode) {
		this.colorMode = colorMode;
	}

}
