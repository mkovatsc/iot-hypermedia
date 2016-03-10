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
package ch.ethz.inf.vs.hypermedia.corehal;

import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import com.google.common.collect.Iterators;

import java.util.*;

/**
 * Created by ynh on 26/11/15.
 */
public class LinkCollection extends HashMap<String, LinkList> {
	public Link put(String key, Link value) {
		LinkList list = computeIfAbsent(key, (x) -> new LinkList());
		list.add(value);
		return value;
	}

	public Link getLink(String s, String name) {
		Iterable<Link> links = getLinks(s, name);
		if (links == null)
			return null;
		return Iterators.getOnlyElement(links.iterator());
	}

	public Iterable<Link> getLinks(String s, String name) {
		LinkList linkList = get(s);
		if (linkList == null)
			return null;
		return linkList.get(name);
	}

	public Iterable<Link> getLinks(String key) {
		LinkList list = get(key);
		if (list == null)
			return Collections.emptyList();
		return () -> Iterators.concat(Iterators.transform(list.values().iterator(), (x) -> x.iterator()));
	}
}
