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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import java.lang.reflect.Method;

import org.glassfish.samples.chess.persistence.PlayerEntity;

/**
 * PlayerReaderWriter class.
 *
 * @author Santiago.Pericas-Geertsen@oracle.com
 */
public class PlayerReaderWriter implements MessageBodyReader, MessageBodyWriter {

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(PlayerEntity.class) && mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        final JsonParser parser = Json.createParser(entityStream);
        if (parser.next() != Event.START_OBJECT) {
            throw new IOException("Expected JSON object");
        }
        final PlayerEntity player = new PlayerEntity();
        while (parser.hasNext()) {
            Event e = parser.next();
            if (e == Event.END_OBJECT) {
                break;
            }
            if (e == Event.KEY_NAME) {
                final String name = parser.getString();
                e = parser.next();
                if (e == Event.VALUE_STRING) {
                    try {
                        final String methodName = "set" + Character.toUpperCase(name.charAt(0))
                                + name.substring(1);
                        final Method method = player.getClass().getMethod(methodName, String.class);
                        method.invoke(player, parser.getString());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return player;
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(PlayerEntity.class) && mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public long getSize(Object t, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1L;
    }

    @Override
    public void writeTo(Object t, Class type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        final PlayerEntity player = (PlayerEntity) t;
        final JsonGenerator gen = Json.createGenerator(entityStream);
        gen.writeStartObject();
        gen.write("username", player.getUsername());
        gen.write("password", player.getPassword());
        gen.write("nickname", player.getNickname());
        gen.writeEnd();
        gen.close();
    }
}
