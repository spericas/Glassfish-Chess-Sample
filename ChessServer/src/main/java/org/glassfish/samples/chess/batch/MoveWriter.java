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

package org.glassfish.samples.chess.batch;

import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.glassfish.samples.chess.model.Game;
import org.glassfish.samples.chess.persistence.GameEntity;
import org.glassfish.samples.chess.persistence.MoveEntity;
import org.glassfish.samples.chess.persistence.PlayerEntity;
import org.glassfish.samples.chess.websocket.ChessServerLogger;
import org.glassfish.samples.chess.websocket.GameCatalog;
import org.glassfish.samples.chess.websocket.Observer;

/**
 * MoveWriter class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
@Named("MoveWriter")
public class MoveWriter extends AbstractItemWriter {

    private static final ChessServerLogger logger = new ChessServerLogger();

    @Inject
    private JobContext jobContext;

    @Inject
    private GameCatalog gameCatalog;

    private GameEntity gameEntity;

    @PostConstruct
    public void init() {
        Properties props = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
        final String gameId = (String) props.get("gameId");
        gameEntity = gameCatalog.getGameEntity(gameId);
        if (logger.logInfo()) {
            logger.info("[ReplayGame] Starting job to replay persisted game " + gameId);
        }
    }

    @Override
    public void writeItems(List<Object> items) throws Exception {
        final Game<PlayerEntity, Observer> game = gameCatalog.getGame(gameEntity.getGameId());
        for (Object item : items) {
            final MoveEntity move = (MoveEntity) item;
            if (logger.logInfo()) {
                logger.info("[ReplayGame] Replaying move " + move.getMoveFrom() + " " +
                        move.getMoveTo() + " in game " + gameEntity.getGameId());
            }
            game.makeMove(move.getColor(), move.getMoveFrom(), move.getMoveTo());
        }
    }
}
