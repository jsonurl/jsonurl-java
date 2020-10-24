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
 * An interface for common input/output options.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-10-21
 */
public interface JsonUrlOptions {

    /**
     * Test if the wfu-composite/wfu-implied-composite option is enabled.
     * If this option is enabled then application/x-www-form-urlencoded
     * style separators are allowed for an implied top-level object or
     * array.
     * @return true if enabled
     */
    default boolean isFormUrlEncoded() {
        return false;
    }

    /**
     * Test if the wfu-composite/wfu-implied-composite option is enabled,
     * supplying the default value if necessary.
     * @param opt a valid JsonUrlOptions or null
     * @see #isFormUrlEncoded()
     */
    static boolean isFormUrlEncoded(JsonUrlOptions opt) {
        return opt != null && opt.isFormUrlEncoded() || false;
    }

    /**
     * Test if the implied-string-literals option is enabled.
     * If this option is enabled then all literals are assumed to be strings.
     * @return true if enabled
     */
    default boolean isImpliedStringLiterals() {
        return false;
    }

    /**
     * Test if the implied-string-literals option is enabled,
     * supplying the default value if necessary.
     * @param opt a valid JsonUrlOptions or null
     * @see #isImpliedStringLiterals()
     */
    static boolean isImpliedStringLiterals(JsonUrlOptions opt) {
        return opt != null && opt.isImpliedStringLiterals() || false;
    }
    
    /**
     * Test if the empty-unquoted-key option is enabled.
     * If this option is enabled then the empty string may
     * be represented via a zero length value. Otherwise,
     * two single quotes must be used: {@code ''}.
     * @return true if enabled
     */
    default boolean isEmptyUnquotedKeyAllowed() {
        return false;
    }

    /**
     * Test if the empty-unquoted-key option is enabled,
     * supplying the default value if necessary.
     * @param opt a valid JsonUrlOptions or null
     * @see #isEmptyUnquotedKeyAllowed()
     */
    static boolean isEmptyUnquotedKeyAllowed(JsonUrlOptions opt) {
        return opt != null && opt.isEmptyUnquotedKeyAllowed() || false;
    }

    /**
     * Test if the empty-unquoted-value option is enabled.
     * If this option is enabled then the empty string may
     * be represented via a zero length key. Otherwise,
     * two single quotes must be used: {@code ''}.
     * @return true if enabled
     */
    default boolean isEmptyUnquotedValueAllowed() {
        return false;
    }

    /**
     * Test if the empty-unquoted-value option is enabled,
     * supplying the default value if necessary.
     * @param opt a valid JsonUrlOptions or null
     * @see #isEmptyUnquotedValueAllowed()
     */
    static boolean isEmptyUnquotedValueAllowed(JsonUrlOptions opt) {
        return opt != null && opt.isEmptyUnquotedValueAllowed() || false;
    }
    
    /**
     * Test if the skip-nulls option is enabled.
     * If this option is enabled then {@code null} values in the
     * input or output will be removed.
     * @return true if enabled
     */
    default boolean isSkipNulls() {
        return false;
    }

    /**
     * Test if the skip-nulls option is enabled,
     * supplying the default value if necessary.
     * @param opt a valid JsonUrlOptions or null
     * @see #isSkipNulls()
     */
    static boolean isSkipNulls(JsonUrlOptions opt) {
        return opt != null && opt.isSkipNulls() || false;
    }

    /**
     * Test if the coerce-null-to-empty-string option is enabled.
     * If this option is enabled then {@code null} values in the
     * input or output will be replaced with an empty string.
     * @return true if enabled
     */
    default boolean isCoerceNullToEmptyString() {
        return false;
    }

    /**
     * Test if the coerce-null-to-empty-string option is enabled,
     * supplying the default value if necessary.
     * @param opt a valid JsonUrlOptions or null
     * @see #isCoerceNullToEmptyString()
     */
    static boolean isCoerceNullToEmptyString(JsonUrlOptions opt) {
        return opt != null && opt.isCoerceNullToEmptyString() || false;
    }

    /**
     * Cast an object to JsonUrlOptions.
     * @param obj a valid object or null
     * @return {@code obj} if it is an instance of JsonUrlOptions; otherwise,
     *     {@code null}.
     */
    static JsonUrlOptions fromObject(Object obj) {
        if (obj instanceof JsonUrlOptions) {
            return (JsonUrlOptions)obj;
        }
        
        return null;
    }
}
