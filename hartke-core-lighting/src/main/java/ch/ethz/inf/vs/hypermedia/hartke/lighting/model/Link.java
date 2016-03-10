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
package ch.ethz.inf.vs.hypermedia.hartke.lighting.model;

import ch.ethz.inf.vs.hypermedia.client.Utils;

import java.net.URISyntaxException;

/**
 * Created by ynh on 24/09/15.
 */
public class Link {

	private String href;

	private String type;

	public Link() {
	}

	public Link(String href) {
		this.href = href;
	}

	public Link(String href, String type) {
		this.href = href;
		this.type = type;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl(CoREAppBase context, String url) {
		try {
			return Utils.resolve(url, context.getBase(), getHref());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
}
