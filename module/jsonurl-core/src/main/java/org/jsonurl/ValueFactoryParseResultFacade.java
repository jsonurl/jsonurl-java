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
import static org.jsonurl.JsonUrlOptions.isSkipNulls;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_OBJECT_VALUE;

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
     * The current parse position.
     */
    private int position;

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
     * A MissingValueProvider.
     */
    private final MissingValueProvider<V> missingValueProvider;
    
    /**
     * JsonUrlOptions.
     */
    private final JsonUrlOptions options;

    /**
     * Create a new ValueFactoryParseResultFacade.
     */
    public ValueFactoryParseResultFacade(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
            JsonUrlOptions options,
            ABT impliedArray,
            JBT impliedObject,
            MissingValueProvider<V> missingValueProvider) {

        this.factory = factory;
        this.options = options;
        this.numb = newNumberBuilder(factory);
        this.missingValueProvider = missingValueProvider == null
                ? this::defaultMissingValueProvier : missingValueProvider;

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
            int stop) {

        factoryValueStack.push(
            literal(
                    buf,
                    numb,
                    text,
                    start,
                    stop,
                    factory,
                    options));
    }

    @Override
    public void addSingleElementArray(
            CharSequence text,
            int start,
            int stop) {
        
        V val = literal(
                buf,
                numb,
                text,
                start,
                stop,
                factory,
                options);

        ABT sea = factory.newArrayBuilder();

        boolean skip = factory.isNull(val) && isSkipNulls(options);        
        if (!skip) {
            //
            // note that this produces an empty array rather than the empty
            // composite value. This is really an implementation choice -- I
            // think either would be "correct". I chose this because it isn't
            // ambiguous; we know it's an array by the syntax, even if it's
            // empty.
            //
            factory.add(sea, val);
        }

        factoryValueStack.push(factory.newArray(sea));                
    }

    @Override
    public void addObjectKey(
            CharSequence text,
            int start,
            int stop) {
        keyStack.push(parseKey(
                text,
                start,
                stop,
                options));                
    }

    @Override
    public ParseResultFacade<V> addArrayElement() {
        V topval = factoryValueStack.pop();

        boolean skip = factory.isNull(topval) && isSkipNulls(options);

        if (!skip) {
            @SuppressWarnings("unchecked")
            ABT destArray = (ABT)builderStack.peek();
    
            factory.add(destArray, topval);
        }
        return this;
    }

    @Override
    public ParseResultFacade<V> addObjectElement() {
        V topval = factoryValueStack.pop();
        String key = keyStack.pop();

        boolean skip = factory.isNull(topval) && isSkipNulls(options);

        if (!skip) {
            @SuppressWarnings("unchecked")
            JBT builder = (JBT)builderStack.peek();
    
            factory.put(builder, key, topval);
        }
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
            int stop) {

        return literal(
                buf,
                numb,
                text,
                start,
                stop,
                factory,
                options);
    }

    @Override
    public boolean isValid(Set<ValueType> canReturn, V result) {
        return factory.isValid(canReturn, result);
    }

    @Override
    public ParseResultFacade<V> setLocation(int location) {
        this.position = location;
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

    @Override
    public ParseResultFacade<V> addMissingValue(
            CharSequence text,
            int start,
            int stop) {

        final String key = parseKey(
                text,
                start,
                stop,
                options);

        keyStack.push(key);
        factoryValueStack.push(missingValueProvider.getValue(key, position));

        return this;
    }

    /**
     * Parse the object key.
     */
    private String parseKey(
            CharSequence text,
            int start,
            int stop,
            JsonUrlOptions options) {

        return literalToJavaString(
                buf,
                numb,
                text,
                start,
                stop,
                options);
    }

    /**
     * default implementation of MissingValueProvider.
     */
    private V defaultMissingValueProvier(String key, int pos) {
        throw new SyntaxException(
                MSG_EXPECT_OBJECT_VALUE,
                String.format("%s: %s", MSG_EXPECT_OBJECT_VALUE, key),
                pos);
    }
}
