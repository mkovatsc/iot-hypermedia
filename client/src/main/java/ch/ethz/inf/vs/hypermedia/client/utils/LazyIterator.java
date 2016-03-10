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

import java.util.Iterator;

/**
 * Created by ynh on 08/11/15.
 */
public class LazyIterator<V> implements Iterator<V> {
	public final Iterable<V> iterable;
	public Iterator<V> iter;

	public LazyIterator(Iterable<V> iterable) {
		this.iterable = iterable;
	}

	@Override
	public boolean hasNext() {
		init();
		return iter.hasNext();
	}

	public void init() {
		if (iter == null) {
			iter = iterable.iterator();
		}
	}

	@Override
	public V next() {
		init();
		return iter.next();
	}
}
