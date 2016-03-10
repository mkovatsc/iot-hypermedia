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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ynh on 10/10/15.
 */
public class BaseFutureTest {

	private ScheduledThreadPoolExecutor executor;

	@Before
	public void setUp() throws Exception {
		executor = new ScheduledThreadPoolExecutor(10);
	}

	@Test
	public void testConcurrency() throws InterruptedException {
		System.err.println("testConcurrency");
		AtomicInteger successful = new AtomicInteger();

		AtomicInteger counter = new AtomicInteger();
		Future<Integer> base = new BaseFuture<Integer>() {
			@Override
			public void process() throws InterruptedException {
				counter.incrementAndGet();
				Thread.sleep(10000);
				set(99);
			}
		};
		int runs = 100;
		Semaphore s = new Semaphore(1 - runs);
		for (int i = 0; i < runs; i++) {

			executor.execute(() -> {
				try {
					if (base.get() != 99) {
						throw new RuntimeException("Test");
					}
					successful.incrementAndGet();
				} catch (Exception e) {

				}
				s.release();
			});
		}
		s.acquire();
		assertEquals(1, counter.get());
		assertEquals(runs, successful.get());
	}

	@Test
	public void testMultiGet() throws InterruptedException, ExecutionException {
		System.err.println("testMultiGet");
		AtomicInteger counter = new AtomicInteger();
		Future<Integer> base = new BaseFuture<Integer>() {
			@Override
			public void process() throws InterruptedException {
				Thread.sleep(1000);
				counter.incrementAndGet();
				set(counter.get());
			}
		};

		assertEquals(1, (int) base.get());
		base.reset(true);
		assertEquals(2, (int) base.get());
		assertEquals(2, (int) base.get());
	}

	@Test
	public void testSleepReset() throws InterruptedException, ExecutionException {
		System.err.println("testSleepReset");
		AtomicInteger number = new AtomicInteger();
		AtomicBoolean b = new AtomicBoolean();
		Future<Integer> base = new BaseFuture<Integer>() {

			@Override
			public void process() throws Exception {
				int num = number.get();
				if (num == 0)
					b.set(true);
				Thread.sleep(1000);
				set(num);
			}
		};

		executor.execute(() -> {

			try {
				Thread.sleep(500);
				number.incrementAndGet();
				base.reset(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		assertEquals(1, (int) base.get());
		assertTrue(b.get());
	}

	@Test
	public void testBusyTaskReset() throws InterruptedException, ExecutionException {
		System.err.println("testBusyTaskReset");
		AtomicInteger number = new AtomicInteger();

		AtomicBoolean b = new AtomicBoolean();
		Future<Integer> base = new BaseFuture<Integer>() {

			@Override
			public void process() throws Exception {
				int num = number.get();
				if (num == 0)
					b.set(true);
				long x = System.currentTimeMillis() + 2000;
				// noinspection StatementWithEmptyBody
				while (x > System.currentTimeMillis()) {

				}
				set(num);
			}
		};

		executor.execute(() -> {

			try {
				Thread.sleep(500);
				number.set(199);
				base.reset(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		assertEquals(199, (int) base.get());
		assertTrue(b.get());
	}

	@Test
	public void testResetUnstarted() throws InterruptedException, ExecutionException {
		System.err.println("testResetUnstarted");
		AtomicInteger number = new AtomicInteger();
		Future<Integer> base = new BaseFuture<Integer>() {

			@Override
			public void process() throws Exception {
				int num = number.getAndIncrement();
				Thread.sleep(500);
				set(num);
			}
		};
		base.reset(false);
		assertEquals("READY", base.getStateName());
		assertEquals(0, (int) base.get());
		base.reset(false);
		assertEquals("READY", base.getStateName());
		assertEquals(1, (int) base.get());
	}

	@Test
	public void testResetInterrupt() throws InterruptedException, ExecutionException {
		System.err.println("testResetInterrupt");
		AtomicInteger number = new AtomicInteger();
		AtomicInteger sleepTime = new AtomicInteger();
		sleepTime.set(10000000);
		Future<Integer> base = new BaseFuture<Integer>() {
			@Override
			public void process() throws Exception {
				Thread.sleep(sleepTime.get());
				set(number.getAndIncrement());
			}
		};
		executor.execute(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			base.cancel(true);
		});
		try {
			base.get();

		} catch (Exception exx) {

		}
		assertEquals("INTERRUPTED", base.getStateName());
		base.reset(false);
		assertEquals("READY", base.getStateName());
		sleepTime.set(10);
		assertEquals(0, (int) base.get());
	}

	@Test
	public void testResetCancel() throws InterruptedException, ExecutionException {
		System.err.println("testResetCancel");
		AtomicInteger number = new AtomicInteger();

		Future<Integer> base = new BaseFuture<Integer>() {
			@Override
			public void process() throws Exception {
				Thread.sleep(1000);
				set(number.getAndIncrement());
			}
		};
		executor.execute(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			base.cancel(false);
		});
		try {
			base.get();

		} catch (Exception exx) {

		}
		assertEquals("CANCELLED", base.getStateName());
		base.reset(false);
		assertEquals("READY", base.getStateName());
		assertEquals(0, (int) base.get());
	}

	@Test
	public void testMultiReset() throws InterruptedException, ExecutionException {
		System.err.println("testMultiReset");
		AtomicInteger number = new AtomicInteger();

		AtomicBoolean b = new AtomicBoolean();
		Future<Integer> base = new BaseFuture<Integer>() {

			@Override
			public void process() throws Exception {
				int num = number.get();
				if (num == 0)
					b.set(true);
				long x = System.currentTimeMillis() + 2000 + (long) (Math.random() * 8000);
				// noinspection StatementWithEmptyBody
				while (x > System.currentTimeMillis()) {

				}
				set(num);
			}
		};

		executor.execute(() -> {

			try {
				Thread.sleep((long) (Math.random() * 1200));
				for (int i = 0; i < 10; i++) {
					number.incrementAndGet();
					base.reset(false);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		assertEquals(10, (int) base.get());
		assertTrue(b.get());
	}

	@After
	public void tearDown() throws InterruptedException {
		executor.shutdownNow();
	}
}
