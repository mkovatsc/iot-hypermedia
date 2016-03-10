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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by wilhelmk on 01/10/15.
 */
class LIFXSetPowerRequest extends LIFXRequest {
	private boolean powerLevel;

	public LIFXSetPowerRequest(byte[] address, int delay, boolean powerLevel) {
		super(address, delay);
		this.powerLevel = powerLevel;
	}

	protected byte[] generatePayload() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(6);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		if (powerLevel) {
			byteBuffer.putShort((short) 65535);
		} else {
			byteBuffer.putShort((short) 0);
		}
		// Set delay
		byteBuffer.putInt(delay);
		return byteBuffer.array();
	}

	@Override
	protected int getRequestType() {
		return LightMessage.SetPower;
	}
}
