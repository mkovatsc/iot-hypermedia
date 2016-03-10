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
package ch.ethz.inf.vs.hypermedia.corehal.values;

/**
 * Created by ynh on 30/11/15.
 */
public class TemperatureValue extends LightingValue {
	private int kelvin;
	private double saturation;

	public TemperatureValue() {

	}

	public TemperatureValue(int kelvin, double saturation) {
		this.kelvin = kelvin;
		this.saturation = saturation;
	}

	public double getSaturation() {
		return saturation;
	}

	public void setSaturation(double saturation) {
		this.saturation = saturation;
	}

	public int getKelvin() {
		return kelvin;
	}

	public void setKelvin(int kelvin) {
		this.kelvin = kelvin;
	}
}
