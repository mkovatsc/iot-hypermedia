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

import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by ynh on 26/09/15.
 */
public class LinkListFuture extends ResourceFuture<Iterable<WebLink>> implements ContextProvider, IterableFuture<WebLink, Iterable<WebLink>> {

	public LinkListFuture() {

	}

	public LinkListFuture(String base_url) {
		setPreProcess(() -> setRequestURL(base_url));
	}

	private Predicate<WebLink> filterByContentTypePredicate(int contentType) {
		return x -> x.getAttributes().getContentTypes().contains(String.valueOf(contentType));
	}

	public <V extends ResourceFuture> V getByMediaType(Supplier<V> type) {
		V block = type.get();
		Predicate<WebLink> filter = filterByContentTypePredicate(block.getContentType());
		return getByPredicate(() -> block, filter);
	}

	public <V extends ResourceFuture> V getByPredicate(Supplier<V> type, Predicate<WebLink> filter) {
		V block = type.get();
		block.addParent(this);
		block.setPreProcess(() -> {
			WebLink link = findFirst(filter);
			try {
				block.setRequestURL(Utils.resolve(getUrl(), link.getURI()));
			} catch (URISyntaxException e) {
				throw new ExecutionException(e);
			}
		});
		return block;
	}

	public <V extends ResourceFuture> V getFirstByAttribute(Supplier<V> type, String value, String attr) {
		Predicate<WebLink> filter = filterByAttributePredicate(attr, value);
		return getByPredicate(type, filter);
	}

	public LinkListFuture resourceLookup() {
		LinkListFuture block = new LinkListFuture();
		block.addParent(this);
		block.setPreProcess(() -> {
			WebLink link = findFirst(filterByAttributePredicate("rt", "core.rd-lookup"));
			try {
				block.setRequestURL(Utils.resolve(getUrl(), link.getURI()) + "/res");
			} catch (URISyntaxException e) {
				throw new ExecutionException(e);
			}
		});
		return block;
	}

	private Predicate<WebLink> filterByAttributePredicate(String attr, String value) {
		return x -> value.equals(Utils.getWebLinkAttribute(x, attr));
	}

	@Override
	public void setRequestURL(String url) {
		setRequestURL(url, true);
	}

	@Override
	public int getContentType() {
		return MediaTypeRegistry.APPLICATION_LINK_FORMAT;
	}

	@Override
	public Set<WebLink> deserialize(String text) throws Exception {
		return LinkFormat.parse(text);
	}

	@Override
	public String getMediaType() {
		return "application/link-format";
	}

	@Override
	public <V extends ContextConsumer> V use(V item) {
		return (V) item.withContext(this);
	}
}
