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
 *    Klaus Hartke - CoRE Lighting specification
 *******************************************************************************/
package ch.ethz.inf.vs.hypermedia.hartke.lighting.block;

import ch.ethz.inf.vs.hypermedia.client.CoapRequestFuture;
import ch.ethz.inf.vs.hypermedia.client.MediaType;
import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.CoREAppBase;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Form;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.model.Link;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Created by ynh on 26/09/15.
 */
public class CoREAppResourceFuture<V extends CoREAppBase> extends ResourceFuture<V> {

	public void update(Object payload) throws ExecutionException, InterruptedException {
		getFormRequest("update", payload, true).get();
	}

	public CoapRequestFuture getFormRequest(String key, Object payload, boolean resetParent) {
		MediaType mediaType = null;
		if (payload != null)
			mediaType = Utils.getMediaTypeAnnotation(payload.getClass());
		int ct = MediaTypeRegistry.UNDEFINED;
		String mt = null;
		if (mediaType != null) {
			mt = mediaType.mediaType();
			ct = mediaType.contentType();
		}
		int payloadContentType = ct;
		String payloadMediaType = mt;
		CoapRequestFuture request = new CoapRequestFuture();
		request.addParent(this);
		request.setPreProcess(() -> {
			Form update = get().getForm(key);
			if (update == null) {
				throw new ExecutionException(new Exception("Form not found"));
			}
			if (update.getAccept() != null && !update.getAccept().equals(payloadMediaType)) {
				throw new ExecutionException(new Exception("Invalid media type"));
			}
			try {
				String url = Utils.resolve(getUrl(), get().getBase(), update.getHref());
				request.setMethod(update.getMethod());
				request.setUrl(url);
				request.setPayloadContentType(payloadContentType);
				request.setPayload(ResourceFuture.gson.toJson(payload));
				request.setExpectedContentType(MediaTypeRegistry.UNDEFINED);
			} catch (URISyntaxException e) {
				throw new ExecutionException(e);
			}
		});
		request.setPostProcess(() -> {
			reset(false);
		});
		return request;
	}

	public <X extends CoREAppBase, Y extends CoREAppResourceFuture<X>> Y follow(Supplier<Y> type, String rel) {
		Y config = type.get();
		config.addParent(this);
		config.setPreProcess(() -> {
			// Get config link
			Link link = get().getLink(rel);
			boolean loaded = get().loadEmbedded(rel, config, getBaseUrl(), this);

			if (!loaded) {
				// Validate config link
				if (link != null && (link.getType() == null || link.getType().equals(config.getMediaType()))) {
					config.setRequestURL(link.getUrl(get(), getUrl()));
					return;
				}
				config.setException(new Exception("Not found"));
			}
		});
		return config;
	}

	public String getBaseUrl() {
		try {
			String url = getUrl();
			if (url == null) {
				throw new RuntimeException("Invalid state " + getClassName() + " " + getStateName());
			}
			return Utils.resolve(url, get().getBase());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
