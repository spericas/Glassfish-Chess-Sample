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

import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Move;
import org.glassfish.samples.chess.model.Piece;
import org.glassfish.samples.chess.model.Point;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name="Move")
public class MoveEntity implements Serializable {

    private Long id;

    private GameEntity game;

    private String moveFrom;

    private String moveTo;

    private String piece;

    private String captured;

    private boolean promoted;

    private boolean enPassant;

    public MoveEntity() {
    }

    public MoveEntity(Move move, GameEntity game) {
        moveFrom = move.getFrom().toNotation();
        moveTo = move.getTo().toNotation();
        piece = move.getPiece().toString();
        captured = move.hasCaptured() ? move.getCaptured().toString() : null;
        promoted = move.isPromoted();
        enPassant = move.isEnPassant();
        this.game = game;
    }

    public Move toMove() {
        final Move move = new Move();
        move.setFrom(Point.fromNotation(moveFrom));
        move.setTo(Point.fromNotation(moveTo));
        move.setPiece(Piece.fromString(piece));
        if (captured != null) {
            move.setCaptured(Piece.fromString(captured));
        }
        move.setPromoted(promoted);
        move.setEnPassant(enPassant);
        return move;
    }

    public static MoveEntity fromMove(Move move, GameEntity game) {
        return new MoveEntity(move, game);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "gameId")
    @NotNull
    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public String getMoveFrom() {
        return moveFrom;
    }

    public void setMoveFrom(String from) {
        this.moveFrom = from;
    }

    public String getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(String to) {
        this.moveTo = to;
    }

    public String getPiece() {
        return piece;
    }

    public void setPiece(String piece) {
        this.piece = piece;
    }

    public String getCaptured() {
        return captured;
    }

    public void setCaptured(String captured) {
        this.captured = captured;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(boolean promoted) {
        this.promoted = promoted;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public void setEnPassant(boolean enPassant) {
        this.enPassant = enPassant;
    }

    @Transient
    public Color getColor() {
        return Piece.fromString(piece).getColor();
    }
}
