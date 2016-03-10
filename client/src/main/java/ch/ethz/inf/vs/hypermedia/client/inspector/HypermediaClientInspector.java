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
package ch.ethz.inf.vs.hypermedia.client.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ynh on 09/11/15.
 */
public class HypermediaClientInspector {
	static AtomicInteger idCounter = new AtomicInteger();
	static List<RequestInspector> requestInspectors = new ArrayList<>();

	public static void add(Object service) {
		if (service instanceof RequestInspector) {
			addRequestInspector((RequestInspector) service);
		}
	}

	public static void clear() {
		requestInspectors.clear();
	}

	public static void addRequestInspector(RequestInspector service) {
		requestInspectors.add(service);
	}

	public static int startRequest(String method, String url, int payloadContentType, String payload) {
		if (requestInspectors != null) {
			int id = idCounter.incrementAndGet();
			for (RequestInspector req : requestInspectors) {
				req.startRequest(id, method, url, payloadContentType, payload);
			}
			return id;
		}
		return -1;
	}

	public static void endRequest(int id, boolean failed) {
		for (RequestInspector req : requestInspectors) {
			req.endRequest(id, failed);
		}
	}
}
