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
import org.glassfish.samples.chess.model.Move;
import org.glassfish.samples.chess.protocol.CreateGame;
import org.glassfish.samples.chess.protocol.CreateGameRsp;
import org.glassfish.samples.chess.protocol.JoinGame;
import org.glassfish.samples.chess.protocol.JoinGameRsp;
import org.glassfish.samples.chess.protocol.SendMove;
import org.glassfish.samples.chess.protocol.SendMoveRsp;
import org.glassfish.samples.chess.protocol.UpdateGame;

import static org.junit.Assert.*;

/**
 * GameReplayingTest class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
public class GameReplayingTest {

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

    @Test
    public void testGameReplay() {
        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                    .decoders(Collections.<Class<? extends Decoder>>singletonList(MessageDecoder.class))
                    .encoders(Collections.<Class<? extends Encoder>>singletonList(MessageEncoder.class))
                    .build();
            ClientManager client = ClientManager.createClient();
            
            final CountDownLatch white = new CountDownLatch(1);
            final CountDownLatch black = new CountDownLatch(1);
            final CountDownLatch observer = new CountDownLatch(2);

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
                                        SendMove sa = new SendMove();
                                        sa.setGameId(message.getGameId());
                                        sa.setFrom("e2");
                                        sa.setTo("e4");
                                        sa.setColor(Color.W);
                                        session.getBasicRemote().sendObject(sa);
                                        System.out.println("W sent SendMove e2e4");
                                    } else if (message instanceof SendMoveRsp) {
                                        white.countDown();
                                    }
                                } catch (IOException | EncodeException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
                        CreateGame cg = new CreateGame();
                        cg.setColor(Color.W);
                        session.getBasicRemote().sendObject(cg);
                        System.out.println("W sent CreateGame");
                    } catch (IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));

            white.await();

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
                                        SendMove sa = new SendMove();
                                        sa.setGameId(message.getGameId());
                                        sa.setFrom("e7");
                                        sa.setTo("e5");
                                        sa.setColor(Color.B);
                                        session.getBasicRemote().sendObject(sa);
                                    } catch (IOException | EncodeException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                } else if (message instanceof SendMoveRsp) {
                                    black.countDown();
                                }
                            }
                        });
                        JoinGame jg = new JoinGame();
                        jg.setColor(Color.B);
                        session.getBasicRemote().sendObject(jg);
                        System.out.println("B sent JoinGame");
                    } catch (IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));

            black.await();

            // -- Observer -----------------------------------------------

            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(final Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<Message>() {
                            @Override
                            public void onMessage(Message message) {
                                if (message instanceof UpdateGame) {
                                    System.out.println("Observer received UpdateGame");
                                    UpdateGame ug = (UpdateGame) message;
                                    if (observer.getCount() == 2) {
                                        assertEquals("e2", ug.getFrom());
                                        assertEquals("e4", ug.getTo());
                                        assertEquals(Move.Type.NORMAL, ug.getMoveType());
                                    } else if (observer.getCount() == 1) {
                                        assertEquals("e7", ug.getFrom());
                                        assertEquals("e5", ug.getTo());
                                        assertEquals(Move.Type.NORMAL, ug.getMoveType());
                                    } else {
                                        fail("Unexpected UpdateGame received");
                                    }
                                    observer.countDown();
                                }
                            }
                        });
                        JoinGame jg = new JoinGame();
                        jg.setObserver(true);
                        jg.setReplay(true);     // replay game!
                        session.getBasicRemote().sendObject(jg);
                        System.out.println("Observer sent JoinGame");
                    } catch (IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));

            observer.await();

        } catch (InterruptedException | URISyntaxException | DeploymentException | IOException e) {
            fail("Exception throw " + e);
        }
    }
}
