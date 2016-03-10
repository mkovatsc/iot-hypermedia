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
import ch.ethz.inf.vs.hypermedia.corehal.block.ThingCrawler;
import ch.ethz.inf.vs.hypermedia.corehal.block.ThingDescriptionFuture;
import ch.ethz.inf.vs.hypermedia.corehal.block.TimeFuture;

/**
 * Created by ynh on 20/11/15.
 */
public class TestTimeObserve {

    public static void main(String[] args) {
        HypermediaClient client = new HypermediaClient("coap://localhost:5783/");
        ThingDescriptionFuture thing = client.resources()
                .use(new ThingCrawler())
                .withLocationName("/CH/ETH/CAB/51")
                .findFirstWithLink("time");
		TimeFuture time = thing.follow("time", TimeFuture::new);
		System.out.println(time.tryGet().getTime());
		time.observe().onChange((x) -> {
			System.out.println(x.getTime());
		});
    }
}
