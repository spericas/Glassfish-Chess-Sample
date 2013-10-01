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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Point class.
 *
 */
public final class Point {

    static final char[] letters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g' ,'h' };

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point decrementX(int delta) {
        return fromXY(x - delta, y);
    }

    public Point incrementX(int delta) {
        return fromXY(x + delta, y);
    }

    public Point decrementY(int delta) {
        return fromXY(x, y - delta);
    }

    public Point incrementY(int delta) {
        return fromXY(x, y + delta);
    }
    
    @Override
    public int hashCode() {
        return y * 8 + x;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    private static final Map<Integer, Point> cache = new ConcurrentHashMap<>();

    public static Point fromXY(int x, int y) {
        final int index = y * Board.N_SQUARES + x;
        Point point = cache.get(index);
        if (point == null) {
            point = new Point(x, y);
            cache.put(index, point);
        }
        return point;
    }

    public static Point fromNotation(String s) {
        return fromXY((int) s.charAt(0) - 'a', (int) s.charAt(1) - '1');
    }

    public String toNotation() {
        return letters[x] + Integer.toString(y + 1);
    }
}
