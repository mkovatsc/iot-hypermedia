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

import com.google.common.util.concurrent.ListenableFuture;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ynh on 06/11/15.
 */
public interface Future<V> extends ListenableFuture<V> {
	void addParent(BaseFuture parent);

	void addDependency(Future parent);

	Future<V> setPreProcess(ProcessFunction r);

	@Override
	V get() throws InterruptedException, ExecutionException;

	@Override
	V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException;

	void fetch();

	void fetchData() throws InterruptedException;

	void resolveFutures(Iterable<Future> futures) throws InterruptedException, ExecutionException;

	void process() throws Exception;

	boolean set(V value) throws InterruptedException;

	boolean setException(Throwable throwable) throws InterruptedException;

	@Override
	boolean cancel(boolean mayInterruptIfRunning);

	void cancelFetch();

	@Override
	boolean isDone();

	boolean isCompleted();

	@Override
	boolean isCancelled();

	void reset(boolean parent);

	void reset();

	void cleanup();

	Throwable getException();

	List<Future> getDependencies();

	List<Future> getParents();

	List<WeakReference<Future>> getChildern();

	String getStateName();

	String getClassName();

	void link(Future<V> option) throws InterruptedException, ExecutionException;

	int getQueueState();

	void resetFailed();
}
