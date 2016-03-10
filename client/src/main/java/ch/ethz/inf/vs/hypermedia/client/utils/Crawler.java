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

import ch.ethz.inf.vs.hypermedia.client.BaseFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by ynh on 07/12/15.
 */
public abstract class Crawler<B> implements Iterable<B> {
	protected final Iterable<B> start;

	public Crawler(Iterable<B> start) {
		this.start = start;
	}

	public Crawler(B start) {
		this.start = () -> Iterators.singletonIterator(start);
	}

	public abstract Iterator<B> getChildren(B item);

	@Override
	public Iterator<B> iterator() {
		Iterator<B> transform = buildIterator();
		return Iterators.filter(transform, (x) -> filter(x));
	}

	protected abstract Iterator<B> buildIterator();

	protected Iterator<B> internalGetChildren(B item, Set<Object> visited) {
		if (!visitChildren(item))
			return Collections.emptyIterator();
		Iterator<B> children = getChildren(item);
		if (children != null)
			return getFilteredIterator(visited, children);
		return Collections.emptyIterator();
	}

	protected UnmodifiableIterator<B> getFilteredIterator(Set<Object> visited, Iterator<B> iter) {
		return Iterators.filter(iter, (x) -> removeDuplicates(x, visited));
	}

	public boolean removeDuplicates(B item, Set<Object> visited) {
		return visited.add(identify(item));
	}

	public Object identify(B item) {
		return item;
	}

	public <V extends BaseFuture> V one(Supplier<V> s) {
		return Utils.or(() -> {
			V x = s.get();
			x.addParent(getParent());
			return x;
		} , (Iterable<V>) this);
	}

	public boolean filter(B item) {
		return true;
	}

	public boolean visitChildren(B item) {
		return true;
	}

	public Stream<B> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	public BaseFuture getParent() {
		throw new RuntimeException("Not Implemented");
	}
}
