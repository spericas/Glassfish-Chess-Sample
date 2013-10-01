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

import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Square;
import org.glassfish.samples.chess.model.Board;
import org.glassfish.samples.chess.model.Point;
import org.glassfish.samples.chess.model.Move;
import org.glassfish.samples.chess.model.Piece;
import java.util.Iterator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BoardTest class.
 *
 */
public class BoardTest {

    public BoardTest() {
    }

    @Test
    public void testToString() {
        String boardRep = new Board().toString();
        System.out.println(boardRep);
        assertTrue(boardRep.contains("|Ra8|Nb8|Bc8|Qd8|Ke8|Bf8|Ng8|Rh8|"));
        assertTrue(boardRep.contains("|Pa7|Pb7|Pc7|Pd7|Pe7|Pf7|Pg7|Ph7|"));
        assertTrue(boardRep.contains("|Pa2|Pb2|Pc2|Pd2|Pe2|Pf2|Pg2|Ph2|"));
        assertTrue(boardRep.contains("|Ra1|Nb1|Bc1|Qd1|Ke1|Bf1|Ng1|Rh1|"));
    }

    @Test
    public void testBoardIterator1() {
        int n;
        Board board = new Board();

        n = 0;
        Iterator<Square> whites = board.getIterator(Color.W);
        while (whites.hasNext()) {
            System.out.println(whites.next());
            n++;
        }
        assertEquals(16, n);
        n = 0;
        Iterator<Square> blacks = board.getIterator(Color.B);
        while (blacks.hasNext()) {
            System.out.println(blacks.next());
            n++;
        }
        assertEquals(16, n);
    }

    @Test
    public void testBoardIterator2() {
        int n;
        Board board = new Board();
        board.clear();      // Only kings left!

        n = 0;
        Iterator<Square> whites = board.getIterator(Color.W);
        while (whites.hasNext()) {
            System.out.println(whites.next());
            n++;
        }
        assertEquals(1, n);
        n = 0;
        Iterator<Square> blacks = board.getIterator(Color.B);
        while (blacks.hasNext()) {
            System.out.println(blacks.next());
            n++;
        }
        assertEquals(1, n);
    }

    @Test
    public void testColors() {
        Board board = new Board();
        assertEquals("d1 piece is white", Color.W, board.getPiece(Point.fromNotation("d1")).getColor());
        assertEquals("white d1 piece is queen", "Q", board.getPiece(Point.fromNotation("d1")).toNotation());
        assertEquals("white queen d1 square is white", Color.W, board.getSquare(Point.fromNotation("d1")).getColor());
        assertEquals("d8 piece is black", Color.B, board.getPiece(Point.fromNotation("d8")).getColor());
        assertEquals("black d8 piece is queen", "Q", board.getPiece(Point.fromNotation("d8")).toNotation());
        assertEquals("black queen d8 square is black", Color.B, board.getSquare(Point.fromNotation("d8")).getColor());
    }

    @Test
    public void testUndoNormal() {
        Board board = new Board();
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("e2"), Point.fromNotation("e4")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_PAWN, board.getPiece(Point.fromNotation("e2")));
        assertNull(board.getPiece(Point.fromNotation("e4")));
    }

    @Test
    public void testUndoCapture() {
        Board board = new Board();
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("e2"), Point.fromNotation("e4")));
        board.doMove(new Move(Piece.BLACK_PAWN, Point.fromNotation("d7"), Point.fromNotation("d5")));
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("e4"), Point.fromNotation("d5")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_PAWN, board.getPiece(Point.fromNotation("e4")));
        assertEquals(Piece.BLACK_PAWN, board.getPiece(Point.fromNotation("d5")));
    }

    @Test
    public void testUndoPromotion() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.WHITE_PAWN, Point.fromNotation("a7"));
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("a7"), Point.fromNotation("a8")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_PAWN, board.getPiece(Point.fromNotation("a7")));
        assertNull(board.getPiece(Point.fromNotation("a8")));
    }

    @Test
    public void testUndoEnPassingWhite() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_PAWN, Point.fromNotation("d7"));
        board.setPiece(Piece.WHITE_PAWN, Point.fromNotation("e5"));
        board.doMove(new Move(Piece.BLACK_PAWN, Point.fromNotation("d7"), Point.fromNotation("d5")));
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("e5"), Point.fromNotation("d6")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_PAWN, board.getPiece(Point.fromNotation("e5")));
        assertEquals(Piece.BLACK_PAWN, board.getPiece(Point.fromNotation("d5")));
        assertNull(board.getPiece(Point.fromNotation("d6")));
    }

    @Test
    public void testUndoEnPassingBlack() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_PAWN, Point.fromNotation("d4"));
        board.setPiece(Piece.WHITE_PAWN, Point.fromNotation("e2"));
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("e2"), Point.fromNotation("e4")));
        board.doMove(new Move(Piece.BLACK_PAWN, Point.fromNotation("d4"), Point.fromNotation("e3")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_PAWN, board.getPiece(Point.fromNotation("e4")));
        assertEquals(Piece.BLACK_PAWN, board.getPiece(Point.fromNotation("d4")));
        assertNull(board.getPiece(Point.fromNotation("e3")));
    }

    @Test
    public void testUndoTwoMoves() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_PAWN, Point.fromNotation("d4"));
        board.setPiece(Piece.WHITE_PAWN, Point.fromNotation("e2"));
        board.doMove(new Move(Piece.WHITE_PAWN, Point.fromNotation("e2"), Point.fromNotation("e4")));
        board.doMove(new Move(Piece.BLACK_PAWN, Point.fromNotation("d4"), Point.fromNotation("e3")));
        System.out.println(board);
        board.undoLastMove();
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.BLACK_PAWN, board.getPiece(Point.fromNotation("d4")));
        assertEquals(Piece.WHITE_PAWN, board.getPiece(Point.fromNotation("e2")));
    }

    @Test
    public void testUndoCastlingWhiteLeft() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.WHITE_ROOK, Point.fromNotation("a1"));
        board.doMove(new Move(Piece.WHITE_KING, Point.fromNotation("e1"), Point.fromNotation("c1")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_ROOK, board.getPiece(Point.fromNotation("a1")));
        assertEquals(Piece.WHITE_KING, board.getPiece(Point.fromNotation("e1")));
        assertNull(board.getPiece(Point.fromNotation("b1")));
        assertNull(board.getPiece(Point.fromNotation("c1")));
        assertNull(board.getPiece(Point.fromNotation("d1")));
    }

   @Test
    public void testUndoCastlingWhiteRight() {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.WHITE_ROOK, Point.fromNotation("h1"));
        board.doMove(new Move(Piece.WHITE_KING, Point.fromNotation("e1"), Point.fromNotation("g1")));
        System.out.println(board);
        board.undoLastMove();
        System.out.println(board);
        assertEquals(Piece.WHITE_ROOK, board.getPiece(Point.fromNotation("h1")));
        assertEquals(Piece.WHITE_KING, board.getPiece(Point.fromNotation("e1")));
        assertNull(board.getPiece(Point.fromNotation("f1")));
        assertNull(board.getPiece(Point.fromNotation("g1")));
    }
}