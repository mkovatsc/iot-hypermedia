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

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

import java.util.*;

/**
 * Created by ynh on 07/12/15.
 */
public abstract class BFSCrawler<B> extends Crawler<B> {

	public BFSCrawler(Iterable<B> start) {
		super(start);
	}

	public BFSCrawler(B start) {
		super(start);
	}

	@Override
	public Iterator<B> buildIterator() {
		Set<Object> visited = new HashSet<Object>();
		Queue<Iterable<B>> queue = new LinkedList<>();
		queue.add(() -> getFilteredIterator(visited, start.iterator()));
		Iterator<Iterator<B>> iterator = new AbstractIterator<Iterator<B>>() {
			@Override
			protected Iterator<B> computeNext() {
				if (queue.isEmpty())
					return endOfData();
				return queue.poll().iterator();
			}
		};
		return Iterators.transform(Iterators.concat(iterator), (item) -> process(item, queue, visited));
	}

	public B process(B item, Queue<Iterable<B>> iterator, Set<Object> visited) {
		iterator.add(() -> internalGetChildren(item, visited));
		return item;
	}
}
