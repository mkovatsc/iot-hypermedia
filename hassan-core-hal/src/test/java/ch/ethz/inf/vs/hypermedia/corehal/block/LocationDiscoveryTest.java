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
import ch.ethz.inf.vs.hypermedia.corehal.model.LocationDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by ynh on 05/11/15.
 */
@RunWith(Parameterized.class)
public class LocationDiscoveryTest extends BaseLocationTest {

    private static ArrayList<ArrayList<Integer>> items;

    public LocationDiscoveryTest(String[] path, boolean addLocationToRD, ArrayList<Integer> connections) {
        super(path, addLocationToRD, connections);
    }

    @Parameters(name = "{1} {2}")
    public static Collection<Object[]> data() {
        String[] path = "/CH/ETH/CAB/51".substring(1).split("/");
        return getFlags(path.length).stream().flatMap(x -> Stream.of(new Object[]{path, true, x}, new Object[]{path, false, x})).collect(Collectors.toList());
    }

    private static ArrayList<ArrayList<Integer>> getFlags(int i) {
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        if (i == 1) {
            ArrayList<Integer> item = new ArrayList<Integer>();
            item.add(0);
            list.add(item);
            return list;
        }
        items = getFlags(i - 1);
        for (ArrayList<Integer> it : items) {
            for (int j = 0; j < i; j++) {
                ArrayList<Integer> item = new ArrayList<Integer>();
                item.addAll(it);
                item.add(j);
                list.add(item);
            }

        }
        return list;
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH/CAB/51";
        LocationDescriptionFuture locationDesc = findLocation(location);
        LocationDescription item = locationDesc.get();
        assertEquals(location, item.getLocation());
    }


    @Test
    public void test2() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH/CAB";
        LocationDescriptionFuture locationDesc = findLocation(location);
        LocationDescription item = locationDesc.get();
        assertEquals(location, item.getLocation());
    }


    @Test
    public void test3() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH";
        LocationDescriptionFuture locationDesc = findLocation(location);
        LocationDescription item = locationDesc.get();
        assertEquals(location, item.getLocation());
    }


    @Test
    public void testInvalidLocation() throws ExecutionException, InterruptedException {
        String location = "/CH/ETH/CAB/51xxx";
        LocationDescriptionFuture locationDesc = findLocation(location);
        try {
            locationDesc.get();
            assertFalse(true);
        } catch (Exception ex) {
            assertTrue(true);

        }
    }

    @Test
    public void testDiscovery() throws ExecutionException, InterruptedException {
        HypermediaClient client = new HypermediaClient(getEndpoint());

        Object[] locationDesc = client.links()
                .use(new LocationCrawler())
                .locationStartWith("/CH/ETH")
                .stream()
                .map(this::getLocation)
                .sorted()
                .collect(Collectors.toList())
                .toArray();

        assertArrayEquals(new String[]{"/CH/ETH", "/CH/ETH/CAB", "/CH/ETH/CAB/51", "/CH/ETH/CAB/51_2"}, locationDesc);
    }

    @Test
    public void testDiscovery2() throws ExecutionException, InterruptedException {
        HypermediaClient client = new HypermediaClient(getEndpoint());

        Object[] locationDesc = client.links()
                .use(new LocationCrawler())
                .matchingPrefix("/CH/ETH/CAB")
                .stream()
                .map(this::getLocation)
                .sorted()
                .collect(Collectors.toList())
                .toArray();

        assertArrayEquals(new String[]{"/CH", "/CH/ETH", "/CH/ETH/CAB"}, locationDesc);
    }

    @Test
    public void testDiscovery3() throws ExecutionException, InterruptedException {
        HypermediaClient client = new HypermediaClient(getEndpoint());

        Object[] locationDesc = client.links()
                .use(new LocationCrawler())
                .matchingPrefix("/CH/ETH/CAB/51")
                .stream()
                .map(this::getLocation)
                .sorted()
                .collect(Collectors.toList())
                .toArray();

        assertArrayEquals(new String[]{"/CH", "/CH/ETH", "/CH/ETH/CAB", "/CH/ETH/CAB/51"}, locationDesc);
    }


    public LocationDescriptionFuture findLocation(String location) {
        HypermediaClient client = new HypermediaClient(getEndpoint());
        return client.links()
                .use(new LocationCrawler())
                .locationEquals(location)
                .one(LocationDescriptionFuture::new);

    }

}