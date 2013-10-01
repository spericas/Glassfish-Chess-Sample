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
 * Square class.
 *
 */
public class Square {

    /**
     * Point or coordinate of this square in the board.
     */
    private final Point point;

    /**
     * Piece sitting on this square or <code>null</code> if square
     * is empty.
     */
    private Piece piece;

    public Square(int x, int y) {
        this(x, y, null);
    }
    
    public Square(int x, int y, Piece piece) {
        point = new Point(x, y);
        this.piece = piece;
    }

    public Square(Point point) {
        this(point, null);
    }

    public Square(Point point, Piece piece) {
        this.point = point;
        this.piece = piece;
    }

    /**
     * Returns the point of this square on the board.
     *
     * @return Point or coordinate for this square.
     */
    public Point getPoint() {
        return point;
    }

    /**
     * Returns the piece sitting on this square or <code>null<code>
     * if the square is empty.
     *
     * @return Piece on square or <code>null</code>.
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Sets a new piece on this square.
     *
     * @param piece New piece.
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * Determines if a square is empty or not.
     *
     * @return Value <code>true</code> if piece on square, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return piece == null;
    }

    /**
     * Returns the color for this square on the board.
     *
     * @return Color for this square.
     */
    public Color getColor() {
        return (point.getX() + point.getY()) % 2 == 0 ? Color.B : Color.W;
    }

    /**
     * Returns representation in algebraic notation. Letter for piece followed
     * by coordinate. For example, Ra1 for rook on a1 (0, 0). If no piece in
     * square, returns a strings with spaces.
     *
     * @return Notation representation.
     */
    public String toNotation() {
        if (piece == null) {
            return "   ";
        }
        return piece.toNotation() + point.toNotation();
    }

    /**
     * String representation for this square.
     *
     * @return String representation.
     */
    @Override
    public String toString() {
        return toNotation();
    }
}
