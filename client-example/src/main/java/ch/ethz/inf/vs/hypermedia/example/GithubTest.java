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
package ch.ethz.inf.vs.hypermedia.example;

import ch.ethz.inf.vs.hypermedia.client.HypermediaClient;

import java.util.concurrent.ExecutionException;

/**
 * Created by ynh on 26/09/15.
 */
public class GithubTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        GithubEntryPointFuture entrypoint = new HypermediaClient("https://api.github.com/")
                .get(GithubEntryPointFuture::new);
        Iterable<GithubRepoFuture> repos = entrypoint.getUser("ynh").getRepositories();
        for (GithubRepoFuture r:repos){
            System.out.println(r.getName());
        }

        String[] users = {"ynh", "mkovatsc"};
        for (String user:users){
            System.out.println(entrypoint.getUser(user).getName());
        }
    }
}
