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
import org.glassfish.samples.chess.model.Piece;
import org.glassfish.samples.chess.model.Pawn;
import org.glassfish.samples.chess.model.GameException.ErrorCode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.glassfish.samples.chess.model.GameException.ErrorCode.*;

/**
 * GameTest class.
 *
 */
public class GameTest {

    public GameTest() {
    }

    @Test
    public void testGame1() throws GameException {
        Game game = new Game();
        System.out.println(game);
        game.makeMove(Color.W, Point.fromXY(3, 1), Point.fromXY(3, 3));
        System.out.println(game);
        game.makeMove(Color.B, Point.fromXY(4, 6), Point.fromXY(4, 4));
        System.out.println(game);
        game.makeMove(Color.W, Point.fromXY(3, 3), Point.fromXY(4, 4));
        System.out.println(game);
    }

    @Test
    public void testGame2() {
        try {
            Game game = new Game();
            System.out.println(game);
            game.makeMove(Color.W, Point.fromXY(3, 1), Point.fromXY(3, 4));
            fail("Illegal starting move accepted!");
        } catch (GameException _) {
            // falls through
        }
    }

    @Test
    public void testGame3() {
        try {
            Game game = new Game();
            System.out.println(game);
            game.makeMove(Color.B, Point.fromXY(3, 1), Point.fromXY(3, 3));
            fail("Black starting move accepted!");
        } catch (GameException _) {
            // falls through
        }
    }

