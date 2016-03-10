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
import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.TestUtils;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import ch.ethz.inf.vs.hypermedia.corehal.model.CoREHalBase;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
/**
 * Created by ynh on 29/11/15.
 */
public class TestDecoupled {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        TestUtils.setupEnviroment();
        TestServer ts = new TestServer();
        ts.start();
        System.out.println("TEST");
        HypermediaClient hc = new HypermediaClient("coap://localhost:"+ts.getEndpoints().get(0).getAddress().getPort());
        DataBlock db = hc.discover().getByMediaType(DataBlockFuture::new).get();
        System.out.println(db);
        for (int i = 0; i < 10; i++) {
            assertEquals("DataDataDataDataData", db.getData());
            System.out.println(db);
        }
        db.setData("test");
        assertEquals("test", db.getData());
        System.out.println(db);
        System.out.println(db.getData());
        for (int i = 0; i < 10; i++) {
            assertEquals("DataDataDataDataData", db.getDataNumber2());
            System.out.println(db);
        }
        db.setDataNumber2("test");
        assertEquals("test", db.getDataNumber2());
        System.out.println(db);
        System.out.println(db.getData());
    }

    @Test
    public void testGeneric() throws ExecutionException, InterruptedException {
        TestUtils.setupEnviroment();
        TestServer ts = new TestServer();
        ts.start();
        System.out.println("TEST");
        HypermediaClient hc = new HypermediaClient("coap://localhost:"+ts.getEndpoints().get(0).getAddress().getPort());
        CoREHalBase db = hc.discover().getByMediaType(GenericFuture::new).get();
        System.out.println(db);
        for (int i = 0; i < 10; i++) {
            assertEquals("DataDataDataDataData", db.get("data"));
            System.out.println(db);
        }
        db.set("data","test");
        assertEquals("test", db.get("data"));
        System.out.println(db);
        System.out.println(db.get("data"));
        for (int i = 0; i < 10; i++) {
            assertEquals("DataDataDataDataData", db.get("dataNumber2"));
            System.out.println(db);
        }
        db.set("dataNumber2","test");
        assertEquals("test", db.get("dataNumber2"));
        System.out.println(db);
        System.out.println(db.get("data"));
    }

    @Test
    public void testLoadFullObject() throws ExecutionException, InterruptedException {
        TestUtils.setupEnviroment();
        TestServer ts = new TestServer();
        ts.start();
        System.out.println("TEST");
        HypermediaClient hc = new HypermediaClient("coap://localhost:"+ts.getEndpoints().get(0).getAddress().getPort());
        DataBlock db = hc.discover().getByMediaType(DataBlockFuture::new).get();
        System.out.println(db);
        assertEquals(2, db.getDecoupled().size());
        db.eagerLoad();
        assertNull(db.getDecoupled());
        System.out.println(db);
    }
    static class GenericFuture extends CoREHalBaseResourceFuture{
        @Override
        public int getContentType() {
            return  Utils.getContentType(DataBlock.class);
        }
    }
    private class TestServer extends CoapServer {
        public TestServer(){
            super();
            addEndpoint(TestConnector.getEndpoint(0));
            add(new DecoupledData("data"));
        }

        private class DecoupledData extends CoapResource {

            private final DataResource d;

            public DecoupledData(String name) {
                super(name);
                d = new DataResource("d");
                add(d);
                getAttributes().addContentType(Utils.getContentType(DataBlock.class));
            }

            @Override
            public void handleGET(CoapExchange exchange) {
                DataBlock db = new DataBlock();
                db.addDecoupled("data", new Link(d.getURI()));
                db.addDecoupled("dataNumber2", new Link(d.getURI()));

                exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(db), Utils.getContentType(DataBlock.class));
            }

            private class DataResource extends CoapResource{
                public DataResource(String name) {
                    super(name);
                }

                @Override
                public void handleGET(CoapExchange exchange) {
                    exchange.respond(CoAP.ResponseCode.CONTENT, "DataDataDataDataData", MediaTypeRegistry.TEXT_PLAIN);
                }
            }
        }
    }
}
