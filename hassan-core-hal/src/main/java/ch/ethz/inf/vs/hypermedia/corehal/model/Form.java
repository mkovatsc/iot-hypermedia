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

import ch.ethz.inf.vs.hypermedia.corehal.OptionalList;
import com.google.gson.annotations.SerializedName;

/**
 * Created by ynh on 25/09/15.
 */
public class Form {

	@SerializedName("name")
	private OptionalList<String> names;

	private String href;
	private String method;
	private String accepts;
	private String[] excludes;
	private String value;
	private String input;
	private String type;
	private Boolean templated;

	public Form(String method, String href, String accepts) {
		this.method = method;
		this.href = href;
		this.accepts = accepts;
	}

	public String getHref() {
		return href;
	}

	public Form setHref(String href) {
		this.href = href;
		return this;
	}

	public String getMethod() {
		return method;
	}

	public Form setMethod(String method) {
		this.method = method;
		return this;
	}

	public String getAccepts() {
		return accepts;
	}

	public Form setAccepts(String accepts) {
		this.accepts = accepts;
		return this;
	}

	public String[] getExcludes() {
		return excludes;
	}

	public Form setExcludes(String[] excludes) {
		this.excludes = excludes;
		return this;
	}

	public String getValue() {
		return value;
	}

	public Form setValue(String value) {
		this.value = value;
		return this;
	}

	public String getInput() {
		if (input == null)
			return getAccepts();
		return input;
	}

	public Form setInput(String input) {
		this.input = input;
		return this;
	}

	public Boolean getTemplated() {
		return templated;
	}

	public Form setTemplated(Boolean templated) {
		this.templated = templated;
		return this;
	}

	public OptionalList<String> getNames() {
		return names;
	}

	public Form setNames(String... names) {
		this.names = new OptionalList<>();
		for (String name : names)
			this.names.add(name);
		return this;
	}

	public String getType() {
		return type;
	}

	public Form setType(String type) {
		this.type = type;
		return this;
	}
}
