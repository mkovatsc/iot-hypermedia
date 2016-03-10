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
package ch.ethz.inf.vs.hypermedia.example;

import ch.ethz.inf.vs.hypermedia.client.HTTPResourceFuture;
import com.damnhandy.uri.template.UriTemplate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by ynh on 26/09/15.
 */
public class GithubBaseFuture extends HTTPResourceFuture<JsonElement> {

    @Override
    public String getMediaType() {
        return "application/vnd.github.v3+json";
    }

    public <V extends GithubBaseFuture> V follow(String rel, Map<String, Object> parameters, Supplier<V> supplier) {
        V next = supplier.get();
        next.addParent(this);
        next.setPreProcess(() -> {
            JsonObject item = get().getAsJsonObject();
            // Get next link
            if (item.has(rel + "_url")) {
                // Resolve url template
                String uriTemplate = item.get(rel + "_url").getAsString();
                String url = UriTemplate.fromTemplate(uriTemplate)
                        .expand(parameters);
                // Pass resolved url to future
                next.setRequestURL(url);
                return;
            }
            next.setException(new Exception("Not found"));
        });
        return next;
    }

}
