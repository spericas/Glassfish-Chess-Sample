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
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;
import static org.glassfish.samples.chess.model.Piece.*;

/**
 * KingTest class.
 *
 */
public class KingTest extends PieceTest {

    public KingTest() {
    }

    @Test
    public void testKingMoves00() {
        List<Point> moves = WHITE_KING.generateMoves(Point.fromXY(0, 0), getClearedBoard());
        assertEquals(3, moves.size());
        assertTrue(moves.contains(Point.fromXY(0, 1)));
        assertTrue(moves.contains(Point.fromXY(1, 1)));
        assertTrue(moves.contains(Point.fromXY(1, 0)));
    }

    @Test
    public void testKingMoves77() {
        List<Point> moves = WHITE_KING.generateMoves(Point.fromXY(7, 7), getClearedBoard());
        assertEquals(3, moves.size());
        assertTrue(moves.contains(Point.fromXY(6, 7)));
        assertTrue(moves.contains(Point.fromXY(6, 6)));
        assertTrue(moves.contains(Point.fromXY(7, 6)));
    }

    @Test
    public void testKingMoves04() {
        List<Point> moves = WHITE_KING.generateMoves(Point.fromXY(0, 4), getClearedBoard());
        assertEquals(5, moves.size());
        assertTrue(moves.contains(Point.fromXY(0, 5)));
        assertTrue(moves.contains(Point.fromXY(0, 3)));
        assertTrue(moves.contains(Point.fromXY(1, 3)));
        assertTrue(moves.contains(Point.fromXY(1, 4)));
        assertTrue(moves.contains(Point.fromXY(1, 5)));
    }

    @Test
    public void testKingMoves44() {
        List<Point> moves = WHITE_KING.generateMoves(Point.fromXY(4, 4), getClearedBoard());
        assertEquals(8, moves.size());
        assertTrue(moves.contains(Point.fromXY(3, 5)));
        assertTrue(moves.contains(Point.fromXY(3, 4)));
        assertTrue(moves.contains(Point.fromXY(3, 3)));
        assertTrue(moves.contains(Point.fromXY(5, 5)));
        assertTrue(moves.contains(Point.fromXY(5, 4)));
        assertTrue(moves.contains(Point.fromXY(5, 3)));
        assertTrue(moves.contains(Point.fromXY(4, 5)));
        assertTrue(moves.contains(Point.fromXY(4, 3)));
    }
}