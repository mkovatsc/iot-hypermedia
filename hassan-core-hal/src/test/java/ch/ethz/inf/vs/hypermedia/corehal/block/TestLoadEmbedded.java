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

import ch.ethz.inf.vs.hypermedia.client.*;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import com.google.gson.JsonObject;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by ynh on 29/11/15.
 */
public class TestLoadEmbedded {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        TestUtils.setupEnviroment();
        TestServer ts = new TestServer();
        ts.start();
        HypermediaClient hc = new HypermediaClient("coap://localhost:"+ts.getEndpoints().get(0).getAddress().getPort());
        DataBlockFuture other = hc.discover().getByMediaType(DataBlockFuture::new)
                .follow("other", DataBlockFuture::new);
        assertTrue(other.getStateName().equals("READY"));
        assertEquals("Test", other.get().getData());
        assertEquals("PARTIALLY_LOADED", other.getStateName());
        assertEquals("Test2", other.get().getDataNumber2());
        assertEquals("COMPLETED", other.getStateName());
        // Reload
        other.reset();
        assertTrue(other.getStateName().equals("READY"));
        assertEquals("Test", other.get().getData());
        assertTrue(other.getStateName().equals("COMPLETED"));
        // Reload Parent and therefore the embedded representation can be used
        other.reset(true);
        assertTrue(other.getStateName().equals("READY"));
        assertEquals("Test", other.get().getData());
        assertTrue(other.getStateName().equals("PARTIALLY_LOADED"));
        assertEquals("Test2", other.get().getDataNumber2());
        assertEquals("COMPLETED", other.getStateName());
    }

    @Test
    public void testGeneric() throws ExecutionException, InterruptedException {
        TestUtils.setupEnviroment();
        TestServer ts = new TestServer();
        ts.start();
        HypermediaClient hc = new HypermediaClient("coap://localhost:"+ts.getEndpoints().get(0).getAddress().getPort());
        GenericFuture other = hc.discover().getByMediaType(GenericFuture::new)
                .follow("other", GenericFuture::new);
        assertTrue(other.getStateName().equals("READY"));
        assertEquals("Test", other.get().get("data"));
        assertEquals("PARTIALLY_LOADED", other.getStateName());
        assertEquals("Test2", other.get().get("dataNumber2"));
        assertEquals("COMPLETED", other.getStateName());
        // Reload
        other.reset();
        assertTrue(other.getStateName().equals("READY"));
        assertEquals("Test", other.get().get("data"));
        assertTrue(other.getStateName().equals("COMPLETED"));
        // Reload Parent and therefore the embedded representation can be used
        other.reset(true);
        assertTrue(other.getStateName().equals("READY"));
        assertEquals("Test", other.get().get("data"));
        assertTrue(other.getStateName().equals("PARTIALLY_LOADED"));
        assertEquals("Test2", other.get().get("dataNumber2"));
        assertEquals("COMPLETED", other.getStateName());
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
            add(new StartData("start"));
        }

        private class StartData extends CoapResource {

            private final LinkedData d;

            public StartData(String name) {
                super(name);
                d = new LinkedData("linked");
                add(d);
                getAttributes().addContentType(Utils.getContentType(DataBlock.class));
            }

            @Override
            public void handleGET(CoapExchange exchange) {
                DataBlock db = new DataBlock();
                db.setData("First");

                JsonObject jo = new JsonObject();
                jo.addProperty("_self", d.getURI());
                jo.addProperty("data","Test");
                db.addLink("other",new Link(d.getURI()), jo);
                exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(db), Utils.getContentType(DataBlock.class));
            }

            private class LinkedData extends CoapResource{
                public LinkedData(String name) {
                    super(name);
                }


                @Override
                public void handleGET(CoapExchange exchange) {
                    DataBlock db = new DataBlock();
                    db.setData("Test");
                    db.setDataNumber2("Test2");
                    exchange.respond(CoAP.ResponseCode.CONTENT, CoREHalResourceFuture.getGson().toJson(db), Utils.getContentType(DataBlock.class));
                }
            }
        }
    }
}
