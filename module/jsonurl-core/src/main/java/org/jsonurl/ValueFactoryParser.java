/*
 * Copyright 2020 David MacCormack
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

/**
 * A JSON&#x2192;URL text parser bound to a {@link ValueFactory}.
 * 
 * <p>An instance of this class may be used to parse JSON&#x2192;URL text and
 * instantiate JVM heap Objects. The interface allows you to control the
 * types and values of the Objects. This is really just a convenience wrapper
 * around {@link Parser}. It accepts a {@link ValueFactory} in the
 * constructor and uses it whenever a {@code ValueFactory} is called for.
 * 
 * <p>Normally this class isn't used by directly but rather it's subclassed
 * with specific types and an instance of
 * {@link org.jsonurl.ValueFactory ValueFactory}.
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
 */
public class ValueFactoryParser<
        V,
        C extends V,
        ABT, // NOPMD - GenericsNaming
        A extends C,
        JBT, // NOPMD - GenericsNaming
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> extends Parser {

    /**
     * This parser's ValueFactory.
     */
    private final ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;
     
    /**
     * A {@link ValueFactoryParser} with {@link ValueFactory.TransparentBuilder
     * transparent} array and object builders.
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
     * @see org.jsonurl.ValueFactory.TransparentBuilder
     */
    public static class TransparentBuilder<
        V,
        C extends V,
        A extends C,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> extends ValueFactoryParser<V,C,A,A,J,J,B,M,N,S> {

        /**
         * Instantiate a new Parser.
         * @param factory a valid ValueFactory
         */
        public TransparentBuilder(ValueFactory<V,C,A,A,J,J,B,M,N,S> factory) {
            super(factory);
        }
    }

    /**
     * Instantiate a new ValueFactoryParser.
     * @param factory a valid ValueFactory
     */
    public ValueFactoryParser(ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        this.factory = factory;
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link org.jsonurl.Parser#parseObject(CharSequence, int, int, ValueFactory)
     * parse(s, 0, s.length(), getFactory())}.
     */
    public J parseObject(CharSequence text) {
        return parseObject(text, 0, text.length(), factory);
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link org.jsonurl.Parser#parseObject(CharSequence, int, int, ValueFactory)
     * parse(s, off, length, getFactory())}.
     */
    public J parseObject(CharSequence text, int off, int length) {
        return super.parseObject(text, off, length, factory);
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link org.jsonurl.Parser#parseObject(CharSequence, int, int, ValueFactory, Object)
     * parse(s, 0, s.length(), getFactory(), impliedObject)}.
     */
    public J parseObject(CharSequence text, JBT impliedObject) {
        return super.parseObject(text, factory, impliedObject);
    }

    /**
     * Parse a character sequence as a JSON object. This simply calls
     * {@link org.jsonurl.Parser#parseObject(CharSequence, int, int, ValueFactory, Object)
     * parse(s, off, length, getFactory(), impliedObject)}.
     */
    public J parseObject(
                CharSequence text,
                int off,
                int length,
                JBT impliedObject) {
        return super.parseObject(text, off, length, factory, impliedObject);
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link org.jsonurl.Parser#parseArray(CharSequence, int, int, ValueFactory)
     * parse(s, 0, s.length(), getFactory())}.
     */
    public A parseArray(CharSequence text) {
        return super.parseArray(text, 0, text.length(), factory);
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link org.jsonurl.Parser#parseArray(CharSequence, int, int, ValueFactory)
     * parse(s, off, length, getFactory())}.
     */
    public A parseArray(CharSequence text, int off, int length) {
        return super.parseArray(text, off, length, factory);
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link org.jsonurl.Parser#parseArray(CharSequence, int, int, ValueFactory, Object)
     * parse(s, 0, s.length(), getFactory())}.
     */
    public A parseArray(CharSequence text, ABT impliedArray) {
        return super.parseArray(text, 0, text.length(), factory, impliedArray);
    }

    /**
     * Parse a character sequence as a JSON array. This simply calls
     * {@link org.jsonurl.Parser#parseArray(CharSequence, int, int, ValueFactory, Object)
     * parse(s, off, length, getFactory())}.
     */
    public A parseArray(
                CharSequence text,
                int off,
                int length,
                ABT impliedArray) {
        return super.parseArray(text, off, length, factory, impliedArray);
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), null, factory)}.
     */
    public V parse(CharSequence text) {
        return parse(text, 0, text.length(), (EnumSet<ValueType>)null, factory);
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, null, factory)}.  
     */
    public V parse(CharSequence text, int off, int length) {
        return parse(text, off, length, (EnumSet<ValueType>)null, factory);
    }
    
    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, off, length, EnumSet.of(canReturn), factory)}.
     */
    public V parse(
                CharSequence text,
                int off,
                int length,
                ValueType canReturn) {
        return parse(text, off, length, EnumSet.of(canReturn), factory);
    }

    /**
     * Parse a character sequence. This simply calls
     * {@link #parse(CharSequence, int, int, ValueType, ValueFactory)
     * parse(s, 0, s.length(), EnumSet.of(canReturn), factory)}.
     */
    public V parse(CharSequence text, ValueType canReturn) {
        return parse(text, 0, text.length(), EnumSet.of(canReturn), factory);
    }

    /**
     * Get the associated ValueFactory.
     */
    public ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> getFactory() {
        return factory;
    }
}
