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

import ch.ethz.inf.vs.hypermedia.client.BaseFuture;
import ch.ethz.inf.vs.hypermedia.client.CoapRequestFuture;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.FormCollection;
import ch.ethz.inf.vs.hypermedia.corehal.LinkCollection;
import ch.ethz.inf.vs.hypermedia.corehal.OptionalList;
import ch.ethz.inf.vs.hypermedia.corehal.block.CoREHalResourceFuture;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 25/09/15.
 */
public class CoREHalBase {
	private transient HashMap<String, BaseFuture> loaders = new HashMap<>();
	@SerializedName("_links")
	private LinkCollection links;

	@SerializedName("_forms")
	private FormCollection forms;

	@SerializedName("_embedded")
	private HashMap<String, OptionalList<JsonElement>> embedded;

	@SerializedName("_decoupled")
	private HashMap<String, Link> decoupled;

	@SerializedName("_self")
	private String self;
	private transient JsonObject json;

	public LinkCollection getLinks() {
		return links;
	}

	public void setLinks(LinkCollection links) {
		this.links = links;
	}

	public void addLink(String name, Link link) {
		if (links == null) {
			links = new LinkCollection();
		}
		links.put(name, link);
	}

	public void addLink(String name, Link link, JsonElement obj) {
		addLink(name, link);
		if (embedded == null) {
			embedded = new HashMap<>();
		}

		embedded.computeIfAbsent(name, (x) -> new OptionalList<JsonElement>()).add(obj);
	}

	public void addDecoupled(String name, Link link) {
		if (getDecoupled() == null) {
			setDecoupled(new HashMap<>());
		}
		getDecoupled().put(name, link);
	}

	public FormCollection getForms() {
		return forms;
	}

	public void setForms(FormCollection forms) {
		this.forms = forms;
	}

	public void addForm(String name, Form form) {
		if (forms == null) {
			forms = new FormCollection();
		}
		forms.put(name, form);
	}

	public List<JsonElement> getEmbedded(String key) {
		if (embedded == null)
			return null;
		OptionalList<JsonElement> list = embedded.get(key);
		return list == null ? Collections.emptyList() : list;
	}

//	public void addEmbedded(String name, Object obj) {
//		if (embedded == null) {
//			embedded = new HashMap<>();
//		}
//		embedded.put(name, obj);
//	}

	public Link getLink(String key) {
		return getLinks().getLink(key, null);
	}

	public Link getLink(String key, String name) {
		return getLinks().getLink(key, name);
	}

	public Iterable<Link> getLinks(String key) {
		LinkCollection links = getLinks();
		if (links == null) {
			return Collections.emptyList();
		}
		return links.getLinks(key);
	}

	public Form getForm(String key) {
		return getForms().getForm(key, null);
	}

	public Form getForm(String key, String name) {
		return getForms().getForm(key, name);
	}

	public Collection<Form> getForms(String key) {
		return getForms().getForm(key);
	}

	public String getSelf(String url) {
		try {
			if (url == null)
				return self;
			return Utils.resolve(url, self);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setSelf(String self) {
		this.self = self;
	}

	@Override
	public String toString() {
		return CoREHalResourceFuture.serialize(this);
	}

	public void setJson(JsonObject json) {
		this.json = json;
	}

	public JsonObject json() {
		return json;
	}

	public HashMap<String, Link> getDecoupled() {
		return decoupled;
	}

	public void setDecoupled(HashMap<String, Link> decoupled) {
		this.decoupled = decoupled;
	}

	public BaseFuture decoupledLoader(String itemname, String url) {
		Preconditions.checkState(getDecoupled() != null);
		Preconditions.checkState(getDecoupled().containsKey(itemname));

		return loaders.computeIfAbsent(itemname, (k) -> {
			Link link = getDecoupled().get(k);
			BaseFuture sf = new BaseFuture() {
				@Override
				public void process() throws Exception {
					CoapRequestFuture req = new CoapRequestFuture(link.getUrl(url), MediaTypeRegistry.UNDEFINED);
					String data = req.get().getResponseText();
					Class<? extends CoREHalBase> tempClass = CoREHalBase.this.getClass();
					try {
						tempClass.getMethod("set" + k.substring(0, 1).toUpperCase() + k.substring(1), data.getClass()).invoke(CoREHalBase.this, data);
					} catch (Exception e) {
						CoREHalBase.this.set(k, data);
					}
					removeDecoupled(itemname);
					this.set(data);
				}
			};
			return sf;
		});
	}

	public Object loadDecoupled(String itemname, String url) {
		try {
			return decoupledLoader(itemname, url).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void eagerLoad() {
		if (getDecoupled() == null)
			return;
		ArrayList<String> keys = new ArrayList<>();
		keys.addAll(getDecoupled().keySet());
		keys.stream().map(k -> decoupledLoader(k, null)).forEach(x -> {
			try {
				x.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
	}

	public Object get(String key) {
		return CoREHalResourceFuture.getGson().fromJson(json.get(key), Object.class);
	}

	public void set(String key, Object o) {
		json.add(key, CoREHalResourceFuture.getGson().toJsonTree(o));
	}

	public void removeDecoupled(String itemname) {
		if (decoupled != null) {
			decoupled.remove(itemname);
			if (decoupled.size() == 0) {
				decoupled = null;
			}
		}
	}
}
