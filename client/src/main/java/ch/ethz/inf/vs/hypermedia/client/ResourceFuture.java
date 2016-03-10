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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javassist.util.proxy.ProxyFactory;

import java.util.logging.Logger;

/**
 * Created by ynh on 26/09/15.
 */
public class ResourceFuture<V> extends BaseFuture<V> implements LoadableFuture<V> {
	private static final Logger LOGGER = Logger.getLogger(CoapRequestFuture.class.getName());

	public static Gson gson = new GsonBuilder().serializeNulls().create();
	private final Class<V> type;
	private CoapRequestFuture request;
	private String mediaType;
	protected String source;
	private int contentType = -1;
	private Future<V> sourceFuture;
	private LoadableFuture<V> active;

	public ResourceFuture() {
		try {
			this.type = (Class<V>) Utils.getTypeArguments(ResourceFuture.class, getClass()).get(0);
			MediaType mediatype = Utils.getMediaTypeAnnotation(type);
			if (mediatype != null) {
				contentType = mediatype.contentType();
				mediaType = mediatype.mediaType();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ResourceFuture(int contentType, String mediaType) {
		type = (Class<V>) JsonObject.class;
		this.mediaType = mediaType;
		this.contentType = contentType;
	}

	public static <T> Class<T> createProxyClass(Class<T> classs) throws Exception {
		ProxyFactory factory = new ProxyFactory();
		factory.setUseCache(true);
		factory.setSuperclass(classs);
		Class clazz = factory.createClass();
		return clazz;
	}

	public void setFromSource(V item, String source, Future sourceFuture) throws InterruptedException {
		assert request == null;
		if (getState() == BaseFuture.LOADING || getState() == BaseFuture.READY) {
			setState(BaseFuture.LOADING);
			this.source = source;
			this.sourceFuture = sourceFuture;
			set(item);
		}
	}

	@Override
	public void setFromSource(String item, String source, Future sourceFuture) throws Exception {
		setFromSource(deserialize(item), source, sourceFuture);
	}

	@Override
	public int getContentType() {
		return this.contentType;
	}

	@Override
	public void process() throws Exception {
		set(deserialize(getResponseText()));
	}

	protected String getResponseText() throws InterruptedException, java.util.concurrent.ExecutionException {
		return request.get().getResponseText();
	}

	@Override
	public V deserialize(String text) throws Exception {
		if (getMediaType() != null && getMediaType().endsWith("+json"))
			return gson.fromJson(text, getType());
		throw new Exception("No deserializer defined for media type " + getMediaType());
	}

	@Override
	public String getUrl() {
		ResourceFuture<V> linkSource = (ResourceFuture<V>) this.linkSource;
		if (linkSource != null) {
			return linkSource.getUrl();
		}
		return source;
	}

	@Override
	public void setRequestURL(String url) {
		setRequestURL(url, false);
	}

	public void setRequestURL(String url, boolean allowEmpty) {
		request = new CoapRequestFuture(url, getContentType());
		request.setAllowEmpty(allowEmpty);
		source = url;
		addDependency(request);
	}

	@Override
	public String getMediaType() {
		return mediaType;
	}

	public Class<V> getType() {
		return type;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		cleanupRequest();
		source = null;
		Future sourceFuture = this.sourceFuture;
		this.sourceFuture = null;
		if (sourceFuture != null) {
			sourceFuture.reset(false);
		}
	}

	protected void cleanupRequest() {
		request = null;
	}
}
