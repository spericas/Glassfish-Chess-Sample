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

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import org.glassfish.samples.chess.websocket.ChessServerEndpoint;

/**
 * Class Player.
 *
 * @author Daniel.Guo@oracle.com
 * @author Santiago.PericasGeertsen@oracle.com
 */
@Entity(name="Player")
public class PlayerEntity implements Serializable {

    private String username;

    private String password;

    private String nickname;

    private ChessServerEndpoint endpoint;

    public PlayerEntity() {
        final UUID uuid = UUID.randomUUID();
        username = password = "player_" + uuid.toString().substring(0, 8);
    }

    public PlayerEntity(String username) {
        this(username, null, null);
    }

    public PlayerEntity(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    @Id
    @NotNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Transient
    public ChessServerEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(ChessServerEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isOnline() {
        return endpoint != null;
    }

    public boolean isOnline(ChessServerEndpoint endpoint) {
        return this.endpoint == endpoint;
    }

    public void setOffline() {
        endpoint = null;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.username);
        return hash;
    }

    /**
     * Player identity is based on <code>username</code>, unless this is an
     * anonymous player in which case memory location is used.
     *
     * @param obj Other object.
     * @return Result of test.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlayerEntity other = (PlayerEntity) obj;
        return username.equals(other.username);
    }
}
