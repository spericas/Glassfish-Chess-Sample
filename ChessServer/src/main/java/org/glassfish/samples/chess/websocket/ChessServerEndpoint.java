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

import java.io.IOException;
import java.util.List;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.inject.Inject;
import javax.websocket.OnClose;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import org.glassfish.samples.chess.model.Game;
import org.glassfish.samples.chess.model.Game.State;
import org.glassfish.samples.chess.model.GameException;
import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Move;
import org.glassfish.samples.chess.model.Piece;
import org.glassfish.samples.chess.persistence.ChessServerDao;
import org.glassfish.samples.chess.persistence.PlayerEntity;
import org.glassfish.samples.chess.protocol.BoardRep;
import org.glassfish.samples.chess.protocol.CheckCredentials;
import org.glassfish.samples.chess.protocol.CheckCredentialsRsp;
import org.glassfish.samples.chess.protocol.Message;
import org.glassfish.samples.chess.protocol.CreateGame;
import org.glassfish.samples.chess.protocol.JoinGame;
import org.glassfish.samples.chess.protocol.JoinGameRsp;
import org.glassfish.samples.chess.protocol.ServerMessageProcessor;
import org.glassfish.samples.chess.protocol.MessageRsp.Alert;
import org.glassfish.samples.chess.protocol.MessageRsp.AlertType;
import org.glassfish.samples.chess.protocol.SendMove;
import org.glassfish.samples.chess.protocol.QueryMoves;
import org.glassfish.samples.chess.protocol.QueryMovesRsp;
import org.glassfish.samples.chess.protocol.UpdateGame;
import org.glassfish.samples.chess.protocol.QueryGame;
import org.glassfish.samples.chess.protocol.QueryGameRsp;
import org.glassfish.samples.chess.protocol.QueryGames;
import org.glassfish.samples.chess.protocol.QueryGamesRsp;
import org.glassfish.samples.chess.protocol.SendAction;
import org.glassfish.samples.chess.protocol.SendActionRsp;
import org.glassfish.samples.chess.protocol.SendMoveRsp;
import org.glassfish.samples.chess.protocol.CreateGameRsp;

import static org.glassfish.samples.chess.protocol.MessageRsp.Error;
import static org.glassfish.samples.chess.protocol.CheckCredentialsRsp.Check;

