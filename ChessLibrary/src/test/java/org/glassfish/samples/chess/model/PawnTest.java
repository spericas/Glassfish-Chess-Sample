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

import org.glassfish.samples.chess.model.Game;
import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Point;
import org.glassfish.samples.chess.model.Board;
import org.glassfish.samples.chess.model.GameException;
import org.glassfish.samples.chess.model.Pawn;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;
import static org.glassfish.samples.chess.model.Piece.*;

/**
 * PawnTest class.
 *
 */
public class PawnTest {

    public PawnTest() {
    }

    @Test
    public void testWhitePawn31() {
        // Starting at (3,1)
        assertTrue(WHITE_PAWN.isValidMove(3, 1, 3, 2));
        assertTrue(WHITE_PAWN.isValidMove(3, 1, 3, 3));
        assertTrue(WHITE_PAWN.isValidMove(3, 1, 2, 2));
        assertTrue(WHITE_PAWN.isValidMove(3, 1, 4, 2));

        assertFalse(WHITE_PAWN.isValidMove(3, 1, 2, 0));
        assertFalse(WHITE_PAWN.isValidMove(3, 1, 3, 0));
        assertFalse(WHITE_PAWN.isValidMove(3, 1, 4, 0));
        assertFalse(WHITE_PAWN.isValidMove(3, 1, 2, 1));
        assertFalse(WHITE_PAWN.isValidMove(3, 1, 4, 1));
    }

    @Test
    public void testWhitePawn33() {
        // Starting at (3,3)
        assertTrue(WHITE_PAWN.isValidMove(3, 3, 3, 4));
        assertTrue(WHITE_PAWN.isValidMove(3, 3, 2, 4));
        assertTrue(WHITE_PAWN.isValidMove(3, 3, 4, 4));

        assertFalse(WHITE_PAWN.isValidMove(3, 3, 2, 3));
        assertFalse(WHITE_PAWN.isValidMove(3, 3, 2, 2));
        assertFalse(WHITE_PAWN.isValidMove(3, 3, 3, 2));
        assertFalse(WHITE_PAWN.isValidMove(3, 3, 4, 2));
        assertFalse(WHITE_PAWN.isValidMove(3, 3, 4, 3));
    }

    @Test
    public void testBlackPawn36() {
        // Starting at (3,6)
        assertTrue(BLACK_PAWN.isValidMove(3, 6, 3, 5));
        assertTrue(BLACK_PAWN.isValidMove(3, 6, 3, 4));
        assertTrue(BLACK_PAWN.isValidMove(3, 6, 2, 5));
        assertTrue(BLACK_PAWN.isValidMove(3, 6, 4, 5));

        assertFalse(BLACK_PAWN.isValidMove(3, 6, 3, 7));
        assertFalse(BLACK_PAWN.isValidMove(3, 6, 2, 7));
        assertFalse(BLACK_PAWN.isValidMove(3, 6, 4, 7));
        assertFalse(BLACK_PAWN.isValidMove(3, 6, 2, 6));
        assertFalse(BLACK_PAWN.isValidMove(3, 6, 4, 6));
    }

    @Test
    public void testBlackPawn44() {
        // Starting at (4,4)
        assertTrue(BLACK_PAWN.isValidMove(4, 4, 3, 3));
        assertTrue(BLACK_PAWN.isValidMove(4, 4, 4, 3));
        assertTrue(BLACK_PAWN.isValidMove(4, 4, 4, 3));

        assertFalse(BLACK_PAWN.isValidMove(4, 4, 3, 4));
        assertFalse(BLACK_PAWN.isValidMove(4, 4, 3, 5));
        assertFalse(BLACK_PAWN.isValidMove(4, 4, 4, 5));
        assertFalse(BLACK_PAWN.isValidMove(4, 4, 5, 5));
        assertFalse(BLACK_PAWN.isValidMove(4, 4, 5, 4));
    }

    @Test
    public void testPath3132() throws GameException {
        Point from = Point.fromXY(3, 1);
        Point to = Point.fromXY(3, 2);
        List<Point> path = WHITE_PAWN.generatePath(from, to);
        assertEquals(0, path.size());
    }

    @Test
    public void testPath3133() throws GameException {
        Point from = Point.fromXY(3, 1);
        Point to = Point.fromXY(3, 3);
        List<Point> path = WHITE_PAWN.generatePath(from, to);
        assertEquals(1, path.size());
        assertTrue(path.contains(Point.fromXY(3, 2)));
        System.out.println(path);
    }

    @Test
    public void testPath3122() throws GameException {
        Point from = Point.fromXY(3, 1);
        Point to = Point.fromXY(2, 2);
        List<Point> path = WHITE_PAWN.generatePath(from, to);
        assertEquals(0, path.size());
    }

    @Test
    public void testPath3142() throws GameException {
        Point from = Point.fromXY(3, 1);
        Point to = Point.fromXY(4, 2);
        List<Point> path = WHITE_PAWN.generatePath(from, to);
        assertEquals(0, path.size());
    }

    @Test
    public void testPath4143() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromXY(4, 3));
        assertFalse(WHITE_PAWN.isLegalMove(Point.fromXY(4, 1), Point.fromXY(4, 3), board));
    }

    @Test
    public void testEnPassantWhite() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("d7"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("e5"));
        Game game = new Game(board, Color.B);
        System.out.println(game);
        game.makeMove(Color.B, "d7", "d5");
        List<String> moves = game.queryMoves(Color.W, "e5");
        assertEquals(2, moves.size());
        assertTrue(moves.contains("d6"));
        assertTrue(moves.contains("e6"));
    }

    @Test
    public void testEnPassantBlack() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("d4"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("e2"));
        Game game = new Game(board, Color.W);
        System.out.println(game);
        game.makeMove(Color.W, "e2", "e4");
        List<String> moves = game.queryMoves(Color.B, "d4");
        assertEquals(2, moves.size());
        assertTrue(moves.contains("e3"));
        assertTrue(moves.contains("d3"));
    }
}