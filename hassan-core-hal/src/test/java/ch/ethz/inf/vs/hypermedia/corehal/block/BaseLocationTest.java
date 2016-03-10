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

import ch.ethz.inf.vs.hypermedia.corehal.server.SetupLocationServers;
import org.eclipse.californium.core.CoapServer;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 09/11/15.
 */
public class BaseLocationTest {
    protected final SetupLocationServers setupLocationServers;

    public BaseLocationTest(String[] path, boolean addLocationToRD, ArrayList<Integer> connections) {
        setupLocationServers = new SetupLocationServers(path, addLocationToRD, connections, true);
    }

    @Before
    public void setUp() throws Exception {
        setupLocationServers.setUp();
    }

    public void registerLocation(CoapServer rd, CoapServer server) throws IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
        setupLocationServers.registerLocation(rd, server);
    }

    public void addServer(CoapServer cab) {
        setupLocationServers.addServer(cab);
    }

    @After
    public void tearDown() throws InterruptedException {
        setupLocationServers.tearDown();
    }

    public String getEndpoint() {
        return setupLocationServers.getEndpoint();
    }

    public String getLocation(LocationDescriptionFuture x) {
        return setupLocationServers.getLocation(x);
    }
}
