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
package ch.ethz.inf.vs.hypermedia.hartke.lighting.client;

import ch.ethz.inf.vs.hypermedia.client.BaseFuture;

import java.util.function.Supplier;

/**
 * Created by ynh on 10/10/15.
 */
public class FailFuture {

	public static <V extends BaseFuture> V fail(Supplier<V> type) {
		V item = type.get();
		item.setPreProcess(() -> {
			item.setException(new Exception());
		});
		return item;
	}
}
