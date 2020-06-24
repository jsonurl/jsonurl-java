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

import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import org.jsonurl.AbstractWriteTest;
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.Parser;

/**
 * Unit test for writing JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonpWriteTest extends AbstractWriteTest<
        JsonValue,
        JsonStructure,
        JsonArray,
        JsonObject> {


    @Override
    public JsonArray newArray(String s) {
        try (JsonReader r = Json.createReader(new StringReader(s))) {
            return r.readArray();
        }
    }

    @Override
    public JsonObject newObject(String s) {
        try (JsonReader r = Json.createReader(new StringReader(s))) {
            return r.readObject();
        }
    }

    @Override
    public Parser<
            JsonValue,
            JsonStructure,
            ?,
            JsonArray,
            ?,
            JsonObject,?,?,?,?> newParser() {
        return new JsonUrlParser();
    }

    @Override
    public String valueToString(JsonValue value) {
        StringWriter out = new StringWriter();
        try (JsonGenerator generator = Json.createGenerator(out)) {
            generator.write(value);
            generator.flush();
            generator.close();
        }
        return out.toString();
    }

    @Override
    public void write(JsonTextBuilder<?, ?> out, JsonValue value)
            throws Exception {
        JsonUrlWriter.write(out, value);
    }
}
