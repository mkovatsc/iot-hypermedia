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
package ch.ethz.inf.vs.hypermedia.client.utils;

import ch.ethz.inf.vs.hypermedia.client.Future;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ynh on 11/10/15.
 */
public class DumpState {
	private final File temp;
	private final FileWriter writer;
	private final Set<Future> futures = new HashSet<>();

	public DumpState(Future start) throws IOException {
		temp = File.createTempFile("temp-file-name", ".xdot");
		writer = new FileWriter(temp);
		visit(start);
	}

	public static void dump(Future start) {
		try {
			new DumpState(start).show();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void visit(Future el) {
		if (el != null && futures.add(el)) {
			List<Future> dependencies = el.getDependencies();
			for (Future bf : dependencies) {
				visit(bf);
			}
			List<Future> parents = el.getParents();
			for (Future bf : parents) {
				visit(bf);
			}
			List<WeakReference<Future>> childern = el.getChildern();
			for (WeakReference<Future> bf : childern) {
				visit(bf.get());
			}
		}
	}

	private void show() throws IOException, InterruptedException {
		// create a temp file
		writer.write("digraph G {\n");
		for (Future f : futures) {
			writer.write("N" + f.hashCode() + " [label=\"" + f.getClassName() + "@" + f.hashCode() + "  | " + f.getStateName() + "\"];\n");
		}
		for (Future el : futures) {

			List<Future> parents = el.getParents();
			for (Future bf : parents) {
				writer.write("N" + bf.hashCode() + " -> N" + el.hashCode() + ";\n");
			}
			List<Future> dependencies = el.getDependencies();
			for (Future dep : dependencies) {
				writer.write("N" + el.hashCode() + " -> N" + dep.hashCode() + " [style=bold,color=orange];\n");
			}
		}
		writer.write("}\n");
		writer.flush();
		writer.close();
		Process myProcess = new ProcessBuilder("xdot", temp.getAbsolutePath()).start();
		myProcess.waitFor();
	}
}
