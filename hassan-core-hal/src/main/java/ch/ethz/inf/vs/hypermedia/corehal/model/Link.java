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
package ch.ethz.inf.vs.hypermedia.corehal.model;

import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.OptionalList;
import com.google.gson.annotations.SerializedName;

import java.net.URISyntaxException;

/**
 * Created by ynh on 24/09/15.
 */
public class Link {

	private String href;

	private String type;

	@SerializedName("name")
	private OptionalList<String> names;

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

	public Link setHref(String href) {
		this.href = href;
		return this;
	}

	public String getType() {
		return type;
	}

	public Link setType(String type) {
		this.type = type;
		return this;
	}

	public String getUrl(String url) {
		try {
			return Utils.resolve(url, getHref());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public OptionalList<String> getNames() {
		return names;
	}

	public Link setNames(String... names) {
		this.names = new OptionalList<>();
		for (String name : names)
			this.names.add(name);
		return this;
	}
}
