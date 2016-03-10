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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by ynh on 25/09/15.
 */
public class HTTPRequestFuture extends BaseFuture<String> {

	private static final Logger LOGGER = Logger.getLogger(HTTPRequestFuture.class.getName());
	private final String url;
	private final String mediaType;

	public HTTPRequestFuture(String url, String mediaType) {
		this.url = url;
		this.mediaType = mediaType;
	}

	@Override
	public void process() throws InterruptedException {
		try {
			LOGGER.info("GET " + this.url + " @" + hashCode());
			StringBuilder result = new StringBuilder();
			URL url = new URL(this.url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			set(result.toString());
		} catch (Exception ex) {
			setException(ex);
		}
	}
}
