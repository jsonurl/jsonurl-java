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

import static org.jsonurl.JsonUrl.Parse.literal;
import static org.jsonurl.JsonUrl.Parse.literalToJavaString;
import static org.jsonurl.JsonUrl.Parse.newNumberBuilder;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

/**
 * Used by {@link Parser#parse(CharSequence, int, int, ValueType, ValueFactory)}
 * to parse JSON&#x2192;URL text for a {@link ValueFactory}.
 */
class ValueFactoryParseResultFacade<V,
        C extends V,
        ABT, // NOPMD - GenericsNaming
        A extends C,
        JBT, // NOPMD - GenericsNaming
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> implements ParseResultFacade<V> {

    /**
     * object key stack.
     */
    private final Deque<String> keyStack = new LinkedList<>();

    /**
     * stack of factory-provided values.
     */
    private final Deque<V> factoryValueStack = new LinkedList<>();
    
    /**
     * stack of factory-defined ABT and JBT instances.
     */
    private final Deque<Object> builderStack = new LinkedList<>();

    /**
     * reusable buffer.
     *
     * <p>PMD warning suppressed because this is a buffer that's reset after
     * every use.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder buf = new StringBuilder(64);

    /**
     * reusable number builder.
     */
    private final NumberBuilder numb;

    /**
     * ValueFactory.
     */
    private final ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;

    /**
     * True if this facade holds an implied array result.
     */
    private boolean impliedArray;

    /**
     * True if this facade holds an implied object result.
     */
    private boolean impliedObject;

    /**
     * Create a new ValueFactoryParseResultFacade.
     */
    public ValueFactoryParseResultFacade(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
            ABT impliedArray,
            JBT impliedObject) {

        this.factory = factory;
        this.numb = newNumberBuilder(factory);
 
        if (impliedArray != null) {
            this.impliedArray = true;
            builderStack.push(impliedArray);
        }

        if (impliedObject != null) {
            this.impliedObject = true;
            builderStack.push(impliedObject);
        }
    }

    @Override
    public ParseResultFacade<V> beginArray() {
        builderStack.push(factory.newArrayBuilder());
        return this;
    }

    @Override
    public ParseResultFacade<V> endArray() {
        @SuppressWarnings("unchecked") //NOPMD
        ABT builder = (ABT)builderStack.pop();
        factoryValueStack.push(factory.newArray(builder));
        return this;
    }

    @Override
    public ParseResultFacade<V> beginObject() {
        builderStack.push(factory.newObjectBuilder());
        return this;
    }

    @Override
    public ParseResultFacade<V> endObject() {
        @SuppressWarnings("unchecked")
        JBT builder = (JBT)builderStack.pop();
        factoryValueStack.push(factory.newObject(builder));
        return this;
    }

    @Override
    public C getEmptyComposite() {
        return factory.getEmptyComposite();
    }

    @Override
    public void addEmptyComposite() {
        factoryValueStack.push(factory.getEmptyComposite());
    }

    @Override
    public void addLiteral(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK) {

        factoryValueStack.push(
            literal(buf, numb, text, start, stop, factory, isEmptyUnquotedStringOK));
    }

    @Override
    public void addSingleElementArray(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK) {
        ABT sea = factory.newArrayBuilder();

        factory.add(sea, literal(
            buf, numb, text, start, stop, factory, isEmptyUnquotedStringOK));

        factoryValueStack.push(factory.newArray(sea));                
    }

    @Override
    public void addObjectKey(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK) {
        keyStack.push(literalToJavaString(
            buf, numb, text, start, stop, isEmptyUnquotedStringOK));                
    }

    @Override
    public ParseResultFacade<V> addArrayElement() {
        V topval = factoryValueStack.pop();

        @SuppressWarnings("unchecked")
        ABT destArray = (ABT)builderStack.peek();

        factory.add(destArray, topval);
        return this;
    }

    @Override
    public ParseResultFacade<V> addObjectElement() {
        V topval = factoryValueStack.pop();
        String key = keyStack.pop();

        @SuppressWarnings("unchecked")
        JBT builder = (JBT)builderStack.peek();

        factory.put(builder, key, topval);
        return this;
    }

    @Override
    public V getResult() {
        //
        // using getLast() rather than peek() because an implied value
        // means that the stack could have more than one element, and the
        // top element is not the result
        //
        return factoryValueStack.getLast();
    }

    @Override
    public V getResult(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK) {
        return literal(buf, numb, text, start, stop, factory, isEmptyUnquotedStringOK);
    }

    @Override
    public boolean isValid(Set<ValueType> canReturn, V result) {
        return factory.isValid(canReturn, result);
    }

    @Override
    public ParseResultFacade<V> setLocation(int location) {
        //
        // this class doesn't throw any exceptions so I don't need to
        // store the location
        //
        return this;
    }

    @Override
    public boolean isImpliedArray() {
        return impliedArray;
    }

    @Override
    public boolean isImpliedObject() {
        return impliedObject;
    }
}
