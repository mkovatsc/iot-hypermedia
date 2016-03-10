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
package ch.ethz.inf.vs.hypermedia.corehal.block;

import ch.ethz.inf.vs.hypermedia.client.BaseFuture;
import ch.ethz.inf.vs.hypermedia.client.ContextConsumer;
import ch.ethz.inf.vs.hypermedia.client.LinkListFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.model.ThingDescription;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import org.eclipse.californium.core.WebLink;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Created by ynh on 10/11/15.
 */
public class ThingCrawler implements ContextConsumer<ThingCrawler, BaseFuture>, Iterable<ThingDescriptionFuture> {
	private BaseFuture parent;
	private String targetLocation;

	public ThingCrawler() {

	}

	public ThingCrawler(BaseFuture parent) {
		this.parent = parent;
	}

	public ThingCrawler(BaseFuture parent, String locationName) {
		this(parent);
		this.targetLocation = locationName;
	}

	public static <V> boolean hasContentType(WebLink item, Class<V> type) {
		return item.getAttributes().getContentTypes().contains(String.valueOf(Utils.getContentType(type)));
	}

	public static Iterator<WebLink> getSortedWebLinks(LinkListFuture links) {
		return links.stream().filter(x -> hasContentType(x, ThingDescription.class)).sorted(ThingCrawler::getCompareLocationNames).iterator();
	}

	public static int getCompareLocationNames(WebLink x, WebLink y) {
		int xc = hasContentType(x, ThingDescription.class) ? 1 : -1;
		int yc = hasContentType(y, ThingDescription.class) ? 1 : -1;
		if (xc == yc) {
			if (Utils.getWebLinkAttribute(x, "location") != null)
				xc = Utils.getWebLinkAttribute(x, "location").length();
			if (Utils.getWebLinkAttribute(y, "location") != null)
				yc = Utils.getWebLinkAttribute(y, "location").length();
		}
		return Integer.compare(yc, xc);
	}

	public static ThingDescriptionFuture transform(WebLink item) {
		return CoREHalResourceFuture.createFromWebLink(ThingDescriptionFuture::new, item);
	}

	@Override
	public ThingCrawler withContext(BaseFuture item) {
		return new ThingCrawler(item);
	}

	public ThingCrawler withLocationName(String locationName) {
		return new ThingCrawler(parent, locationName);
	}

	public boolean filter(ThingDescriptionFuture item) {
		if (targetLocation == null) {
			return true;
		}
		String location = item.tryGet().getLocation();
		if (location != null) {
			return targetLocation.equals(location);
		}
		return true;
	}

	@Override
	public Iterator<ThingDescriptionFuture> iterator() {
		LocationCrawler locationCrawler = new LocationCrawler().withContext(parent);
		if (targetLocation != null) {
			locationCrawler = locationCrawler.matchingPrefix(targetLocation);
		}
		// Use pre order for thing discovery
		locationCrawler.setPostOrder(false);
		Iterator<Iterator<ThingDescriptionFuture>> locationResources = Iterators.transform(locationCrawler.iterator(), this::discoverLocation);
		Iterator<ThingDescriptionFuture> flatLocationResources = Iterators.concat(locationResources);
		Iterator<WebLink> links = Collections.emptyIterator();
		if (parent instanceof LinkListFuture) {
			links = getSortedWebLinks((LinkListFuture) parent);
		}
		return Iterators.filter(Iterators.concat(Iterators.transform(links, ThingCrawler::transform), flatLocationResources), this::filter);
	}

	Iterator<ThingDescriptionFuture> discoverLocation(LocationDescriptionFuture lf) {
		return lf.getThings().stream().sorted((a, b) -> Integer.compare(getLength(b), getLength(a))).iterator();
	}

	private int getLength(ThingDescriptionFuture a) {
		ThingDescription thingDescription = a.tryGet();
		if (thingDescription == null)
			return 0;
		String location = thingDescription.getLocation();
		if (location == null)
			return 0;
		return location.length();
	}

	public <V extends BaseFuture> V one(Supplier<V> s) {
		return Utils.or(() -> {
			V x = s.get();
			x.addParent(parent);
			return x;
		} , (Iterable<V>) this);
	}

	public ThingDescriptionFuture first() {
		return one(ThingDescriptionFuture::new);
	}

	public ThingDescriptionFuture findFirst(Predicate<ThingDescriptionFuture> pred) {
		return Utils.find(() -> {
			ThingDescriptionFuture x = new ThingDescriptionFuture();
			x.addParent(parent);
			return x;
		} , pred, (Iterable<ThingDescriptionFuture>) this);
	}

	public ThingDescriptionFuture findFirstWithLink(String linkRelation) {
		return findFirst((x) -> x.hasLink(linkRelation));
	}

	public FluentIterable<ThingDescriptionFuture> filter(Predicate<ThingDescriptionFuture> pred) {
		return fluent().filter(pred);
	}

	public FluentIterable<ThingDescriptionFuture> fluent() {
		return FluentIterable.from(this);
	}

	public <T> FluentIterable<T> transform(Function<? super ThingDescriptionFuture, T> function) {
		return fluent().transform(function);
	}
}
