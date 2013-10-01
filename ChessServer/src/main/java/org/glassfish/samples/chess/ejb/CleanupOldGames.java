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

package org.glassfish.samples.chess.ejb;

import org.glassfish.samples.chess.model.Game;
import org.glassfish.samples.chess.persistence.PlayerEntity;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import java.util.Iterator;
import javax.inject.Inject;

import org.glassfish.samples.chess.websocket.ChessServerLogger;
import org.glassfish.samples.chess.websocket.GameCatalog;
import org.glassfish.samples.chess.websocket.Observer;

/**
 * CleanupOldGames class.
 *
 * @author Santiago.PericasGeertsen@oracle.com
 */
@Stateless
public class CleanupOldGames {

    private static final ChessServerLogger logger = new ChessServerLogger();
    
    private static long GAME_TIMEOUT;

    static {
        int minutes = 60;       // default 1 hour
        final String s = System.getProperty("org.glassfish.samples.chess.batch.GAME_TIMEOUT");
        if (s != null) {
            minutes = Integer.parseInt(s);
        }
        GAME_TIMEOUT = minutes * 60 * 1000L;
    }

    @Inject
    private GameCatalog catalog;

    @Schedule(minute = "*/30", hour = "*", persistent = false)
    public void cleanup() {
        synchronized (catalog) {
            if (logger.logInfo()) {
                logger.info("[CleanupOldGames] Starting cleanup process");
            }
            Iterator<Game<PlayerEntity, Observer>> it;
            for (it = catalog.getInMemoryGames().iterator(); it.hasNext();) {
                Game<PlayerEntity, Observer> game = it.next();
                if (!catalog.isGamePersisted(game)
                        && System.currentTimeMillis() - game.getUpdateStamp() > GAME_TIMEOUT) {
                    if (logger.logInfo()) {
                        logger.info("[CleanupOldGames] Removing old game " + game.getGameId());
                    }
                    catalog.removeGameEntity(game);
                    it.remove();
                }
            }
            if (logger.logInfo()) {
                logger.info("[CleanupOldGames] Cleanup process completed");
            }
        }
    }
}
