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

import ch.ethz.inf.vs.hypermedia.corehal.block.CoREHalResourceFuture;
import ch.ethz.inf.vs.hypermedia.corehal.model.CoREHalBase;
import ch.ethz.inf.vs.hypermedia.corehal.model.Form;
import ch.ethz.inf.vs.hypermedia.corehal.model.Link;
import com.google.common.collect.Iterators;
import com.google.gson.JsonElement;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ynh on 26/11/15.
 */
public class SerializerTest {
    @Test
    public void testNoLinks(){
        CoREHalBase base = new CoREHalBase();
        CoREHalBase baseTranslated = translate(base);
        assertTrue(baseTranslated.getLinks()==null);
    }

    @Test
    public void testSingeLink(){
        CoREHalBase base = new CoREHalBase();
        base.addLink("test", new Link("testLink"));
        CoREHalBase baseTranslated = translate(base);
        assertTrue(baseTranslated.getLink("test").getHref().equals("testLink"));
    }

    @Test
    public void testMultipleLinks(){
        CoREHalBase base = new CoREHalBase();
        base.addLink("test", new Link("testLink").setNames("l1"));
        base.addLink("test", new Link("testLink2").setNames("l2","l3"));
        CoREHalBase baseTranslated = translate(base);
        assertNull(baseTranslated.getLink("test"));
        assertEquals(3, Iterators.size(baseTranslated.getLinks("test").iterator()));
        assertTrue(baseTranslated.getLink("test","l1").getHref().equals("testLink"));
        assertTrue(baseTranslated.getLink("test","l2").getHref().equals("testLink2"));
        assertTrue(baseTranslated.getLink("test","l3").getHref().equals("testLink2"));
        JsonElement json = CoREHalResourceFuture.getGson().toJsonTree(base);
        assertEquals(2, json.getAsJsonObject().get("_links").getAsJsonObject().get("test").getAsJsonArray().size());

    }

    @Test
    public void testMultipleLinksWithSameRelation(){
        CoREHalBase base = new CoREHalBase();
        base.addLink("test", new Link("testLink"));
        base.addLink("test", new Link("testLink2"));
        CoREHalBase baseTranslated = translate(base);
        assertEquals(2, Iterators.size(baseTranslated.getLinks("test").iterator()));
        JsonElement json = CoREHalResourceFuture.getGson().toJsonTree(base);
        assertEquals(2, json.getAsJsonObject().get("_links").getAsJsonObject().get("test").getAsJsonArray().size());

    }

    @Test
    public void testNoForm(){
        CoREHalBase base = new CoREHalBase();
        CoREHalBase baseTranslated = translate(base);
        assertTrue(baseTranslated.getForms()==null);
    }

    @Test
    public void testSingeForm(){
        CoREHalBase base = new CoREHalBase();
        base.addForm("test", new Form("POST","testLink","application/test"));
        CoREHalBase baseTranslated = translate(base);
        assertTrue(baseTranslated.getForm("test").getHref().equals("testLink"));
    }

    @Test
    public void testMultipleForm(){
        CoREHalBase base = new CoREHalBase();
        base.addForm("test", new Form("POST","testLink","application/test").setNames("l1"));
        base.addForm("test", new Form("POST","testLink2","application/test").setNames("l2","l3"));
        CoREHalBase baseTranslated = translate(base);
        assertNull(baseTranslated.getForm("test"));
        assertEquals(3, baseTranslated.getForms("test").size());
        assertTrue(baseTranslated.getForm("test","l1").getHref().equals("testLink"));
        assertTrue(baseTranslated.getForm("test","l2").getHref().equals("testLink2"));
        assertTrue(baseTranslated.getForm("test","l3").getHref().equals("testLink2"));
        JsonElement json = CoREHalResourceFuture.getGson().toJsonTree(base);
        assertEquals(2, json.getAsJsonObject().get("_forms").getAsJsonObject().get("test").getAsJsonArray().size());

    }

    private CoREHalBase translate(CoREHalBase base) {
        String json = CoREHalResourceFuture.getGson().toJson(base);
        return CoREHalResourceFuture.getGson().fromJson(json, CoREHalBase.class);
    }
}