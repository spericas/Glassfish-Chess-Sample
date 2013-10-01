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
import java.util.concurrent.Exchanger;
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

import org.glassfish.samples.chess.model.Board;
import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.GameException.ErrorCode;
import org.glassfish.samples.chess.model.Move.Type;
import org.glassfish.samples.chess.model.Piece;
import org.glassfish.samples.chess.model.Point;
import org.glassfish.samples.chess.protocol.BoardRep;
import org.glassfish.samples.chess.protocol.CreateGame;
import org.glassfish.samples.chess.protocol.CreateGameRsp;
import org.glassfish.samples.chess.protocol.JoinGame;
import org.glassfish.samples.chess.protocol.JoinGameRsp;
import org.glassfish.samples.chess.protocol.Message;
import org.glassfish.samples.chess.protocol.MessageRsp.AlertType;
import org.glassfish.samples.chess.protocol.QueryGame;
import org.glassfish.samples.chess.protocol.QueryGameRsp;
import org.glassfish.samples.chess.protocol.QueryMoves;
import org.glassfish.samples.chess.protocol.QueryMovesRsp;
import org.glassfish.samples.chess.protocol.SendAction;
import org.glassfish.samples.chess.protocol.SendActionRsp;
import org.glassfish.samples.chess.protocol.SendMove;
import org.glassfish.samples.chess.protocol.SendMoveRsp;

import static org.junit.Assert.*;

