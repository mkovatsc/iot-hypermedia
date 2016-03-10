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

import ch.ethz.inf.vs.hypermedia.client.ResourceFuture;
import ch.ethz.inf.vs.hypermedia.hartke.lighting.block.CoREAppResourceFuture;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ynh on 25/09/15.
 */
public class CoREAppBase {
	@SerializedName("_base")
	private String base;
	@SerializedName("_links")
	private HashMap<String, Link> links;
	@SerializedName("_forms")
	private HashMap<String, Form> forms;
	@SerializedName("_embedded")
	private HashMap<String, Object> embedded;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public HashMap<String, Link> getLinks() {
		return links;
	}

	public void setLinks(HashMap<String, Link> links) {
		this.links = links;
	}

	public void addLink(String name, Link link) {
		if (links == null) {
			links = new HashMap<>();
		}
		links.put(name, link);
	}

	public Map<String, Form> getForms() {
		return forms;
	}

	public void setForms(HashMap<String, Form> forms) {
		this.forms = forms;
	}

	public void addForm(String name, Form form) {
		if (forms == null) {
			forms = new HashMap<>();
		}
		forms.put(name, form);
	}

	public Map<String, Object> getEmbedded() {
		return embedded;
	}

	public void setEmbedded(HashMap<String, Object> embedded) {
		this.embedded = embedded;
	}

	public void addEmbedded(String name, Object obj) {
		if (embedded == null) {
			embedded = new HashMap<>();
		}
		embedded.put(name, obj);
	}

	public Link getLink(String key) {
		return links.get(key);
	}

	public Form getForm(String key) {
		return getForms().get(key);
	}

	public <X extends CoREAppBase, V extends CoREAppResourceFuture<X>> boolean loadEmbedded(String rel, V container, String source, CoREAppResourceFuture capp) {
		if (embedded != null && embedded.containsKey(rel)) {
			try {
				container.setFromSource(ResourceFuture.gson.toJson(embedded.get(rel)), source, capp);
				return true;
			} catch (Exception ex) {

			}
		}
		return false;
	}

	@Override
	public String toString() {
		return ResourceFuture.gson.toJson(this);
	}
}
