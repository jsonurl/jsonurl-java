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

package org.jsonurl.stream;

/**
 * Enumeration of JSON&#x2192;URL events.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-10-21
 */
public enum JsonUrlEvent {
    /**
     * Start of a JSON&#x2192;URL object.
     */
    START_OBJECT,
    /**
     * End of a JSON&#x2192;URL object.
     */
    END_OBJECT,
    /**
     * Start of a JSON&#x2192;URL array.
     */
    START_ARRAY,
    /**
     * End of a JSON&#x2192;URL array.
     */
    END_ARRAY,
    /**
     * A JSON&#x2192;URL object key name.
     */
    KEY_NAME,
    /**
     * A literal {@code false}.
     */
    VALUE_FALSE,
    /**
     * A literal {@code true}.
     */
    VALUE_TRUE,
    /**
     * A literal {@code null}.
     */
    VALUE_NULL,
    /**
     * A number literal.
     */
    VALUE_NUMBER,
    /**
     * A string literal.
     */
    VALUE_STRING,
    /**
     * The empty composite.
     */
    VALUE_EMPTY_COMPOSITE,
    /**
     * The empty literal value (e.g. zero-length literal).
     */
    VALUE_EMPTY_LITERAL,
    /**
     * A missing value (e.g. object key name with no
     * following separator and value).
     */
    VALUE_MISSING,
    /**
     * The end-of-stream marker.
     */
    END_STREAM
}