    @Test
    public void testWhitePawnPromotion() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.WHITE_PAWN, Point.fromXY(0, 6));
        Game game = new Game(board, Color.W);
        System.out.println(game);
        game.makeMove(Color.W, Point.fromXY(0, 6), Point.fromXY(0, 7));
        System.out.println(game);
        assertTrue(board.getPiece(Point.fromXY(0, 7)) == Piece.WHITE_QUEEN);
    }

    @Test
    public void testBlackPawnPromotion() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_PAWN, Point.fromXY(0, 1));
        Game game = new Game(board, Color.B);
        System.out.println(game);
        game.makeMove(Color.B, Point.fromXY(0, 1), Point.fromXY(0, 0));
        System.out.println(game);
        assertTrue(board.getPiece(Point.fromXY(0, 0)) == Piece.BLACK_QUEEN);
    }

    @Test
    public void testKingInCheckError() {
        try {
            Board board = new Board();
            board.clear();
            board.setPiece(Piece.BLACK_KING, Point.fromXY(3, 6));
            board.setPiece(Piece.BLACK_PAWN, Point.fromXY(4, 5));
            board.setPiece(Piece.WHITE_KING, Point.fromXY(3, 0));
            board.setPiece(Piece.WHITE_BISHOP, Point.fromXY(7, 2));
            Game game = new Game(board, Color.B);
            System.out.println(game);
            // Moving this pawn leaves king in check!
            game.makeMove(Color.B, Point.fromXY(4, 5), Point.fromXY(4, 4));
        } catch (GameException e) {
            assertEquals(ILLEGAL_MOVE_KING_CHECK, e.getErrorCode());
            return;
        }
        fail("Illegal move leaving king in check not detected!");
    }

    @Test
    public void testBlackKingCheckmate1() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromXY(1, 7));
        board.setPiece(Piece.BLACK_PAWN, Point.fromXY(0, 4));
        board.setPiece(Piece.WHITE_KING, Point.fromXY(3, 0));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromXY(4, 6));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(5, 6));
        Game game = new Game(board, Color.W);
        System.out.println(game);
        assertFalse(game.isCheckmate(Color.B));
        // Moving this rook results in checkmate
        game.makeMove(Color.W, Point.fromXY(5, 6), Point.fromXY(5, 7));
        System.out.println(game);
        assertTrue(game.isCheckmate(Color.B));
    }

    @Test
    public void testBlackKingCheckmate2() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromXY(1, 7));
        board.setPiece(Piece.BLACK_PAWN, Point.fromXY(0, 4));
        board.setPiece(Piece.BLACK_ROOK, Point.fromXY(2, 4));
        board.setPiece(Piece.WHITE_KING, Point.fromXY(3, 0));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromXY(4, 6));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(5, 6));
        Game game = new Game(board, Color.W);
        System.out.println(game);
        assertFalse(game.isCheckmate(Color.B));
        // Moving white rook is not checkmate because black rook can block
        game.makeMove(Color.W, Point.fromXY(5, 6), Point.fromXY(5, 7));
        System.out.println(game);
        assertFalse(game.isCheckmate(Color.B));
    }

    @Test
    public void testBlackKingCheckmate3() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_BISHOP, Point.fromXY(3, 7));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromXY(4, 6));
        board.setPiece(Piece.WHITE_BISHOP, Point.fromXY(7, 3));
        Game game = new Game(board, Color.W);
        System.out.println(game);
        assertFalse(game.isCheckmate(Color.B));
    }

    @Test
    public void testBlackKingCheckmate4() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_BISHOP, Point.fromXY(3, 7));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromXY(4, 5));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(6, 7));
        Game game = new Game(board, Color.W);
        System.out.println(game);
        assertTrue(game.isCheckmate(Color.B));
    }

    @Test
    public void testQueryMovesKingInCheck() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_QUEEN, Point.fromXY(5, 6));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromXY(7, 4));
        Game game = new Game(board, Color.B);
        System.out.println(game);
        List<String> moves = game.queryMoves(Color.B, Point.fromXY(5, 6).toNotation());
        assertEquals(2, moves.size());
        assertTrue(moves.contains("g6"));
        assertTrue(moves.contains("h5"));
    }

    @Test
    public void testBlackKingStalemate() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("h8"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("f7"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("g6"));
        Game game = new Game(board, Color.B);
        System.out.println(game);
        assertTrue(game.isStalemate(Color.B));
    }

    @Test
    public void testBlackKingNotStalemate() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("h8"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("f7"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("g6"));
        board.setPiece(Piece.BLACK_PAWN, Point.fromNotation("a7"));
        Game game = new Game(board, Color.B);
        System.out.println(game);
        assertFalse(game.isStalemate(Color.B));     // can move pawn
    }

    @Test
    public void testRT32190() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Piece.BLACK_KING, Point.fromNotation("d7"));
        board.setPiece(Piece.WHITE_KING, Point.fromNotation("e2"));
        board.setPiece(Piece.WHITE_QUEEN, Point.fromNotation("c7"));
        board.setPiece(Piece.WHITE_KNIGHT, Point.fromNotation("d5"));
        Game game = new Game(board, Color.B);
        System.out.println(game);
        List<String> moves = game.queryMoves(Color.B, Point.fromNotation("d7"));
        assertFalse(moves.contains(Point.fromNotation("c7")));
        try {
            game.makeMove(Color.B, "d7", "c7");
        } catch (GameException e) {
            assertEquals(e.getErrorCode(), ErrorCode.ILLEGAL_MOVE_KING_CHECK);
            return;
        }
        fail("Exception expected and not thrown");
    }

    @Test
    public void testEnPassantWhiteL() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("d7"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("e5"));
        Game game = new Game(board, Color.B);
        game.makeMove(Color.B, "d7", "d5");
        System.out.println(game);
        game.makeMove(Color.W, "e5", "d6");     // en passant
        assertNull(board.getSquare(Point.fromNotation("d5")).getPiece());
        System.out.println(game);
    }

    @Test
    public void testEnPassantWhiteR() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("d7"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("c5"));
        Game game = new Game(board, Color.B);
        game.makeMove(Color.B, "d7", "d5");
        System.out.println(game);
        game.makeMove(Color.W, "c5", "d6");     // en passant
        assertNull(board.getSquare(Point.fromNotation("d5")).getPiece());
        System.out.println(game);
    }

    @Test
    public void testEnPassantBlackL() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("f4"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("e2"));
        Game game = new Game(board, Color.W);
        game.makeMove(Color.W, "e2", "e4");
        System.out.println(game);
        game.makeMove(Color.B, "f4", "e3");     // en passant
        assertNull(board.getSquare(Point.fromNotation("e4")).getPiece());
        System.out.println(game);
    }

    @Test
    public void testEnPassantBlackR() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("d4"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("e2"));
        Game game = new Game(board, Color.W);
        game.makeMove(Color.W, "e2", "e4");
        System.out.println(game);
        game.makeMove(Color.B, "d4", "e3");     // en passant
        assertNull(board.getSquare(Point.fromNotation("e4")).getPiece());
        System.out.println(game);
    }

    @Test
    public void testIllegalEnPassantWhite() throws GameException {
        Board board = new Board();
        board.clear();
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("d7"));
        board.setPiece(Pawn.BLACK_PAWN, Point.fromNotation("a7"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("e5"));
        board.setPiece(Pawn.WHITE_PAWN, Point.fromNotation("a2"));
        Game game = new Game(board, Color.B);
        game.makeMove(Color.B, "d7", "d5");
        game.makeMove(Color.W, "a2", "a3");     // other pawn
        game.makeMove(Color.B, "a7", "a6");     // other pawn
        System.out.println(game);
        try {
            game.makeMove(Color.W, "e5", "d6");     // en passant not allowed now
        } catch (GameException e) {
            System.out.println(e.getMessage());
            return;
        }
        fail("Illegal en passant move allowed");
    }

    @Test
    public void testCastlingBasic() {
        System.out.println();
        System.out.println("Test for Castling basic");
        Board board = new Board();
        board.clear();

        board.setPiece(Piece.WHITE_KING, Point.fromXY(4, 0));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(7, 0));

        Point kingFrom = Point.fromXY(4, 0);
        Point kingTo = Point.fromXY(6, 0);
        try{
            Game game = new Game(board, Color.W);
            System.out.println("Initial board is above");
            System.out.println(game);
            System.out.println();

            game.makeMove(Color.W, kingFrom, kingTo);
            System.out.println("After move:");
            System.out.println(game);
        } catch (GameException e) {
            fail("Unexpected exception " + e);
        }
        assertTrue(board.getPiece(kingTo) == Piece.WHITE_KING && board.getPiece(Point.fromXY(kingTo.getX() -1, kingTo.getY())) == Piece.WHITE_ROOK);
    }

    @Test
    public void testCastlingKingMoved() {
        System.out.println();
        System.out.println("Test for Castling King is moved");
        Board board = new Board();
        board.clear();

        board.setPiece(Piece.WHITE_KING, Point.fromXY(4, 0));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(7, 0));
        board.setPiece(Piece.BLACK_PAWN, Point.fromXY(0, 6));

        Point kingFrom = Point.fromXY(4, 0);
        Point kingTo = Point.fromXY(6, 0);
        try{
            Game game = new Game(board, Color.W);
            game.makeMove(Color.W, kingFrom, Point.fromXY(5, 0));
            game.makeMove(Color.B, Point.fromXY(0, 6), Point.fromXY(0, 5));
            game.makeMove(Color.W, Point.fromXY(5, 0), kingFrom);
            game.makeMove(Color.B, Point.fromXY(0, 5), Point.fromXY(0, 4));
            System.out.println("Initial board is above");
            System.out.println(game);
            System.out.println();

            game.makeMove(Color.W, kingFrom, kingTo);
            System.out.println("After move:");
            System.out.println(game);
        } catch (GameException e) {
            System.out.println(e.toString());
        }
        assertFalse(board.getPiece(kingTo) == Piece.WHITE_KING && board.getPiece(Point.fromXY(kingTo.getX() -1, kingTo.getY())) == Piece.WHITE_ROOK);
    }

    @Test
    public void testCastlingRookMoved() {
        System.out.println();
        System.out.println("Test for Castling rook is moved");
        Board board = new Board();
        board.clear();

        board.setPiece(Piece.WHITE_KING, Point.fromXY(4, 0));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(7, 0));
        board.setPiece(Piece.BLACK_PAWN, Point.fromXY(0, 6));

        Point kingFrom = Point.fromXY(4, 0);
        Point kingTo = Point.fromXY(6, 0);
        try{
            Game game = new Game(board, Color.W);
            game.makeMove(Color.W, Point.fromXY(7, 0), Point.fromXY(5, 0));
            game.makeMove(Color.B, Point.fromXY(0, 6), Point.fromXY(0, 5));
            game.makeMove(Color.W, Point.fromXY(5, 0), Point.fromXY(7, 0));
            game.makeMove(Color.B, Point.fromXY(0, 5), Point.fromXY(0, 4));
            System.out.println("Initial board is above");
            System.out.println(game);
            System.out.println();

            game.makeMove(Color.W, kingFrom, kingTo);
            System.out.println("After move:");
            System.out.println(game);
        } catch (GameException e) {
            System.out.println(e.toString());
        }
        assertFalse(board.getPiece(kingTo) == Piece.WHITE_KING && board.getPiece(Point.fromXY(kingTo.getX() -1, kingTo.getY())) == Piece.WHITE_ROOK);
    }

    @Test
    public void testCastlingPieceInBetween() {
        System.out.println();
        System.out.println("Test for Castling piece in between");
        Board board = new Board();
        board.clear();

        board.setPiece(Piece.BLACK_KING, Point.fromXY(4, 7));
        board.setPiece(Piece.BLACK_ROOK, Point.fromXY(7, 7));
        board.setPiece(Piece.BLACK_KNIGHT, Point.fromXY(6, 7));
        Point kingFrom = Point.fromXY(4, 7);
        Point kingTo = Point.fromXY(6, 7);
        try{
            Game game = new Game(board, Color.B);
            System.out.println("Initial board is above");
            System.out.println(game);
            System.out.println();

            game.makeMove(Color.B, kingFrom, kingTo);
            System.out.println("After move:");
            System.out.println(game);
        } catch (GameException e) {
            System.out.println(e.toString());
        }
        assertFalse(board.getPiece(kingTo) == Piece.BLACK_KING && board.getPiece(Point.fromXY(kingTo.getX() -1, kingTo.getY())) == Piece.BLACK_ROOK);
    }

    @Test
    public void testQueensideCastling() {
        System.out.println();
        System.out.println("Test for Queen side Castling");
        Board board = new Board();
        board.clear();

        board.setPiece(Piece.BLACK_KING, Point.fromXY(4, 7));
        board.setPiece(Piece.BLACK_ROOK, Point.fromXY(0, 7));
        Point kingFrom = Point.fromXY(4, 7);
        Point kingTo = Point.fromXY(2, 7);
        try{
            Game game = new Game(board, Color.B);
            System.out.println("Initial board is above");
            System.out.println(game);
            System.out.println();

            game.makeMove(Color.B, kingFrom, kingTo);
            System.out.println("After move:");
            System.out.println(game);
        } catch (GameException e) {
            System.out.println(e.toString());
        }
        assertTrue(board.getPiece(kingTo) == Piece.BLACK_KING && board.getPiece(Point.fromXY(kingTo.getX() + 1, kingTo.getY())) == Piece.BLACK_ROOK);
    }

    @Test
    public void testCastlingAnotherRookMoved() {
        System.out.println();
        System.out.println("Test for Castling basic");
        Board board = new Board();
        board.clear();

        board.setPiece(Piece.WHITE_KING, Point.fromXY(4, 0));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(7, 0));
        board.setPiece(Piece.WHITE_ROOK, Point.fromXY(0, 0));

        Point kingFrom = Point.fromXY(4, 0);
        Point kingTo = Point.fromXY(6, 0);
        try{
            Game game = new Game(board, Color.W);
            System.out.println("Initial board is above");
            System.out.println(game);
            System.out.println();

            game.makeMove(Color.W, Point.fromXY(0, 0), Point.fromXY(0, 1));
            game.makeMove(Color.B, Point.fromXY(4, 7), Point.fromXY(4, 6));
            game.makeMove(Color.W, kingFrom, kingTo);
            System.out.println("After move:");
            System.out.println(game);
        } catch (GameException e) {
            System.out.println(e.toString());
        }
        assertTrue(board.getPiece(kingTo) == Piece.WHITE_KING && board.getPiece(Point.fromXY(kingTo.getX() -1, kingTo.getY())) == Piece.WHITE_ROOK);
    }

}