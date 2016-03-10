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

import ch.ethz.inf.vs.hypermedia.corehal.model.LightingState;
import ch.ethz.inf.vs.hypermedia.corehal.values.HSVValue;
import ch.ethz.inf.vs.hypermedia.corehal.values.RGBValue;

import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 06/11/15.
 */
public class LightingStateFuture extends CoREHalResourceFuture<LightingState> {

	public LightingStateFuture getHSV() {
		return getByType("hsv");
	}

	public LightingStateFuture getRGB() {
		return getByType("rgb");
	}

	public LightingStateFuture getCIE() {
		return getByType("cie");
	}

	public LightingStateFuture getTemperature() {
		return getByType("temperature");
	}

	public LightingStateFuture getBrightness() {
		return getByType("brightness");
	}

	public LightingStateFuture getObserve() {
		return getByType("observe");
	}

	/**
	 * Find lighting state representation with the given value type
	 * 
	 * @param type
	 * @return
	 */
	public LightingStateFuture getByType(String type) {
		LightingStateFuture future = new LightingStateFuture();
		future.setPreProcess(() -> {
			if (tryGet().getType().equals(type)) {
				future.link(this);
			} else {
				future.link(follow("same-as", type, LightingStateFuture::new));
			}
		});
		return future;
	}

	public void setRGB(int r, int g, int b) throws ExecutionException, InterruptedException {
		LightingState lightingState = new LightingState();
		lightingState.setValue(new RGBValue(r, g, b));
		update(lightingState);
	}

	public void setHSV(int h, double s, double v) throws ExecutionException, InterruptedException {
		LightingState lightingState = new LightingState();
		lightingState.setValue(new HSVValue(h, s, v));
		update(lightingState);
	}

	public void update(LightingState lightingState) throws ExecutionException, InterruptedException {
		getFormRequest("edit", lightingState.getType(), lightingState).get();
	}
}
