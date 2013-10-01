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

package org.glassfish.samples.chess.persistence;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Game;
import org.glassfish.samples.chess.model.Game.State;
import org.glassfish.samples.chess.model.Move;
import org.glassfish.samples.chess.websocket.Observer;

/**
 * Game entity.
 *
 * @author Daniel.Guo@oracle.com
 * @author Santiago.PericasGeertsen@oracle.com
 */
@Entity(name="Game")
@NamedQuery(name="findAllGames", query="SELECT g FROM Game g")
public class GameEntity implements Serializable {

    private String gameId;

    private PlayerEntity whitePlayer;

    private PlayerEntity blackPlayer;

    private List<MoveEntity> moveList = new ArrayList<>();

    private Color startTurn = Color.W;

    private String summary;

    private State gameState = State.PLAYING;

    private long creationStamp;

    public GameEntity() {
    }

    public GameEntity(Game<PlayerEntity, Observer> game) {
        gameId = game.getGameId();
        whitePlayer = game.getPlayer(Color.W);
        blackPlayer = game.getPlayer(Color.B);
        summary = game.getSummary();
        gameState = game.getState();
        startTurn = game.getStartTurn();
        creationStamp = game.getCreationStamp();
        for (Move move : game.getMoves()) {
            moveList.add(MoveEntity.fromMove(move, this));
        }
    }

    public Game<PlayerEntity, Observer> toGame() {
        Game<PlayerEntity, Observer> game = new Game<>();
        game.setGameId(gameId);
        game.setPlayer(Color.W, whitePlayer);
        game.setPlayer(Color.B, blackPlayer);
        game.setSummary(summary);
        game.setState(gameState);
        game.setStartTurn(startTurn);
        game.setCreationStamp(creationStamp);
        return game;
    }

    public static GameEntity fromGame(Game<PlayerEntity, Observer> game) {
        return new GameEntity(game);
    }

    @Id
    @NotNull
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @OneToOne(fetch=FetchType.LAZY)
    public PlayerEntity getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(PlayerEntity whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    @OneToOne(fetch=FetchType.LAZY)
    public PlayerEntity getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(PlayerEntity blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    @OneToMany(fetch=FetchType.LAZY, mappedBy="game", cascade=CascadeType.ALL)
    public List<MoveEntity> getMoveList() {
        return moveList;
    }

    public void setMoveList(List<MoveEntity> moveList) {
        this.moveList = moveList;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    public Color getStartTurn() {
        return startTurn;
    }

    public void setStartTurn(Color startColor) {
        this.startTurn = startColor;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    public State getGameState() {
        return gameState;
    }

    public void setGameState(State state) {
        this.gameState = state;
    }

    public long getCreationStamp() {
        return creationStamp;
    }

    public void setCreationStamp(long creationStamp) {
        this.creationStamp = creationStamp;
    }
}
