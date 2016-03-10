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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by ynh on 20/11/15.
 */
public abstract class Observer<V> {

	private List<Consumer<V>> listeners = new ArrayList<>();

	public Observer<V> onChange(Consumer<V> consumer) {
		start();
		listeners.add(consumer);
		return this;
	}

	public void trigger(V data) {
		HypermediaClient.execute(() -> {
			listeners.stream().forEach(c -> {
				try {
					c.accept(data);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			});
		});
	}

	abstract void start();

	public abstract void stop();

	public Observer<V> removeListener(Consumer<V> consumer) {
		listeners.remove(consumer);
		return this;
	}

	public Observer<V> removeAllListeners() {
		listeners.clear();
		return this;
	}
}
