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
package ch.ethz.inf.vs.hypermedia.client.observe;

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.client.LoadableFuture;
import com.google.common.base.Preconditions;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ynh on 20/11/15.
 */
public class CoapObserver<V> extends Observer<V> {
	static final int READY = 0;
	static final int INPROGRESS = 1;
	static final int STARTED = 2;
	private final ObservableFuture<V> parent;
	private AtomicInteger started = new AtomicInteger();
	private CoapObserveRelation relation;
	private CoapClient client;
	private ScheduledFuture<?> task;

	public CoapObserver(ObservableFuture<V> parent) {
		Preconditions.checkArgument(parent instanceof LoadableFuture);
		this.parent = parent;
		HypermediaClient.addShutdownHook(() -> stop());
	}

	@Override
	void start() {
		if (started.compareAndSet(READY, STARTED)) {
			client = new CoapClient(parent.getResourceUrl());
			client.setTimeout(2000);
			ping();
			relation = client.observe(new CoapHandler() {
				@Override
				public void onLoad(CoapResponse response) {
					try {
						trigger(((LoadableFuture<V>) parent).deserialize(response.getResponseText()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onError() {
				}
			});
			started.set(STARTED);
		}

	}

	private void ping() {
		task = HypermediaClient.schedule(() -> {
			CoapClient client = this.client;
			if (client != null) {
				if (!client.ping()) {
					reload();
					return;
				}
			}
			ping();
		} , 10000, TimeUnit.MILLISECONDS);

	}

	private void reload() {
		HypermediaClient.execute(() -> {
			stop();
			((LoadableFuture<V>) parent).reset(false);
			start();
		});
	}

	@Override
	public void stop() {
		if (started.compareAndSet(STARTED, INPROGRESS)) {
			task.cancel(true);
			client = null;
			relation.proactiveCancel();
			started.set(READY);
		}
	}
}
