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
package ch.ethz.inf.vs.hypermedia.client;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Created by ynh on 24/09/15.
 */
public class HypermediaClient {
	private static ExecutorService executor = Executors.newCachedThreadPool();
	private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
	private static ArrayList<Runnable> shutdownHooks = new ArrayList<>();
	private final String entrypoint;

	public HypermediaClient(String entrypoint) {
		this.entrypoint = entrypoint;
	}

	public LinkListFuture discover() {
		return new LinkListFuture(getDiscoverUrl());
	}

	private String getDiscoverUrl() {
		try {
			return Utils.resolve(entrypoint, "/.well-known/core");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public LinkListFuture resources() {
		return discover().resourceLookup();
	}

	public <V extends ResourceFuture> V get(Supplier<V> type) {
		V block = type.get();
		block.setPreProcess(() -> {
			block.setRequestURL(entrypoint);
		});
		return block;
	}

	public static void addShutdownHook(Runnable run) {
		shutdownHooks.add(run);
	}

	public static void shutdown() {
		shutdownHooks.forEach(x -> x.run());
		shutdownHooks.clear();
		executor.shutdown();
		scheduledExecutor.shutdown();
	}

	public static void execute(Runnable runnable) {
		executor.execute(runnable);
	}

	public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return scheduledExecutor.schedule(command, delay, unit);
	}
}
