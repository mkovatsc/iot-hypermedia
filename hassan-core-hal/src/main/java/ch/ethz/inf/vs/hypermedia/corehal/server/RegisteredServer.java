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
package ch.ethz.inf.vs.hypermedia.corehal.server;

import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

import java.util.List;

/**
 * Created by ynh on 06/11/15.
 */
public interface RegisteredServer {
	Resource getRoot();

	List<Endpoint> getEndpoints();

	String getId();

	void addRDHandle(String handle);

	void updateRegistration();
}
