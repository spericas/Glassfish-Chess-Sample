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

package org.glassfish.samples.chess.persistence;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.glassfish.samples.chess.websocket.ChessServerLogger;

/**
 * Class ChessServerDao.
 *
 * @author Daniel.Guo@oracle.com
 * @author Santiago.PericasGeertsen@oracle.com
 */
@WebListener
public class ChessServerDao implements ServletContextListener {

    private static final ChessServerLogger logger = new ChessServerLogger();

    private static final String PERSISTENCE_UNIT_NAME = "ChessServerPU";

    private static EntityManagerFactory emFactory;

    private EntityManager em;

    /**
     * Initializes entity manager factory on deploy.
     *
     * @param sce Context.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        emFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        if (logger.logInfo()) {
            logger.info("Creating EntityManagerFactory " + emFactory);
        }
    }

    /**
     * Closes entity manager factory on undeploy.
     *
     * @param sce Context.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (emFactory != null) {
            emFactory.close();
        }
        if (logger.logInfo()) {
            logger.info("Closing EntityManagerFactory " + emFactory);
        }
    }

    /**
     * Return entity manager depending on context.
     *
     * @return Entity manager.
     */
    private EntityManager getEntityManager() {
        if (emFactory == null) {        // standalone mode
            emFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        if (em == null) {
            em = emFactory.createEntityManager();
        }
        return em;
    }

    /**
     * Persists a game on entity manager.
     *
     * @param game The game.
     */
    public void storeGame(GameEntity game) {
        getEntityManager().persist(game);
    }

    /**
     * Finds a game given its ID.
     *
     * @param gameId The game ID.
     * @return The game entity or <code>null</code> if not found.
     */
    public GameEntity findGame(String gameId) {
        return getEntityManager().find(GameEntity.class, gameId);
    }

    /**
     * Returns a list of all the game entities in the DB.
     *
     * @return List of all game entities.
     */
    public List<GameEntity> findAllGames() {
        final TypedQuery<GameEntity> query = getEntityManager()
                .createNamedQuery("findAllGames", GameEntity.class);
        return query.getResultList();
    }

    /**
     * Updates a game in underlying database.
     *
     * @param game The game.
     */
    public void updateGame(GameEntity game) {
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
        }
        getEntityManager().persist(game);
        getEntityManager().getTransaction().commit();
    }

    /**
     * Finds a player given its username.
     *
     * @param username The username.
     * @return The player entity or <code>null</code> if not found.
     */
    public PlayerEntity findPlayer(String username) {
        return getEntityManager().find(PlayerEntity.class, username);
    }

    /**
     * Updates a player in underlying database.
     *
     * @param player The player.
     */
    public void updatePlayer(PlayerEntity player) {
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
        }
        getEntityManager().persist(player);
        getEntityManager().getTransaction().commit();
    }

    /**
     * Deletes a player from underlying database.
     *
     * @param username The username.
     */
    public void deletePlayer(String username) {
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
        }
        getEntityManager().remove(new PlayerEntity(username));
        getEntityManager().getTransaction().commit();
    }
}
