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
     * @see <a href="https://github.com/jsonurl/specification#23-objects"
     * >JSON&#x2192;URL specification, section 2.3</a>
     */
    START_OBJECT,
    /**
     * End of a JSON&#x2192;URL object.
     * @see <a href="https://github.com/jsonurl/specification#23-objects"
     * >JSON&#x2192;URL specification, section 2.3</a>
     */
    END_OBJECT,
    /**
     * Start of a JSON&#x2192;URL array.
     * @see <a href="https://github.com/jsonurl/specification#24-arrays"
     * >JSON&#x2192;URL specification, section 2.4</a>
     */
    START_ARRAY,
    /**
     * End of a JSON&#x2192;URL array.
     * @see <a href="https://github.com/jsonurl/specification#24-arrays"
     * >JSON&#x2192;URL specification, section 2.4</a>
     */
    END_ARRAY,
    /**
     * A JSON&#x2192;URL object key name.
     * @see <a href="https://github.com/jsonurl/specification#23-objects"
     * >JSON&#x2192;URL specification, section 2.3</a>
     */
    KEY_NAME,
    /**
     * A literal {@code false}.
     * @see <a href="https://github.com/jsonurl/specification#21-values"
     * >JSON&#x2192;URL specification, section 2.1</a>
     */
    VALUE_FALSE,
    /**
     * A literal {@code true}.
     * @see <a href="https://github.com/jsonurl/specification#21-values"
     * >JSON&#x2192;URL specification, section 2.1</a>
     */
    VALUE_TRUE,
    /**
     * A literal {@code null}.
     * @see <a href="https://github.com/jsonurl/specification#21-values"
     * >JSON&#x2192;URL specification, section 2.1</a>
     */
    VALUE_NULL,
    /**
     * A number literal.
     * @see <a href="https://github.com/jsonurl/specification#26-numbers"
     * >JSON&#x2192;URL specification, section 2.6</a>
     */
    VALUE_NUMBER,
    /**
     * A string literal.
     * @see <a href="https://github.com/jsonurl/specification#25-strings"
     * >JSON&#x2192;URL specification, section 2.5</a>
     * @see <a href="https://github.com/jsonurl/specification#296-address-bar-query-string-friendly"
     * >JSON&#x2192;URL specification, section 2.9.6</a>
     */
    VALUE_STRING,
    /**
     * The empty composite.
     * @see <a href="https://github.com/jsonurl/specification#22-composites"
     * >JSON&#x2192;URL specification, section 2.2</a>
     */
    VALUE_EMPTY_COMPOSITE,
    /**
     * The empty literal value (e.g. zero-length literal).
     */
    VALUE_EMPTY_LITERAL,
    /**
     * A missing value (e.g. object key name with no
     * following separator and value).
     * @see <a href="https://github.com/jsonurl/specification#294-implied-object-missing-values"
     * >JSON&#x2192;URL specification, section 2.9.4</a>
     */
    VALUE_MISSING,
    /**
     * The end-of-stream marker.
     */
    END_STREAM
}