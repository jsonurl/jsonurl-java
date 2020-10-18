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

import static org.jsonurl.JsonUrl.parseLiteralLength;
import static org.jsonurl.LimitException.Message.MSG_LIMIT_MAX_PARSE_CHARS;
import static org.jsonurl.LimitException.Message.MSG_LIMIT_MAX_PARSE_DEPTH;
import static org.jsonurl.LimitException.Message.MSG_LIMIT_MAX_PARSE_VALUES;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_CHAR;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_LITERAL;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_OBJECT_VALUE;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_STRUCT_CHAR;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_TYPE;
import static org.jsonurl.SyntaxException.Message.MSG_EXTRA_CHARS;
import static org.jsonurl.SyntaxException.Message.MSG_NO_TEXT;
import static org.jsonurl.SyntaxException.Message.MSG_STILL_OPEN;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A JSON&#x2192;URL text parser.
 * 
 * <p>An instance of this class may be used to parse JSON&#x2192;URL text. Each
 * instance maintains a set of limits that are applied any time the
 * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)}
 * (or any of the related convenience methods are called). No other state
 * maintained globally, so a single instance of this class is thread-safe so
 * long as you:
 * <ul>
 * <li>Set limits in a single-threaded context and don't change them in
 * individual threads.
 * <li>Provide proper locking around the setter/getter methods on this
 * class. 
 * </ul>
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class Parser { //NOPMD

    /**
     * Parse state.
     */
    private enum State {
        PAREN,
        IN_ARRAY,
        ARRAY_AFTER_ELEMENT,
        IN_OBJECT,
        OBJECT_HAVE_KEY,
        OBJECT_AFTER_ELEMENT
    }

    /**
     * Default {@link #setMaxParseChars(int)}.
     */
    public static final int DEFAULT_MAX_PARSE_CHARS = 1 << 13;

    /**
     * Default {@link #setMaxParseDepth(int)}.
     */
    public static final int DEFAULT_MAX_PARSE_DEPTH = 1 << 4;
    
    /**
     * Default {@link #setMaxParseValues(int)}.
     */
    public static final int DEFAULT_MAX_PARSE_VALUES = 1 << 10;

    /**
     * EnumSet.of(ValueType.OBJECT).
     */
    static final EnumSet<ValueType> TYPE_VALUE_OBJECT =
        EnumSet.of(ValueType.OBJECT);

    /**
     * EnumSet.of(ValueType.ARRAY).
     */
    static final EnumSet<ValueType> TYPE_VALUE_ARRAY =
        EnumSet.of(ValueType.ARRAY);

    /**
     * Maximum parse depth.
     */
    private int maxParseDepth = DEFAULT_MAX_PARSE_DEPTH;

    /**
     * Maximum number of parsed chars.
     */
    private int maxParseChars = DEFAULT_MAX_PARSE_CHARS;

    /**
     * Maximum number of parsed values.
     */
    private int maxParseValues = DEFAULT_MAX_PARSE_VALUES;

    /**
     * begin-composite (open paren).
     */
    private static final char BEGIN_COMPOSITE = '(';

    /**
     * end-composite (close paren).
     */
    private static final char END_COMPOSITE = ')';

    /**
     * name-separator (colon).
     */
    private static final char NAME_SEPARATOR = ':';

    /**
     * value-separator (comma).
     */
    private static final char VALUE_SEPARATOR = ',';

    /**
     * x-www-form-urlencoded name-separator (equals).
     */
    private static final char WFU_NAME_SEPARATOR = '=';

    /**
     * x-www-form-urlencoded value-separator (ampersand).
     */
    private static final char WFU_VALUE_SEPARATOR = '&';

    /**
     * Allow application/x-www-form-urlencoded style separators.
     */
    private boolean wwwFormUrlEncoded;

    /**
     * Allow empty unquoted keys.
     */
    private boolean allowEmptyUnquotedKey;

    /**
     * Allow empty unquoted values.
     */
    private boolean allowEmptyUnquotedValue;

    /**
     * Assume all literals are strings.
     */
    private boolean impliedStringLiterals;

    /**
     * Get the maximum number of parsed characters.
     * @see #setMaxParseChars(int)
     */
    public int getMaxParseChars() {
        return maxParseChars;
    }

    /**
     * Set the maximum number of parsed characters.
     * This provides a limit on the maximum number of characters that will
     * be parsed before throwing an exception. The default value is
     * {@link #DEFAULT_MAX_PARSE_CHARS}.
     */
    public void setMaxParseChars(int maxParseChars) {
        this.maxParseChars = maxParseChars;
    }

    /**
     * Get the maximum parse depth.
     * @see #setMaxParseDepth(int)
     */
    public int getMaxParseDepth() {
        return maxParseDepth;
    }

    /**
     * Set the maximum parse depth.
     * This provides a limit on the depth of a JSON&#x2192;URL object/array
     * before an exception of thrown. The default value is
     * {@link #DEFAULT_MAX_PARSE_DEPTH}.
     */
    public void setMaxParseDepth(int maxParseDepth) {
        this.maxParseDepth = maxParseDepth;
    }

    /**
     * Get the maximum number of parsed values.
     * @see #setMaxParseValues(int)
     */
    public int getMaxParseValues() {
        return maxParseValues;
    }

    /**
     * Set the maximum number of parsed values.
     * This provides a limit on the number of values that will be
     * parsed/instantiated before an exception is thrown. The default value is
     * {@link #DEFAULT_MAX_PARSE_VALUES}.
     */
    public void setMaxParseValues(int maxParseValues) {
        this.maxParseValues = maxParseValues;
    }

    /**
     * Returns true if application/x-www-form-urlencoded style separators are
     * allowed for an implied top-level object or array.
     * @see #setFormUrlEncodedAllowed(boolean) 
     */
    public boolean isFormUrlEncodedAllowed() {
        return wwwFormUrlEncoded;
    }

    /**
     * Set this to true if you want to allow {@code &amp} to be used as a
     * top-level value separator and {@code =} to be used as top-level name
     * separator. 
     * 
     * @param wwwFormUrlEncoded true or false
     */
    public void setFormUrlEncodedAllowed(boolean wwwFormUrlEncoded) {
        this.wwwFormUrlEncoded = wwwFormUrlEncoded;
    }

    /**
     * Returns true if the parser accepts empty, unquoted keys.
     * @see #setEmptyUnquotedKeyAllowed(boolean) 
     */
    public boolean isEmptyUnquotedKeyAllowed() {
        return allowEmptyUnquotedKey;
    }

    /**
     * Set this to true if you want the parser to accept empty, unquoted keys.
     * For example, {@code (:value)}.
     * @param allow boolean
     */
    public void setEmptyUnquotedKeyAllowed(boolean allow) {
        this.allowEmptyUnquotedKey = allow;
    }

    /**
     * Returns true if the parser accepts empty, unquoted values.
     * @see #setEmptyUnquotedValueAllowed(boolean) 
     */
    public boolean isEmptyUnquotedValueAllowed() {
        return allowEmptyUnquotedValue;
    }

    /**
     * Set this to true if you want the parser to accept empty, unquoted values.
     * For example, {@code (1,,3)}.
     * @param allow boolean
     */
    public void setEmptyUnquotedValueAllowed(boolean allow) {
        this.allowEmptyUnquotedValue = allow;
    }

    /**
     * Test if this parser assumes all literals are strings.
     * @see #setImpliedStringLiterals(boolean) 
     */
    public boolean isImpliedStringLiterals() {
        return impliedStringLiterals;
    }

    /**
     * If true the parser will assume all literals are strings.
     *
     *<p>This can be useful when you don't want the native Java type of a
     * value to determine how it's encoded.
     * 
     * @param impliedStringLiterals boolean
     */
    public void setImpliedStringLiterals(boolean impliedStringLiterals) {
        this.impliedStringLiterals = impliedStringLiterals;
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(ValueType.OBJECT), factory)}.
     */
    @SuppressWarnings("unchecked") // NOPMD 
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> J parseObject(
                CharSequence text,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return (J)parse(text, 0, text.length(), TYPE_VALUE_OBJECT, factory);
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, EnumSet.of(ValueType.OBJECT), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> J parseObject(
                CharSequence text,
                int off,
                int length,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return (J)parse(text, off, length, TYPE_VALUE_OBJECT, factory);
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(ValueType.OBJECT), factory)}.
     */
    @SuppressWarnings("unchecked") // NOPMD 
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> J parseObject(
                CharSequence text,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
                JBT impliedObject) {

        return (J)parse(text, 0, text.length(), TYPE_VALUE_OBJECT,
            new ValueFactoryParseResultFacade<>(
                    factory, null, impliedObject, null));
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(ValueType.OBJECT), factory)}.
     */
    @SuppressWarnings("unchecked") // NOPMD 
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> J parseObject(
                CharSequence text,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
                JBT impliedObject,
                MissingValueProvider<V> mvp) {

        return (J)parse(text, 0, text.length(), TYPE_VALUE_OBJECT,
            new ValueFactoryParseResultFacade<>(
                    factory, null, impliedObject, mvp));
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, EnumSet.of(ValueType.OBJECT), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> J parseObject(
                CharSequence text,
                int off,
                int length,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
                JBT impliedObject) {

        return (J)parse(text, off, length, TYPE_VALUE_OBJECT,
            new ValueFactoryParseResultFacade<>(
                    factory, null, impliedObject, null));
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, EnumSet.of(ValueType.OBJECT), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> J parseObject(
                CharSequence text,
                int off,
                int length,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
                JBT impliedObject,
                MissingValueProvider<V> mvp) {

        return (J)parse(text, off, length, TYPE_VALUE_OBJECT,
            new ValueFactoryParseResultFacade<>(
                    factory, null, impliedObject, mvp));
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(ValueType.ARRAY), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> A parseArray(
                CharSequence text,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {

        return (A)parse(text, 0, text.length(), TYPE_VALUE_ARRAY, factory);
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, EnumSet.of(ValueType.ARRAY), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> A parseArray(
                CharSequence text,
                int off,
                int length,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return (A)parse(text, off, length, TYPE_VALUE_ARRAY, factory);
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(ValueType.ARRAY), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> A parseArray(
                CharSequence text,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
                ABT impliedArray) {

        return (A)parse(text, 0, text.length(), TYPE_VALUE_ARRAY,
            new ValueFactoryParseResultFacade<>(
                    factory, impliedArray, null, null));
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, EnumSet.of(ValueType.ARRAY), factory)}.
     */
    @SuppressWarnings("unchecked")
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> A parseArray(
                CharSequence text,
                int off,
                int length,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
                ABT impliedArray) {

        return (A)parse(text, off, length, TYPE_VALUE_ARRAY,
            new ValueFactoryParseResultFacade<>(
                    factory, impliedArray, null, null));
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), null, factory)}.
     */
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> V parse(
                CharSequence text,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return parse(text, 0, text.length(), (EnumSet<ValueType>)null, factory);
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, null, factory)}.  
     */
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> V parse(
                CharSequence text,
                int off,
                int length,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return parse(text, off, length, (EnumSet<ValueType>)null, factory);
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, Set, ValueFactory)
     * parse(s, off, length, EnumSet.of(canReturn), factory)}.
     */
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> V parse(
                CharSequence text,
                int off,
                int length,
                ValueType canReturn,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return parse(text, off, length, EnumSet.of(canReturn), factory);
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, Set, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(canReturn), factory)}.
     */
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> V parse(
                CharSequence text,
                ValueType canReturn,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return parse(text, 0, text.length(), EnumSet.of(canReturn), factory);
    }
    
    /**
     * Parse the given JSON&#x2192;URL text and return a JSON value.
     * The parse will start at the character offset given by {@code off}, and
     * {@code length} total characters will be parsed.
     * 
     * <p>The provided factory will be used to instantiate values, so this
     * method is target API agnostic. The {@code canReturn} value may be used
     * to limit what types are allowed in the response. If an invalid value
     * type is found then a {@link ParseException} will be thrown.
     *
     * @param text the text to be parsed
     * @param off offset of the first character to be parsed
     * @param length a <em>length</em>, not an offset
     * @param canReturn set of allowed return types
     * @param factory a valid ValueFactory
     * @return a factory-typed value
     */
    public <V,
            C extends V,
            ABT,
            A extends C,
            JBT,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> V parse(
                CharSequence text,
                int off,
                int length,
                Set<ValueType> canReturn,
                ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {

        return parse(text, off, length, canReturn,
            new ValueFactoryParseResultFacade<>(factory, null, null, null));
    }

    /**
     * Parse a character sequence.
     * 
     * <p>This is the "real" parse function that all the convenience methods
     * call.
     */
    @SuppressWarnings({
        "PMD.AvoidDuplicateLiterals",
        "PMD.CyclomaticComplexity",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.ModifiedCyclomaticComplexity",
        "PMD.NcssCount",
        "PMD.NcssMethodCount",
        "PMD.NPathComplexity",
        "PMD.StdCyclomaticComplexity",
        "PMD.SwitchDensity"})
    private <R> R parse(
            CharSequence text,
            int off,
            int length,
            final Set<ValueType> canReturn,
            ParseResultFacade<R> result) {

        final boolean impliedArray = result.isImpliedArray();
        final boolean impliedObject = result.isImpliedObject();

        if (length == 0) {
            //
            // if the text is empty and this is an implied array or object then
            // simply return the caller provided value.
            //
            if (impliedArray) {
                return result.endArray().getResult();
            }

            if (impliedObject) {
                return result.endObject().getResult();
            }

            //
            // if the text is empty and the caller allows unquoted empty string
            // literals then return one.
            //
            if (allowEmptyUnquotedValue) {
                return result.getResult(
                        text,
                        off,
                        off,
                        true,
                        impliedStringLiterals);
            }
            
            //
            // otherwise, it's an error
            //
            throw new SyntaxException(MSG_NO_TEXT, 0);
        }
        if (length >= this.maxParseChars) {
            throw new LimitException(MSG_LIMIT_MAX_PARSE_CHARS);
        }

        result.setLocation(1);

        final int stop = off + length;

        //
        // the current parse position
        //
        int pos = off;
        
        //
        // I only need to run the type check once so I track it with this
        // boolean. If canReturn == null then there's nothing to check.
        //
        boolean isDoneTypeCheck = canReturn == null;

        //
        // parse state stack
        //
        final LinkedList<State> stateStack = new LinkedList<>();

        if (impliedObject) {
            stateStack.push(State.IN_OBJECT);
            isDoneTypeCheck = true;

        } else if (impliedArray) {
            stateStack.push(State.IN_ARRAY);
            isDoneTypeCheck = true;

        } else if (text.charAt(off) == BEGIN_COMPOSITE) {
            //
            // composite and no implied object/array
            //
            stateStack.push(State.PAREN);
            pos++;

        } else {
            //
            // not composite; parse as a single literal value
            //
            // Note, I could remove the call to parseLiteralLength() and
            // just assume the whole text is a literal. The consequence is
            // that there would be values which are valid top-level literals
            // (specifically, non-quoted strings could include non-encoded
            // structural characters) but not valid when inside a composite.
            // I don't think I want that.
            //
            int litlen = parseLiteralLength(text, off, stop, null);
            if (litlen != length) {
                throw new SyntaxException(MSG_EXPECT_LITERAL);
            }

            R ret = result.getResult(
                    text,
                    off,
                    stop,
                    allowEmptyUnquotedValue,
                    impliedStringLiterals);

            if (canReturn != null && !result.isValid(canReturn, ret)) {
                throw new SyntaxException(
                    MSG_EXPECT_TYPE,
                    String.format("%s: %s",
                        MSG_EXPECT_TYPE.getMessageText(),
                        canReturn));
            }

            return ret;

        }
        
        final boolean allowEmptyUnquotedKey = this.allowEmptyUnquotedKey;
        final boolean wwwFormUrlEncoded = this.wwwFormUrlEncoded;

        int parseDepth = 1;
        int parseValueCount = 0;

        for (;;) {
            if (pos == stop) {
                throw new SyntaxException(MSG_STILL_OPEN, pos);
            }

            char c = text.charAt(pos); // NOPMD - ShortVariable

            switch (stateStack.peek()) { // NOPMD -- no switch default
            case PAREN:
                switch (c) {
                case BEGIN_COMPOSITE:
                    //
                    // found two back-to-back open parens. We know the first,
                    // paren is starting an array.
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.ARRAY)) {
                            throw new SyntaxException(
                                MSG_EXPECT_TYPE,
                                String.format("%s: %s",
                                    MSG_EXPECT_TYPE,
                                    canReturn));
                        }
                        isDoneTypeCheck = true;
                    }

                    parseValueCount = incrementLimit(
                            MSG_LIMIT_MAX_PARSE_VALUES,
                            this.maxParseValues,
                            parseValueCount,
                            pos);

                    stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                    stateStack.push(State.PAREN);
                    result.setLocation(pos).beginArray();

                    parseDepth = incrementLimit(
                        MSG_LIMIT_MAX_PARSE_DEPTH,
                        this.maxParseDepth,
                        parseDepth,
                        pos);

                    pos++;
                    continue;

                case END_COMPOSITE:
                    //
                    // found open paren followed by close paren; the empty
                    // composite value.
                    //
                    if (!isDoneTypeCheck) {
                        if (!ValueType.containsComposite(canReturn)) {
                            throw new SyntaxException(
                                MSG_EXPECT_TYPE,
                                String.format("%s: %s",
                                    MSG_EXPECT_TYPE.getMessageText(),
                                    canReturn),
                                pos);
                        }
                        isDoneTypeCheck = true;
                    }

                    parseDepth--;
                    pos++;

                    if (parseDepth == 0) {
                        if (pos == stop) {
                            //
                            // In theory this should return resultBuilder.getResult()
                            // but if parseDepth is zero then I know I can simply
                            // return the empty-composite value.
                            //
                            return result.getEmptyComposite();
                        }

                        throw new SyntaxException(MSG_EXTRA_CHARS, pos);
                    }

                    parseValueCount = incrementLimit(
                            MSG_LIMIT_MAX_PARSE_VALUES,
                            this.maxParseValues,
                            parseValueCount,
                            pos);

                    stateStack.pop();
                    result.setLocation(pos).addEmptyComposite();

                    if (pos == stop && parseDepth == 1) {
                        if (impliedArray) {
                            return result.addArrayElement().endArray().getResult();
                        }
                        if (impliedObject) {
                            return result.addObjectElement().endObject().getResult();
                        }

                        throw new SyntaxException(MSG_STILL_OPEN, pos);
                    }

                    continue;

                default:
                    break;
                }

                //
                // paren followed by a literal.  I need to lookahead
                // one token to see if this is an object or array.
                //
                int litlen = parseLiteralLength(text, pos, stop, MSG_STILL_OPEN);

                parseValueCount = incrementLimit(
                        MSG_LIMIT_MAX_PARSE_VALUES,
                        this.maxParseValues,
                        parseValueCount,
                        pos);

                int litpos = pos; // NOPMD - capture the literal's position

                pos += litlen;

                if (pos == stop) {
                    throw new SyntaxException(MSG_STILL_OPEN, pos);
                }

                c = text.charAt(pos);

                switch (c) {
                case WFU_VALUE_SEPARATOR:
                    if (!wwwFormUrlEncoded || parseDepth != 1) {
                        throw new SyntaxException(MSG_BAD_CHAR, pos);
                    }
                    // fall through
                case VALUE_SEPARATOR:
                    //
                    // multi-element array
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.ARRAY)) {
                            throw new SyntaxException(
                                MSG_EXPECT_TYPE,
                                String.format("%s: %s",
                                    MSG_EXPECT_TYPE.getMessageText(),
                                    canReturn));
                        }
                        isDoneTypeCheck = true;
                    }

                    parseValueCount = incrementLimit(
                            MSG_LIMIT_MAX_PARSE_VALUES,
                            this.maxParseValues,
                            parseValueCount,
                            pos);

                    stateStack.set(0, State.ARRAY_AFTER_ELEMENT);

                    result
                        .setLocation(litpos)
                        .beginArray()
                        .addLiteral(
                                text,
                                litpos,
                                pos,
                                allowEmptyUnquotedValue,
                                impliedStringLiterals);

                    continue;

                case END_COMPOSITE:
                    //
                    // single element array
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.ARRAY)) {
                            throw new SyntaxException(
                                MSG_EXPECT_TYPE,
                                String.format("%s: %s",
                                    MSG_EXPECT_TYPE.getMessageText(),
                                    canReturn),
                                pos);
                        }
                        isDoneTypeCheck = true;
                    }
                    
                    result
                        .setLocation(litpos)
                        .addSingleElementArray(
                                text,
                                litpos,
                                pos,
                                allowEmptyUnquotedValue,
                                impliedStringLiterals);

                    parseDepth--;
                    pos++;
                    
                    switch (parseDepth) {
                    case 0:
                        if (pos == stop) {
                            return result.getResult();
                        }
                        throw new SyntaxException(MSG_EXTRA_CHARS, pos);

                    case 1:
                        if (pos == stop) {
                            if (impliedArray) {
                                return result
                                    .addArrayElement()
                                    .endArray()
                                    .getResult();
                            }
                            if (impliedObject) {
                                return result
                                    .addObjectElement()
                                    .endObject()
                                    .getResult();
                            }
                            throw new SyntaxException(MSG_STILL_OPEN, pos);    
                        }
                        break;

                    default:
                        break;
                    }

                    stateStack.pop();
                    continue;

                case WFU_NAME_SEPARATOR:
                    if (!wwwFormUrlEncoded || parseDepth != 1) {
                        throw new SyntaxException(MSG_BAD_CHAR, pos);
                    }
                    // fall through
                case NAME_SEPARATOR:
                    //
                    // key name for object
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.OBJECT)) {
                            throw new SyntaxException(
                                MSG_EXPECT_TYPE,
                                String.format("%s: %s",
                                    MSG_EXPECT_TYPE.getMessageText(),
                                    canReturn));
                        }
                        isDoneTypeCheck = true;
                    }

                    stateStack.set(0, State.OBJECT_HAVE_KEY);

                    result
                        .setLocation(litpos)
                        .beginObject()
                        .addObjectKey(
                                text,
                                litpos,
                                pos,
                                allowEmptyUnquotedKey,
                                impliedStringLiterals);

                    pos++;
                    continue;

                default:
                    break;
                }
                throw new SyntaxException(MSG_EXPECT_LITERAL, pos);

            case IN_ARRAY:
                if (c == BEGIN_COMPOSITE) {
                    stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                    stateStack.push(State.PAREN);

                    parseDepth = incrementLimit(
                            MSG_LIMIT_MAX_PARSE_DEPTH,
                            this.maxParseDepth,
                            parseDepth,
                            pos);
                    pos++;
                    continue;
                }

                litpos = pos;
                litlen = parseLiteralLength(text, pos, stop, MSG_STILL_OPEN);

                parseValueCount = incrementLimit(
                        MSG_LIMIT_MAX_PARSE_VALUES,
                        this.maxParseValues,
                        parseValueCount,
                        pos);

                pos += litlen;

                stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                result
                    .setLocation(litpos)
                    .addLiteral(
                            text,
                            litpos,
                            pos,
                            allowEmptyUnquotedValue,
                            impliedStringLiterals);

                if (pos == stop) {
                    if (parseDepth == 1 && impliedArray) {
                        return result.addArrayElement().endArray().getResult();
                    }

                    throw new SyntaxException(MSG_STILL_OPEN, stop);
                }
                continue;

            case ARRAY_AFTER_ELEMENT:
                result.setLocation(pos).addArrayElement();

                switch (c) {
                case WFU_VALUE_SEPARATOR:
                    if (!wwwFormUrlEncoded || parseDepth != 1) {
                        throw new SyntaxException(MSG_BAD_CHAR, pos);
                    }
                    // fall through
                case VALUE_SEPARATOR:
                    stateStack.set(0, State.IN_ARRAY);
                    pos++;
                    continue;

                case END_COMPOSITE:
                    result.endArray();

                    parseDepth--;
                    pos++;

                    switch (parseDepth) {
                    case 0:
                        if (pos == stop && !impliedArray) {
                            return result.getResult();
                        }
                        throw new SyntaxException(MSG_EXTRA_CHARS, pos);

                    case 1:
                        if (pos == stop) {
                            if (impliedArray) {
                                return result.addArrayElement().endArray().getResult();
                            }
                            if (impliedObject) {
                                return result.addObjectElement().endObject().getResult();
                            }
                            throw new SyntaxException(MSG_STILL_OPEN, pos);
                        }
                        break;

                    default:
                        break;
                    }

                    stateStack.pop();
                    continue;

                default:
                    break;
                }
                throw new SyntaxException(MSG_EXPECT_STRUCT_CHAR, pos);

            case OBJECT_HAVE_KEY:
                if (c == BEGIN_COMPOSITE) {
                    stateStack.set(0, State.OBJECT_AFTER_ELEMENT);
                    stateStack.push(State.PAREN);

                    parseDepth = incrementLimit(
                            MSG_LIMIT_MAX_PARSE_DEPTH,
                            this.maxParseDepth,
                            parseDepth,
                            pos);

                    pos++;
                    continue;
                }

                litpos = pos;
                litlen = parseLiteralLength(text, pos, stop, MSG_STILL_OPEN);

                parseValueCount = incrementLimit(
                        MSG_LIMIT_MAX_PARSE_VALUES,
                        this.maxParseValues,
                        parseValueCount,
                        pos);

                pos += litlen;

                stateStack.set(0, State.OBJECT_AFTER_ELEMENT);
                result
                    .setLocation(litpos)
                    .addLiteral(
                            text,
                            litpos,
                            pos,
                            allowEmptyUnquotedValue,
                            impliedStringLiterals);
                
                if (pos == stop) {
                    if (parseDepth == 1 && impliedObject) {
                        return result.addObjectElement().endObject().getResult();    
                    }

                    throw new SyntaxException(MSG_STILL_OPEN, stop);
                }

                continue;

            case OBJECT_AFTER_ELEMENT:
                result.setLocation(pos).addObjectElement();

                switch (c) {
                case WFU_VALUE_SEPARATOR:
                    if (!wwwFormUrlEncoded || parseDepth != 1) {
                        throw new SyntaxException(MSG_BAD_CHAR, pos);
                    }
                    // fall through
                case VALUE_SEPARATOR:
                    stateStack.set(0, State.IN_OBJECT);
                    pos++;
                    continue;

                case END_COMPOSITE:
                    result.endObject();

                    parseDepth--;
                    pos++;

                    switch (parseDepth) {
                    case 0:
                        if (pos == stop && !impliedObject) {
                            return result.getResult();
                        }
                        throw new SyntaxException(MSG_EXTRA_CHARS, pos);

                    case 1:
                        if (pos == stop) {
                            if (impliedArray) {
                                return result.addArrayElement().endArray().getResult();
                            }
                            if (impliedObject) {
                                return result.addObjectElement().endObject().getResult();
                            }
                            throw new SyntaxException(MSG_EXTRA_CHARS, pos);    
                        }
                        break;

                    default:
                        break;
                    }

                    stateStack.pop();
                    continue;
                default:
                    break;
                }
                throw new SyntaxException(MSG_EXPECT_STRUCT_CHAR, pos);

            case IN_OBJECT:
                litpos = pos;
                litlen = parseLiteralLength(text, pos, stop, MSG_STILL_OPEN);
                pos += litlen;

                if (pos == stop) {
                    if (impliedObject && parseDepth == 1) {
                        return result
                            .setLocation(litpos)
                            .addMissingValue(text, litpos, pos, impliedStringLiterals)
                            .addObjectElement()
                            .endObject()
                            .getResult();
                    }
                    throw new SyntaxException(MSG_STILL_OPEN, pos);
                }

                c = text.charAt(pos);

                switch (c) {
                case WFU_NAME_SEPARATOR:
                    if (!wwwFormUrlEncoded || parseDepth != 1) {
                        throw new SyntaxException(MSG_BAD_CHAR, pos);
                    }
                    // fall through
                case NAME_SEPARATOR:
                    break;

                case WFU_VALUE_SEPARATOR:
                    if (!wwwFormUrlEncoded || parseDepth != 1) {
                        throw new SyntaxException(MSG_EXPECT_OBJECT_VALUE, pos);
                    }
                    // fall through
                case VALUE_SEPARATOR:
                    if (impliedObject && parseDepth == 1) {
                        //
                        // this may be a key that's missing a value; give
                        // the result a chance to handle that case.
                        //
                        result
                            .setLocation(litpos)
                            .addMissingValue(
                                    text,
                                    litpos,
                                    pos,
                                    impliedStringLiterals);

                        stateStack.set(0, State.OBJECT_AFTER_ELEMENT);
                        continue;
                    }
                    // fall through
                default:
                    throw new SyntaxException(MSG_EXPECT_OBJECT_VALUE, pos);
                }

                stateStack.set(0, State.OBJECT_HAVE_KEY);

                result
                    .setLocation(litpos)
                    .addObjectKey(
                            text,
                            litpos,
                            pos,
                            allowEmptyUnquotedKey,
                            impliedStringLiterals);

                pos++;
                continue;
            }
        }
    }

    /**
     * increment a limit value, throwing an exception if the limit is reached.
     */
    private static int incrementLimit(
            LimitException.Message msg,
            int limit,
            int value,
            int pos) {

        final int newValue = value + 1;

        if (newValue > limit) {
            throw new LimitException(msg, pos);
        }
        return newValue;
    }
}
