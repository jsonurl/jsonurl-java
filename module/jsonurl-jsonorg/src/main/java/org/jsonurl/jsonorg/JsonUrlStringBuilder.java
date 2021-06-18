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

import java.io.IOException;
import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;

/**
 * An {@link Appendable} that can write json.org datatypes
 * as JSON&#x2192;URL text.
 * 
 * @see JsonUrlWriter
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
@SuppressWarnings("java:S2176") // See SuppressWarnings.md
public class JsonUrlStringBuilder extends org.jsonurl.text.JsonUrlStringBuilder {

    /**
     * Create a new JsonUrlStringBuilder.
     *
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, CompositeType, Set)
     * JsonUrlStringBuilder(new StringBuilder(1024), null, null)}.
     */
    public JsonUrlStringBuilder() {
        super();
    }

    /**
     * Create a new JsonUrlStringBuilder.
     *
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, JsonUrlOption, JsonUrlOption...)
     * JsonUrlStringBuilder(new StringBuilder(1024, first, rest)}.
     *
     * @param first first option
     * @param rest rest of the options
     */
    public JsonUrlStringBuilder(JsonUrlOption first, JsonUrlOption... rest) {
        super(first, rest);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     *
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, CompositeType, Set) 
     * JsonUrlStringBuilder(new StringBuilder(1024), null, options)}.
     */
    public JsonUrlStringBuilder(Set<JsonUrlOption> options) {
        super(options);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     *
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, CompositeType, Set)
     * JsonUrlStringBuilder(new StringBuilder(1024), type, options)}.
     */
    public JsonUrlStringBuilder(
            CompositeType type,
            Set<JsonUrlOption> options) {
        super(new StringBuilder(DEFAULT_SIZE), type, options);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * 
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, CompositeType, Set)
     * JsonUrlStringBuilder(new StringBuilder(size), null, null)}.
     */
    public JsonUrlStringBuilder(int size) {
        super(new StringBuilder(size), null, null);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     * 
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, CompositeType, Set)
     * JsonUrlStringBuilder(new StringBuilder(size), null, options)}.
     */
    public JsonUrlStringBuilder(int size, Set<JsonUrlOption> options) {
        super(new StringBuilder(size), null, options);
    }

    /**
     * Create a new JsonUrlStringBuilder.
     *
     * <p>This simply calls {@link
     * #JsonUrlStringBuilder(StringBuilder, CompositeType, Set)
     * JsonUrlStringBuilder(dest, null, EnumSet.of(first,last))}.
     */
    public JsonUrlStringBuilder(
            StringBuilder dest,
            JsonUrlOption first,
            JsonUrlOption... rest) {
        super(dest, null, JsonUrlOption.newSet(first, rest));
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
    }

    @Override
    public JsonUrlStringBuilder add(Object value) throws IOException {
        JsonUrlWriter.write(this, options(), value);
        return this;
    }
}
