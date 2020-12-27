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

import java.util.EnumSet;
import java.util.Set;

/**
 * An enumeration of JSON&#x2192;URL options available to both parsing
 * and serialization.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-10-21
 */
public enum JsonUrlOption {
    /**
     * The wfu-composite/wfu-implied-composite option.
     * If this option is enabled then application/x-www-form-urlencoded
     * style separators are allowed for an implied top-level object or
     * array.
     * @see <a href="https://github.com/jsonurl/specification/#293-x-www-form-urlencoded-arrays-and-objects"
     * >JSON&#x2192;URL specification, section 2.9.3</a>
     */
    WFU_COMPOSITE,

    /**
     * The implied-string-literals option.
     * If this option is enabled then all literals are assumed to be strings.
     * @see #enableImpliedStringLiterals(Set)
     * @see <a href="https://github.com/jsonurl/specification/#292-implied-objects"
     * >JSON&#x2192;URL specification, section 2.9.2</a>
     */
    IMPLIED_STRING_LITERALS,

    /**
     * The empty-unquoted-key option.
     * If this option is enabled then the empty string may
     * be represented via a zero length key. Otherwise,
     * two single quotes must be used: {@code ''}.
     */
    EMPTY_UNQUOTED_KEY,

    /**
     * The empty-unquoted-value option.
     * If this option is enabled then the empty string may
     * be represented via a zero length value. Otherwise,
     * two single quotes must be used: {@code ''}.
     */
    EMPTY_UNQUOTED_VALUE,

    /**
     * The skip-nulls option.
     * If this option is enabled then {@code null} values in the
     * input or output will be removed.
     */
    SKIP_NULLS,

    /**
     * The coerce-null-to-empty-string option.
     * If this option is enabled then {@code null} values in the
     * input or output will be replaced with an empty string.
     */
    COERCE_NULL_TO_EMPTY_STRING,

    /**
     * Address bar query string friendly.
     */
    AQF;

    /**
     * Create an empty set of options. This is just a convenience wrapper
     * around {@link EnumSet#noneOf(Class)
     * EnumSet.noneOf}({@link JsonUrlOption}.class)
     */
    public static final Set<JsonUrlOption> newSet() {
        return EnumSet.noneOf(JsonUrlOption.class);
    }
    
    /**
     * Create a set of the given options.
     */
    public static final Set<JsonUrlOption> newSet(
            JsonUrlOption first,
            JsonUrlOption...rest) {
        return first == null ? null : EnumSet.of(first, rest);
    }

    /**
     * Use this method to enable implied string literals and common related
     * options.
     *
     * <p>This is a convenience method for:
     * <pre>
     *   setOption(Type.EMPTY_UNQUOTED_KEY);
     *   setOption(Type.EMPTY_UNQUOTED_VALUE);
     *   setOption(Type.SKIP_NULLS);
     *   setOption(Type.IMPLIED_STRING_LITERALS);
     * </pre>
     *
     * <p>If {@link #IMPLIED_STRING_LITERALS} is true but
     * {@link #EMPTY_UNQUOTED_VALUE} is false then an attempt to serialize
     * an empty string literal value will trigger an Exception because there
     * wouldn't be any way to represent it.
     *
     * <p>If {@link #IMPLIED_STRING_LITERALS} is true but
     * {@link #EMPTY_UNQUOTED_KEY} is false then an an attempt to serialize
     * an empty key will trigger an Exception because there wouldn't be any
     * way to represent it.
     *
     * <p>If {@link #IMPLIED_STRING_LITERALS} is true but
     * {@link #SKIP_NULLS} is false then an an attempt to serialize a
     * {@code null} will trigger an Exception because there wouldn't be a
     * way to represent it.
     *
     * <p>If you want the above behavior, because your data doesn't
     * support those cases and you want the code to catch them for you,
     * then set the {@link #IMPLIED_STRING_LITERALS} option directly rather
     * than using this method.
     * @see #EMPTY_UNQUOTED_KEY
     * @see #EMPTY_UNQUOTED_VALUE
     * @see #SKIP_NULLS
     * @see #IMPLIED_STRING_LITERALS
     */
    public static final Set<JsonUrlOption> enableImpliedStringLiterals(
            Set<JsonUrlOption> set) {

        set.add(EMPTY_UNQUOTED_KEY);
        set.add(EMPTY_UNQUOTED_VALUE);
        set.add(SKIP_NULLS);
        set.add(IMPLIED_STRING_LITERALS);
        return set;
    }

    /**
     * Test if the {@link #IMPLIED_STRING_LITERALS} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionWfuComposite(
            Set<JsonUrlOption> options) {
        return options != null && options.contains(WFU_COMPOSITE);
    }

    /**
     * Test if the {@link #IMPLIED_STRING_LITERALS} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionImpliedStringLiterals(
            Set<JsonUrlOption> options) {
        return options != null && options.contains(IMPLIED_STRING_LITERALS);
    }

    /**
     * Test if the {@link #EMPTY_UNQUOTED_KEY} or
     * {@link #EMPTY_UNQUOTED_VALUE} option is enabled,
     * supplying the default value if necessary.
     * If {@code isKey} is {@code true} then this method will test
     * {@link #EMPTY_UNQUOTED_KEY}; otherwise, it will test
     * {@link #EMPTY_UNQUOTED_VALUE}.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionEmptyUnquoted(
            Set<JsonUrlOption> options,
            boolean isKey) {
        return options != null && options.contains(
                isKey ? EMPTY_UNQUOTED_KEY : EMPTY_UNQUOTED_VALUE);
    }

    /**
     * Test if the {@link #EMPTY_UNQUOTED_KEY} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionEmptyUnquotedKey(
            Set<JsonUrlOption> options) {
        return options != null && options.contains(EMPTY_UNQUOTED_KEY);
    }

    /**
     * Test if the {@link #EMPTY_UNQUOTED_VALUE} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionEmptyUnquotedValue(
            Set<JsonUrlOption> options) {
        return options != null && options.contains(EMPTY_UNQUOTED_VALUE);
    }

    /**
     * Test if the {@link #SKIP_NULLS} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionSkipNulls(
            Set<JsonUrlOption> options) {
        return options != null && options.contains(SKIP_NULLS);
    }

    /**
     * Test if the {@link #COERCE_NULL_TO_EMPTY_STRING} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    public static final boolean optionCoerceNullToEmptyString(
            Set<JsonUrlOption> options) {
        return options != null
                && options.contains(COERCE_NULL_TO_EMPTY_STRING);
    }

    /**
     * Test if the {@link #AQF} option is enabled,
     * supplying the default value if necessary.
     * @param options a valid Set or {@code null}.
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static final boolean optionAQF(
            Set<JsonUrlOption> options) {
        return options != null && options.contains(AQF);
    }

}
