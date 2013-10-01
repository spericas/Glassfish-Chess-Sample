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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import java.util.Collection;

import org.glassfish.samples.chess.model.Board;
import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Game;
import org.glassfish.samples.chess.model.GameWatcher;
import org.glassfish.samples.chess.model.Move;
import org.glassfish.samples.chess.persistence.ChessServerDao;
import org.glassfish.samples.chess.persistence.GameEntity;
import org.glassfish.samples.chess.persistence.MoveEntity;
import org.glassfish.samples.chess.persistence.PlayerEntity;

/**
 * GameCatalog class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
@ApplicationScoped
public class GameCatalog implements GameWatcher<PlayerEntity, Observer> {

    private static final String REPLAY_GAME = "ReplayGame";
    private static final String CLEANUP_OLD_GAMES = "CleanupOldGames";

    private Game lastGame;

    private Map<String, Game<PlayerEntity, Observer>> catalog = new ConcurrentHashMap<>();

    private Map<Game, GameEntity> entities = new ConcurrentHashMap<>();

    private ChessServerDao dao = new ChessServerDao();

    private JobOperator jobOperator = BatchRuntime.getJobOperator();

    private CountDownLatch latch;

    /**
     * Returns latch used to synchronize with batch jobs.
     *
     * @return The latch.
     */
    public CountDownLatch getLatch() {
        return latch;
    }

    /**
     * Returns a collection of all the games in the in-memory catalog.
     *
     * @return Collection of in-memory games.
     */
    public Collection<Game<PlayerEntity, Observer>> getInMemoryGames() {
        return catalog.values();
    }

    /**
     * Creates a new game and stores it in the in-memory catalog. Keeps track of last
     * game created.
     *
     * @param board Initial board.
     * @param turn Whose turn is next.
     * @param summary A summary for this game.
     * @return Newly created game.
     */
    public synchronized Game<PlayerEntity, Observer> newGame(Board board, Color turn, String summary) {
        final Game<PlayerEntity, Observer> game = new Game<>(board, turn, summary);
        catalog.put(game.getGameId(), game);
        lastGame = game;
        return game;
    }

    /**
     * Searches for a game given its ID. If not in the in-memory catalog, the game is search
     * in the DB and loaded.
     *
     * @param gameId ID for the game.
     * @return The game instance or <code>null</code> if game not found.
     */
    public Game<PlayerEntity, Observer> getGame(String gameId) {
        Game<PlayerEntity, Observer> game = catalog.get(gameId);
        if (game == null) {
            final GameEntity entity = dao.findGame(gameId);
            if (entity != null) {
                game = entity.toGame();
                catalog.put(gameId, game);
                entities.put(game, entity);
                try {
                    latch = new CountDownLatch(1);

                    // Start batch process to compute board
                    Properties props = new Properties();
                    props.setProperty("gameId", gameId);
                    jobOperator.start(REPLAY_GAME, props);

                    // Synchronously wait for job to complete
                    latch.await();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return game;
    }

    /**
     * Searches for a game entity given the game ID.
     *
     * @param gameId ID for the game.
     * @return The game entity instance or <code>null</code> if not found.
     */
    public GameEntity getGameEntity(String gameId) {
        final Game<PlayerEntity, Observer> game = catalog.get(gameId);
        if (game != null) {
            return entities.get(game);
        }
        return null;
    }


    /**
     * Removes the association between a game and its corresponding entity.
     *
     * @param game The game.
     */
    public void removeGameEntity(Game<PlayerEntity, Observer> game) {
        entities.remove(game);
    }

    /**
     * Returns the last game created in the in-memory catalog.
     *
     * @return Last game created.
     */
    public synchronized Game getLastGame() {
        return lastGame;
    }

    /**
     * Persists a game in the DB.
     *
     * @param game Game instance to be persisted.
     */
    public void persistGame(Game<PlayerEntity, Observer> game) {
        final GameEntity gameEntity = GameEntity.fromGame(game);
        entities.put(game, gameEntity);
        dao.storeGame(gameEntity);
    }

    /**
     * Checks if a game is persisted.
     *
     * @param game The game.
     * @return Outcome of test.
     */
    public boolean isGamePersisted(Game<PlayerEntity, Observer> game) {
        return entities.containsKey(game);
    }

    /**
     * Returns a list of all the games in-memory or in DB sorted in descending order of
     * creation timestamp.
     *
     * @return Sorted list of all games.
     */
    public List<Game<PlayerEntity, Observer>> getAllGames() {
        List<Game<PlayerEntity, Observer>> list = new ArrayList<>();
        for (GameEntity entity : dao.findAllGames()) {
            list.add(entity.toGame());
        }
        for (Entry<String, Game<PlayerEntity, Observer>> e : catalog.entrySet()) {
            if (!list.contains(e.getValue())) {
                list.add(e.getValue());
            }
        }
        Collections.sort(list, new Comparator<Game<PlayerEntity, Observer>>() {
            @Override
            public int compare(Game<PlayerEntity, Observer> o1, Game<PlayerEntity, Observer> o2) {
                return (int) (o2.getCreationStamp() - o1.getCreationStamp());
            }
        });
        return list;
    }

    // -- GameWatcher<P,O> Interface -------------------------------------
    
    @Override
    public void addMove(Game<PlayerEntity, Observer> game, Move move) {
        final GameEntity entity = entities.get(game);
        if (entity != null) {
            entity.getMoveList().add(MoveEntity.fromMove(move, entity));
            dao.updateGame(entity);
        }
    }

    @Override
    public void setStartTurn(Game<PlayerEntity, Observer> game, Color startTurn) {
        final GameEntity entity = entities.get(game);
        if (entity != null) {
            entity.setStartTurn(startTurn);
            dao.updateGame(entity);
        }
    }

    @Override
    public void setSummary(Game<PlayerEntity, Observer> game, String summary) {
        final GameEntity entity = entities.get(game);
        if (entity != null) {
            entity.setSummary(summary);
            dao.updateGame(entity);
        }
    }

    @Override
    public void setState(Game<PlayerEntity, Observer> game, Game.State state) {
        final GameEntity entity = entities.get(game);
        if (entity != null) {
            entity.setGameState(state);
            dao.updateGame(entity);
        }
    }

    @Override
    public void setPlayer(Game<PlayerEntity, Observer> game, Color color, PlayerEntity player) {
        final GameEntity entity = entities.get(game);
        if (entity != null) {
            if (color == Color.W) {
                entity.setWhitePlayer(player);
            } else {
                entity.setBlackPlayer(player);
            }
            dao.updateGame(entity);
        }
    }
}
