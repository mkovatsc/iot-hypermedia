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
package ch.ethz.inf.vs.hypermedia.client;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ynh on 18/11/15.
 */
public class TestConnector implements Connector {
	static AtomicInteger portGenerator = new AtomicInteger(1000);
	static Map<Integer, TestConnector> testConnectors = new HashMap<>();
	static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	final int port;
	private RawDataChannel receiver;
	private InetSocketAddress addr;

	private TestConnector() {
		this(0);
	}

	private TestConnector(int a_port) {
		if (a_port != 0) {
			port = a_port;
		} else {
			port = portGenerator.getAndIncrement();
		}
		addr = new InetSocketAddress(port);
	}

	public static Endpoint getEndpoint(int port) {
		CoapEndpoint coapEndpoint = new CoapEndpoint(new TestConnector(port), NetworkConfig.getStandard());
		coapEndpoint.setExecutor(executor);
		return coapEndpoint;
	}

	@Override
	public void start() throws IOException {
		System.err.println("Start" + port);
		testConnectors.put(port, this);

	}

	@Override
	public void stop() {
		testConnectors.remove(port);
	}

	@Override
	public void destroy() {
		stop();
	}

	@Override
	public void send(RawData rawData) {
		if (testConnectors.containsKey(rawData.getPort())) {
			testConnectors.get(rawData.getPort()).addToReceiver(new RawData(rawData.getBytes(), this.getAddress()));
		}
	}

	private void addToReceiver(RawData rawData) {
		receiver.receiveData(rawData);
	}

	@Override
	public void setRawDataReceiver(RawDataChannel rawDataChannel) {
		this.receiver = rawDataChannel;
	}

	@Override
	public InetSocketAddress getAddress() {
		return addr;
	}
}
