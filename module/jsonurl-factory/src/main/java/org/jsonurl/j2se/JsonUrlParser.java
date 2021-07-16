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

package org.jsonurl.j2se;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.factory.ValueFactoryParser;

/**
 * A {@link org.jsonurl.factory.Parser Parser} bound to Java SE data types.
 *
 * <p>See {@link org.jsonurl.factory.ValueFactoryParser ValueFactoryParser}
 * for a note regarding concurrency.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonUrlParser extends ValueFactoryParser.TransparentBuilder<
        Object,
        Object,
        List<Object>,
        Map<String,Object>,
        Boolean,
        Number,
        Object,
        String> {
    
    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JavaValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser() {
        super(JavaValueFactory.PRIMITIVE, null, null);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JavaValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(JsonUrlLimits limits, Set<JsonUrlOption> options) {
        super(JavaValueFactory.PRIMITIVE, limits, options);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JavaValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(JsonUrlLimits limits) {
        super(JavaValueFactory.PRIMITIVE, limits, null);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JavaValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(Set<JsonUrlOption> options) {
        super(JavaValueFactory.PRIMITIVE, null, options);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JavaValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(
            JsonUrlLimits limits,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(JavaValueFactory.PRIMITIVE, limits, first, rest);
    }

    /**
     * Instantiate a new Parser. The returned parser uses the
     * {@link JavaValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser(
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(JavaValueFactory.PRIMITIVE, null, first, rest);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JavaValueFactory#PRIMITIVE
     * @see JavaValueFactory#DOUBLE
     * @see JavaValueFactory#BIGMATH64
     * @see JavaValueFactory#BIGMATH128
     */
    public JsonUrlParser(JavaValueFactory factory) {
        super(factory, null, null);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JavaValueFactory#PRIMITIVE
     * @see JavaValueFactory#DOUBLE
     * @see JavaValueFactory#BIGMATH64
     * @see JavaValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JavaValueFactory factory,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {
        super(factory, limits, options);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JavaValueFactory#PRIMITIVE
     * @see JavaValueFactory#DOUBLE
     * @see JavaValueFactory#BIGMATH64
     * @see JavaValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JavaValueFactory factory,
            Set<JsonUrlOption> options) {
        super(factory, null, options);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JavaValueFactory#PRIMITIVE
     * @see JavaValueFactory#DOUBLE
     * @see JavaValueFactory#BIGMATH64
     * @see JavaValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JavaValueFactory factory,
            JsonUrlLimits limits) {
        super(factory, limits, null);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JavaValueFactory#PRIMITIVE
     * @see JavaValueFactory#DOUBLE
     * @see JavaValueFactory#BIGMATH64
     * @see JavaValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JavaValueFactory factory,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(factory, null, first, rest);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JavaValueFactory#PRIMITIVE
     * @see JavaValueFactory#DOUBLE
     * @see JavaValueFactory#BIGMATH64
     * @see JavaValueFactory#BIGMATH128
     */
    public JsonUrlParser(
            JavaValueFactory factory,
            JsonUrlLimits limits,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        super(factory, limits, EnumSet.of(first, rest));
    }
}
