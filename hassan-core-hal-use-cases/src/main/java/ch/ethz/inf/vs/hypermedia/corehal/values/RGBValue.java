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

import java.awt.*;

/**
 * Created by ynh on 30/11/15.
 */
public class RGBValue extends LightingValue {
	private int r;
	private int g;
	private int b;

	public RGBValue() {

	}

	public RGBValue(int r, int g, int b) {
		this.r = r;
		this.g = g;

		this.b = b;
	}

	public RGBValue(int rgb) {
		Color c = new Color(rgb);
		r = c.getRed();
		g = c.getGreen();
		b = c.getBlue();
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}
}
