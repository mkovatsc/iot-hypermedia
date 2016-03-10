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

import ch.ethz.inf.vs.hypermedia.client.*;
import ch.ethz.inf.vs.hypermedia.client.observe.CoapObserver;
import ch.ethz.inf.vs.hypermedia.client.observe.ObservableFuture;
import ch.ethz.inf.vs.hypermedia.client.observe.Observer;
import ch.ethz.inf.vs.hypermedia.corehal.*;
import ch.ethz.inf.vs.hypermedia.corehal.model.CoREHalBase;
import ch.ethz.inf.vs.hypermedia.corehal.model.Form;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import com.damnhandy.uri.template.UriTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by ynh on 26/09/15.
 */
public class CoREHalResourceFuture<V extends CoREHalBase> extends ResourceFuture<V> implements ObservableFuture<V> {
	private static final Logger LOGGER = Logger.getLogger(CoapRequestFuture.class.getName());

	public static Gson gson = null;

	public boolean isForceFullLoad() {
		return forceFullLoad;
	}

	private boolean forceFullLoad;

	public static Gson getGson() {
		if (gson == null) {
			gson = getGsonBuilder().create();
		}
		return gson;
	}

	public static GsonBuilder getGsonBuilder() {
		return new GsonBuilder().registerTypeAdapter(LinkList.class, new LinkListDeserializer()).registerTypeAdapter(OptionalList.class, new OptionalListDeserializer()).registerTypeAdapter(FormList.class, new FormListDeserializer()).serializeNulls();
	}

	public static <V extends CoREHalResourceFuture> V createFromWebLink(Supplier<V> type, WebLink item) {
		String location = Utils.getWebLinkAttribute(item, "location");
		V block = type.get();
		JsonObject jo = new JsonObject();
		jo.addProperty("_self", item.getURI());
		if (location != null) {
			jo.addProperty("location", location);
		}
		block.loadPartial(jo);
		return block;
	}

	@Override
	public V deserialize(String text) throws Exception {
		JsonObject jsonobj = getGson().fromJson(text, JsonObject.class);
		boolean hasDecoupled = jsonobj.has("_decoupled") && jsonobj.get("_decoupled").isJsonObject() && jsonobj.get("_decoupled").getAsJsonObject().entrySet().size() > 0;

		Class<V> type = hasDecoupled ? createProxyClass(getType()) : getType();
		V item = getGson().fromJson(jsonobj, type);
		item.setJson(jsonobj);
		if (hasDecoupled) {
			MethodHandler handler = (self, overridden, forwarder, args) -> {
				String methodName = overridden.getName();
				if (methodName.equals("decoupledLoader")) {
					args[1] = getUrl();
				}
				if ((!methodName.startsWith("get") && !methodName.startsWith("set")) || methodName.equals("getDecoupled")) {
					return forwarder.invoke(self, args);
				}
				// Intercept call to the getter function getter access
				String itemname = methodName.substring(3);
				if (methodName.equals("get") || methodName.equals("set")) {
					itemname = (String) args[0];
				}
				itemname = itemname.substring(0, 1).toLowerCase() + itemname.substring(1);
				if (methodName.startsWith("get")) {
					if (item.getDecoupled() != null && item.getDecoupled().containsKey(itemname)) {
						return item.loadDecoupled(itemname, getUrl());
					}
				}
				Object invoke = forwarder.invoke(self, args);
				if (methodName.startsWith("set")) {
					item.removeDecoupled(itemname);
				}
				return invoke;
			};
			((ProxyObject) item).setHandler(handler);
		}
		return item;
	}

	public CoapRequestFuture getFormRequest(String key, Object payload) {
		return getFormRequest(key, null, payload);
	}

