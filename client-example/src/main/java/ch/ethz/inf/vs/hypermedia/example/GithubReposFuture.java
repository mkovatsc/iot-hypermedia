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

import ch.ethz.inf.vs.hypermedia.client.utils.LazyIterator;
import com.google.common.collect.Iterators;
import com.google.gson.JsonElement;

import java.util.Iterator;

/**
 * Created by ynh on 24/11/15.
 */
public class GithubReposFuture extends GithubBaseFuture implements Iterable<GithubRepoFuture> {

    @Override
    public Iterator<GithubRepoFuture> iterator() {
        return Iterators.transform(tryGet().getAsJsonArray().iterator(),
                    GithubReposFuture::transform);
    }

    public static GithubRepoFuture transform(JsonElement el){
        GithubRepoFuture item = new GithubRepoFuture();
        item.setPreProcess(()->{
            item.set(el);
        });
        return item;
    }
}
