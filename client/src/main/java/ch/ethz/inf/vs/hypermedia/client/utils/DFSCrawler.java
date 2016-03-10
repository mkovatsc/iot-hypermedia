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

import com.google.common.collect.Iterators;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ynh on 07/12/15.
 */
public abstract class DFSCrawler<B> extends Crawler<B> {
	private boolean postOrder;

	public DFSCrawler(Iterable<B> start) {
		super(start);
	}

	public DFSCrawler(B start) {
		super(start);
	}

	@Override
	public Iterator<B> buildIterator() {
		if (isPostOrder()) {
			Set<Object> visited = new HashSet<>();
			return Iterators.concat(Iterators.transform(getFilteredIterator(visited, start.iterator()), (item) -> process(item, visited)));
		} else {
			Set<Object> visited = new HashSet<>();
			return Iterators.concat(Iterators.transform(getFilteredIterator(visited, start.iterator()), (item) -> process(item, visited)));
		}
	}

	public Iterator<B> process(B item, Set<Object> visited) {
		if (isPostOrder()) {
			Iterator<B> children = Iterators.concat(Iterators.transform(internalGetChildren(item, visited), (x) -> process(x, visited)));
			return Iterators.concat(children, Iterators.singletonIterator(item));
		} else {
			Iterator<B> children = Iterators.concat(Iterators.transform(internalGetChildren(item, visited), (item1) -> process(item1, visited)));
			return Iterators.concat(Iterators.singletonIterator(item), children);
		}
	}

	public boolean isPostOrder() {
		return postOrder;
	}

	public void setPostOrder(boolean postOrder) {
		this.postOrder = postOrder;
	}
}