/**
 * ChessServerEndpoint class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
@ServerEndpoint(
        value = "/chessserver",
        encoders = {MessageEncoder.class},
        decoders = {MessageDecoder.class})
public class ChessServerEndpoint implements ServerMessageProcessor {

    private static final ChessServerLogger logger = new ChessServerLogger();

    private static final String NO_PLAYER = "<pending>";

    static final int UNKNOWN_GAME      = 1000;
    static final int CLOSED_GAME       = 1100;
    static final int COLOR_TAKEN       = 1200;
    static final int INVALID_GAME      = 1300;
    static final int INVALID_ACTION    = 1400;
    static final int DRAW_IN_PROGRESS  = 1500;
    static final int UNREGISTERED_USER = 1600;
    static final int CANNOT_PERSIST    = 1700;
    static final int PLAYER_ONLINE     = 1800;
    static final int INVALID_PASSWORD  = 1900;

    @Inject
    private GameCatalog catalog;

    private Session session;

    private Set<String> gameIds = new HashSet<>();

    private ChessServerDao dao = new ChessServerDao();

    @OnMessage
    public Message onMessage(Message message, Session session) {
        if (logger.logFine()) {
            logger.fine(message.toString());
        }
        this.session = session;
        return message.processMe(this);
    }

    @OnClose
    public void onClose(Session session) {
        for (String gameId : gameIds) {
            // Set players associated with this endpoint instance as offline
            final Game<PlayerEntity, Observer> game = getCatalog().getGame(gameId);
            PlayerEntity player = game.getPlayer(Color.W);
            if (player != null && player.isOnline(this)) {
                player.setOffline();
                if (logger.logInfo()) {
                    logger.info("Game " + gameId + " player " + player.getUsername() + " now offline");
                }
            }
            player = game.getPlayer(Color.B);
            if (player != null && player.isOnline(this)) {
                player.setOffline();
                if (logger.logInfo()) {
                    logger.info("Game " + gameId + " player " + player.getUsername() + " now offline");
                }
            }

            // Remove any observers associated with this endpoint instance
            synchronized (game) {
                Iterator<Observer> it = game.getObservers().iterator();
                while (it.hasNext()) {
                    final Observer observer = it.next();
                    if (observer.isOnline(this)) {
                        observer.setOffline();
                        it.remove();
                        if (logger.logInfo()) {
                            logger.info("Game " + gameId + " observer " + observer + " has left");
                        }
                    }
                }
            }
        }
    }

    // -- MessageProcessor -----------------------------------------------

    @Override
    public Message process(CreateGame message) {
        // Create new game and store it in catalog
        final Game<PlayerEntity, Observer> game = getCatalog().newGame(
                message.hasBoard() ? message.getBoard().toBoard() : null,
                message.hasTurn() ? message.getTurn() : Color.W, message.getSummary());
        final Color color = message.hasColor() ? message.getColor() : Color.W;
        final CreateGameRsp rsp = message.newResponse();

        // Associate endpoint with this game
        final String gameId = game.getGameId();
        gameIds.add(gameId);
        
        // If a player is specified in the message, it must be registered
        final PlayerEntity player = findPlayerEntity(message, message.isPersisted());
        if (player == null) {
            rsp.setError(new Error(INVALID_PASSWORD, "Invalid password for " + message.getUsername()));
            return rsp;
        }

        // Check if game needs to be persisted
        if (message.isPersisted()) {
            if (message.hasBoard()) {
                rsp.setError(new Error(CANNOT_PERSIST, "Cannot persist game with initial board"));
                return rsp;
            }
            game.setWatcher(getCatalog());
            getCatalog().persistGame(game);
        }

        // Update player information
        player.setEndpoint(this);
        game.setPlayer(color, player);

        // If no summary, use "username1 vs. <pending>" or vice versa
        if (game.getSummary() == null) {
            final String wp = game.hasPlayer(Color.W) ? game.getPlayer(Color.W).getUsername() : NO_PLAYER;
            final String bp = game.hasPlayer(Color.B) ? game.getPlayer(Color.B).getUsername() : NO_PLAYER;
            game.setSummary(wp + " vs. " + bp);
        }

        // Log game creation
        if (logger.logInfo()) {
            logger.info("Game " + gameId + " created by " + color + " player");
        }

        // Respond with CreateGameRsp message
        rsp.setGameId(gameId);
        rsp.setTurn(game.getTurn());
        rsp.setColor(color);
        rsp.setBoard(new BoardRep(game.getBoard()));
        rsp.setSummary(game.getSummary());
        rsp.setOpen(true);
        if (color == Color.W) {
            rsp.setWhitePlayer(player.getUsername());
        } else {
            rsp.setBlackPlayer(player.getUsername());
        }
        rsp.setCompleted(false);
        return rsp;
    }

    @Override
    public Message process(JoinGame message) {
        final JoinGameRsp rsp = message.newResponse();
        final Game<PlayerEntity, Observer> game = message.getGameId() != null
                ? getCatalog().getGame(message.getGameId()) : getCatalog().getLastGame();
        if (game == null) {
            rsp.setError(new Error(UNKNOWN_GAME, "Unable to find game with ID " + message.getGameId()));
            return rsp;
        }

        // Associate endpoint with this game
        final String gameId = game.getGameId();
        gameIds.add(gameId);

        // If a player is specified in the message, it must be registered
        final PlayerEntity player = findPlayerEntity(message, getCatalog().isGamePersisted(game));
        if (player == null) {
            rsp.setError(new Error(INVALID_PASSWORD, "Invalid password for " + message.getUsername()));
            return rsp;
        }

        // Only one connection at a time
        if (player.isOnline() && !player.isOnline(this)) {
            rsp.setError(new Error(PLAYER_ONLINE, "Cannot join more over more than one connection at a time "
                    + message.getUsername()));
            return rsp;
        }
        player.setEndpoint(this);    // now online

        // If game is not open and not observer, must be a player re-joining
        Color oldColor = null;
        if (!game.isOpen() && !message.isObserver()) {
            oldColor = game.getPlayerColor(player);
            if (oldColor == null) {
                rsp.setError(new Error(CLOSED_GAME, "Game already has two players " + message.getGameId()));
                return rsp;
            }
        }

        // Initialize JoinGameRsp object
        rsp.setGameId(gameId);
        rsp.setSummary(game.getSummary());
        rsp.setCompleted(false);

        // Joining as an observer?
        if (message.isObserver()) {
            final Observer observer = new Observer(this);
            game.addObserver(observer);

            if (logger.logInfo()) {
                logger.info("Game " + gameId + " joined by observer " + observer);
            }

            // Wants to replay game from beginning?
            if (message.isReplay()) {
                // Send JoinGameRsp before replaying game
                rsp.setOpen(game.isOpen());
                if (game.hasPlayer(Color.W)) {
                    rsp.setWhitePlayer(game.getPlayer(Color.W).getUsername());
                }
                if (game.hasPlayer(Color.B)) {
                    rsp.setBlackPlayer(game.getPlayer(Color.B).getUsername());
                }
                sendMessage(rsp);

                if (logger.logInfo()) {
                    logger.info("Replaying game " + gameId + " for observer " + observer);
                }

                synchronized (game) {
                    for (Move move : game.getMoves()) {
                        final UpdateGame ug = new UpdateGame(gameId);
                        ug.setColor(move.getColor());
                        ug.setTurn(move.getColor().getOpponentColor());
                        ug.setFrom(move.getFrom().toNotation());
                        ug.setTo(move.getTo().toNotation());
                        ug.setMoveType(move.getType());
                        sendMessage(ug);
                    }
                }

                return null;    // we're done
            }
        }
        else {
            Color color = oldColor != null ? oldColor : game.hasPlayer(Color.W) ? Color.B : Color.W;

            if (message.hasColor()) {
                if (message.getColor() != color) {
                    rsp.setError(new Error(COLOR_TAKEN, "Unable to join game with requested color "
                            + message.getGameId()));
                    return rsp;
                } else {
                    game.setPlayer(color, player);
                }
            } else {
                game.setPlayer(color, player);
            }

            // Update game summary with player usernames
            if (game.getSummary() == null || game.getSummary().contains(NO_PLAYER)) {
                game.setSummary(game.getPlayer(Color.W).getUsername() + " vs. "
                        + game.getPlayer(Color.B).getUsername());
            }

            rsp.setColor(color);

            if (logger.logInfo()) {
                logger.info("Game " + gameId + " joined by " + color + " player");
            }
        }

        rsp.setTurn(game.getTurn());
        rsp.setBoard(new BoardRep(game.getBoard()));
        for (Move move : game.getMoves()) {
            rsp.addMove(move.toNotation());
        }
        rsp.setOpen(game.isOpen());
        if (game.hasPlayer(Color.W)) {
            rsp.setWhitePlayer(game.getPlayer(Color.W).getUsername());
        }
        if (game.hasPlayer(Color.B)) {
            rsp.setBlackPlayer(game.getPlayer(Color.B).getUsername());
        }
        return rsp;
    }

    @Override
    public Message process(SendMove message) {
        final SendMoveRsp rsp = message.newResponse();

        // Find game in catalog or report an error 
        final String gameId = message.getGameId();
        final Game<PlayerEntity, Observer> game = getCatalog().getGame(gameId);
        if (game == null) {
            rsp.setError(new Error(UNKNOWN_GAME, "Unable to find game with ID " + message.getGameId()));
            return rsp;
        }

        Move move = null;
        boolean draw = false;
        boolean check = false;
        boolean checkmate = false;
        final Color color = message.getColor();

        if (logger.logInfo()) {
            logger.info("Game " + gameId + " received " + message.getFrom() + " " + message.getTo()
                    + " from "+ color + " player");
        }

        try {
            // Update game's state with this move
            String from = message.getFrom();
            if (from == null || from.length() == 1) {
                final Piece piece = Piece.fromNotation(color, message.getPiece());
                move = game.makeMove(piece, from, message.getTo());
            } else {
                move = game.makeMove(color, message.getFrom(), message.getTo());
            }
            rsp.setMoveType(move.getType());

            // Have I won the game with this move?
            final Color opponent = color.getOpponentColor();
            if (game.isCheckmate(opponent)) {
                rsp.setAlert(new Alert(AlertType.CHECKMATE, "Winner is " + color));
                checkmate = true;
                game.setWinner(color);

                if (logger.logInfo()) {
                    logger.info("Game " + gameId + " " + color + " player wins");
                }
            } // Opponent's king in check?
            else if (game.isKingAttacked(opponent)) {
                rsp.setAlert(new Alert(AlertType.CHECK, opponent + " king in check"));
                check = true;
            } // Have we reached a stalemate situation?
            else if (game.isStalemate(opponent)) {
                rsp.setAlert(new Alert(AlertType.DRAW, "Stalemate situation for " + color.getOpponentColor()));
                draw = true;
                game.setState(State.DRAW);

                if (logger.logInfo()) {
                    logger.info("Game " + gameId + " is a draw");
                }
            } 
        } catch (GameException e) {
            rsp.setError(new Error(e.getErrorCode().getCode(), e.getMessage()));
        } finally {
            rsp.setBoard(new BoardRep(game.getBoard()));
            rsp.setTurn(game.getTurn());
            rsp.setColor(color);
        }

        try {
            // Send response before informing other player of move
            session.getBasicRemote().sendObject(rsp);

            // If no errors, inform opponent and observers
            if (!rsp.hasError()) {
                UpdateGame ug = new UpdateGame(gameId);
                ug.setBoard(new BoardRep(game.getBoard()));
                ug.setTurn(game.getTurn());
                ug.setFrom(message.getFrom());
                ug.setTo(message.getTo());
                ug.setColor(color);
                if (move != null) {
                    ug.setMoveType(move.getType());
                }

                // Inform opponent of move and any alert condition
                if (checkmate) {
                    ug.setAlert(new Alert(AlertType.CHECKMATE, "Winner is " + color));
                } else if (check) {
                    ug.setAlert(new Alert(AlertType.CHECK, color + " king in check"));
                } else if (draw) {
                    ug.setAlert(new Alert(AlertType.DRAW, "Stalemate situation for " + color.getOpponentColor()));
                }
                if (game.hasOpponent(color)) {
                    game.getOpponent(color).getEndpoint().sendMessage(ug);
                }

                // Inform observers of move and any alert condition
                synchronized (game) {
                    for (Observer obs : game.getObservers()) {
                        obs.getEndpoint().sendMessage(ug);
                    }
                }
            }
        } catch (IOException | EncodeException e) {
            throw new RuntimeException(e);
        }
        return null;        // Response already sent
    }

    @Override
    public Message process(QueryMoves message) {
        final QueryMovesRsp rsp = message.newResponse();

        // Find game in catalog or report an error
        final Game<PlayerEntity, Observer> game = getCatalog().getGame(message.getGameId());
        if (game == null) {
            rsp.setError(new Error(UNKNOWN_GAME, "Unable to find game with ID " + message.getGameId()));
            return rsp;
        }
        
        try {
            final List<String> moves = game.queryMoves(message.getColor(), message.getFrom());
            rsp.setMoves(moves);
            rsp.setFrom(message.getFrom());
        } catch (GameException e) {
            rsp.setError(new Error(e.getErrorCode().getCode(), e.getMessage()));
        }
        return rsp;
    }

    @Override
    public Message process(QueryGames message) {
        QueryGamesRsp rsp = message.newResponse();
        for (Game<PlayerEntity, Observer> gm : getCatalog().getAllGames()) {
            QueryGamesRsp.Game game = new QueryGamesRsp.Game();
            game.setGameId(gm.getGameId());
            game.setOpen(gm.isOpen());
            game.setSummary(gm.getSummary());
            if (gm.hasPlayer(Color.W)) {
                game.setWhitePlayer(gm.getPlayer(Color.W).getUsername());
            }
            if (gm.hasPlayer(Color.B)) {
                game.setBlackPlayer(gm.getPlayer(Color.B).getUsername());
            }
            game.setCompleted(gm.getState() != Game.State.PLAYING);
            rsp.addGame(game);
        }
        return rsp;
    }

    @Override
    public Message process(QueryGame message) {
        final QueryGameRsp rsp = message.newResponse();

        // Find game in catalog or report an error
        final Game<PlayerEntity, Observer> game = getCatalog().getGame(message.getGameId());
        if (game == null) {
            rsp.setError(new Error(UNKNOWN_GAME, "Unable to find game with ID " + message.getGameId()));
            return rsp;
        }

        // Return response with list of moves
        rsp.setGameId(message.getGameId());
        rsp.setTurn(game.getTurn());
        rsp.setBoard(new BoardRep(game.getBoard()));
        for (Move move : game.getMoves()) {
            rsp.addMove(move.toNotation());
        }
        rsp.setSummary(game.getSummary());
        rsp.setOpen(game.isOpen());
        if (game.hasPlayer(Color.W)) {
            rsp.setWhitePlayer(game.getPlayer(Color.W).getUsername());
        }
        if (game.hasPlayer(Color.B)) {
            rsp.setBlackPlayer(game.getPlayer(Color.B).getUsername());
        }
        rsp.setCompleted(game.getState() != Game.State.PLAYING);
        return rsp;
    }

    @Override
    public Message process(SendAction message) {
        final SendActionRsp rsp = message.newResponse();

        // Find game in catalog or report an error
        final Game<PlayerEntity, Observer> game = getCatalog().getGame(message.getGameId());
        if (game == null) {
            rsp.setError(new Error(UNKNOWN_GAME, "Unable to find game with ID " + message.getGameId()));
            return rsp;
        }

        final Color color = message.getColor();
        rsp.setGameId(message.getGameId());
        rsp.setColor(color);

        try {
            switch (message.getType()) {
                case DRAW_REQUEST:
                    if (!game.hasDrawRequester()) {
                        if (game.hasOpponent(color)) {
                            // Update game state
                            game.setDrawRequester(color);

                            // Forward draw message to opponent
                            game.getOpponent(color).getEndpoint().sendMessage(message);

                            // Inform observers of draw request
                            synchronized (game) {
                                for (Observer obs : game.getObservers()) {
                                    obs.getEndpoint().sendMessage(message);
                                }
                            }
                        } else {
                            rsp.setError(new Error(INVALID_ACTION, "Cannot draw if no opponent in game "
                                    + message.getGameId()));
                            return rsp;
                        }
                    } else {
                        rsp.setError(new Error(DRAW_IN_PROGRESS, "Already a draw in progress "
                                + message.getGameId()));
                        return rsp;
                    }
                    break;
                case RESIGN:
                    if (game.hasOpponent(color)) {
                        // Inform opponent of resignation
                        game.getOpponent(color).getEndpoint().sendMessage(message);

                        // Inform observers of resignation
                        synchronized (game) {
                            for (Observer obs : game.getObservers()) {
                                obs.getEndpoint().sendMessage(message);
                            }
                        }

                        // Update internal game state
                        final Color oppColor = message.getColor().getOpponentColor();
                        game.setWinner(oppColor);
                        if (logger.logInfo()) {
                            logger.info("Game " + message.getGameId() + " won by player " + oppColor
                                    + " by resignation");
                        }

                        // Respond to player resigning from game
                        rsp.setType(SendActionRsp.Type.RESIGN_PROCESSED);
                        rsp.setMessage(message.getColor() + " resigned from game");
                        return rsp;
                    } else {
                        rsp.setError(new Error(INVALID_ACTION, "Cannot resign if no opponent in game "
                                + message.getGameId()));
                        return rsp;
                    }
                default:
                    throw new InternalError("Unknown action type " + message.getType());
            }
        } catch (InternalError e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Message process(SendActionRsp message) {
        // Find game in catalog or report an error
        final Game<PlayerEntity, Observer> game = getCatalog().getGame(message.getGameId());
        if (game == null) {
            return null;        // ignore
        }

        // Forward response if there is a draw requester
        if (game.hasDrawRequester()) {
            game.getDrawRequester().getEndpoint().sendMessage(message);
            game.setDrawRequester(null);

            // Inform observers of outcome
            synchronized (game) {
                for (Observer obs : game.getObservers()) {
                    obs.getEndpoint().sendMessage(message);
                }
            }

            // Update internal game state
            if (message.getType() == SendActionRsp.Type.DRAW_ACCEPTED) {
                game.setState(State.DRAW);

                if (logger.logInfo()) {
                    logger.info("Game " + message.getGameId() + " is a draw");
                }
            }
        }
        return null;
    }

    @Override
    public Message process(CheckCredentials message) {
        final CheckCredentialsRsp rsp = message.newResponse();
        final PlayerEntity player = dao.findPlayer(message.getUsername());
        if (player != null) {
            rsp.setCheck(player.getPassword().equals(message.getPassword()) ? Check.VALID : Check.INVALID);
        } else {
            rsp.setCheck(Check.NOT_REGISTERED);
        }
        return rsp;
    }

    // -- Utility methods ------------------------------------------------

    public GameCatalog getCatalog() {
        if (catalog == null) {
            System.err.println("ChessServer Warning: GameCatalog not injected via CDI!");
            catalog = GameCatalogFactory.getGameCatalog();
        }
        return catalog;
    }

    private void sendMessage(Message message) {
        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            throw new RuntimeException(e);
        }
    }
    
    private PlayerEntity findPlayerEntity(Message message, boolean persisted) {
        PlayerEntity newPlayer;
        if (message.hasUsername()) {
            newPlayer = dao.findPlayer(message.getUsername());
            // If not found, accept auto-registration
            if (newPlayer == null) {
                newPlayer = new PlayerEntity(message.getUsername(), message.getPassword(), null);
                dao.updatePlayer(newPlayer);
            } else if (!newPlayer.getPassword().equals(message.getPassword())) {
                return null;
            }
        } else {
            newPlayer = new PlayerEntity();      // generates unique username
            // If game is persisted, so should the player
            if (persisted) {
                dao.updatePlayer(newPlayer);
            }
        }
        return newPlayer;
    }

}
