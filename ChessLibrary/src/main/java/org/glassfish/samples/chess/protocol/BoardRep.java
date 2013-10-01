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

package org.glassfish.samples.chess.protocol;

import java.util.List;

import org.glassfish.samples.chess.model.Board;
import org.glassfish.samples.chess.model.Color;
import org.glassfish.samples.chess.model.Piece;
import org.glassfish.samples.chess.model.Point;
import org.glassfish.samples.chess.model.Square;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * BoardRep class.
 *
 */
public class BoardRep {

    private List<String> whites;

    private List<String> blacks;

    public BoardRep() {
    }

    public BoardRep(Board board) {
        whites = new ArrayList<>();
        final Iterator<Square> wi = board.getIterator(Color.W);
        while (wi.hasNext()) {
            whites.add(wi.next().toNotation());
        }
        blacks = new ArrayList<>();
        final Iterator<Square> bi = board.getIterator(Color.B);
        while (bi.hasNext()) {
            blacks.add(bi.next().toNotation());
        }
    }

    public List<String> getWhites() {
        return whites;
    }

    public void setWhites(List<String> whites) {
        this.whites = whites;
    }

    public List<String> getBlacks() {
        return blacks;
    }

    public void setBlacks(List<String> blacks) {
        this.blacks = blacks;
    }

    public Board toBoard() {
        final Board board = new Board();
        board.clear();
        if (whites != null) {
            for (String w : whites) {
                board.setPiece(Piece.fromNotation(Color.W, w.substring(0, 1)),
                               Point.fromNotation(w.substring(1)));
            }
        }
        if (blacks != null) {
            for (String b : blacks) {
                board.setPiece(Piece.fromNotation(Color.B, b.substring(0, 1)),
                               Point.fromNotation(b.substring(1)));
            }
        }
        return board;
    }

}
