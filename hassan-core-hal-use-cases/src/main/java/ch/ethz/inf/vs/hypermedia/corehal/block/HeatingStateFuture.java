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

import ch.ethz.inf.vs.hypermedia.client.CoapRequestFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.HeatingState;
import ch.ethz.inf.vs.hypermedia.corehal.model.ThingDescription;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 06/11/15.
 */
public class HeatingStateFuture extends CoREHalResourceFuture<HeatingState> {


//    public HeatingStateFuture getCelsius() {
//        return getByUnit("celsius");
//    }
//
//    public HeatingStateFuture getFahrenheit() {
//        return getByUnit("fahrenheit");
//    }
//    /**
//     * Find heating state representation with the given value unit
//     * @param type
//     * @return
//     */
//    public HeatingStateFuture getByUnit(String type) {
//        HeatingStateFuture future = new HeatingStateFuture();
//        future.setPreProcess(() -> {
//            if (tryGet().getUnit().equals(type)) {
//                future.link(this);
//            } else {
//                future.link(follow("same-as", type, HeatingStateFuture::new));
//            }
//        });
//        return future;
//    }

    public void update(HeatingState targetTemperature) throws ExecutionException, InterruptedException {
        getFormRequest("edit", targetTemperature.getUnit(), targetTemperature).get();
    }

    public void setTargetTemperature(double value, String unit) throws ExecutionException, InterruptedException {
        HeatingState state = new HeatingState();
        state.setTargetTemperature(value);
        state.setUnit(unit);
        update(state);
    }
}
