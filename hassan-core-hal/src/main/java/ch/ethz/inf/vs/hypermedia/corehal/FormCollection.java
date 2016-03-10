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
package ch.ethz.inf.vs.hypermedia.corehal;

import ch.ethz.inf.vs.hypermedia.corehal.model.Form;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by ynh on 26/11/15.
 */
public class FormCollection extends HashMap<String, FormList> {
	public Form put(String key, Form value) {
		FormList list = computeIfAbsent(key, (x) -> new FormList());
		list.add(value);
		return value;
	}

	public Form getForm(String s, String name) {
		return get(s).get(name);
	}

	public Collection<Form> getForm(String key) {
		return get(key).values();
	}
}
