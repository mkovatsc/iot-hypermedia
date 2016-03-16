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

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;
import ch.ethz.inf.vs.hypermedia.corehal.server.CoREHALUtils;
import ch.ethz.inf.vs.hypermedia.corehal.server.DeviceServer;
import ch.ethz.inf.vs.hypermedia.corehal.server.SetupLocationServers;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by ynh on 05/11/15.
 */
@RunWith(Parameterized.class)
public class ThingDiscoveryTest extends BaseLocationTest {

    private static ArrayList<ArrayList<Integer>> items;

    public ThingDiscoveryTest(String[] path, ArrayList<Integer> connections) {
        super(path, true, connections);
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        String[] path = "/CH/ETH/CAB/51".substring(1).split("/");
        return getFlags(path.length).stream().map(x -> new Object[]{path, x}).collect(Collectors.toList());
    }

    private static ArrayList<ArrayList<Integer>> getFlags(int i) {
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        for (int k = 0; k <= i; k++) {
            ArrayList<Integer> item = new ArrayList<>();
            ArrayList<Integer> item2 = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                item.add(j);
                item2.add(0);
            }
            item.add(k);
            item2.add(k);
            list.add(item2);
            list.add(item);
        }
        return list;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        DeviceServer server = new DeviceServer("/CH/ETH/CAB/51", true);
        server.start();
        Thread.sleep(100);
        if(getThingConnection()==0){
            CoREHALUtils.register(setupLocationServers.rds[getThingConnection()], server);
        }else {
            SetupLocationServers.registerThing(setupLocationServers.rds[getThingConnection()], server);
        }
        System.err.println("??????+++++?????????????????????????????");
        setupLocationServers.rc.reset();
    }

    public int getThingConnection() {
        return setupLocationServers.connections.get(setupLocationServers.connections.size() - 1);
    }

    public boolean connected(int source, int target) {
        if (source == target) {
            return true;
        }
        if (target == 0) {
            return false;
        }
        int next = setupLocationServers.connections.get(target - 1);
        if (next == target) {
            return false;
        }
        return connected(source, next);
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        HypermediaClient client = new HypermediaClient(getEndpoint());


        ThingDescriptionFuture thing = client.links()
                .use(new ThingCrawler())
                .findLocation("/CH/ETH/CAB/51")
                .one(ThingDescriptionFuture::new);
        thing.get();
        assertEquals("/CH/ETH/CAB/51", thing.get().getLocation());
    }

    @Test
    public void test4() throws ExecutionException, InterruptedException {
        Assume.assumeTrue(connected(2, getThingConnection()));
        String location = "/CH/ETH";
        LocationDescriptionFuture locationDesc = getLocation(location);
        ThingDescriptionFuture thing = locationDesc.getThingCrawler()
                .one(ThingDescriptionFuture::new);
        assertEquals("/CH/ETH/CAB/51", thing.get().getLocation());
    }

    public LocationDescriptionFuture getLocation(String location) {
        String endpoint = getEndpoint();
        HypermediaClient client = new HypermediaClient(endpoint);

        return client.links()
                .use(new LocationCrawler())
                .locationEquals(location)
                .one(LocationDescriptionFuture::new);
    }

}