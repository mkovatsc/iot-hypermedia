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

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ExecutionList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by ynh on 25/09/15.
 */
public abstract class BaseFuture<V> extends AbstractQueuedSynchronizer implements Future<V> {

	/* Valid states. */
	public static final int READY = 0;
	public static final int LOADING = 1;
	public static final int COMPLETING = 2;
	public static final int COMPLETED = 4;
	public static final int CANCELLED = 8;
	public static final int INTERRUPTED = 16;
	public static final int RESETTING = 32;
	public static final int RESETTING_LOADING = 64;
	public static final int PARTIALLY_LOADED = 128;
	public static final int COMPLETING_POST_PROCESS = 256;

	private static final long serialVersionUID = 0L;
	// The execution list to hold our executors.
	private final ExecutionList executionList = new ExecutionList();
	private final List<WeakReference<Future>> childern = new ArrayList<>();
	private final List<Future> dependencies = new ArrayList<>();
	public List<Future> parents = new ArrayList<>();
	protected Future<V> linkSource;
	private ProcessFunction preprocess;
	private ProcessFunction postprocess;
	private V value;
	private Throwable exception;
	private FutureTask<Object> fetchFuture;
	private int tries;

	/**
	 * Constructor for use by subclasses.
	 */
	public BaseFuture() {

	}

	static final CancellationException cancellationExceptionWithCause(String message, Throwable cause) {
		CancellationException exception = new CancellationException(message);
		exception.initCause(cause);
		return exception;
	}

	@Override
	public void addParent(BaseFuture parent) {
		if (getState() != READY)
			throw new RuntimeException("Invalid state");
		parent.childern.add(new WeakReference<>(this));
		parents.add(parent);
		tries = 1;
	}

	@Override
	public void addDependency(Future parent) {
		if (getState() != LOADING)
			throw new RuntimeException("Invalid state");
		dependencies.add(parent);
	}

	@Override
	public Future<V> setPreProcess(ProcessFunction r) {
		Preconditions.checkState(preprocess == null);
		preprocess = r;
		return this;
	}

	public boolean hasPreProcess() {
		return preprocess != null;
	}

	/*
	 * Acquisition succeeds if the future is done, otherwise it fails.
	 */
	@Override
	protected int tryAcquireShared(int ignored) {
		if (isDone() || getQueueState() == COMPLETING_POST_PROCESS) {
			return 1;
		}
		return -1;
	}

	/*
	 * We always allow a release to go through, this means the state has been
	 * successfully changed and the result is available.
	 */
	@Override
	protected boolean tryReleaseShared(int finalState) {
		setState(finalState);
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <p>
	 * The default {@link AbstractFuture} implementation throws {@code
	 * InterruptedException} if the current thread is interrupted before or
	 * during the call, even if the value is already available.
	 *
	 * @throws InterruptedException
	 *             if the current thread was interrupted before or during the
	 *             call (optional but recommended).
	 * @throws CancellationException
	 *             {@inheritDoc}
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		Future<V> linkSource = this.linkSource;
		if (linkSource != null) {
			return linkSource.get();
		}
		fetch();
		// Acquire the shared lock allowing interruption.
		acquireSharedInterruptibly(-1);
		linkSource = this.linkSource;
		if (linkSource != null) {
			return linkSource.get();
		}
		return getValue();

	}

