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

import ch.ethz.inf.vs.hypermedia.client.utils.LazyIterator;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.eclipse.californium.core.WebLink;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by ynh on 02/12/15.
 */
public interface IterableFuture<X, V extends Iterable<X>> extends LoadableFuture<V>, Iterable<X> {

	default X findFirst(Predicate<? super X> filter) {
		return stream().filter(filter).findFirst().get();
	}

	default Stream<X> stream() {
		return StreamSupport.stream(tryGet().spliterator(), false);
	}

	V tryGet();

	default Iterator<X> iterator() {
		return new LazyIterator<>(() -> tryGet().iterator());
	}

	default Collection<X> getAll(Predicate<? super X> fn) throws ExecutionException, InterruptedException {
		return stream().filter(fn).collect(Collectors.toList());
	}

	default <Q> Iterable<Q> map(Function<? super X, Q> fn) {
		return () -> Iterators.<X, Q> transform(iterator(), fn);
	}

}
