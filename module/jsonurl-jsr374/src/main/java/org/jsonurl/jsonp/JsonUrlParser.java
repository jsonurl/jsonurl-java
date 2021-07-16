/*
 * Copyright 2019-2020 David MacCormack
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
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

package org.jsonurl.jsonp;

import java.util.EnumSet;
import java.util.Set;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.factory.ValueFactoryParser;

/**
 * A <a href="https://jsonurl.org/">JSON&#x2192;URL</a> parser bound to the
 * JSON-P interface defined by JSR-374.
 *
 * <p>See {@link org.jsonurl.factory.ValueFactoryParser ValueFactoryParser}
 * for a note regarding concurrency.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 * @see <a href="https://javaee.github.io/jsonp/">About</a>
 * @see <a href="https://github.com/javaee/jsonp/">GitHub</a>
 * @see <a href=
 * "https://mvnrepository.com/artifact/org.glassfish/javax.json">Maven</a>
 */
public class JsonUrlParser extends ValueFactoryParser<
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
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonpValueFactory#BIGMATH64 BIGMATH64} factory.
     */
    public JsonUrlParser() {
        super(JsonpValueFactory.BIGMATH64, null, null);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonpValueFactory#BIGMATH64 BIGMATH64} factory.
     */
    public JsonUrlParser(JsonUrlLimits limits, Set<JsonUrlOption> options) {
        super(JsonpValueFactory.BIGMATH64, limits, options);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonpValueFactory#BIGMATH64 BIGMATH64} factory.
     */
    public JsonUrlParser(JsonUrlLimits limits) {
        super(JsonpValueFactory.BIGMATH64, limits, null);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonpValueFactory#BIGMATH64 BIGMATH64} factory.
     */
    public JsonUrlParser(Set<JsonUrlOption> options) {
        super(JsonpValueFactory.BIGMATH64, null, options);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonpValueFactory#BIGMATH64 BIGMATH64} factory.
     */
    public JsonUrlParser(
            JsonUrlLimits limits,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(JsonpValueFactory.BIGMATH64, limits, first, rest);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonpValueFactory#BIGMATH64 BIGMATH64} factory.
     */
    public JsonUrlParser(
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(JsonpValueFactory.BIGMATH64, null, first, rest);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonpValueFactory
     * @see JsonpValueFactory#BIGMATH128
     * @see JsonpValueFactory#BIGMATH64
     * @see JsonpValueFactory#BIGMATH32
     * @see JsonpValueFactory#DOUBLE
     * @see JsonpValueFactory#PRIMITIVE
     */
    public JsonUrlParser(JsonpValueFactory factory) {
        super(factory, null, null);
    }
    
    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonpValueFactory
     * @see JsonpValueFactory#BIGMATH128
     * @see JsonpValueFactory#BIGMATH64
     * @see JsonpValueFactory#BIGMATH32
     * @see JsonpValueFactory#DOUBLE
     * @see JsonpValueFactory#PRIMITIVE
     */
    public JsonUrlParser(
            JsonpValueFactory factory,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {
        super(factory, limits, options);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonpValueFactory
     * @see JsonpValueFactory#BIGMATH128
     * @see JsonpValueFactory#BIGMATH64
     * @see JsonpValueFactory#BIGMATH32
     * @see JsonpValueFactory#DOUBLE
     * @see JsonpValueFactory#PRIMITIVE
     */
    public JsonUrlParser(
            JsonpValueFactory factory,
            Set<JsonUrlOption> options) {
        super(factory, null, options);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonpValueFactory
     * @see JsonpValueFactory#BIGMATH128
     * @see JsonpValueFactory#BIGMATH64
     * @see JsonpValueFactory#BIGMATH32
     * @see JsonpValueFactory#DOUBLE
     * @see JsonpValueFactory#PRIMITIVE
     */
    public JsonUrlParser(
            JsonpValueFactory factory,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        super(factory, null, EnumSet.of(first, rest));
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonpValueFactory
     * @see JsonpValueFactory#BIGMATH128
     * @see JsonpValueFactory#BIGMATH64
     * @see JsonpValueFactory#BIGMATH32
     * @see JsonpValueFactory#DOUBLE
     * @see JsonpValueFactory#PRIMITIVE
     */
    public JsonUrlParser(
            JsonpValueFactory factory,
            JsonUrlLimits limits) {
        super(factory, limits, null);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonpValueFactory
     * @see JsonpValueFactory#BIGMATH128
     * @see JsonpValueFactory#BIGMATH64
     * @see JsonpValueFactory#BIGMATH32
     * @see JsonpValueFactory#DOUBLE
     * @see JsonpValueFactory#PRIMITIVE
     */
    public JsonUrlParser(
            JsonpValueFactory factory,
            JsonUrlLimits limits,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        super(factory, limits, EnumSet.of(first, rest));
    }
}
