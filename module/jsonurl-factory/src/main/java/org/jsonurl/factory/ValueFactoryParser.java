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

import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.ValueType;
import org.jsonurl.stream.JsonUrlEvent;
import org.jsonurl.stream.JsonUrlIterator;

/**
 * Concrete implementation of {@link Parser}.
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
@SuppressWarnings({"PMD.GenericsNaming", "java:S119"}) // See SuppressWarnings.md
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
        S extends V> implements Parser<V,C,ABT,A,JBT,J,B,M,N,S> {

    /**
     * This parser's ValueFactory.
     */
    private final ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;

    /**
     * JsonUrlOptions.
     */
    private final Set<JsonUrlOption> options;

    /**
     * JsonUrlLimits.
     */
    private final JsonUrlLimits limits;
    
    /**
     * A {@link Parser} whose array builder is the same type as its array type
     * and object builder is the same type as its object type.
     *
     * @param <V> value type (any JSON value)
     * @param <C> composite type (array or object)
     * @param <A> array type
     * @param <J> object type
     * @param <B> boolean type
     * @param <M> number type
     * @param <N> null type
     * @param <S> string type
     */
    public static class TransparentBuilder<
        V,
        C extends V,
        A extends C,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V>
            extends ValueFactoryParser<V,C,A,A,J,J,B,M,N,S>
            implements Parser.TransparentBuilder<V,C,A,J,B,M,N,S> {

        /**
         * Create a new TransparentBuilder.
         */
        public TransparentBuilder(
                ValueFactory<V,C,A,A,J,J,B,M,N,S> factory,
                JsonUrlLimits limits,
                Set<JsonUrlOption> options) {
            super(factory, limits, options);
        }

    }

    /**
     * Create a new Parser.
     * @param factory a valid ValueFactory
     */
    public ValueFactoryParser(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {
        this.factory = factory;
        this.limits = limits;
        this.options = options;
    }

    @Override
    public Set<JsonUrlOption> options() {
        return options;
    }

    @Override
    public JsonUrlLimits limits() {
        return limits;
    }

    @Override
    public ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory() {
        return factory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public J parseObject(
            JsonUrlIterator iter,
            JBT impliedObject,
            MissingValueProvider<V> mvp) {

        if (impliedObject == null) {
            iter.setType(ValueType.OBJECT);

        } else {
            iter.setType(CompositeType.OBJECT);            
        }

        return (J)parse(
            iter,
            new ParseResult<>(factory, null, impliedObject, mvp));
    }

    @Override
    @SuppressWarnings("unchecked")
    public A parseArray(
            JsonUrlIterator iter,
            ABT impliedArray,
            MissingValueProvider<V> mvp) {

        if (impliedArray == null) {
            iter.setType(ValueType.ARRAY);

        } else {
            iter.setType(CompositeType.ARRAY);            
        }

        return (A)parse(
            iter,
            new ParseResult<>(factory, impliedArray, null, mvp));
    }

    @Override
    public V parse(JsonUrlIterator iter) {
        return parse(iter, new ParseResult<>(
            factory, null, null, null));
    }
    
    /**
     * Parse a character sequence.
     *
     * <p>This is the "real" parse function that all the convenience methods
     * call.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private V parse(
            JsonUrlIterator text,
            ParseResult<V,C,ABT,A,JBT,J,B,M,N,S> result) {

        for (;;) {
            JsonUrlEvent evt = text.next();

            switch (evt) {
            case END_ARRAY:
                result.endArray();
                break;
            case END_OBJECT:
                result.endObject();
                break;
            case END_STREAM:
                return result.getResult();
            case KEY_NAME:
                result.addObjectKey(text.getString());
                break;
            case START_ARRAY:
                result.beginArray();
                break;
            case START_OBJECT:
                result.beginObject();
                break;
            case VALUE_EMPTY_COMPOSITE:
                result.addEmptyComposite();
                break;
            case VALUE_EMPTY_LITERAL:
                result.addEmptyLiteral();
                break;
            case VALUE_FALSE:
                result.addFalse();
                break;
            case VALUE_MISSING:
                result.addMissingValue(text);
                break;
            case VALUE_NULL:
                result.addNull();
                break;
            case VALUE_NUMBER:
                result.addNumber(text.getNumberText());
                break;
            case VALUE_STRING:
                result.addString(text.getString());
                break;
            case VALUE_TRUE:
                result.addTrue();
                break;
            }
        }
    }
}
