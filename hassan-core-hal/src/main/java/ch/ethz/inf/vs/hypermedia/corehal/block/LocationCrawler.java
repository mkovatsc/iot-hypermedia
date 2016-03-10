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
import ch.ethz.inf.vs.hypermedia.client.utils.PostOrderCrawler;
import ch.ethz.inf.vs.hypermedia.corehal.model.LocationDescription;
import com.google.common.collect.Iterators;
import org.eclipse.californium.core.WebLink;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Created by ynh on 08/11/15.
 */
public class LocationCrawler extends PostOrderCrawler<LocationDescriptionFuture> implements ContextConsumer<LocationCrawler, Object> {
	LocationDescriptionFuture startLocation;
	String targetLocation;
	private int STARTS_WITH = 0;
	private int EQUALS = 1;
	private int MATCHING = 2;
	private int filterMode;
	private LinkListFuture links;

	public LocationCrawler() {
		super((LocationDescriptionFuture) null);
	}

	public LocationCrawler(LinkListFuture links) {
		super(getSortedWebLinks(links));
		this.links = links;
	}

	public LocationCrawler(LinkListFuture links, String location, int filterMode) {
		super(getSortedWebLinks(links));
		this.links = links;
		targetLocation = location;
		this.filterMode = filterMode;
	}

	public LocationCrawler(LocationDescriptionFuture startLocation) {
		super(() -> Iterators.singletonIterator(startLocation));
		this.startLocation = startLocation;
	}

	public LocationCrawler(LocationDescriptionFuture startLocation, String location, int filterMode) {
		super(() -> Iterators.singletonIterator(startLocation));
		this.startLocation = startLocation;
		targetLocation = location;
		this.filterMode = filterMode;
	}

	@Override
	public Iterator<LocationDescriptionFuture> getChildren(LocationDescriptionFuture item) {
		return item.getChildren().stream().sorted(LocationCrawler::sortByLocationLength).iterator();
	}

	@Override
	public boolean visitChildren(LocationDescriptionFuture item) {
		String location = item.tryGet().getLocation();
		if (location == null) {
			return targetLocation == null;
		}
		if (targetLocation != null) {
			if (filterMode == EQUALS) {
				return targetLocation.startsWith(location) && !location.equals(targetLocation);
			} else {
				return matching(location);
			}
		}
		return true;
	}

	private boolean matching(String location) {
		String a = targetLocation + "/";
		String b = location + "/";
		int segment = Math.min(a.length(), b.length());
		return b.substring(0, segment).equals(a.substring(0, segment));
	}

	@Override
	public boolean filter(LocationDescriptionFuture item) {
		LocationDescription locationDescription = item.tryGet();
		if (targetLocation != null) {
			if (filterMode == EQUALS) {
				return locationDescription.getLocation().equals(targetLocation);
			} else if (filterMode == STARTS_WITH) {
				return locationDescription.getLocation().startsWith(targetLocation);
			} else {
				return targetLocation.startsWith(locationDescription.getLocation());
			}
		} else {
			return true;
		}
	}

	@Override
	public BaseFuture getParent() {
		if (links != null) {
			return links;
		} else if (startLocation != null) {
			return startLocation;
		} else {
			throw new RuntimeException("Invalid state");
		}
	}

	@Override
	public Object identify(LocationDescriptionFuture item) {
		return item.tryGet().getSelf(item.getUrl());
	}

	public static Iterable<LocationDescriptionFuture> getSortedWebLinks(LinkListFuture links) {
		return () -> links.stream().filter(LocationCrawler::filterLinks).sorted(LocationCrawler::getCompareLocationNames).map((link) -> CoREHalResourceFuture.createFromWebLink(LocationDescriptionFuture::new, link)).iterator();
	}

	public static boolean filterLinks(WebLink item) {
		return item.getAttributes().getContentTypes().contains(String.valueOf(Utils.getContentType(LocationDescription.class)));
	}

	public static int getCompareLocationNames(WebLink x, WebLink y) {
		int xc = 0;
		int yc = 0;
		if (Utils.getWebLinkAttribute(x, "location") != null)
			xc = Utils.getWebLinkAttribute(x, "location").length();
		if (Utils.getWebLinkAttribute(y, "location") != null)
			yc = Utils.getWebLinkAttribute(y, "location").length();
		return Integer.compare(yc, xc);
	}

	public static int sortByLocationLength(LocationDescriptionFuture a, LocationDescriptionFuture b) {
		try {
			return Integer.compare(b.get().getLocation().length(), a.get().getLocation().length());
		} catch (Exception e) {
			
		}
		return 0;
	}

	public LocationCrawler locationEquals(String location) {
		if (startLocation != null) {
			return new LocationCrawler(startLocation, location, EQUALS);
		}
		return new LocationCrawler(links, location, EQUALS);
	}

	public LocationCrawler locationStartWith(String location) {
		if (startLocation != null) {
			return new LocationCrawler(startLocation, location, STARTS_WITH);
		}
		return new LocationCrawler(links, location, STARTS_WITH);
	}

	public LocationCrawler matchingPrefix(String location) {
		if (startLocation != null) {
			return new LocationCrawler(startLocation, location, MATCHING);
		}
		return new LocationCrawler(links, location, MATCHING);
	}

	public LocationDescriptionFuture first() {
		return one(LocationDescriptionFuture::new);
	}

	@Override
	public LocationCrawler withContext(Object obj) {
		if (obj instanceof LinkListFuture) {
			return new LocationCrawler((LinkListFuture) obj);
		} else if (obj instanceof LocationDescriptionFuture) {
			return new LocationCrawler((LocationDescriptionFuture) obj);
		} else {
			throw new RuntimeException("Invalid link type");
		}
	}

}
