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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.glassfish.samples.chess.model.Color;
import java.lang.reflect.ParameterizedType;

/**
 * Message class.
 *
 */
public abstract class Message {

    // Create JSON writer factory with pretty printing enabled
    private static final Map<String, Boolean> config;
    private static final JsonWriterFactory factory;
    static {
        config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
        factory = Json.createWriterFactory(config);
    }

    protected String msg;
    
    protected String gameId;

    protected Color color;
    
    private String username;

    private String password;

    public Message() {
        msg = getClass().getSimpleName();
    }

    public Message(String gameId) {
        msg = getClass().getSimpleName();
        this.gameId = gameId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public boolean hasGameId() {
        return gameId != null;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean hasColor() {
        return color != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean hasUsername() {
        return username != null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Message readFrom(JsonObject jobj) {
        readFrom(this, jobj);
        return this;
    }

    private static void readFrom(Object object, JsonObject jobj) {
        try {
            for (PropertyDescriptor pd
                    : Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors()) {
                final Method m = pd.getWriteMethod();
                if (m != null) {
                    JsonValue jv = jobj.get(pd.getName());
                    if (jv == null) {
                        continue;
                    }
                    Class<?> clazz = m.getParameterTypes()[0];
                    switch (jv.getValueType()) {
                        case NULL:
                            break;
                        case STRING:
                            final String sv = ((JsonString) jv).getString();
                            if (clazz.isEnum()) {
                                m.invoke(object, Enum.valueOf((Class<? extends Enum>) clazz, sv));
                            } else {
                                m.invoke(object, sv);
                            }
                            break;
                        case NUMBER:
                            m.invoke(object, ((JsonNumber) jv).intValue());
                            break;
                        case TRUE:
                            m.invoke(object, true);
                            break;
                        case FALSE:
                            m.invoke(object, false);
                            break;
                        case OBJECT:
                            Object instance = clazz.newInstance();
                            readFrom(instance, (JsonObject) jv);
                            m.invoke(object, instance);
                            break;
                        case ARRAY:     // only array of strings and objects supported!
                            final JsonArray ja = (JsonArray) jv;
                            final List<Object> list = new ArrayList<>(ja.size());
                            for (JsonValue v : ja) {
                                if (v instanceof JsonString) {
                                    list.add(((JsonString) v).getString());
                                } else {
                                    ParameterizedType pt = (ParameterizedType) m.getGenericParameterTypes()[0];
                                    clazz = (Class<?>) pt.getActualTypeArguments()[0];
                                    instance = clazz.newInstance();
                                    readFrom(instance, (JsonObject) v);
                                    list.add(instance);
                                }
                            }
                            m.invoke(object, list);
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported type " + jv.getValueType());
                    }
                }
            }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeTo(JsonObjectBuilder jobj) {
        writeTo(this, jobj);
    }

    private static void writeTo(Object object, JsonObjectBuilder jobj) {
        try {
            for (PropertyDescriptor pd
                    : Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors()) {
                final Method m = pd.getReadMethod();
                if (m != null) {
                    Object v = m.invoke(object);
                    if (v == null) {
                        continue;
                    } else if (v instanceof String) {
                        jobj.add(pd.getName(), (String) v);
                    } else if (v instanceof Integer) {
                        jobj.add(pd.getName(), (Integer) v);
                    } else if (v instanceof Boolean) {
                        jobj.add(pd.getName(), (Boolean) v);
                    } else if (v instanceof Enum) {
                        jobj.add(pd.getName(), ((Enum) v).toString());
                    } else if (v instanceof List) {     // only list of strings or objects supported!
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        for (Object o : (List) v) {
                            if (o instanceof String) {
                                jab.add((String) o);
                            } else {
                                JsonObjectBuilder njobj = Json.createObjectBuilder();
                                writeTo(o, njobj);
                                jab.add(njobj);
                            }
                        }
                        jobj.add(pd.getName(), jab.build());
                    } else {
                        JsonObjectBuilder newJobj = Json.createObjectBuilder();
                        writeTo(v, newJobj);
                        jobj.add(pd.getName(), newJobj.build());
                    }
                }
            }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Message createInstance(String className) {
        try {
            final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            return (Message) ccl.loadClass("org.glassfish.samples.chess.protocol." + className).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public abstract Message processMe(ServerMessageProcessor processor);

    public MessageRsp newResponse() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String toString() {
        final JsonObjectBuilder jobj = Json.createObjectBuilder();
        writeTo(jobj);
        final StringWriter sw = new StringWriter();
        try (JsonWriter jw = factory.createWriter(sw)) {
            jw.writeObject(jobj.build());
        }
        return sw.toString();
    }
}
