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

import static org.junit.Assert.*;
import static org.glassfish.samples.chess.model.Piece.*;

/**
 * BishopTest class.
 *
 */
public class BishopTest extends PieceTest {

    public BishopTest() {
    }

    @Test
    public void testBishop20() {
        // Starting at (2,0)
        testMove(2, 0, WHITE_BISHOP, Direction.NE, true);
        testMove(2, 0, WHITE_BISHOP, Direction.NW, true);

        testMove(2, 0, WHITE_BISHOP, Direction.SE, false);
        testMove(2, 0, WHITE_BISHOP, Direction.SW, false);
        testMove(2, 0, WHITE_BISHOP, Direction.N, false);
        testMove(2, 0, WHITE_BISHOP, Direction.S, false);
        testMove(2, 0, WHITE_BISHOP, Direction.E, false);
        testMove(2, 0, WHITE_BISHOP, Direction.W, false);
    }

    @Test
    public void testBishop33() {
        // Starting at (3,3)
        testMove(3, 3, WHITE_BISHOP, Direction.NE, true);
        testMove(3, 3, WHITE_BISHOP, Direction.NW, true);
        testMove(3, 3, WHITE_BISHOP, Direction.SE, true);
        testMove(3, 3, WHITE_BISHOP, Direction.SW, true);

        testMove(3, 3, WHITE_BISHOP, Direction.N, false);
        testMove(3, 3, WHITE_BISHOP, Direction.S, false);
        testMove(3, 3, WHITE_BISHOP, Direction.E, false);
        testMove(3, 3, WHITE_BISHOP, Direction.W, false);
    }

    @Test
    public void testPath3300() throws GameException {
        Point from = Point.fromXY(3, 3);
        Point to = Point.fromXY(0, 0);
        List<Point> path = WHITE_BISHOP.generatePath(from, to);
        assertEquals(2, path.size());
        assertTrue(path.contains(Point.fromXY(2, 2)));
        assertTrue(path.contains(Point.fromXY(1, 1)));
        System.out.println(path);
    }

    @Test
    public void testPath3360() throws GameException {
        Point from = Point.fromXY(3, 3);
        Point to = Point.fromXY(6, 0);
        List<Point> path = WHITE_BISHOP.generatePath(from, to);
        assertEquals(2, path.size());
        assertTrue(path.contains(Point.fromXY(4, 2)));
        assertTrue(path.contains(Point.fromXY(5, 1)));
        System.out.println(path);
    }

    @Test
    public void testPath3366() throws GameException {
        Point from = Point.fromXY(3, 3);
        Point to = Point.fromXY(6, 6);
        List<Point> path = WHITE_BISHOP.generatePath(from, to);
        assertEquals(2, path.size());
        assertTrue(path.contains(Point.fromXY(4, 4)));
        assertTrue(path.contains(Point.fromXY(5, 5)));
        System.out.println(path);
    }

    @Test
    public void testPath3306() throws GameException {
        Point from = Point.fromXY(3, 3);
        Point to = Point.fromXY(0, 6);
        List<Point> path = WHITE_BISHOP.generatePath(from, to);
        assertEquals(2, path.size());
        assertTrue(path.contains(Point.fromXY(2, 4)));
        assertTrue(path.contains(Point.fromXY(1, 5)));
        System.out.println(path);
    }

    @Test
    public void testGenerateMoves00() {
        List<Point> moves = WHITE_BISHOP.generateMoves(Point.fromXY(0, 0), getClearedBoard());
        assertEquals(7, moves.size());
        assertTrue(moves.contains(Point.fromXY(1, 1)));
        assertTrue(moves.contains(Point.fromXY(2, 2)));
        assertTrue(moves.contains(Point.fromXY(3, 3)));
        assertTrue(moves.contains(Point.fromXY(4, 4)));
        assertTrue(moves.contains(Point.fromXY(5, 5)));
        assertTrue(moves.contains(Point.fromXY(6, 6)));
        assertTrue(moves.contains(Point.fromXY(7, 7)));
        System.out.println(moves);
    }

    @Test
    public void testGenerateMoves77() {
        List<Point> moves = WHITE_BISHOP.generateMoves(Point.fromXY(7, 7), getClearedBoard());
        assertEquals(7, moves.size());
        assertTrue(moves.contains(Point.fromXY(0, 0)));
        assertTrue(moves.contains(Point.fromXY(1, 1)));
        assertTrue(moves.contains(Point.fromXY(2, 2)));
        assertTrue(moves.contains(Point.fromXY(3, 3)));
        assertTrue(moves.contains(Point.fromXY(4, 4)));
        assertTrue(moves.contains(Point.fromXY(5, 5)));
        assertTrue(moves.contains(Point.fromXY(6, 6)));
        System.out.println(moves);
    }

    @Test
    public void testGenerateMoves44() {
        List<Point> moves = WHITE_BISHOP.generateMoves(Point.fromXY(4, 4), getClearedBoard());
        assertEquals(13, moves.size());
        assertTrue(moves.contains(Point.fromXY(0, 0)));
        assertTrue(moves.contains(Point.fromXY(7, 7)));
        assertTrue(moves.contains(Point.fromXY(7, 1)));
        assertTrue(moves.contains(Point.fromXY(1, 7)));
        System.out.println(moves);
    }
}