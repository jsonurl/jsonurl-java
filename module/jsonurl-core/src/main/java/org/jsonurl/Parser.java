package org.jsonurl;

/*
 * Copyright 2019 David MacCormack
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

import static org.jsonurl.JsonUrl.Parse.literal;
import static org.jsonurl.JsonUrl.Parse.literalToJavaString;
import static org.jsonurl.JsonUrl.parseLiteralLength;
import static org.jsonurl.LimitException.ERR_MSG_LIMIT_MAX_PARSE_CHARS;
import static org.jsonurl.LimitException.ERR_MSG_LIMIT_MAX_PARSE_DEPTH;
import static org.jsonurl.LimitException.ERR_MSG_LIMIT_MAX_PARSE_VALUES;
import static org.jsonurl.SyntaxException.ERR_MSG_EXPECT_LITERAL;
import static org.jsonurl.SyntaxException.ERR_MSG_EXPECT_OBJVALUE;
import static org.jsonurl.SyntaxException.ERR_MSG_EXPECT_STRUCTCHAR;
import static org.jsonurl.SyntaxException.ERR_MSG_EXPECT_TYPE;
import static org.jsonurl.SyntaxException.ERR_MSG_EXTRACHARS;
import static org.jsonurl.SyntaxException.ERR_MSG_NOTEXT;
import static org.jsonurl.SyntaxException.ERR_MSG_STILLOPEN;

import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A JSON&#x2192;URL text parser.
 * 
 * <p>An instance of this class may be used to parse JSON&#x2192;URL text and
 * instantiate JVM heap objects. The interface allows you to control the
 * types and values of the Objects created.
 *
 * <p>Normally this class isn't used by directly but rather it's subclassed
 * with specific types and an instance of
 * {@link org.jsonurl.ValueFactory ValueFactory}. If you don't need
 * control over types you may use
 * {@link org.jsonurl.j2se.JsonUrlParser JsonUrlParser}. 
 *
 * @param V value type (any JSON value)
 * @param C composite type (array or object)
 * @param ABT array builder type
 * @param A array type
 * @param JBT object builder type
 * @param J object type
 * @param B boolean type
 * @param M number type
 * @param N null type
 * @param S string type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
@SuppressWarnings("PMD")
public class Parser<
        V,
        C extends V,
        ABT,
        A extends C,
        JBT,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> {

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
     * A {@code Parser} with transparent array and object builders.
     *
     * <p>A {@code Parser} whose JSON array and JSON array builder are the
     * same instance of the same class, and whose JSON object and JSON object
     * builder are the same instance of the same class. 
     *
     * @param V value type (any JSON value)
     * @param C composite type (array or object)
     * @param A array type
     * @param J object type
     * @param B boolean type
     * @param M number type
     * @param N null type
     * @param S string type
     *
     * @author jsonurl.org
     * @author David MacCormack
     * @since 2019-09-01
     */
    public static class TransparentBuilder<
            V,
            C extends V,
            A extends C,
            J extends C,
            B extends V,
            M extends V,
            N extends V,
            S extends V> extends Parser<V,C,A,A,J,J,B,M,N,S> {

        /**
         * Instantiate a new Parser.
         *
         * @param factory a valid ValueFactory
         */
        public TransparentBuilder(ValueFactory<V,C,A,A,J,J,B,M,N,S> factory) {
            super(factory);
        }
    }

    public static final int DEFAULT_MAX_PARSE_CHARS = 1 << 13;
    public static final int DEFAULT_MAX_PARSE_DEPTH = 1 << 4;
    public static final int DEFAULT_MAX_PARSE_VALUES = 1 << 10;
    
    private static final EnumSet<ValueType> TYPE_VALUE_ANY =
        EnumSet.allOf(ValueType.class);

    private static final EnumSet<ValueType> TYPE_VALUE_OBJECT =
        EnumSet.of(ValueType.OBJECT);

    private static final EnumSet<ValueType> TYPE_VALUE_ARRAY =
        EnumSet.of(ValueType.ARRAY);

    private StringBuilder buf = new StringBuilder(64);
    private int maxParseDepth = DEFAULT_MAX_PARSE_DEPTH;
    private int maxParseChars = DEFAULT_MAX_PARSE_CHARS;
    private int maxParseValues = DEFAULT_MAX_PARSE_VALUES;
    private ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;

    private C emptyValue;

    /**
     * Instantiate a new Parser.
     *
     * @param factory a valid ValueFactory
     */
    public Parser(ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        this.factory = factory;
        this.emptyValue = factory.getEmptyComposite();
    }

    public int getMaxParseChars() {
        return maxParseChars;
    }

    public int getMaxParseDepth() {
        return maxParseDepth;
    }

    public int getMaxParseValues() {
        return maxParseValues;
    }

    /**
     * increment a limit value, throwing an exception if the limit is reached.
     */
    private int incrementLimit(String msg, int c, int pos) {
        if (++c > this.maxParseValues) {
            throw new LimitException(msg, pos);
        }
        return c;
    }

    /**
     * Parse a character sequence.
     *
     * <p>This is equivalent to {@link #parseObject(CharSequence, int, int)
     * parseObject(s, 0, s.length())}.
     * @return a factory-typed JSON Object
     */
    @SuppressWarnings("unchecked")
    public J parseObject(CharSequence s) {
        return (J)parse(s, 0, s.length(), TYPE_VALUE_OBJECT);
    }

    /**
     * Parse a character sequence.
     *
     * <p>Parse the given JSON&#x2192;URL text and return an object.
     * @param s the text to be parsed
     * @param off offset of the first character to be parsed
     * @param length the number of characters to be parsed
     * @return a factory-typed JSON Object  
     */
    @SuppressWarnings("unchecked")
    public J parseObject(CharSequence s, int off, int length) {
        return (J)parse(s, off, length, TYPE_VALUE_OBJECT);
    }

    /**
     * Parse a character sequence.
     *
     * <p>This is equivalent to {@link #parseArray(CharSequence, int, int)
     * parseObject(s, 0, s.length())}.
     * @return a factory-typed JSON Object  
     */
    @SuppressWarnings("unchecked")
    public A parseArray(CharSequence s) {
        return (A)parse(s, 0, s.length(), TYPE_VALUE_ARRAY);
    }

    /**
     * Parse a character sequence.
     *
     * <p>Parse the given JSON&#x2192;URL text and return an array.
     * @param s the text to be parsed
     * @param off offset of the first character to be parsed
     * @param length the number of characters to be parsed
     * @return a factory-typed JSON Object  
     */
    @SuppressWarnings("unchecked")
    public A parseArray(CharSequence s, int off, int length) {
        return (A)parse(s, off, length, TYPE_VALUE_ARRAY);
    }

    /**
     * Parse a character sequence.
     *
     * <p>This simply calls {@link #parse(CharSequence, int, int)
     * parse(s,0,s.length())}.
     * @see #parse(CharSequence, int, int)
     */
    public V parse(CharSequence s) {
        return parse(s, 0, s.length(), (EnumSet<ValueType>)null);
    }

    /**
     * Parse a character sequence.
     *
     * <p>Parse the given JSON&#x2192;URL text and return a typed value.
     * @param s the text to be parsed
     * @param off offset of the first character to be parsed
     * @param length the number of characters to be parsed
     * @return a factory-typed value  
     */
    public V parse(CharSequence s, int off, int length) {
        return parse(s, off, length, (EnumSet<ValueType>)null);
    }
    
    /**
     * Parse a character sequence. Simple calls
     * {@link #parse(CharSequence, int, int, Set)
     * parse(s, off, length, EnumSet.of(canReturn))}.
     */
    public V parse(CharSequence s, int off, int length, ValueType canReturn) {
        return parse(s, off, length, EnumSet.of(canReturn));
    }

    /**
     * Parse a character sequence. Simple calls
     * {@link #parse(CharSequence, int, int, Set)
     * parse(s, off, length, EnumSet.of(canReturn))}.
     */
    public V parse(CharSequence s, ValueType canReturn) {
        return parse(s, 0, s.length(), EnumSet.of(canReturn));
    }

    /**
     * Parse a character sequence.
     *
     * <p>Parse the given JSON&#x2192;URL text and return a typed value.
     * @param s the text to be parsed
     * @param off offset of the first character to be parsed
     * @param length the number of characters to be parsed
     * @param canReturn set of allowed return types
     * @return a factory-typed value
     */
    public V parse(
            CharSequence s,
            int off,
            int length,
            Set<ValueType> canReturn) {

        if (length == 0) {
            throw new SyntaxException(ERR_MSG_NOTEXT, 0);
        }
        if (length >= this.maxParseChars) {
            throw new LimitException(ERR_MSG_LIMIT_MAX_PARSE_CHARS);
        }
        if (canReturn == null) {
            canReturn = TYPE_VALUE_ANY;
        }

        NumberBuilder numb = new NumberBuilder();

        char c = s.charAt(off);

        if (c != '(') {
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
            int litlen = parseLiteralLength(s, off, off + length, null);
            if (litlen != length) {
                throw new SyntaxException(ERR_MSG_EXPECT_LITERAL);
            }

            V ret = literal(buf, numb, s, off, off + length, factory);

            if (!factory.isValid(canReturn, ret)) {
                throw new SyntaxException(
                    String.format("%s: %s", ERR_MSG_EXPECT_TYPE, canReturn));
            }

            return ret;
        }

        LinkedList<State> stateStack = new LinkedList<>();
        Deque<V> valueStack = new LinkedList<>();
        Deque<String> keyStack = new LinkedList<>();
        Deque<Object> builderStack = new LinkedList<>();
        int parseDepth = 1;
        int parseValueCount = 0;
        final int stop = off + length;
        boolean isDoneTypeCheck = false;

        stateStack.push(State.PAREN);

        int pos = off + 1;

        for (;;) {
            if (pos == stop) {
                throw new SyntaxException(ERR_MSG_STILLOPEN, pos);
            }

            c = s.charAt(pos);

            switch (stateStack.peek()) {
            case PAREN:
                switch (c) {
                case '(':
                    //
                    // found two back-to-back open parens. We know the first,
                    // paren is starting an array.
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.ARRAY)) {
                            throw new SyntaxException(
                                String.format("%s: %s",
                                    ERR_MSG_EXPECT_TYPE,
                                    canReturn));
                        }
                        isDoneTypeCheck = true;
                    }

                    parseValueCount = incrementLimit(
                            ERR_MSG_LIMIT_MAX_PARSE_VALUES,
                            parseValueCount,
                            pos);

                    stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                    stateStack.push(State.PAREN);
                    builderStack.push(factory.newArrayBuilder());

                    if (++parseDepth > this.maxParseDepth) {
                        throw new LimitException(
                                ERR_MSG_LIMIT_MAX_PARSE_DEPTH, pos);
                    }
                    pos++;
                    continue;

                case ')':
                    //
                    // found open paren followed by close paren; the empty
                    // composite value.
                    //
                    if (!isDoneTypeCheck) {
                        if (!ValueType.containsComposite(canReturn)) {
                            throw new SyntaxException(
                                String.format("%s: %s",
                                    ERR_MSG_EXPECT_TYPE,
                                    canReturn),
                                pos);
                        }
                        isDoneTypeCheck = true;
                    }

                    if (--parseDepth == 0) {
                        if (pos + 1 != stop) {
                            throw new SyntaxException(ERR_MSG_EXTRACHARS, pos);   
                        }

                        //
                        // In theory should return valueStack.peek() here, 
                        // but if parseDepth is zero here then I know I can
                        // simply return the emptyValue.
                        //
                        return emptyValue;
                    }

                    parseValueCount = incrementLimit(
                            ERR_MSG_LIMIT_MAX_PARSE_VALUES,
                            parseValueCount,
                            pos);

                    stateStack.pop();
                    valueStack.push(this.emptyValue);
                    pos++;
                    continue;

                default:
                    break;
                }

                //
                // paren followed by a literal.  I need to lookahead
                // one token to see if this is an object or array.
                //
                int litlen = parseLiteralLength(s, pos, stop, ERR_MSG_STILLOPEN);

                parseValueCount = incrementLimit(
                        ERR_MSG_LIMIT_MAX_PARSE_VALUES,
                        parseValueCount,
                        pos);

                int litpos = pos;
                pos += litlen;
                c = s.charAt(pos);

                switch (c) {
                case ',':
                    //
                    // multi-element array
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.ARRAY)) {
                            throw new SyntaxException(
                                String.format("%s: %s",
                                    ERR_MSG_EXPECT_TYPE,
                                    canReturn));
                        }
                        isDoneTypeCheck = true;
                    }

                    parseValueCount = incrementLimit(
                            ERR_MSG_LIMIT_MAX_PARSE_VALUES,
                            parseValueCount,
                            pos);

                    stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                    builderStack.push(factory.newArrayBuilder());
                    valueStack.push(literal(buf, numb, s, litpos, pos, factory));
                    continue;

                case ')':
                    //
                    // single element array
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.ARRAY)) {
                            throw new SyntaxException(
                                String.format("%s: %s",
                                    ERR_MSG_EXPECT_TYPE,
                                    canReturn),
                                pos);
                        }
                        isDoneTypeCheck = true;
                    }

                    ABT sea = factory.newArrayBuilder();
                    factory.add(sea, literal(buf, numb, s, litpos, pos, factory));
                    valueStack.push(factory.newArray(sea));

                    if (--parseDepth == 0) {
                        if (pos + 1 == stop) {
                            return valueStack.peek();
                        }
                        throw new SyntaxException(ERR_MSG_EXTRACHARS, pos);
                    }
                    stateStack.pop();
                    pos++;
                    continue;

                case ':':
                    //
                    // key name for object
                    //
                    if (!isDoneTypeCheck) {
                        if (!canReturn.contains(ValueType.OBJECT)) {
                            throw new SyntaxException(
                                String.format("%s: %s",
                                    ERR_MSG_EXPECT_TYPE,
                                    canReturn));
                        }
                        isDoneTypeCheck = true;
                    }

                    stateStack.set(0, State.OBJECT_HAVE_KEY);
                    builderStack.push(factory.newObjectBuilder());
                    keyStack.push(literalToJavaString(buf, numb, s, litpos, pos));
                    pos++;
                    continue;

                default:
                    throw new SyntaxException(ERR_MSG_EXPECT_LITERAL, pos);
                }

            case IN_ARRAY:
                if (c == '(') {
                    stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                    stateStack.push(State.PAREN);

                    parseDepth = incrementLimit(
                            ERR_MSG_LIMIT_MAX_PARSE_DEPTH,
                            parseDepth,
                            pos);
                    pos++;
                    continue;
                }

                litpos = pos;
                litlen = parseLiteralLength(s, pos, stop, ERR_MSG_STILLOPEN);

                parseValueCount = incrementLimit(
                        ERR_MSG_LIMIT_MAX_PARSE_VALUES,
                        parseValueCount,
                        pos);

                pos += litlen;

                stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
                valueStack.push(literal(buf, numb, s, litpos, pos, factory));
                continue;

            case ARRAY_AFTER_ELEMENT:
                V topval = valueStack.pop();

                @SuppressWarnings("unchecked")
                ABT destArray = (ABT)builderStack.peek();

                factory.add(destArray, topval);

                switch (c) {
                case ',':
                    stateStack.set(0, State.IN_ARRAY);
                    pos++;
                    continue;

                case ')':
                    @SuppressWarnings("unchecked")
                    ABT ab = (ABT)builderStack.pop();
                    valueStack.push(factory.newArray(ab));

                    if (--parseDepth == 0) {
                        if (pos + 1 == stop) {
                            return valueStack.peek();
                        }
                        throw new SyntaxException(ERR_MSG_EXTRACHARS, pos);
                    }
                    stateStack.pop();
                    pos++;
                    continue;
                default:
                    break;
                }
                throw new SyntaxException(ERR_MSG_EXPECT_STRUCTCHAR, pos);

            case OBJECT_HAVE_KEY:
                if (c == '(') {
                    stateStack.set(0, State.OBJECT_AFTER_ELEMENT);
                    stateStack.push(State.PAREN);

                    parseDepth = incrementLimit(
                            ERR_MSG_LIMIT_MAX_PARSE_DEPTH,
                            parseDepth,
                            pos);

                    pos++;
                    continue;
                }

                litpos = pos;
                litlen = parseLiteralLength(s, pos, stop, ERR_MSG_STILLOPEN);

                parseValueCount = incrementLimit(
                        ERR_MSG_LIMIT_MAX_PARSE_VALUES,
                        parseValueCount,
                        pos);

                pos += litlen;

                stateStack.set(0, State.OBJECT_AFTER_ELEMENT);
                valueStack.push(literal(buf, numb, s, litpos, pos, factory));
                continue;

            case OBJECT_AFTER_ELEMENT:
                topval = valueStack.pop();
                String key = keyStack.pop();

                @SuppressWarnings("unchecked") JBT jb = (JBT)builderStack.peek();
                factory.put(jb, key, topval);

                switch (c) {
                case ',':
                    stateStack.set(0, State.IN_OBJECT);
                    pos++;
                    continue;

                case ')':
                    builderStack.pop();
                    valueStack.push(factory.newObject(jb));

                    if (--parseDepth == 0) {
                        if (pos + 1 == stop) {
                            return valueStack.peek();
                        }
                        throw new SyntaxException(ERR_MSG_EXTRACHARS, pos);
                    }
                    stateStack.pop();
                    pos++;
                    continue;
                default:
                    break;
                }
                throw new SyntaxException(ERR_MSG_EXPECT_STRUCTCHAR, pos);

            case IN_OBJECT:
                litpos = pos;
                litlen = parseLiteralLength(s, pos, stop, ERR_MSG_STILLOPEN);
                pos += litlen;

                c = s.charAt(pos);
                if (c != ':') {
                    throw new SyntaxException(ERR_MSG_EXPECT_OBJVALUE, pos);
                }

                stateStack.set(0, State.OBJECT_HAVE_KEY);
                keyStack.push(literalToJavaString(buf, numb, s, litpos, pos));
                pos++;
                continue;
            }
        }
    }

    public void setMaxParseChars(int maxParseChars) {
        this.maxParseChars = maxParseChars;
    }

    public void setMaxParseDepth(int maxParseDepth) {
        this.maxParseDepth = maxParseDepth;
    }

    public void setMaxParseValues(int maxParseValues) {
        this.maxParseValues = maxParseValues;
    }
}
