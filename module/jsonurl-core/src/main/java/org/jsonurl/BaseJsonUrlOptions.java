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
 * Base class implementation of {@link JsonUrlOptions}.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-10-20
 */
public class BaseJsonUrlOptions implements JsonUrlOptions {

    /**
     * Use application/x-www-form-urlencoded style separators at depth 0.
     */
    private boolean wwwFormUrlEncoded;

    /**
     * All literals are strings.
     */
    private boolean impliedStringLiterals;
    
    /**
     * Allow empty unquoted keys.
     */
    private boolean allowEmptyUnquotedKey;

    /**
     * Allow empty unquoted values.
     */
    private boolean allowEmptyUnquotedValue;

    /**
     * When read/writing, use empty string rather than null.
     */
    private boolean skipNulls;

    @Override
    public boolean isFormUrlEncoded() {
        return wwwFormUrlEncoded;
    }

    /**
     * Enable/disable the wfu-composite/wfu-implied-composite options.
     * @param wfu true or false
     * @see #isFormUrlEncoded()
     */
    public void setFormUrlEncoded(boolean wfu) {
        this.wwwFormUrlEncoded = wfu;
    }

    @Override
    public boolean isImpliedStringLiterals() {
        return impliedStringLiterals;
    }

    /**
     * Enable/disable the implied-string-literals option.
     * @param implied true or false
     * @see #isImpliedStringLiterals()
     */
    public void setImpliedStringLiterals(boolean implied) {
        this.impliedStringLiterals = implied;
    }

    @Override
    public boolean isEmptyUnquotedKeyAllowed() {
        return allowEmptyUnquotedKey;
    }

    /**
     * Enable/disable the the empty-unquoted-key option.
     * @param allow true or false
     * @see #isEmptyUnquotedKeyAllowed()
     */
    public void setEmptyUnquotedKeyAllowed(boolean allow) {
        this.allowEmptyUnquotedKey = allow;
    }

    @Override
    public boolean isEmptyUnquotedValueAllowed() {
        return allowEmptyUnquotedValue;
    }

    /**
     * Enable/disable the the empty-unquoted-value option.
     * @param allow true or false
     * @see #isEmptyUnquotedValueAllowed()
     */
    public void setEmptyUnquotedValueAllowed(boolean allow) {
        this.allowEmptyUnquotedValue = allow;
    }

    @Override
    public boolean isSkipNulls() {
        return skipNulls;
    }

    /**
     * Enable/disable the skip-nulls option.
     * @param skipNulls true or false
     * @see #isSkipNulls()
     */
    public void setSkipNulls(boolean skipNulls) {
        this.skipNulls = skipNulls;
    }

    /**
     * Use this method to enable implied string literals and common related
     * options.
     *
     * <p>This is a convenience method for:
     * <pre>
     *   setEmptyUnquotedKeyAllowed(true);
     *   setEmptyUnquotedValueAllowed(true);
     *   setSkipNulls(true);
     *   setImpliedStringLiterals(true);
     * </pre>
     *
     * <p>If {@link #isImpliedStringLiterals()} is true but
     * {@link #isEmptyUnquotedValueAllowed()} is false then an empty string
     * literal value will trigger an Exception because there wouldn't be any
     * way to represent it.
     *
     * <p>If {@link #isImpliedStringLiterals()} is true but
     * {@link #isEmptyUnquotedKeyAllowed()} is false then an empty key will
     * trigger an Exception because there wouldn't be any way to represent it.
     *
     * <p>If {@link #isImpliedStringLiterals()} is true but
     * {@link #isSkipNulls()} is false:<ul>
     * <li>when writing to a {@link JsonTextBuilder} - a {@code null} will
     * trigger an Exception because there wouldn't be a way to represent it.
     * <li>during parse - no exception would occur (because a {@code null}
     * value isn't possible) but the parse would happen as though
     * {@code null} values were not present in the stream.
     * </ul>
     *
     * <p>If you want the above behavior, because your data doesn't
     * support those cases and you want the parser to catch them for you,
     * then call {@link #setImpliedStringLiterals(boolean)
     * setImpliedStringLiterals(true)} directly.
     * 
     * @see #setEmptyUnquotedKeyAllowed(boolean)
     * @see #setEmptyUnquotedValueAllowed(boolean)
     * @see #setImpliedStringLiterals(boolean)
     */
    public void enableImpliedStringLiterals() {
        setEmptyUnquotedKeyAllowed(true);
        setEmptyUnquotedValueAllowed(true);
        setSkipNulls(true);
        setImpliedStringLiterals(true);
    }
}
