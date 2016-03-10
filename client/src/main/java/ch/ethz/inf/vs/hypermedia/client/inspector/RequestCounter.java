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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ynh on 09/11/15.
 */
public class RequestCounter implements RequestInspector {
	AtomicInteger runningRequests = new AtomicInteger();
	AtomicInteger requests = new AtomicInteger();
	AtomicInteger failedRequests = new AtomicInteger();

	public int getRequests() {
		return requests.get();
	}

	public int getRunningRequests() {
		return runningRequests.get();
	}

	public int getFailedRequests() {
		return failedRequests.get();
	}

	@Override
	public void startRequest(int id, String method, String url, int payloadContentType, String payload) {
		requests.incrementAndGet();
		runningRequests.incrementAndGet();
	}

	@Override
	public void endRequest(int id, boolean failed) {
		runningRequests.decrementAndGet();
		if (failed) {
			failedRequests.incrementAndGet();
		}
	}

	public void reset() {
		runningRequests.set(0);
		requests.set(0);
		failedRequests.set(0);
	}
}
