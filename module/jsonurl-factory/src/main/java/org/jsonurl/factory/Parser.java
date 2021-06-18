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

package org.jsonurl.factory;

import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOptionable;
import org.jsonurl.stream.CharIterator;
import org.jsonurl.stream.JsonUrlCharSequence;
import org.jsonurl.stream.JsonUrlIterator;

/**
 * A <a href="https://jsonurl.org/">JSON&#x2192;URL</a> text parser.
 *
 * <p>An instance of this interface may be used to parse JSON&#x2192;URL
 * text to build values bound to a specific data model API. Each instance
 * maintains a set of limits that are applied any time the one of the
 * {@code parse()} methods is invoked.
 * 
 * @param <V> value type (any JSON value)
 * @param <C> composite type (array or object)
 * @param <A> array type
 * @param <ABT> array builder type
 * @param <J> object type
 * @param <JBT> object builder type
 * @param <B> boolean type
 * @param <M> number type
 * @param <N> null type
 * @param <S> string type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
@SuppressWarnings({"PMD.GenericsNaming", "java:S119"}) // See SuppressWarnings.md
public interface Parser<
        V,
        C extends V,
        ABT, // NOPMD - GenericsNaming
        A extends C,
        JBT, // NOPMD - GenericsNaming
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> extends JsonUrlOptionable {

    /**
     * A {@link Parser} whose array builder is the same type as its array
     * and object builder is the same type as its object.
     *
     * @param <V> value type (any JSON value)
     * @param <C> composite type (array or object)
     * @param <A> array type
     * @param <J> object type
     * @param <B> boolean type
     * @param <M> number type
     * @param <N> null type
     * @param <S> string type
     *
     * @see org.jsonurl.factory.ValueFactory.TransparentBuilder
     */
    interface TransparentBuilder<
        V,
        C extends V,
        A extends C,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> extends Parser<V,C,A,A,J,J,B,M,N,S> {
    }

    /**
     * Get the parser limits.
     */
    JsonUrlLimits limits();

    /**
     * Get the parser factory.
     */
    ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory();

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseObject(CharSequence, int, int, Object, MissingValueProvider)
     */
    default J parseObject(CharSequence text) {
        return parseObject(text, 0, text.length(), null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseObject(CharSequence, int, int, Object, MissingValueProvider)
     */
    default J parseObject(
            CharSequence text,
            int off,
            int length) {
        return parseObject(text, off, length, null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseObject(CharSequence, int, int, Object, MissingValueProvider)
     */
    default J parseObject(
            CharSequence text,
            JBT impliedObject) {
        return parseObject(text, 0, text.length(), impliedObject, null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseObject(CharSequence, int, int, Object, MissingValueProvider)
     */
    default J parseObject(
            CharSequence text,
            JBT impliedObject,
            MissingValueProvider<V> mvp) {
        return parseObject(text, 0, text.length(), impliedObject, mvp);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseObject(CharSequence, int, int, Object, MissingValueProvider)
     */
    default J parseObject(
            CharSequence text,
            int off,
            int length,
            JBT impliedObject) {
        return parseObject(text, off, length, impliedObject, null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @param text JSON&#x2192;URL text to be parsed
     * @param off offset in {@code text} to start parsing
     * @param length number of characters in {@code text} to parse
     * @param impliedObject a valid factory object or {@code null}
     * @param mvp a valid MissingValueProvider or {@code null}
     * @see #parseObject(JsonUrlIterator, Object, MissingValueProvider)
     */
    default J parseObject(
            CharSequence text,
            int off,
            int length,
            JBT impliedObject,
            MissingValueProvider<V> mvp) {
        //
        // PMD - CloseResource - false posite
        //
        CharIterator chars = new JsonUrlCharSequence(//NOPMD
            null,
            text,
            off,
            length,
            JsonUrlLimits.getMaxParseChars(limits()));

        return parseObject(chars, impliedObject, mvp);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @param iter a valid CharIterator
     * @param impliedObject a valid factory object or {@code null}
     * @param mvp a valid MissingValueProvider or {@code null}
     * @see #parseObject(JsonUrlIterator, Object, MissingValueProvider)
     */
    default J parseObject(
            CharIterator iter,
            JBT impliedObject,
            MissingValueProvider<V> mvp) {

        return parseObject(
            JsonUrlIterator.newInstance(iter, limits(), options()),
            impliedObject,
            mvp);
    }

    /**
     * Parse a sequence of JSON&#x2192;URL events.
     * @param iter a valid JsonUrlIterator
     * @param impliedObject a valid factory object or {@code null}
     * @param mvp a valid MissingValueProvider or {@code null}
     * @see <a href="https://github.com/jsonurl/specification/#292-implied-objects"
     * >JSON&#x2192;URL specification, section 2.9.2</a>
     * @see <a href="https://github.com/jsonurl/specification/#294-implied-object-missing-values"
     * >JSON&#x2192;URL specification, section 2.9.4</a>
     */
    J parseObject(
        JsonUrlIterator iter,
        JBT impliedObject,
        MissingValueProvider<V> mvp);

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseArray(CharSequence, int, int, Object, MissingValueProvider)
     */
    default A parseArray(CharSequence text) {
        return parseArray(text, 0, text.length(), null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseArray(CharSequence, int, int, Object, MissingValueProvider)
     */
    default A parseArray(
            CharSequence text,
            int off,
            int length) {
        return parseArray(text, off, length, null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseArray(CharSequence, int, int, Object, MissingValueProvider)
     */
    default A parseArray(
            CharSequence text,
            ABT impliedArray) {
        return parseArray(text, 0, text.length(), impliedArray, null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseArray(CharSequence, int, int, Object, MissingValueProvider)
     */
    default A parseArray(
            CharSequence text,
            ABT impliedArray,
            MissingValueProvider<V> mvp) {
        return parseArray(text, 0, text.length(), impliedArray, mvp);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @see #parseArray(CharSequence, int, int, Object, MissingValueProvider)
     */
    default A parseArray(
            CharSequence text,
            int off,
            int length,
            ABT impliedArray) {
        return parseArray(text, off, length, impliedArray, null);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @param text JSON&#x2192;URL text to be parsed
     * @param off offset in {@code text} to start parsing
     * @param length number of characters in {@code text} to parse
     * @param impliedArray a valid factory array or {@code null}
     * @param mvp a valid MissingValueProvider or {@code null}
     * @see #parseArray(JsonUrlIterator, Object, MissingValueProvider)
     */
    default A parseArray(
            CharSequence text,
            int off,
            int length,
            ABT impliedArray,
            MissingValueProvider<V> mvp) {

        //
        // PMD - CloseResource - false posite
        //
        CharIterator chars = new JsonUrlCharSequence(//NOPMD
            null,
            text,
            off,
            length,
            JsonUrlLimits.getMaxParseChars(limits()));

        return parseArray(chars, impliedArray, mvp);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @param iter a valid CharIterator
     * @param impliedArray a valid factory array or {@code null}
     * @param mvp a valid MissingValueProvider or {@code null}
     * @see #parseArray(JsonUrlIterator, Object, MissingValueProvider)
     */
    default A parseArray(
            CharIterator iter,
            ABT impliedArray,
            MissingValueProvider<V> mvp) {

        return parseArray(
            JsonUrlIterator.newInstance(iter, limits(), options()),
            impliedArray,
            mvp);
    }
    
    /**
     * Parse a sequence of JSON&#x2192;URL events.
     * @param iter a valid JsonUrlIterator
     * @param impliedArray a valid factory array or {@code null}
     * @param mvp a valid MissingValueProvider or {@code null}
     * @see <a href="https://github.com/jsonurl/specification/#291-implied-arrays"
     * >JSON&#x2192;URL specification, section 2.9.1</a>
     */
    A parseArray(
        JsonUrlIterator iter,
        ABT impliedArray,
        MissingValueProvider<V> mvp);

    /**
     * Parse the given JSON&#x2192;URL text.
     */
    default V parse(CharSequence text) {
        return parse(text, 0, text.length());
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     */
    default V parse(CharSequence text, int off, int length) {
        //
        // PMD - CloseResource - false posite
        //
        CharIterator chars = new JsonUrlCharSequence(//NOPMD
            null,
            text,
            off,
            length,
            JsonUrlLimits.getMaxParseChars(limits()));

        return parse(chars);
    }

    /**
     * Parse the given JSON&#x2192;URL text.
     * @param iter a valid CharIterator
     * @return a factory-typed value
     */
    default V parse(CharIterator iter) {
        return parse(
            JsonUrlIterator.newInstance(
                iter,
                limits(),
                options()));
    }

    /**
     * Parse the given JSON&#x2192;URL events.
     * @param iter a valid JSON&#x2192;URL event iterator
     * @return a factory-typed value
     */
    V parse(JsonUrlIterator iter);
}
