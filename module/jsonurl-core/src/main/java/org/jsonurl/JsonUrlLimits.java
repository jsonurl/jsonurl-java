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

package org.jsonurl;

/**
 * JSON&#x2192;URL text parse limits.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-10-01
 */
public interface JsonUrlLimits {

    /**
     * Default {@link #getMaxParseChars()}.
     */
    int DEFAULT_MAX_PARSE_CHARS = 1 << 13;

    /**
     * Default {@link #getMaxParseDepth()}.
     */
    int DEFAULT_MAX_PARSE_DEPTH = 1 << 4;

    /**
     * Default {@link #getMaxParseValues()}.
     */
    int DEFAULT_MAX_PARSE_VALUES = 1 << 10;

    /**
     * JSON&#x2192;URL text limit builder. An instance of this interface
     * may be used to build {@link JsonUrlLimits}.
     */
    interface Builder {
        /**
         * Set the maximum number of parsed characters.
         */
        Builder addMaxParseChars(long maxParseChars);

        /**
         * Set the maximum parse depth.
         */
        Builder addMaxParseDepth(int maxParseDepth);

        /**
         * Set the maximum number of parsed values.
         */
        Builder addMaxParseValues(int maxParseValues);

        /**
         * Build a JsonUrlLimits object.
         */
        JsonUrlLimits build();
    }

    /**
     * Get the maximum number of parsed characters.
     * This provides a limit on the maximum number of characters that will
     * be parsed before throwing an exception. The default value is
     * {@link #DEFAULT_MAX_PARSE_CHARS}.
     */
    long getMaxParseChars();

    /**
     * Get the maximum number of parsed characters, providing the
     * default if necessary.
     * @param limits a valid JsonUrlLimits or {@code null}.
     * @see #getMaxParseChars()
     */
    static long getMaxParseChars(JsonUrlLimits limits) {
        return limits == null
                ? DEFAULT_MAX_PARSE_CHARS
                : limits.getMaxParseChars();
    }

    /**
     * Get the maximum parse depth.
     * This provides a limit on the depth of a JSON&#x2192;URL object/array
     * before an exception of thrown. The default value is
     * {@link #DEFAULT_MAX_PARSE_DEPTH}.
     */
    int getMaxParseDepth();

    /**
     * Get the maximum number of parsed depth, providing the
     * default if necessary.
     * @param limits a valid JsonUrlLimits or {@code null}.
     * @see #getMaxParseDepth()
     */
    static int getMaxParseDepth(JsonUrlLimits limits) {
        return limits == null
                ? DEFAULT_MAX_PARSE_DEPTH
                : limits.getMaxParseDepth();
    }

    /**
     * Set the maximum number of parsed values.
     * This provides a limit on the number of values that will be
     * parsed/instantiated before an exception is thrown. The default value is
     * {@link #DEFAULT_MAX_PARSE_VALUES}.
     */
    int getMaxParseValues();

    /**
     * Get the maximum number of parsed values, providing the
     * default if necessary.
     * @param limits a valid JsonUrlLimits or {@code null}.
     * @see #getMaxParseValues()
     */
    static int getMaxParseValues(JsonUrlLimits limits) {
        return limits == null
                ? DEFAULT_MAX_PARSE_VALUES
                : limits.getMaxParseValues();
    }

    /**
     * Create a new limit builder.
     */
    static Builder builder() {
        return new LimitBuilder();
    }
    
    /**
     * Make a copy of these limits.
     */
    default JsonUrlLimits copy() {
        return copy(this);
    }

    /**
     * Make a copy of the given limits.
     * @param limits source
     */
    static JsonUrlLimits copy(JsonUrlLimits limits) {
        final Limits ret = new Limits();

        if (limits != null) {
            ret.setMaxParseChars(limits.getMaxParseChars());
            ret.setMaxParseDepth(limits.getMaxParseDepth());
            ret.setMaxParseValues(limits.getMaxParseValues());
        }
        
        return ret;
    }
}
