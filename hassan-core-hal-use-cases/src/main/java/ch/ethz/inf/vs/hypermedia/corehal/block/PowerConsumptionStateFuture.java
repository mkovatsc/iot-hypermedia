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
package ch.ethz.inf.vs.hypermedia.corehal.block;

import ch.ethz.inf.vs.hypermedia.corehal.model.HeatingState;
import ch.ethz.inf.vs.hypermedia.corehal.model.PowerConsumptionState;

/**
 * Created by ynh on 06/11/15.
 */
public class PowerConsumptionStateFuture extends CoREHalResourceFuture<PowerConsumptionState> {

	public PowerConsumptionStateFuture getWatt() {
		return getByUnit("watt");
	}

	/**
	 * Find power state representation with the given value unit
	 * 
	 * @param type
	 * @return
	 */
	public PowerConsumptionStateFuture getByUnit(String type) {
		PowerConsumptionStateFuture future = new PowerConsumptionStateFuture();
		future.setPreProcess(() -> {
			if (tryGet().getUnit().equals(type)) {
				future.link(this);
			} else {
				future.link(follow("same-as", type, PowerConsumptionStateFuture::new));
			}
		});
		return future;
	}
}