	public V tryGet() {
		try {
			return get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		long nanos = unit.toNanos(timeout);
		fetch();
		// Attempt to acquire the shared lock with a timeout.
		if (!tryAcquireSharedNanos(-1, nanos)) {
			throw new TimeoutException("Timeout waiting for task.");
		}

		return getValue();
	}

	@Override
	public void fetch() {
		boolean doFetch = compareAndSetState(READY, LOADING);

		if (doFetch) {
			fetchFuture = new FutureTask<>(() -> {
				fetchData();
				return null;
			});
			HypermediaClient.execute(fetchFuture);
		}
	}

	@Override
	public void fetchData() throws InterruptedException {
		try {
			dependencies.clear();
			resolveFutures(parents);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (preprocess != null) {
				preprocess.call();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
			resolveFutures(dependencies);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (!isCompleted())
				process();
		} catch (InterruptedException ex) {
			throw ex;
		} catch (Exception e) {
			setException(e);
		}

	}

	@Override
	public void resolveFutures(Iterable<Future> futures) throws InterruptedException, ExecutionException {
		for (Future p : futures) {
			p.fetch();
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
		for (Future p : futures) {
			p.get();
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
	}

	/**
	 * Subclasses should invoke this method to internalSet the result of the
	 * computation to {@code value}. This will internalSet the state of the
	 * future to {@link COMPLETED} and invoke the listeners if the state was
	 * successfully changed.
	 *
	 * @param value
	 *            the value that was the result of the task.
	 * @return true if the state was successfully changed.
	 */
	@Override
	public boolean set(V value) throws InterruptedException {
		tries = parents.size() > 0 ? 1 : 0;
		return complete(value, null, COMPLETED);
	}

	public boolean trySet(V value) {
		try {
			return set(value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean setPartial(V value) throws InterruptedException {
		tries = parents.size() > 0 ? 1 : 0;
		return complete(value, null, PARTIALLY_LOADED);
	}

	/**
	 * Subclasses should invoke this method to internalSet the result of the
	 * computation to an error, {@code throwable}. This will internalSet the
	 * state of the future to {@link COMPLETED} and invoke the listeners if the
	 * state was successfully changed.
	 *
	 * @param throwable
	 *            the exception that the task failed with.
	 * @return true if the state was successfully changed.
	 */
	@Override
	public boolean setException(Throwable throwable) throws InterruptedException {
		if (canRetry()) {

			setState(RESETTING_LOADING);
			HypermediaClient.execute(() -> reset(true));
			return false;

		}
		return complete(null, throwable, COMPLETED);
	}

	public boolean canRetry() {
		if (tries > 0) {
			boolean doRetry = compareAndSetState(LOADING, RESETTING_LOADING);
			if (doRetry) {
				tries--;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		try {
			cancelFetch();
			if (!complete(null, null, mayInterruptIfRunning ? INTERRUPTED : CANCELLED)) {
				return false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void cancelFetch() {
		if (fetchFuture != null && !fetchFuture.isCancelled()) {
			fetchFuture.cancel(true);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 10.0
	 */
	@Override
	public void addListener(Runnable listener, Executor exec) {
		executionList.add(listener, exec);
	}

	/**
	 * Implementation of the actual value retrieval. Will return the value on
	 * success, an exception on failure, a cancellation on cancellation, or an
	 * illegal state if the synchronizer is in an invalid state.
	 */
	private V getValue() throws CancellationException, ExecutionException, InterruptedException {
		int state = getState();
		switch (state) {
		case COMPLETED:
		case COMPLETING_POST_PROCESS:
		case PARTIALLY_LOADED:
			if (exception != null) {
				throw new ExecutionException(exception);
			} else {
				return value;
			}

		case CANCELLED:
		case INTERRUPTED:
			throw cancellationExceptionWithCause("Task was cancelled.", exception);
		case RESETTING_LOADING:
		case RESETTING:
			return get();
		default:
			throw new IllegalStateException("Error, synchronizer in invalid state: " + state);
		}
	}

	/**
	 * Checks if the state is {@link #COMPLETED}, {@link #CANCELLED}, or
	 * {@link INTERRUPTED}.
	 */
	@Override
	public boolean isDone() {
		return (getQueueState() & (COMPLETED | CANCELLED | INTERRUPTED | PARTIALLY_LOADED)) != 0;
	}

	public boolean isPartiallyLoaded() {
		return (getQueueState() & (PARTIALLY_LOADED)) != 0;
	}

	@Override
	public boolean isCompleted() {
		return (getQueueState() & COMPLETED) != 0;
	}

	/**
	 * Checks if the state is {@link #CANCELLED} or {@link #INTERRUPTED}.
	 */
	@Override
	public boolean isCancelled() {
		return (getQueueState() & (CANCELLED | INTERRUPTED)) != 0;
	}

	/**
	 * Checks if the state is {@link #INTERRUPTED}.
	 */
	boolean wasInterrupted() {
		return getQueueState() == INTERRUPTED;
	}

	/**
	 * Implementation of completing a task. Either {@code v} or {@code t} will
	 * be internalSet but not both. The {@code finalState} is the state to
	 * change to from {@link #READY}. If the state is not in the READY state we
	 * return {@code false} after waiting for the state to be internalSet to a
	 * valid final state ({@link #COMPLETED}, {@link #CANCELLED}, or
	 * {@link #INTERRUPTED}).
	 *
	 * @param v
	 *            the value to internalSet as the result of the computation.
	 * @param t
	 *            the exception to internalSet as the result of the computation.
	 * @param finalState
	 *            the state to transition to.
	 */
	private boolean complete(V v, Throwable t, int finalState) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		boolean doCompletion = compareAndSetState(LOADING, COMPLETING);
		if (doCompletion) {

			// If this thread successfully transitioned to COMPLETING,
			// internalSet the value
			// and exception and then release to the final state.
			value = v;
			// Don't actually construct a CancellationException until necessary.
			exception = (finalState & (CANCELLED | INTERRUPTED)) != 0 ? new CancellationException("Future.cancel() was called.") : t;
			if (postprocess != null) {
				setState(COMPLETING_POST_PROCESS);
				try {
					postprocess.call();
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
			releaseShared(finalState);
		} else if (getState() == LOADING) {
			// If some other thread is currently completing the future, block
			// until
			// they are done so we can guarantee completion.
			acquireSharedInterruptibly(-1);
		}
		return doCompletion;
	}

	@Override
	public void reset() {
		reset(false);
	}

	@Override
	public void resetFailed() {
		if (isDone() && (exception != null | isCancelled())) {
			reset(false);
			for (Future p : parents) {
				p.resetFailed();
			}
		}
	}

	@Override
	public void reset(boolean parent) {
		if (getState() == RESETTING) {
			return;
		}
		int state = getAndSetState(RESETTING);
		cancelFetch();
		cleanup();
		for (Future b : dependencies) {
			b.cancelFetch();
		}
		for (Future b : dependencies) {
			b.reset(false);
		}
		dependencies.clear();
		if (parent) {
			for (Future p : parents) {
				p.reset(false);
			}
		}
		for (Future p : parents) {
			p.resetFailed();
		}
		value = null;
		exception = null;
		setState(READY);
		if (state == LOADING || state == RESETTING_LOADING)
			fetch();
	}

	private int getAndSetState(int targetState) {
		int state = getState();
		while (!compareAndSetState(state, targetState)) {
			Thread.yield();
			state = getState();
		}
		return state;
	}

	@Override
	public void cleanup() {
		linkSource = null;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

	@Override
	public List<Future> getDependencies() {
		return dependencies;
	}

	@Override
	public List<Future> getParents() {
		return parents;
	}

	@Override
	public List<WeakReference<Future>> getChildern() {
		return childern;
	}

	@Override
	public String getStateName() {
		switch (getQueueState()) {
		case READY:
			return "READY";
		case LOADING:
			return "LOADING";
		case COMPLETING:
			return "COMPLETING";
		case COMPLETED:
			return "COMPLETED";
		case RESETTING:
			return "RESETTING";
		case RESETTING_LOADING:
			return "RESETTING_LOADING";
		case CANCELLED:
			return "CANCELLED";
		case INTERRUPTED:
			return "INTERRUPTED";
		case PARTIALLY_LOADED:
			return "PARTIALLY_LOADED";
		}
		return "OTHER " + getQueueState();
	}

	@Override
	public void link(Future<V> option) throws InterruptedException, ExecutionException {
		option.get();
		tries = parents.size() > 0 ? 1 : 0;
		linkSource = option;
		releaseShared(linkSource.getQueueState());
	}

	@Override
	public int getQueueState() {
		Future<V> linkSource = this.linkSource;
		if (linkSource != null) {
			return linkSource.getQueueState();
		}
		return getState();
	}

	public String toString() {
		String s = getStateName();
		String q = hasQueuedThreads() ? "non" : "";
		return getClassName() + " [State = " + s + ", " + q + "empty queue]";
	}

	public String getClassName() {
		return getClass().getSimpleName();
	}

	public void setPostProcess(ProcessFunction postprocess) {
		Preconditions.checkState(this.postprocess == null);
		this.postprocess = postprocess;
	}
}
