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
 *    Klaus Hartke - CoRE Lighting specification
 *******************************************************************************/
package ch.ethz.inf.vs.hypermedia.hartke.lighting.server;

import ch.ethz.inf.vs.hypermedia.client.TestConnector;
import ch.ethz.inf.vs.hypermedia.client.Utils;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;

import java.util.Scanner;

/**
 * Created by ynh on 24/09/15.
 */
public class BulletinBoardServer extends CoapServer {

	public final boolean test;
	private final BulletinBoardResource board;

	public BulletinBoardServer(int port, Class<? extends BulletinBoardResource> boardType, boolean test) throws Exception {
		this.test = test;
		if (test) {
			addEndpoint(TestConnector.getEndpoint(port));
		} else {
			addEndpoint(new CoapEndpoint(port));
		}
		board = boardType.getDeclaredConstructor(String.class, BulletinBoardServer.class).newInstance("bulletins", this);
		add(board);

	}

	public static void main(String[] args) throws Exception {
		int port = 5082;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		Scanner scanner = new Scanner(System.in);
		BoardType boardType = Utils.selectFromEnum(BoardType.class, scanner, "Board Type");
		BulletinBoardServer server = start(port, boardType, false);

		System.out.printf(BulletinBoardServer.class.getSimpleName() + " listening on port %d.\n", server.getPort());
	}

	public static BulletinBoardServer start(int port, BoardType boardType, boolean test) throws Exception {
		Class<? extends BulletinBoardResource> board = null;
		switch (boardType) {
		case BOARD1:
			board = BulletinBoardResource.class;
			break;
		case BOARD2:
			board = BulletinBoardResource2.class;
			break;
		case DECOUPLED_SERVER:
			board = DecoupledBoardResource.class;
			break;
		case DECOUPLED_SERVER2:
			board = DecoupledBoardResource2.class;
			break;
		}
		BulletinBoardServer server = new BulletinBoardServer(port, board, test);
		server.start();
		return server;
	}

	@Override
	public void stop() {
		board.stop();
		super.stop();
	}

	public int getPort() {
		return getEndpoints().get(0).getAddress().getPort();
	}

	public enum BoardType {
		BOARD1, BOARD2, DECOUPLED_SERVER, DECOUPLED_SERVER2
	}
}
