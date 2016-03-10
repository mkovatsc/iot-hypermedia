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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ynh on 26/11/15.
 */
public class LinkList extends HashMap<String, List<Link>> {
	public void add(Link value) {
		if (value.getNames() == null) {
			List<Link> list = computeIfAbsent(null, (x) -> new ArrayList<Link>());
			list.add(value);
		} else {
			for (String name : value.getNames()) {
				List<Link> list = computeIfAbsent(name, (x) -> new ArrayList<Link>());
				list.add(value);
			}
		}
	}

	public Iterable<Link> linkValues() {
		return () -> Iterators.concat(Iterators.transform(values().iterator(), (x) -> x.iterator()));
	}
}