/**
 * ChessServerEndpointTest class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
public class ChessServerEndpointTest {

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

    private Message sendMessages(final Message... messages) {
        try {
            final Exchanger<Message> exchanger = new Exchanger<>();
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                    .decoders(Collections.<Class<? extends Decoder>>singletonList(MessageDecoder.class))
                    .encoders(Collections.<Class<? extends Encoder>>singletonList(MessageEncoder.class))
                    .build();
            ClientManager client = ClientManager.createClient();
            client.connectToServer(new Endpoint() {
                private int k = 0;

                @Override
                public void onOpen(final Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<Message>() {
                            @Override
                            public void onMessage(Message response) {
                                try {
                                    System.out.println("RECEIVED: " + response);
                                    if (k == messages.length) {
                                        System.out.println("-- END PROTOCOL INTERACTION");
                                        exchanger.exchange(response);
                                    } else {
                                        messages[k].setGameId(response.getGameId());    // copy gameId
                                        System.out.println("SENT: " + messages[k]);
                                        session.getBasicRemote().sendObject(messages[k++]);
                                    }
                                } catch (InterruptedException | IOException | EncodeException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
                        System.out.println("\n-- START PROTOCOL INTERACTION");
                        System.out.println("SENT: " + messages[k]);
                        session.getBasicRemote().sendObject(messages[k++]);
                    } catch (IllegalStateException | IOException | EncodeException e) {
                        throw new RuntimeException(e);
                    } 
                }
            }, cec, new URI("ws://localhost:8025/ws/chessserver"));
            return exchanger.exchange(null);
        } catch (URISyntaxException | DeploymentException | IOException | InterruptedException e) {
            fail("Exception throw " + e);
            return null;
        }
    }

    @Test
    public void testCreateGameOk() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        assertEquals(Color.W, rsp.getColor());
        assertNotNull(rsp.getGameId());
        assertNull(rsp.getError());
    }

    @Test
    public void testJoinGameFail() {
        JoinGame jg = new JoinGame();
        jg.setGameId("BOGUS");
        JoinGameRsp rsp = (JoinGameRsp) sendMessages(jg);
        assertNotNull(rsp.getError());
        assertNotNull(rsp.getError().getMessage());
    }

    @Test
    public void testJoinGameOk() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(rsp.getGameId());
        jg.setObserver(false);
        // Join as second player
        JoinGameRsp rsp2 = (JoinGameRsp) sendMessages(jg);
        assertNull(rsp2.getError());
        // Join as non-observer must be an error now
        rsp2 = (JoinGameRsp) sendMessages(jg);
        assertNotNull(rsp2.getError());
    }

    @Test
    public void testJoinGameAsObserver() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(rsp.getGameId());
        jg.setObserver(true);
        // Join as observer should be allowed
        JoinGameRsp rsp2 = (JoinGameRsp) sendMessages(jg);
        assertNull(rsp2.getError());
        // Join as another observer should be allowed too
        rsp2 = (JoinGameRsp) sendMessages(jg);
        assertNull(rsp2.getError());
    }

    @Test
    public void testSendMove() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        SendMove sm = new SendMove();
        sm.setColor(Color.W);
        sm.setFrom("e2");   // classical opening
        sm.setTo("e4");
        SendMoveRsp rsp = (SendMoveRsp) sendMessages(cg, sm);
        assertNull(rsp.getError());
        assertEquals(rsp.getMoveType(), Type.NORMAL);
    }

    @Test
    public void testIllegalSendMove() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        SendMove sm = new SendMove();
        sm.setColor(Color.W);
        sm.setFrom("a1");   // try to move rook at beginning of game
        sm.setTo("a2");
        SendMoveRsp rsp = (SendMoveRsp) sendMessages(cg, sm);
        assertNotNull(rsp.getError());
    }

    @Test
    public void testQueryMovesPawn() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        QueryMoves qm = new QueryMoves();
        qm.setColor(Color.W);
        qm.setFrom("d2");       // pawn
        QueryMovesRsp rsp = (QueryMovesRsp) sendMessages(cg, qm);
        assertEquals(2, rsp.getMoves().size());
        assertTrue(rsp.getMoves().contains("d3"));
        assertTrue(rsp.getMoves().contains("d4"));
        assertEquals(rsp.getFrom(), "d2");
    }

    @Test
    public void testQueryMovesBishop() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        QueryMoves qm = new QueryMoves();
        qm.setColor(Color.W);
        qm.setFrom("c1");       // bishop
        QueryMovesRsp rsp = (QueryMovesRsp) sendMessages(cg, qm);
        assertEquals(0, rsp.getMoves().size());
    }

    @Test
    public void testQueryMovesKnight() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        QueryMoves qm = new QueryMoves();
        qm.setColor(Color.W);
        qm.setFrom("b1");       // knight
        QueryMovesRsp rsp = (QueryMovesRsp) sendMessages(cg, qm);
        assertEquals(2, rsp.getMoves().size());
        assertTrue(rsp.getMoves().contains("a3"));
        assertTrue(rsp.getMoves().contains("c3"));
    }

    @Test
    public void testSendMoveAndQueryMoves() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        SendMove sm = new SendMove();
        sm.setColor(Color.W);
        sm.setFrom("e2");       // classical opening
        sm.setTo("e4");
        QueryMoves qm = new QueryMoves();
        qm.setColor(Color.W);
        qm.setFrom("d1");       // queen
        QueryMovesRsp rsp = (QueryMovesRsp) sendMessages(cg, sm, qm);
        assertEquals(4, rsp.getMoves().size());
        assertTrue(rsp.getMoves().contains("e2"));
        assertTrue(rsp.getMoves().contains("f3"));
        assertTrue(rsp.getMoves().contains("g4"));
        assertTrue(rsp.getMoves().contains("h5"));
    }

    @Test
    public void testJoinLastGameCreated() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setObserver(false);
        JoinGameRsp rsp2 = (JoinGameRsp) sendMessages(jg);
        assertNull(rsp.getError());
        assertEquals(rsp.getGameId(), rsp2.getGameId());
    }

    @Test
    public void testCreateGameSendMoveJoinGame() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        SendMove sm = new SendMove();
        sm.setFrom("e2");
        sm.setTo("e4");
        sm.setColor(Color.W);
        sm.setGameId(rsp.getGameId());
        sendMessages(sm);
        sm = new SendMove();
        sm.setFrom("e7");
        sm.setTo("e5");
        sm.setColor(Color.B);
        sm.setGameId(rsp.getGameId());
        sendMessages(sm);
        JoinGame jg = new JoinGame();
        jg.setObserver(true);
        JoinGameRsp rsp2 = (JoinGameRsp) sendMessages(jg);
        assertNull(rsp.getError());
        assertEquals(2, rsp2.getMoves().size());
        assertEquals("e2e4", rsp2.getMoves().get(0));
        assertEquals("e7e5", rsp2.getMoves().get(1));
    }

    @Test
    public void testBlackKingStalemate() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("h8"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("f7"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("g5"));
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool G");
        cg.setBoard(new BoardRep(board));
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        SendMove sm = new SendMove();
        sm.setFrom("g5");
        sm.setTo("g6");
        sm.setColor(Color.W);
        sm.setGameId(cgr.getGameId());
        SendMoveRsp smr = (SendMoveRsp) sendMessages(sm);
        assertNotNull(smr.getAlert());
        assertEquals(AlertType.DRAW, smr.getAlert().getType());
        assertEquals(smr.getMoveType(), Type.NORMAL);
    }

    @Test
    public void testBlackKingInCheck() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("h8"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("e7"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("g5"));
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool G");
        cg.setBoard(new BoardRep(board));
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        SendMove sm = new SendMove();
        sm.setFrom("g5");
        sm.setTo("h5");
        sm.setColor(Color.W);
        sm.setGameId(cgr.getGameId());
        SendMoveRsp smr = (SendMoveRsp) sendMessages(sm);
        assertNotNull(smr.getAlert());
        assertEquals(AlertType.CHECK, smr.getAlert().getType());
    }

    @Test
    public void testBlackKingInCheckmate() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("h8"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("f7"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("g5"));
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool G");
        cg.setBoard(new BoardRep(board));
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        SendMove sm = new SendMove();
        sm.setFrom("g5");
        sm.setTo("h5");
        sm.setColor(Color.W);
        sm.setGameId(cgr.getGameId());
        SendMoveRsp smr = (SendMoveRsp) sendMessages(sm);
        assertNotNull(smr.getAlert());
        assertEquals(AlertType.CHECKMATE, smr.getAlert().getType());
    }

    @Test
    public void testCreateGameBlack() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.B);
        cg.setSummary("Creating game with color B");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        assertEquals(Color.B, rsp.getColor());
        assertEquals(Color.W, rsp.getTurn());
    }

    @Test
    public void testJoinGameWhite() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.B);
        cg.setSummary("Creating game with color B");
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(cgr.getGameId());
        jg.setColor(Color.W);
        JoinGameRsp jgr = (JoinGameRsp) sendMessages(jg);
        assertEquals(Color.W, jgr.getColor());
        assertEquals(Color.W, jgr.getTurn());
    }

    @Test
    public void testJoinGameWhiteError() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Creating game with color W");
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(cgr.getGameId());
        jg.setColor(Color.W);   // should be an error
        JoinGameRsp jgr = (JoinGameRsp) sendMessages(jg);
        assertNotNull(jgr.getError());
        assertEquals(ChessServerEndpoint.COLOR_TAKEN, jgr.getError().getCode());
    }

    @Test
    public void testJoinGameWhiteOkAsObserver() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Creating game with color W");
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(cgr.getGameId());
        jg.setColor(Color.W);   // should be ignored as observer
        jg.setObserver(true);
        JoinGameRsp jgr = (JoinGameRsp) sendMessages(jg);
        assertNull(jgr.getColor());
        assertEquals(Color.W, jgr.getTurn());
    }

    @Test
    public void testQueryGame() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool Game");
        CreateGameRsp rsp = (CreateGameRsp) sendMessages(cg);
        SendMove sm = new SendMove();
        sm.setFrom("e2");
        sm.setTo("e4");
        sm.setColor(Color.W);
        sm.setGameId(rsp.getGameId());
        sendMessages(sm);
        sm = new SendMove();
        sm.setFrom("e7");
        sm.setTo("e5");
        sm.setColor(Color.B);
        sm.setGameId(rsp.getGameId());
        sendMessages(sm);
        QueryGame qg = new QueryGame();
        qg.setGameId(rsp.getGameId());
        QueryGameRsp rsp2 = (QueryGameRsp) sendMessages(qg);
        assertNull(rsp.getError());
        assertEquals(2, rsp2.getMoves().size());
        assertEquals("e2e4", rsp2.getMoves().get(0));
        assertEquals("e7e5", rsp2.getMoves().get(1));
        assertNotNull(rsp.getTurn());
        assertNotNull(rsp.getBoard());
    }

    @Test
    public void testRT32190() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("d7"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("e2"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("c6"));
        board.setPiece(Piece.WHITE_KNIGHT, Point.fromNotation("d5"));
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool G");
        cg.setBoard(new BoardRep(board));
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        SendMove sm1 = new SendMove();
        sm1.setFrom("c6");
        sm1.setTo("c7");
        sm1.setColor(Color.W);
        sm1.setGameId(cgr.getGameId());
        SendMove sm2 = new SendMove();
        sm2.setFrom("d7");
        sm2.setTo("c7");
        sm2.setColor(Color.B);
        sm2.setGameId(cgr.getGameId());
        SendMoveRsp smr = (SendMoveRsp) sendMessages(sm1, sm2);
        assertNotNull(smr.getError());
        assertEquals(ErrorCode.ILLEGAL_MOVE_KING_CHECK.getCode(), smr.getError().getCode());
        assertNotNull(smr.getBoard());
    }

    @Test
    public void testEnPassingMove() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool G");
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(cgr.getGameId());
        jg.setColor(Color.B);
        sendMessages(jg);
        SendMoveRsp smr = (SendMoveRsp) sendMessages(
                newSendMove(cgr.getGameId(), Color.W, "e2e4"),
                newSendMove(cgr.getGameId(), Color.B, "a7a5"),
                newSendMove(cgr.getGameId(), Color.W, "e4e5"),
                newSendMove(cgr.getGameId(), Color.B, "d7d5")
        );
        System.out.println(smr.getBoard().toBoard());
        SendMoveRsp smr2 = (SendMoveRsp) sendMessages(
                newSendMove(cgr.getGameId(), Color.W, "e5d6"));
        assertNull(smr2.getError());
        assertEquals(smr2.getMoveType(), Type.EN_PASSANT);
    }

    @Test
    public void testBlackResignation() {
        CreateGame cg = new CreateGame();
        cg.setColor(Color.W);
        cg.setSummary("Cool G");
        CreateGameRsp cgr = (CreateGameRsp) sendMessages(cg);
        JoinGame jg = new JoinGame();
        jg.setGameId(cgr.getGameId());
        jg.setColor(Color.B);
        SendAction sa = new SendAction();
        sa.setGameId(jg.getGameId());
        sa.setColor(Color.B);
        sa.setType(SendAction.Type.RESIGN);
        SendActionRsp sar = (SendActionRsp) sendMessages(jg, sa);
        assertEquals(SendActionRsp.Type.RESIGN_PROCESSED, sar.getType());
        assertEquals(Color.B, sar.getColor());
    }

    // -- Utility methods ------------------------------------------------

    private SendMove newSendMove(String gameId, Color color, String move) {
        SendMove sm = new SendMove();
        sm.setFrom(move.substring(0, 2));
        sm.setTo(move.substring(2));
        sm.setColor(color);
        sm.setGameId(gameId);
        return sm;
    }
}
