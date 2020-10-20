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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A JsonTextBuilder that appends JSON&#x2192;URL text to any Appendable.
 *
 * <p>Note, like {@link java.lang.StringBuilder} an instance of this class is
 * not thread-safe.
 *
 * @param <A> Accumulator type
 * @param <R> Result type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public abstract class JsonUrlTextAppender<A extends Appendable, R> // NOPMD
        implements JsonTextBuilder<A, R>, Appendable {

    /**
     * Destination, provided in constructor.
     */
    protected final A out;

    /**
     * Use application/x-www-form-urlencoded style separators at depth 0.
     */
    private boolean wwwFormUrlEncoded;

    /**
     * Do not output the top level parens.
     */
    private boolean impliedComposite;

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
     * Current print depth.
     */
    private int depth;

    /**
     * Create a new JsonUrlTextAppender.
     *
     * @param out JSON&#x2192;URL text destination
     */
    public JsonUrlTextAppender(A out) {
        this.out = out;
    }

    @Override
    public JsonUrlTextAppender<A,R> beginObject() throws IOException {
        return beginComposite();
    }

    @Override
    public JsonUrlTextAppender<A,R> endObject() throws IOException {
        return endComposite();
    }

    @Override
    public JsonUrlTextAppender<A,R> beginArray() throws IOException {
        return beginComposite();
    }

    @Override
    public JsonUrlTextAppender<A,R> endArray() throws IOException {
        return endComposite();
    }

    @Override
    public JsonUrlTextAppender<A,R> valueSeparator() throws IOException {
        out.append(useWfuStructChars() ? '&' : ',');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> nameSeparator() throws IOException {
        out.append(useWfuStructChars() ? '=' : ':');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> addNull() throws IOException {
        if (impliedStringLiterals) {
            throw new IOException("implied strings: unexpected null");
        }
        
        out.append("null");
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(BigDecimal value) throws IOException {
        if (value == null) {
            return addNull();
        }

        if (impliedStringLiterals) {
            add(String.valueOf(value), false);

        } else {
            out.append(String.valueOf(value));    
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(BigInteger value) throws IOException {
        if (value == null) {
            return addNull();
        }

        if (impliedStringLiterals) {
            add(String.valueOf(value), false);

        } else {
            out.append(String.valueOf(value));    
        }

        
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(long value) throws IOException {
        if (impliedStringLiterals) {
            add(String.valueOf(value), false);
        } else {
            out.append(String.valueOf(value));    
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(double value) throws IOException {
        if (impliedStringLiterals) {
            add(String.valueOf(value), false);
        } else {
            out.append(String.valueOf(value));    
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(boolean value) throws IOException {
        out.append(String.valueOf(value));    
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(char value) throws IOException {
        out.append(value);
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(
            CharSequence text,
            int start,
            int end,
            boolean isKey) throws IOException {

        if (text == null) {
            if (isKey) {
                throw new IOException("object key can not be null");
            }
            return addNull();
        }

        JsonUrl.appendLiteral(
                out,
                text,
                start,
                end,
                isKey,
                isKey ? allowEmptyUnquotedKey : allowEmptyUnquotedValue,
                impliedStringLiterals);

        return this;
    }
    
    @Override
    public JsonUrlTextAppender<A,R> append(CharSequence csq) throws IOException {
        out.append(csq);
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> append(
            CharSequence csq,
            int start,
            int end) throws IOException {
        out.append(csq, start, end);
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> append(char value) throws IOException {
        out.append(value);
        return this;
    }

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
    public JsonUrlTextAppender<A,R> setFormUrlEncoded(// NOPMD - LinguisticNaming
            boolean wwwFormUrlEncoded) {

        this.wwwFormUrlEncoded = wwwFormUrlEncoded;
        return this;
    }

    /**
     * Returns true if this appender is writing an implied array or object.
     * @see #setImpliedComposite(boolean) 
     */
    public boolean isImpliedComposite() {
        return impliedComposite;
    }

    /**
     * Set this to true to output an implied array or object.
     * @param implied true or false
     */
    public JsonUrlTextAppender<A,R> setImpliedComposite(// NOPMD - LinguisticNaming
            boolean implied) {
        this.impliedComposite = implied;
        return this;
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
    public JsonUrlTextAppender<A,R> setImpliedStringLiterals(// NOPMD - LinguisticNaming
            boolean implied) {
        this.impliedStringLiterals = implied;
        return this;
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
    public JsonUrlTextAppender<A,R> setImpliedStringLiterals() { // NOPMD - LinguisticNaming
        this.setEmptyUnquotedKeyAllowed(true);
        this.setEmptyUnquotedValueAllowed(true);
        this.setImpliedStringLiterals(true);
        return this;
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
    public JsonUrlTextAppender<A,R> setEmptyUnquotedKeyAllowed(// NOPMD - LinguisticNaming
            boolean allow) {
        this.allowEmptyUnquotedKey = allow;
        return this;
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
    public JsonUrlTextAppender<A,R> setEmptyUnquotedValueAllowed(// NOPMD - LinguisticNaming
            boolean allow) {
        this.allowEmptyUnquotedValue = allow;
        return this;
    }

    /**
     * Test if wwwFormUrlEncoded structural characters should be used.
     */
    private boolean useWfuStructChars() {
        return wwwFormUrlEncoded && depth == 1;
    }

    private JsonUrlTextAppender<A,R> beginComposite() throws IOException {
        if (!impliedComposite || depth > 0) {
            out.append('(');
        }

        depth++;
        return this;
    }

    private JsonUrlTextAppender<A,R> endComposite() throws IOException {
        depth--;

        if (!impliedComposite || depth > 0) {
            out.append(')');
        }
        return this;
    }
}
