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
import ch.ethz.inf.vs.hypermedia.client.inspector.HypermediaClientInspector;
import ch.ethz.inf.vs.hypermedia.client.inspector.RequestCounter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by ynh on 05/11/15.
 */
@RunWith(Parameterized.class)
public class LocationTest extends BaseLocationTest {

    private static ArrayList<ArrayList<Integer>> items;

    public LocationTest(String[] path, ArrayList<Integer> connections) {
        super(path, true, connections);
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        String[] path = "/CH/ETH/CAB/51".substring(1).split("/");
        return getFlags(path.length).stream().map(x -> new Object[]{path, x}).collect(Collectors.toList());
    }

    private static ArrayList<ArrayList<Integer>> getFlags(int i) {
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> item = new ArrayList<Integer>();
        for (int j = 0; j < i; j++) {
            item.add(j);
        }
        list.add(item);
        return list;
    }


    @Test
    public void test() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH/CAB";
        LocationDescriptionFuture locationDesc = getLocation(location);
        assertTrue(locationDesc.getChildren().stream().map(x -> x.tryGet().getLocation()).collect(Collectors.toList()).contains("/CH/ETH/CAB/51"));
        RequestCounter rc = new RequestCounter();
        HypermediaClientInspector.add(rc);
        assertTrue(locationDesc.getChildren().stream().map(x -> x.tryGet().getLocation()).collect(Collectors.toList()).contains("/CH/ETH/CAB/51"));
        assertEquals(0, rc.getRequests());
        locationDesc.reset(false);
        assertEquals(0, rc.getRequests());
        assertTrue(locationDesc.getChildren().stream().map(x -> x.tryGet().getLocation()).collect(Collectors.toList()).contains("/CH/ETH/CAB/51"));
        assertTrue(0 < rc.getRequests());
        int normalReset = rc.getRequests();
        rc.reset();
        locationDesc.reset(true);
        assertTrue(locationDesc.getChildren().stream().map(x -> x.tryGet().getLocation()).collect(Collectors.toList()).contains("/CH/ETH/CAB/51"));
        assertTrue(normalReset < rc.getRequests());

    }


    @Test
    public void test2() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH/CAB";
        LocationDescriptionFuture locationDesc = getLocation(location);
        assertNotNull(locationDesc.getChildLocation("/CH/ETH/CAB/51"));
        RequestCounter rc = new RequestCounter();
        HypermediaClientInspector.add(rc);
        assertNotNull(locationDesc.getChildLocation("/CH/ETH/CAB/51"));
        assertEquals(0, rc.getRequests());
        locationDesc.getChildLocation("/CH/ETH/CAB/51").getChildren();
        assertNotEquals(0, rc.getRequests());
    }


    @Test
    public void test3() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH";
        LocationDescriptionFuture locationDesc = getLocation(location);
        LocationDescriptionFuture subLocations = locationDesc.findSubLocation("/CH/ETH/CAB/51");
        assertNotNull(subLocations);
        assertEquals("/CH/ETH/CAB/51", subLocations.get().getLocation());
    }

    @Test
    public void test4() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH";
        LocationDescriptionFuture locationDesc = getLocation(location);
        List<String> subLocations = locationDesc.getLocationCrawler()
                .stream().map(this::getLocation).sorted().collect(Collectors.toList());
        assertNotNull(subLocations);
        assertArrayEquals(new String[]{"/CH/ETH", "/CH/ETH/CAB", "/CH/ETH/CAB/51", "/CH/ETH/CAB/51_2"}, subLocations.toArray());
    }

    public LocationDescriptionFuture getLocation(String location) {
        String endpoint = getEndpoint();
        HypermediaClient client = new HypermediaClient(endpoint);

        return client.resources()
                .use(new LocationCrawler())
                .locationEquals(location)
                .one(LocationDescriptionFuture::new);
    }

}