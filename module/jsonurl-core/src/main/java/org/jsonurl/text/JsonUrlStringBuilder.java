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

package org.jsonurl.text;

import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;

/**
 * A {@link JsonUrlTextAppender} that appends JSON&#x2192;URL text to a
 * StringBuilder. Like {@link java.lang.StringBuilder} an instance of this class is
 * not thread-safe.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonUrlStringBuilder extends
        JsonUrlTextAppender<StringBuilder,String> implements JsonStringBuilder {

    /**
     * default StringBuilder size.
     */
    protected static final int DEFAULT_SIZE = 1 << 10;

    /**
     * My StringBuilder.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder builder;

    /**
     * Create a new JsonUrlStringBuilder.
     */
    public JsonUrlStringBuilder() {
        this(new StringBuilder(DEFAULT_SIZE), null, null);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * @param options Set of JsonUrlOptions
     */
    public JsonUrlStringBuilder(Set<JsonUrlOption> options) {
        this(new StringBuilder(DEFAULT_SIZE), null, options);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * @param first First JsonUrlOption
     * @param rest zero more more additional options
     */
    public JsonUrlStringBuilder(
            JsonUrlOption first,
            JsonUrlOption... rest) {
        this(new StringBuilder(DEFAULT_SIZE),
            null,
            JsonUrlOption.newSet(first, rest));
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * @param impliedType A valid CompositeType or {@code null}
     */
    public JsonUrlStringBuilder(CompositeType impliedType) {
        this(new StringBuilder(DEFAULT_SIZE), impliedType, null);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * @param impliedType A valid CompositeType or {@code null}
     * @param options Set of JsonUrlOptions
     */
    public JsonUrlStringBuilder(
            CompositeType impliedType,
            Set<JsonUrlOption> options) {
        this(new StringBuilder(DEFAULT_SIZE), impliedType, options);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * @param dest JSON&#x2192;URL text destination
     * @param impliedType A valid CompositeType or {@code null}
     * @param options Set of JsonUrlOptions or {@code null}
     */
    public JsonUrlStringBuilder(
            StringBuilder dest,
            CompositeType impliedType,
            Set<JsonUrlOption> options) {
        super(dest, impliedType, options);
        this.builder = dest;
    }

    @Override
    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        return out.toString();
    }

    /**
     * Clear the internal buffer.
     * @return this
     */
    public JsonUrlStringBuilder clear() {
        builder.setLength(0);
        return this;
    }
}
