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
 * Common parse/print options.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-10-20
 */
public class CommonOptions {

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
     * Returns true if application/x-www-form-urlencoded style separators are
     * allowed for an implied top-level object or array.
     * @see #setFormUrlEncoded(boolean) 
     */
    public boolean isFormUrlEncoded() {
        return wwwFormUrlEncoded;
    }

    /**
     * Set this to true if you want to allow {@code &amp} to be used as a
     * top-level value separator and {@code =} to be used as top-level name
     * separator. 
     * 
     * @param wwwFormUrlEncoded true or false
     */
    public void setFormUrlEncoded(boolean wwwFormUrlEncoded) {
        this.wwwFormUrlEncoded = wwwFormUrlEncoded;
    }

    /**
     * Returns true if all literals are assumed to be strings.
     * @see #setImpliedStringLiterals(boolean)
     */
    public boolean isImpliedStringLiterals() {
        return impliedStringLiterals;
    }

    /**
     * Set this to true to assume all literals are strings.
     * @param implied true or false
     */
    public void setImpliedStringLiterals(boolean implied) {
        this.impliedStringLiterals = implied;
    }

    /**
     * Use this method to enable implied string literals and common related
     * options.
     *
     * <p>This is a convenience method for:
     * <pre>
     *   setEmptyUnquotedKeyAllowed(true);
     *   setEmptyUnquotedValueAllowed(true);
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
     * <p>If you want the above behavior, because your data doesn't
     * support those cases and you want the parser to catch them for you,
     * then call {@link #setImpliedStringLiterals(boolean)
     * setImpliedStringLiterals(true)} directly.
     * 
     * @see #setEmptyUnquotedKeyAllowed(boolean)
     * @see #setEmptyUnquotedValueAllowed(boolean)
     * @see #setImpliedStringLiterals(boolean)
     */
    public void setImpliedStringLiterals() {
        setEmptyUnquotedKeyAllowed(true);
        setEmptyUnquotedValueAllowed(true);
        setImpliedStringLiterals(true);
    }
    
    /**
     * Returns true if empty, unquoted keys are allowed.
     * @see #setEmptyUnquotedKeyAllowed(boolean) 
     */
    public boolean isEmptyUnquotedKeyAllowed() {
        return allowEmptyUnquotedKey;
    }

    /**
     * Set this to true if you want to allow empty, unquoted keys.
     * For example, {@code (:value)}.
     * @param allow boolean
     */
    public void setEmptyUnquotedKeyAllowed(boolean allow) {
        this.allowEmptyUnquotedKey = allow;
    }

    /**
     * Returns true if empty, unquoted values are allowed.
     * @see #setEmptyUnquotedValueAllowed(boolean) 
     */
    public boolean isEmptyUnquotedValueAllowed() {
        return allowEmptyUnquotedValue;
    }

    /**
     * Set this to true to allow empty, unquoted values.
     * For example, {@code (1,,3)}.
     * @param allow boolean
     */
    public void setEmptyUnquotedValueAllowed(boolean allow) {
        this.allowEmptyUnquotedValue = allow;
    }
}
