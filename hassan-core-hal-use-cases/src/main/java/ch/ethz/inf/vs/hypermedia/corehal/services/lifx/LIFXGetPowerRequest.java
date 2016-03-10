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
package ch.ethz.inf.vs.hypermedia.corehal.services.lifx;

/**
 * Created by wilhelmk on 02/10/15.
 */
public class LIFXGetPowerRequest extends LIFXRequest {

	public LIFXGetPowerRequest(byte[] address, int delay) {
		super(address, delay);
	}

	@Override
	int getRequestType() {
		return LightMessage.GetPower;
	}

	@Override
	byte[] generatePayload() {
		return new byte[0];
	}
}
