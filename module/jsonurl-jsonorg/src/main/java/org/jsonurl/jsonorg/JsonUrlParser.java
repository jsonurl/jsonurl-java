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

package org.jsonurl.jsonorg;

import java.util.EnumSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.factory.ValueFactoryParser;

/**
 * A <a href="https://jsonurl.org/">JSON&#x2192;URL</a> parser bound to
 * Douglas Crockford's original Java implementation of JSON.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 * @see <a href="https://www.json.org/">json.org</a>
 * @see <a href="https://github.com/stleary/JSON-java">GitHub</a>
 * @see <a href="https://mvnrepository.com/artifact/org.json/json">Maven</a>
 */
public class JsonUrlParser extends ValueFactoryParser.TransparentBuilder<
        Object,
        Object,
        JSONArray,
        JSONObject,
        Boolean,
        Number,
        Object,
        String> {

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser() {
        super(JsonOrgValueFactory.PRIMITIVE, null, null);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(JsonUrlLimits limits, Set<JsonUrlOption> options) {
        super(JsonOrgValueFactory.PRIMITIVE, limits, options);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(JsonUrlLimits limits) {
        super(JsonOrgValueFactory.PRIMITIVE, limits, null);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(Set<JsonUrlOption> options) {
        super(JsonOrgValueFactory.PRIMITIVE, null, options);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(
            JsonUrlLimits limits,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(JsonOrgValueFactory.PRIMITIVE, limits, first, rest);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(JsonOrgValueFactory.PRIMITIVE, null, first, rest);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH64
     * @see JsonOrgValueFactory#BIGMATH128
     */
    public JsonUrlParser(JsonOrgValueFactory factory) {
        super(factory, null, null);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH64
     * @see JsonOrgValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JsonOrgValueFactory factory,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {
        super(factory, limits, options);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH64
     * @see JsonOrgValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JsonOrgValueFactory factory,
            Set<JsonUrlOption> options) {
        super(factory, null, options);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH64
     * @see JsonOrgValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JsonOrgValueFactory factory,
            JsonUrlLimits limits) {
        super(factory, limits, null);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH64
     * @see JsonOrgValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JsonOrgValueFactory factory,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(factory, null, first, rest);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH64
     * @see JsonOrgValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JsonOrgValueFactory factory,
            JsonUrlLimits limits,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        super(factory, limits, EnumSet.of(first, rest));
    }
}
