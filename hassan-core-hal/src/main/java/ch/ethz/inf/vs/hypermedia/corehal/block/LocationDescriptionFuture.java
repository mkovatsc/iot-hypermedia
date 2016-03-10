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

import ch.ethz.inf.vs.hypermedia.client.CoapRequestFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.LocationDescription;
import ch.ethz.inf.vs.hypermedia.corehal.model.ThingDescription;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 06/11/15.
 */
public class LocationDescriptionFuture extends CoREHalResourceFuture<LocationDescription> {

	public LocationDescriptionFuture getChildLocation(String name) throws ExecutionException, InterruptedException {
		Optional<LocationDescriptionFuture> item = getChildren().stream().filter(x -> getLocation(x).equals(name)).findFirst();
		return item.isPresent() ? item.get() : null;
	}

	private String getLocation(LocationDescriptionFuture x) {
		try {
			return x.get().getLocation();
		} catch (Exception e) {
		}
		return "";
	}

	public LocationDescriptionFuture findSubLocation(String location) {
		return getLocationCrawler().locationEquals(location).one(LocationDescriptionFuture::new);
	}

	public LocationCrawler getLocationCrawler() {
		return new LocationCrawler().withContext(this);
	}

	public ThingCrawler getThingCrawler() {
		return new ThingCrawler().withContext(this);
	}

	public List<LocationDescriptionFuture> getChildren() {
		return getEmbeddedStream("child", LocationDescriptionFuture::new);
	}

	public List<ThingDescriptionFuture> getThings() {
		return getEmbeddedStream("thing", ThingDescriptionFuture::new);
	}

	public CoapRequestFuture addChildLocation(LocationDescription description) {
		JsonObject partialDescription = new JsonObject();
		partialDescription.addProperty("_self", description.getSelf(description.getSelf("")));
		partialDescription.addProperty("location", description.getLocation());
		return getFormRequest("add-child", partialDescription);
	}

	public CoapRequestFuture addThing(ThingDescription description) {
		JsonObject partialDescription = new JsonObject();
		partialDescription.addProperty("_self", description.getSelf(description.getSelf("")));
		partialDescription.addProperty("location", description.getLocation());
		return getFormRequest("add-thing", partialDescription);
	}

	public CoapRequestFuture removeThing(ThingDescription description) {
		JsonObject partialDescription = new JsonObject();
		partialDescription.addProperty("_self", description.getSelf(description.getSelf("")));
		partialDescription.addProperty("location", description.getLocation());
		return getFormRequest("remove-thing", partialDescription);
	}
}
