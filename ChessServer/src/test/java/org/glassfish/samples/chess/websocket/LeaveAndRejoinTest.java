/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.samples.chess.websocket;

import org.glassfish.samples.chess.websocket.MessageDecoder;
import org.glassfish.samples.chess.websocket.MessageEncoder;
import org.glassfish.samples.chess.websocket.ChessServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import org.junit.*;
import java.util.concurrent.CountDownLatch;

import org.glassfish.samples.chess.protocol.Message;
import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.protocol.CreateGame;
import org.glassfish.samples.chess.protocol.CreateGameRsp;
import org.glassfish.samples.chess.protocol.JoinGame;
import org.glassfish.samples.chess.protocol.JoinGameRsp;

import static org.junit.Assert.*;

/**
 * LeaveAndRejoinGameTest class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
public class LeaveAndRejoinTest {

    private static Server server;

    @BeforeClass
    public static void startServer() throws DeploymentException, IOException {
        server = new Server("localhost", 8025, "/ws", ChessServerEndpoint.class);
        server.start();
    }

    @AfterClass
    public static void stopServer() {
        System.out.println("");
        server.stop();
    }

    /**
     * Tests the scenario of a client leaving a game and then rejoining.
     */
    @Test
    public void testLeaveAndRejoin() {
        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                    .decoders(Collections.<Class<? extends Decoder>>singletonList(MessageDecoder.class))
                    .encoders(Collections.<Class<? extends Encoder>>singletonList(MessageEncoder.class))
                    .build();
            ClientManager client = ClientManager.createClient();
            
            final CountDownLatch first = new CountDownLatch(1);
            final CountDownLatch second = new CountDownLatch(1);
            final CountDownLatch third = new CountDownLatch(1);

            // -- White Player -------------------------------------------

            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(final Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<Message>() {
                            @Override
                            public void onMessage(Message message) {
                                try {
                                    if (message instanceof CreateGameRsp) {
                                        System.out.println("W received CreateGameRsp");
                                        first.countDown();
                                    }
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
                        CreateGame cg = new CreateGame();
                        cg.setColor(Color.W);
                        cg.setUsername("white_player");
                        cg.setPassword("white_player");
                        session.getBasicRemote().sendObject(cg);
                        System.out.println("W sent CreateGame");
                    } catch (IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));

            // -- Black Player -------------------------------------------

            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(final Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<Message>() {
                            @Override
                            public void onMessage(Message message) {
                                if (message instanceof JoinGameRsp) {
                                    try {
                                        System.out.println("B1 received JoinGameRsp");
                                        session.close();
                                        System.out.println("B1 closed session");
                                        second.countDown();
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        });
                        first.await();
                        JoinGame jg = new JoinGame();
                        jg.setColor(Color.B);
                        jg.setUsername("black_player");
                        jg.setPassword("black_player");
                        session.getBasicRemote().sendObject(jg);
                        System.out.println("B1 sent JoinGame");
                    } catch (InterruptedException | IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));

            second.await();

            // -- Black Player (2nd time) -------------------------------------------

            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(final Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<Message>() {
                            @Override
                            public void onMessage(Message message) {
                                if (message instanceof JoinGameRsp) {
                                    JoinGameRsp jgr = (JoinGameRsp) message;
                                    System.out.println("B2 received JoinGameRsp");
                                    assertFalse(jgr.hasError());
                                    third.countDown();
                                }
                            }
                        });
                        JoinGame jg = new JoinGame();
                        jg.setColor(Color.B);
                        jg.setUsername("black_player");
                        jg.setPassword("black_player");
                        session.getBasicRemote().sendObject(jg);
                        System.out.println("B2 sent JoinGame");
                    } catch (IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));

            third.await();

        } catch (InterruptedException | URISyntaxException | DeploymentException | IOException e) {
            fail("Exception throw " + e);
        }
    }
}
