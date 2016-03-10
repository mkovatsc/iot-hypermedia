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
public class CIEValue extends LightingValue {
	private double x;
	private double y;
	private double luminosity;

	public CIEValue() {

	}

	public CIEValue(double x, double y, double luminosity) {
		this.x = x;
		this.y = y;
		this.luminosity = luminosity;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getLuminosity() {
		return luminosity;
	}

	public void setLuminosity(double luminosity) {
		this.luminosity = luminosity;
	}
}
