package org.jsonurl.jsonp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.jsonurl.AbstractParseTest;

/**
 * Abstract base class for parser tests.
 *
 * <p>A specialization of this class may be created for each
 * factory constant defined in {@link JsonOrgValueFactory}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
abstract class JsonpParseTest extends AbstractParseTest<
        JsonValue,
        JsonStructure,
        JsonArrayBuilder,
        JsonArray,
        JsonObjectBuilder,
        JsonObject,
        JsonValue,
        JsonNumber,
        JsonValue,
        JsonString> {

    /**
     * Create a new JsonpParseTest.
     */
    public JsonpParseTest(JsonpValueFactory factory) {
        super(factory);
    }
    
    @Override
    protected JsonArray getArray(String key, JsonObject value) {
        return (JsonArray)value.get(key);
    }

    @Override
    protected JsonArray getArray(int index, JsonArray value) {
        return (JsonArray)value.get(index);
    }

    @Override
    protected JsonObject getObject(int index, JsonArray value) {
        return (JsonObject)value.get(index);
    }
    
    @Override
    protected JsonObject getObject(String key, JsonObject value) {
        return (JsonObject)value.get(key);
    }

    @Override
    protected JsonNumber getNumber(int index, JsonArray value) {
        return (JsonNumber)value.get(index);
    }
    
    @Override
    protected JsonNumber getNumber(String key, JsonObject value) {
        return (JsonNumber)value.get(key);
    }

    @Override
    protected String getString(String key, JsonObject value) {
        JsonValue ret = value.get(key);
        return ((JsonString)ret).getString();
    }
    
    @Override
    protected boolean getBoolean(String key, JsonObject value) {
        Object ret = value.get(key);

        if (ret == factory.getTrue()) {
            return true;
        }

        if (ret == factory.getFalse()) {
            return false;
        }

        throw new IllegalArgumentException("value not boolean: " + ret);
    }
    
    @Override
    protected boolean getNull(String key, JsonObject value) {
        Object ret = value.get(key);
        return factory.getNull() == ret;
    }
    
    @Override
    protected boolean getEmptyComposite(String key, JsonObject value) {
        return factory.isEmpty(value.get(key));
    }
}
