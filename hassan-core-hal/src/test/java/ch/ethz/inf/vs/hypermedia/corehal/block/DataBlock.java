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

import ch.ethz.inf.vs.hypermedia.client.MediaType;
import ch.ethz.inf.vs.hypermedia.corehal.model.CoREHalBase;

/**
 * Created by ynh on 29/11/15.
 */
@MediaType(contentType = 654334, mediaType = "application/test-data+json")
public class DataBlock extends CoREHalBase {

    private String data;
    private String dataNumber2;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataNumber2() {
        return dataNumber2;
    }

    public void setDataNumber2(String dataNumber2) {
        this.dataNumber2 = dataNumber2;
    }
}
