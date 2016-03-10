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
package ch.ethz.inf.vs.hypermedia.client.utils;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by ynh on 07/11/15.
 */
public class CrawlerTest {

	private static int calls;

	@Test
	public void testPreOrder() {
		ArrayList<Item> initialItems = new ArrayList<>();
		Item item = new Item("0", 3);
		initialItems.add(item);
		PreOrderTestCrawler c = new PreOrderTestCrawler(initialItems);
		ArrayList<String> items = new ArrayList<>();
		for (Item ne : c) {
			items.add(ne.id);
		}
		ArrayList<String> dfs = new ArrayList<>();
		item.preorder(dfs);
		assertArrayEquals(dfs.toArray(), items.toArray());
		calls = 0;
		int cx = 0;
		ArrayList<Integer> history = new ArrayList<>();
		for (Item it : c) {
			history.add(calls);
			cx++;
			assertEquals(cx, calls);
		}
	}

	@Test
	public void testPostOrder() {
		ArrayList<Item> initialItems = new ArrayList<>();
		int depth = 3;
		Item item = new Item("0", depth);
		initialItems.add(item);
		PostOrderTestCrawler c = new PostOrderTestCrawler(initialItems);
		ArrayList<String> items = new ArrayList<>();
		for (Item ne : c) {
			items.add(ne.id);
		}
		ArrayList<String> dfs = new ArrayList<>();
		item.postorder(dfs);
		assertArrayEquals(dfs.toArray(), items.toArray());
		calls = 0;
		int cx = 0;
		ArrayList<Integer> history = new ArrayList<>();
		for (Item it : c) {
			history.add(calls);
			cx++;
			assertTrue(calls - cx <= depth);
		}
	}

	@Test
	public void testBFS() {
		ArrayList<Item> initialItems = new ArrayList<>();
		Item item = new Item("0", 3);
		initialItems.add(item);
		BFSTestCrawler c = new BFSTestCrawler(initialItems);
		ArrayList<String> items = new ArrayList<>();
		for (Item ne : c) {
			items.add(ne.id);
		}
		ArrayList<String> bfs = new ArrayList<>();
		item.bfs(bfs);
		assertArrayEquals(bfs.toArray(), items.toArray());
		calls = 0;
		int cx = 0;
		ArrayList<Integer> history = new ArrayList<>();
		for (Item it : c) {
			history.add(calls);
			cx++;
			assertEquals(cx, calls);
		}
	}

	@Test
	public void testMultiRun() {
		ArrayList<Item> initialItems = new ArrayList<>();
		Item item = new Item("0", 2);
		initialItems.add(item);
		BFSTestCrawler c = new BFSTestCrawler(initialItems);
		assertArrayEquals(c.stream().map(x -> x.id).collect(Collectors.toList()).toArray(), c.stream().map(x -> x.id).collect(Collectors.toList()).toArray());
	}

	public static class Item {

		private final List<Item> childern = new ArrayList<>();
		private final String id;

		public Item(String id, int levels) {
			this.id = id;
			if (levels > 0) {
				for (int i = 0; i < 10; i++) {
					childern.add(new Item(id + "." + i, levels - 1));
				}
			}
		}

		public void preorder(ArrayList<String> dfs) {
			dfs.add(id);
			for (Item c : childern) {
				c.preorder(dfs);
			}
		}

		public void postorder(ArrayList<String> dfs) {
			for (Item c : childern) {
				c.postorder(dfs);
			}
			dfs.add(id);
		}

		public void bfs(ArrayList<String> bfs) {
			Queue<Item> i = new LinkedList<>();
			i.add(this);
			while (!i.isEmpty()) {
				Item current = i.poll();
				bfs.add(current.id);
				for (Item c : current.childern) {
					i.add(c);
				}
			}
		}

		public List<Item> getChildern() {
			return childern;
		}
	}

	public static class ObservedIterator<V> implements Iterator<V> {

		private final Iterator<V> iter;

		public ObservedIterator(Iterator<V> iter) {
			this.iter = iter;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public V next() {
			V next = iter.next();
			calls++;
			return next;
		}
	}

	public static class PreOrderTestCrawler extends PreOrderCrawler<Item> {

		public PreOrderTestCrawler(Iterable<Item> start) {
			super(() -> new ObservedIterator<>(start.iterator()));
		}

		@Override
		public Iterator<Item> getChildren(Item item) {
			return new ObservedIterator<>(item.getChildern().iterator());
		}
	}

	public static class PostOrderTestCrawler extends PostOrderCrawler<Item> {

		public PostOrderTestCrawler(Iterable<Item> start) {
			super(() -> new ObservedIterator<>(start.iterator()));
		}

		@Override
		public Iterator<Item> getChildren(Item item) {
			return new ObservedIterator<>(item.getChildern().iterator());
		}
	}

	public static class BFSTestCrawler extends BFSCrawler<Item> {

		public BFSTestCrawler(Iterable<Item> start) {
			super(() -> new ObservedIterator<>(start.iterator()));
		}

		@Override
		public Iterator<Item> getChildren(Item item) {
			return new ObservedIterator<>(item.getChildern().iterator());
		}
	}
}