	public CoapRequestFuture getFormRequest(String key, String name, Object payload) {
		MediaType mediaType = null;
		if (payload != null) {
			mediaType = Utils.getMediaTypeAnnotation(payload.getClass());
		}
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
			V item = get();
			Form form;
			if (item instanceof CoREHalBase) {
				form = ((CoREHalBase) item).getForm(key, name);
			} else {
				throw new RuntimeException("Not implemented");
			}
			if (form == null) {
				throw new ExecutionException(new Exception("Form  rel:" + String.valueOf(key) + " name:" + String.valueOf(name) + " not found"));
			}
			if (form.getInput() != null && payloadMediaType != null && !form.getInput().equals(payloadMediaType)) {
				throw new ExecutionException(new Exception("Invalid payload media type"));
			}
			try {
				Map<String, Object> properties = null;
				String url = form.getHref();
				JsonElement data;
				if (payload instanceof CoREHalBase) {
					data = CoREHalResourceFuture.serializeToJsonTree((CoREHalBase) payload);
				} else {
					data = ResourceFuture.gson.toJsonTree(payload);
				}
				if (form.getTemplated() != null && form.getTemplated() == true) {
					if (properties == null)
						properties = dataToObjectMap(data);
					url = UriTemplate.fromTemplate(url).expand(properties);
				}
				url = Utils.resolve(getUrl(), url);
				String formPayload = "";
				if (form.getValue() != null) {
					if (properties == null)
						properties = dataToObjectMap(data);
					Object val = properties.get(form.getValue());
					if (val instanceof String) {
						formPayload = (String) val;
					} else {
						formPayload = ResourceFuture.gson.toJson(val);
					}
				} else if (form.getExcludes() != null && form.getExcludes().length > 0) {
					for (String k : form.getExcludes()) {
						data.getAsJsonObject().remove(k);
					}
					formPayload = ResourceFuture.gson.toJson(data);
				} else {
					formPayload = ResourceFuture.gson.toJson(data);
				}
				request.setMethod(form.getMethod());
				request.setUrl(url);
				// TODO: use expected payload media type
				request.setPayloadContentType(payloadContentType);
				request.setPayload(formPayload);
				request.setExpectedContentType(MediaTypeRegistry.UNDEFINED);
			} catch (URISyntaxException e) {
				throw new ExecutionException(e);
			}

		});
		return request;
	}

	public Map<String, Object> dataToObjectMap(JsonElement data) {
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		return ResourceFuture.gson.fromJson(data, type);
	}

	public <X, Y extends LoadableFuture<X>> Y follow(String rel, Supplier<Y> type) {
		return follow(rel, null, type);
	}

	public <X, Y extends LoadableFuture<X>> Y follow(String rel, String name, Supplier<Y> type) {
		Y config = type.get();
		config.addParent(this);
		config.setPreProcess(() -> {
			V item = get();
			// Get config link
			Link link;
			if (item instanceof CoREHalBase) {
				link = ((CoREHalBase) item).getLink(rel, name);
			} else {
				throw new RuntimeException("Not implemented");
			}
			if (link != null && (link.getType() == null || config.getMediaType() == null || link.getType().equals(config.getMediaType()))) {
				String url = link.getUrl(getUrl());
				// Check for embedded resource representations
				if (config instanceof CoREHalResourceFuture) {
					JsonObject embeded = getEmbeddedRepresentation(item, rel, url);
					if (embeded != null && !((CoREHalResourceFuture) config).isForceFullLoad()) {
						((CoREHalResourceFuture) config).loadPartial(embeded);
						return;
					}
				}
				config.setRequestURL(url);
				return;
			}
			config.setException(new Exception("Link relation rel:" + String.valueOf(rel) + " name:" + String.valueOf(name) + " not found"));
		});
		return config;
	}

	public <X extends CoREHalResourceFuture> List<X> getEmbeddedStream(String key, Supplier<X> type) {
		V item = tryGet();
		Iterable<Link> items = item.getLinks(key);
		if (items != null) {
			return StreamSupport.stream(items.spliterator(), false).map(x -> {
				X fu = type.get();
				// TODO: FIX URL

				String url = x.getUrl(getUrl());
				boolean partial = false;
				if (item instanceof CoREHalBase) {
					JsonObject embeded = getEmbeddedRepresentation(item, key, url);
					try {
						if (embeded != null) {
							fu.loadPartial(embeded.getAsJsonObject());
							partial = true;
						}
					} catch (Exception e) {

					}
				}
				if (!partial) {
					fu.setPreProcess(() -> {
						fu.setRequestURL(url);
					});
				}
				return fu;
			}).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public JsonObject getEmbeddedRepresentation(CoREHalBase item, String key, String url) {
		JsonObject embeded = null;
		List<JsonElement> elements = item.getEmbedded(key);
		if (elements == null) {
			return null;
		}
		for (JsonElement el : elements) {
			try {
				String elUrl = Utils.resolve(getUrl(), el.getAsJsonObject().get("_self").getAsString());
				if (url.equals(elUrl)) {
					embeded = el.getAsJsonObject();
					break;
				}
			} catch (Exception e) {
			}

		}
		return embeded;
	}

	public void loadPartial(JsonObject jsonElement) {
		if (getState() != READY && getState() != LOADING)
			throw new RuntimeException("Invalid state");
		if (getState() == READY)
			setState(LOADING);
		try {
			V item = getGson().fromJson(jsonElement, createProxyClass(getType()));
			item.setJson(jsonElement.getAsJsonObject());
			MethodHandler handler = (self, overridden, forwarder, args) -> {
				String methodName = overridden.getName();
				if (!isPartiallyLoaded()) {
					return overridden.invoke(get(), args);
				}
				// Intercept call to the getter function getter access
				if (methodName.startsWith("get")) {
					String itemname = methodName.substring(3);
					if (methodName.equals("get")) {
						itemname = (String) args[0];
					} else {
						itemname = itemname.substring(0, 1).toLowerCase() + itemname.substring(1);
					}
					// Check if field was present in the JSON object
					// representation
					if (itemname.equals("link") || itemname.equals("links")) {
						itemname = "_links";
					} else if (itemname.equals("self")) {
						itemname = "_self";
					} else if (itemname.equals("form") || itemname.equals("form")) {
						itemname = "_forms";
					} else if (itemname.equals("embedded") || itemname.equals("embeddedStream")) {
						itemname = "_embedded";
					}
					if (!item.json().has(itemname)) {
						forceFullLoad = true;
						if (compareAndSetState(PARTIALLY_LOADED, READY)) {
							LOGGER.info("Load full object " + getClass().getSimpleName() + " " + methodName);
						}
						return overridden.invoke(get(), args);
					}
				}
				return forwarder.invoke(self, args);
			};
			String url = item.getSelf(getUrl());
			if (!hasPreProcess()) {
				setPreProcess(() -> {
					setRequestURL(url);
				});
			}
			((ProxyObject) item).setHandler(handler);
			setPartial(item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Observer<V> observe() {
		return new CoapObserver<>(this);
	}

	public String getResourceUrl() {
		return tryGet().getSelf(getUrl());
	}

	public static String serialize(CoREHalBase base) {
		JsonElement s = serializeToJsonTree(base);
		return CoREHalResourceFuture.getGson().toJson(s);
	}

	@Override
	public void reset(boolean parent) {
		if (parent) {
			// If parent is reloaded, we can start using the embedded
			// representation
			forceFullLoad = false;
		} else {
			// If resource is reloaded stop using embedded resource
			forceFullLoad = true;
		}
		super.reset(parent);
	}

	public static JsonElement serializeToJsonTree(CoREHalBase base) {
		Class<? extends CoREHalBase> type = base.getClass();
		if (ProxyFactory.isProxyClass(type)) {
			type = (Class<? extends CoREHalBase>) type.getSuperclass();
		}
		JsonElement s = CoREHalResourceFuture.getGson().toJsonTree(base, type);
		if (s.isJsonObject() && base.json() != null) {
			JsonObject json = s.getAsJsonObject();
			base.json().entrySet().forEach((el) -> {
				if (!json.has(el.getKey())) {
					json.add(el.getKey(), el.getValue());
				}
			});
		}
		return s;
	}
}
