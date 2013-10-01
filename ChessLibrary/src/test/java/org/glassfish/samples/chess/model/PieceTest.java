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

import org.glassfish.samples.chess.model.Board;
import org.glassfish.samples.chess.model.Piece;
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 * PieceTest class.
 *
 */
@Ignore
public abstract class PieceTest {

    protected enum Direction {
        N, NE, E, SE, S, SW, W, NW
    };

    protected void testMove(int x0, int y0, Piece piece, Direction dir, boolean result) {
        switch (dir) {
            case N:
                for (int y = y0 + 1; y < Board.N_SQUARES; y++) {
                    assertEquals(piece.isValidMove(x0, y0, x0, y), result);
                }
                break;
            case NE:
                for (int x = x0 + 1, y = y0 + 1; x < Board.N_SQUARES && y < Board.N_SQUARES; x++, y++) {
                    assertEquals(piece.isValidMove(x0, y0, x, y), result);
                }
                break;
            case E:
                for (int x = x0 + 1; x < Board.N_SQUARES; x++) {
                    assertEquals(piece.isValidMove(x0, y0, x, y0), result);
                }
                break;
            case SE:
                for (int x = x0 + 1, y = y0 - 1; x < Board.N_SQUARES && y >= 0; x++, y--) {
                    assertEquals(piece.isValidMove(x0, y0, x, y), result);
                }
                break;
            case S:
                for (int y = y0 - 1; y >= 0; y--) {
                    assertEquals(piece.isValidMove(x0, y0, x0, y), result);
                }
                break;
            case SW:
                for (int x = x0 - 1, y = y0 - 1; x >= 0 && y >= 0; x--, y--) {
                    assertEquals(piece.isValidMove(x0, y0, x, y), result);
                }
                break;
            case W:
                for (int x = x0 - 1; x >= 0; x--) {
                    assertEquals(piece.isValidMove(x0, y0, x, y0), result);
                }
                break;
            case NW:
                for (int x = x0 - 1, y = y0 + 1; x >= 0 && y < Board.N_SQUARES; x--, y++) {
                    assertEquals(piece.isValidMove(x0, y0, x, y), result);
                }
                break;
        }
    }

    protected Board getClearedBoard() {
        final Board board = new Board();
        board.clear();
        return board;
    }
}
