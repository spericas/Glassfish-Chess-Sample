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

package org.glassfish.samples.chess.model;

/**
 * Move class.
 *
 */
public class Move {

    public enum Type {
        NORMAL, PROMOTION, EN_PASSANT, LEFT_CASTLING, RIGHT_CASTLING
    };

    private Point from;
    
    private Point to;
    
    private Piece piece;
    
    private Piece captured;

    private boolean promoted;

    private boolean enPassant;

    public Move() {
    }

    public Move(Piece piece, Point from, Point to) {
        this(piece, from, to, null);
    }

    public Move(Piece piece, Point from, Point to, Piece captured) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.captured = captured;
    }

    public Point getFrom() {
        return from;
    }

    public void setFrom(Point from) {
        this.from = from;
    }

    public Point getTo() {
        return to;
    }

    public void setTo(Point to) {
        this.to = to;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Color getColor() {
        return piece.getColor();
    }
    
    public Piece getCaptured() {
        return captured;
    }

    public void setCaptured(Piece captured) {
        this.captured = captured;
    }

    public boolean hasCaptured() {
        return captured != null;
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

    public boolean isEnPassantAllowed(Move lastMove) {
        if (lastMove != null && piece instanceof Pawn) {
            if (piece.getColor() == Color.W) {
                return lastMove.getPiece() == Piece.BLACK_PAWN
                        && from.getY() == lastMove.getTo().getY()
                        && lastMove.getFrom().getY() == 6
                        && to.getX() == lastMove.getTo().getX();
            } else {
                return lastMove.getPiece() == Piece.WHITE_PAWN
                        && from.getY() == lastMove.getTo().getY()
                        && lastMove.getFrom().getY() == 1
                        && to.getX() == lastMove.getTo().getX();
            }
        }
        return false;
    }

    public boolean isLeftCastling() {
        return (piece == Piece.WHITE_KING
                && from.equals(King.W_START_CASTLING)
                && to.equals(King.W_START_CASTLING.decrementX(2))) ||
                (piece == Piece.BLACK_KING
                && from.equals(King.B_START_CASTLING)
                && to.equals(King.B_START_CASTLING.decrementX(2)));
    }

    public boolean isRightCastling() {
        return (piece == Piece.WHITE_KING 
                && from.equals(King.W_START_CASTLING) 
                && to.equals(King.W_START_CASTLING.incrementX(2))) ||
                (piece == Piece.BLACK_KING 
                && from.equals(King.B_START_CASTLING) 
                && to.equals(King.B_START_CASTLING.incrementX(2)));
    }

    public Type getType() {
        return enPassant ? Type.EN_PASSANT
                : promoted ? Type.PROMOTION
                : isLeftCastling() ? Type.LEFT_CASTLING
                : isRightCastling() ? Type.RIGHT_CASTLING
                : Type.NORMAL;
    }

    public String toNotation() {
        return from.toNotation() + to.toNotation();     // TODO: capture/promotion?
    }
}
