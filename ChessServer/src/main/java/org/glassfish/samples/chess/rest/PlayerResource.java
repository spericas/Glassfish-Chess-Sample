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

package org.glassfish.samples.chess.rest;

import javax.json.JsonArray;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.NotFoundException;

import org.glassfish.samples.chess.persistence.PlayerEntity;
import org.glassfish.samples.chess.persistence.ChessServerDao;
import javax.ws.rs.ForbiddenException;

/**
 * PlayerResource class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
@Path("player")
public class PlayerResource {

    private ChessServerDao db = new ChessServerDao();

    @GET
    @Produces("application/json")
    public JsonArray getPlayers() {
        // TODO: Return all players in a JSON array
        throw new NotFoundException("Not implemented");
    }

    @GET
    @Path("{username}")
    @Produces("application/json")
    @Consumes("application/json")
    public PlayerEntity getPlayer(@PathParam("username") String username) {
        final PlayerEntity player = db.findPlayer(username);
        if (player == null) {
            throw new NotFoundException("Unable to find user " + username);
        }
        return player;
    }

    @PUT
    @Path("{username}")
    @Consumes("application/json")
    public void putPlayer(@PathParam("username") String username, PlayerEntity player) {
        final PlayerEntity p = db.findPlayer(username);
        if (p == null) {
            throw new NotFoundException("Unable to find user " + username);
        }
        db.updatePlayer(player);
    }

    @POST
    @Path("{username}")
    @Consumes("application/json")
    public void postPlayer(@PathParam("username") String username, PlayerEntity player) {
        final PlayerEntity p = db.findPlayer(username);
        if (p != null) {
            throw new ForbiddenException("User already exists, use PUT to update " + username);
        }
        db.updatePlayer(player);
    }

    @DELETE
    @Path("{username}")
    public void deletePlayer(@PathParam("username") String username) {
        final PlayerEntity p = db.findPlayer(username);
        if (p == null) {
            throw new NotFoundException("Unable to find user " + username);
        }
        db.deletePlayer(username);
    }
}
