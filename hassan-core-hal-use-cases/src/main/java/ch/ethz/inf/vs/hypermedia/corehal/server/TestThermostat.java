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
package ch.ethz.inf.vs.hypermedia.corehal.server;

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.client.LinkListFuture;
import ch.ethz.inf.vs.hypermedia.corehal.block.*;

import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 20/11/15.
 */
public class TestThermostat {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        HypermediaClient client = new HypermediaClient("coap://localhost:5783/");
        LinkListFuture resources = client.links();

        ThingDescriptionFuture thing = resources
                .use(new ThingCrawler())
                .findLocation("/CH/ETH/CAB/51")
                .findFirstWith("heating-state");

        HeatingStateFuture temperature = thing.follow("heating-state", HeatingStateFuture::new);

		temperature.setTargetTemperature(20, "Celsius");
		temperature.observe().onChange((x) -> {
			dump(x);
		});
    }

    private static void dump(Object value) {
        System.out.println(CoREHalResourceFuture.getGson().toJson(value));
    }
}
