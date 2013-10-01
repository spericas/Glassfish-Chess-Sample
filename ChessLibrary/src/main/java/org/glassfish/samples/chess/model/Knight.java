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
 * Knight class.
 *
 */
public final class Knight extends Piece {

    protected Knight(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int x1, int y1, int x2, int y2) {
        if (!super.isValidMove(x1, y1, x2, y2)) {
            return false;
        }
        return Math.abs(y2 - y1) == 2 && Math.abs(x2 - x1) == 1
                || Math.abs(y2 - y1) == 1 && Math.abs(x2 - x1) == 2;
    }

    @Override
    public String toNotation() {
        return "N";
    }

    @Override
    public List<Point> generatePath(Point from, Point to) throws GameException {
        if (!isValidMove(from, to)) {
            throw new GameException(this, from, to);
        }
        return Collections.EMPTY_LIST;      // horses can jump!
    }

    @Override
    public List<Point> generateMoves(Point from, Board board) {
        final List<Point> moves = new ArrayList<>();
        int x = from.getX();
        int y = from.getY();

        Point to;
        if (x + 1 < Board.N_SQUARES) {
            if (y + 2 < Board.N_SQUARES) {
                to = Point.fromXY(x + 1, y + 2);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
            if (y - 2 >= 0) {
                to = Point.fromXY(x + 1, y - 2);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
        }
        if (x - 1 >= 0) {
            if (y + 2 < Board.N_SQUARES) {
                to = Point.fromXY(x - 1, y + 2);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
            if (y - 2 >= 0) {
                to = Point.fromXY(x - 1, y - 2);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
        }
        if (x + 2 < Board.N_SQUARES) {
            if (y + 1 < Board.N_SQUARES) {
                to = Point.fromXY(x + 2, y + 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
            if (y - 1 >= 0) {
                to = Point.fromXY(x + 2, y - 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
        }
        if (x - 2 >= 0) {
            if (y + 1 < Board.N_SQUARES) {
                to = Point.fromXY(x - 2, y + 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
            if (y - 1 >= 0) {
                to = Point.fromXY(x - 2, y - 1);
                if (isLegalMove(from, to, board)) {
                    moves.add(to);
                }
            }
        }
        return moves;
    }
}
