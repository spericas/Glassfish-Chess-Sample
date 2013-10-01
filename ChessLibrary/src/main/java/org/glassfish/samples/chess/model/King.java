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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * King class.
 *
 */
public final class King extends Piece {

    final static Point W_START_CASTLING = Point.fromXY(4, 0);
    final static Point W_LEFT_CASTLING  = Point.fromXY(2, 0);
    final static Point W_RIGHT_CASTLING = Point.fromXY(6, 0);
    final static Point W_LEFT_ROOK      = Point.fromXY(0, 0);
    final static Point W_RIGHT_ROOK     = Point.fromXY(7, 0);

    final static Point B_START_CASTLING = Point.fromXY(4, 7);
    final static Point B_LEFT_CASTLING  = Point.fromXY(2, 7);
    final static Point B_RIGHT_CASTLING = Point.fromXY(6, 7);
    final static Point B_LEFT_ROOK      = Point.fromXY(0, 7);
    final static Point B_RIGHT_ROOK     = Point.fromXY(7, 7);

    protected King(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int x1, int y1, int x2, int y2) {
        if (!super.isValidMove(x1, y1, x2, y2)) {
            return false;
        }
        return Math.abs(x2 - x1) <= 1 && Math.abs(y2 - y1) <= 1;
    }

    @Override
    public boolean isLegalMove(Point from, Point to, Board board) {
        if (super.isLegalMove(from, to, board)) {
            return true;
        }

        // Check if this is a castling move
        if (color == Color.W) {
            if (from.equals(W_START_CASTLING)) {
                // Has king been moved?
                if (board.hasPiecedMoved(from)) {
                    return false;
                }
                // Check additional castling conditions depending on direction
                if (to.equals(W_LEFT_CASTLING)) {
                    return checkCastlingConditions(W_LEFT_ROOK, board);
                } else if (to.equals(W_RIGHT_CASTLING)) {
                    return checkCastlingConditions(W_RIGHT_ROOK, board);
                }
            }
        } else {
            if (from.equals(B_START_CASTLING)) {
                // Has king been moved?
                if (board.hasPiecedMoved(from)) {
                    return false;
                }
                // Check additional castling conditions depending on direction
                if (to.equals(B_LEFT_CASTLING)) {
                    return checkCastlingConditions(B_LEFT_ROOK, board);
                } else if (to.equals(B_RIGHT_CASTLING)) {
                    return checkCastlingConditions(B_RIGHT_ROOK, board);
                }
            }
        }
        return false;
    }

    @Override
    public String toNotation() {
        return "K";
    }

    @Override
    public List<Point> generatePath(Point from, Point to) throws GameException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Point> generateMoves(Point from, Board board) {
        final List<Point> moves = new ArrayList<>();
        final int x = from.getX();
        final int y = from.getY();

        Point to;
        if (x > 0) {
            to = Point.fromXY(x - 1, y);
            if (isLegalMove(from, to, board)) {
                moves.add(to);
            }
            if (y > 0) {
                to = Point.fromXY(x - 1, y - 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
            if (y < Board.N_SQUARES - 1) {
                to = Point.fromXY(x - 1, y + 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
        }
        if (y > 0) {
            to = Point.fromXY(x, y - 1);
            if (isLegalMove(from, to, board)) {
                moves.add(to);
            }
        }
        if (x < Board.N_SQUARES - 1) {
            to = Point.fromXY(x + 1, y);
            if (isLegalMove(from, to, board)) {
                moves.add(to);
            }
            if (y < Board.N_SQUARES - 1) {
                to = Point.fromXY(x + 1, y + 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
            if (y > 0) {
                to = Point.fromXY(x + 1, y - 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
        }
        if (y < Board.N_SQUARES - 1) {
            to = Point.fromXY(x, y + 1);
            if (isLegalMove(from, to, board)) {
                moves.add(to);
            }
        }

        if (color == Color.W && from.equals(W_START_CASTLING) ||
                color == Color.B && from.equals(B_START_CASTLING)) {
            to = Point.fromXY(x - 2, y);
            if (isLegalMove(from, to, board)) {
                moves.add(to);
            }
            to = Point.fromXY(x + 2, y);
            if (isLegalMove(from, to, board)) {
                moves.add(to);
            }
        }

        return moves;
    }

    /**
     * Checks that (i) the rook has not been moved (ii) that there are no pieces
     * between the rook and the king and (iii) that king is not in check when
     * during and at the end of the castling move.
     *
     * @param rook Rook involved in move.
     * @param board The board.
     * @return Outcome of test.
     */
    private boolean checkCastlingConditions(Point rook, Board board) {
        boolean isAllowed;
        final boolean left = (rook.getX() == 0);
        final Point start = color == Color.W ? W_START_CASTLING : B_START_CASTLING;

        if (left) {
            isAllowed = !board.hasPiecedMoved(rook)
                    && !board.hasPiece(rook.incrementX(1))
                    && !board.hasPiece(rook.incrementX(2))
                    && !board.hasPiece(rook.incrementX(3))
                    && !board.isKingAttacked(color);
            if (isAllowed) {
                board.doMove(new Move(this, start, start.decrementX(1)));
                isAllowed = !board.isKingAttacked(color);
                board.undoLastMove();
                if (isAllowed) {
                    board.doMove(new Move(this, start, start.decrementX(2)));
                    isAllowed = !board.isKingAttacked(color);
                    board.undoLastMove();
                }
            }
        } else {
            isAllowed = !board.hasPiecedMoved(rook)
                    && !board.hasPiece(rook.decrementX(1))
                    && !board.hasPiece(rook.decrementX(2))
                    && !board.isKingAttacked(color);
            if (isAllowed) {
                board.doMove(new Move(this, start, start.incrementX(1)));
                isAllowed = !board.isKingAttacked(color);
                board.undoLastMove();
                if (isAllowed) {
                    board.doMove(new Move(this, start, start.incrementX(2)));
                    isAllowed = !board.isKingAttacked(color);
                    board.undoLastMove();
                }
            }
        }
        return isAllowed;
    }
}
