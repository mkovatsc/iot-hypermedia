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

import ch.ethz.inf.vs.hypermedia.client.inspector.HypermediaClientInspector;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.logging.Logger;

/**
 * Created by ynh on 25/09/15.
 */
public class CoapRequestFuture extends BaseFuture<CoapResponse> {

	private static final Logger LOGGER = Logger.getLogger(CoapRequestFuture.class.getName());
	private String method;
	private String url;
	private int payloadContentType;
	private String payload;
	private int expectedContentType = MediaTypeRegistry.UNDEFINED;
	private boolean allowEmpty;

	public CoapRequestFuture(String method, String url, int payloadContentType, String payload, int expectedContentType) {
		this.method = method;
		this.url = url;
		this.payloadContentType = payloadContentType;
		this.payload = payload;
		this.expectedContentType = expectedContentType;
	}

	public CoapRequestFuture(String url, int expectedContentType) {
		this("GET", url, MediaTypeRegistry.UNDEFINED, "", expectedContentType);
	}

	public CoapRequestFuture() {

	}

	@Override
	public void process() throws InterruptedException {
		int id = HypermediaClientInspector.startRequest(getMethod(), getUrl(), getPayloadContentType(), getPayload());
		CoapClient client = new CoapClient(getUrl());
		LOGGER.info(getMethod() + " " + getUrl() + " @" + hashCode());
		client.setTimeout(2000);
		try {
			switch (getMethod()) {
			case "GET":
				set(client.get());
				break;
			case "POST":
				set(client.post(getPayload(), getPayloadContentType()));
				break;
			case "PUT":
				set(client.put(getPayload(), getPayloadContentType()));
				break;
			case "DELETE":
				set(client.delete());
				break;
			}
			HypermediaClientInspector.endRequest(id, false);
		} catch (RuntimeException e) {
			HypermediaClientInspector.endRequest(id, true);
			if (e.getCause() instanceof InterruptedException) {
				return;
			}
			throw e;
		}
	}

	@Override
	public boolean set(CoapResponse value) throws InterruptedException {
		if (allowEmpty) {
			return super.set(value);

		}
		if (value != null) {
			if (getExpectedContentType() == MediaTypeRegistry.UNDEFINED || getExpectedContentType() == value.getOptions().getContentFormat()) {
				return super.set(value);
			} else {
				setException(new RuntimeException("Invalid Content Type " + getUrl()));
			}
		} else {
			setException(new RuntimeException("Request Failed"));
		}
		return false;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getExpectedContentType() {
		return expectedContentType;
	}

	public void setExpectedContentType(int expectedContentType) {
		this.expectedContentType = expectedContentType;
	}

	public int getPayloadContentType() {
		return payloadContentType;
	}

	public void setPayloadContentType(int payloadContentType) {
		this.payloadContentType = payloadContentType;
	}

	public void setAllowEmpty(boolean ignoreError) {
		this.allowEmpty = ignoreError;
	}
}
