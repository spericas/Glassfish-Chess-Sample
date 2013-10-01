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

import org.glassfish.samples.chess.model.Point;
import org.glassfish.samples.chess.model.GameException;
import org.junit.Test;
import java.util.List;

import static org.glassfish.samples.chess.model.Piece.*;
import static org.junit.Assert.*;

/**
 * RookTest class.
 *
 */
public class RookTest extends PieceTest {

    public RookTest() {
    }

    @Test
    public void testRook00() {
        // Starting at (0,0)
        testMove(0, 0, WHITE_ROOK, Direction.N, true);
        testMove(0, 0, WHITE_ROOK, Direction.E, true);

        testMove(0, 0, WHITE_ROOK, Direction.SE, false);
        testMove(0, 0, WHITE_ROOK, Direction.SW, false);
        testMove(0, 0, WHITE_ROOK, Direction.NE, false);
        testMove(0, 0, WHITE_ROOK, Direction.NW, false);
        testMove(0, 0, WHITE_ROOK, Direction.S, false);
        testMove(0, 0, WHITE_ROOK, Direction.W, false);
    }

    @Test
    public void testRook44() {
        // Starting at (4,4)
        testMove(4, 4, WHITE_ROOK, Direction.N, true);
        testMove(4, 4, WHITE_ROOK, Direction.E, true);
        testMove(4, 4, WHITE_ROOK, Direction.S, true);
        testMove(4, 4, WHITE_ROOK, Direction.W, true);

        testMove(4, 4, WHITE_ROOK, Direction.SE, false);
        testMove(4, 4, WHITE_ROOK, Direction.SW, false);
        testMove(4, 4, WHITE_ROOK, Direction.NE, false);
        testMove(4, 4, WHITE_ROOK, Direction.NW, false);
    }

    @Test
    public void testPath4404() throws GameException {
        Point from = Point.fromXY(4, 4);
        Point to = Point.fromXY(0, 4);
        List<Point> path = WHITE_ROOK.generatePath(from, to);
        assertEquals(3, path.size());
        assertTrue(path.contains(Point.fromXY(3, 4)));
        assertTrue(path.contains(Point.fromXY(2, 4)));
        assertTrue(path.contains(Point.fromXY(1, 4)));
        System.out.println(path);
    }

    @Test
    public void testPath4474() throws GameException {
        Point from = Point.fromXY(4, 4);
        Point to = Point.fromXY(7, 4);
        List<Point> path = WHITE_ROOK.generatePath(from, to);
        assertEquals(2, path.size());
        assertTrue(path.contains(Point.fromXY(5, 4)));
        assertTrue(path.contains(Point.fromXY(6, 4)));
        System.out.println(path);
    }

    @Test
    public void testPath4447() throws GameException {
        Point from = Point.fromXY(4, 4);
        Point to = Point.fromXY(4, 7);
        List<Point> path = WHITE_ROOK.generatePath(from, to);
        assertEquals(2, path.size());
        assertTrue(path.contains(Point.fromXY(4, 5)));
        assertTrue(path.contains(Point.fromXY(4, 6)));
        System.out.println(path);
    }

    @Test
    public void testPath4440() throws GameException {
        Point from = Point.fromXY(4, 4);
        Point to = Point.fromXY(4, 0);
        List<Point> path = WHITE_ROOK.generatePath(from, to);
        assertEquals(3, path.size());
        assertTrue(path.contains(Point.fromXY(4, 3)));
        assertTrue(path.contains(Point.fromXY(4, 2)));
        assertTrue(path.contains(Point.fromXY(4, 1)));
        System.out.println(path);
    }

    @Test
    public void testGenerateMoves01() {
        List<Point> moves = WHITE_ROOK.generateMoves(Point.fromXY(0, 1), getClearedBoard());
        assertEquals(14, moves.size());
        assertTrue(moves.contains(Point.fromXY(1, 1)));
        assertTrue(moves.contains(Point.fromXY(2, 1)));
        assertTrue(moves.contains(Point.fromXY(3, 1)));
        assertTrue(moves.contains(Point.fromXY(4, 1)));
        assertTrue(moves.contains(Point.fromXY(5, 1)));
        assertTrue(moves.contains(Point.fromXY(6, 1)));
        assertTrue(moves.contains(Point.fromXY(7, 1)));
        assertTrue(moves.contains(Point.fromXY(0, 0)));
        assertTrue(moves.contains(Point.fromXY(0, 2)));
        assertTrue(moves.contains(Point.fromXY(0, 3)));
        assertTrue(moves.contains(Point.fromXY(0, 4)));
        assertTrue(moves.contains(Point.fromXY(0, 5)));
        assertTrue(moves.contains(Point.fromXY(0, 6)));
        assertTrue(moves.contains(Point.fromXY(0, 7)));
        System.out.println(moves);
    }

    @Test
    public void testGenerateMoves77() {
        List<Point> moves = WHITE_ROOK.generateMoves(Point.fromXY(7, 6), getClearedBoard());
        assertEquals(14, moves.size());
        assertTrue(moves.contains(Point.fromXY(6, 6)));
        assertTrue(moves.contains(Point.fromXY(5, 6)));
        assertTrue(moves.contains(Point.fromXY(4, 6)));
        assertTrue(moves.contains(Point.fromXY(3, 6)));
        assertTrue(moves.contains(Point.fromXY(2, 6)));
        assertTrue(moves.contains(Point.fromXY(1, 6)));
        assertTrue(moves.contains(Point.fromXY(0, 6)));
        assertTrue(moves.contains(Point.fromXY(7, 7)));
        assertTrue(moves.contains(Point.fromXY(7, 5)));
        assertTrue(moves.contains(Point.fromXY(7, 4)));
        assertTrue(moves.contains(Point.fromXY(7, 3)));
        assertTrue(moves.contains(Point.fromXY(7, 2)));
        assertTrue(moves.contains(Point.fromXY(7, 1)));
        assertTrue(moves.contains(Point.fromXY(7, 0)));
        System.out.println(moves);
    }

    @Test
    public void testGenerateMoves33() {
        List<Point> moves = WHITE_ROOK.generateMoves(Point.fromXY(3, 3), getClearedBoard());
        assertEquals(14, moves.size());
        assertTrue(moves.contains(Point.fromXY(0, 3)));
        assertTrue(moves.contains(Point.fromXY(3, 0)));
        assertTrue(moves.contains(Point.fromXY(7, 3)));
        assertTrue(moves.contains(Point.fromXY(3, 7)));
        System.out.println(moves);
    }
}