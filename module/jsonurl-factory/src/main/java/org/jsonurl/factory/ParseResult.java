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

package org.jsonurl.factory;

import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_OBJECT_VALUE;

import java.util.Deque;
import java.util.LinkedList;
import org.jsonurl.CompositeType;
import org.jsonurl.ExceptionProvider;
import org.jsonurl.text.NumberText;

/**
 * Used by Parser.parse() to separate the logic of parsing the event
 * stream and building a result using a ValueFactory.
 */
class ParseResult<V,
        C extends V,
        ABT, // NOPMD - GenericsNaming
        A extends C,
        JBT, // NOPMD - GenericsNaming
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> {

    /**
     * object key stack.
     */
    private final Deque<String> keyStack = new LinkedList<>();

    /**
     * stack of factory-provided values.
     */
    private final Deque<V> valueStack = new LinkedList<>();
    
    /**
     * stack of factory-defined ABT and JBT instances.
     */
    private final Deque<CompositeBuilder> builderStack = new LinkedList<>();

    /**
     * ValueFactory.
     */
    private final ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;

    /**
     * A MissingValueProvider.
     */
    private final MissingValueProvider<V> missingValueProvider;
    
    /**
     * Implied composite type.
     */
    private final CompositeType impliedComposite;

    /**
     * An array or object builder.
     */
    private interface CompositeBuilder {
        /**
         * Add the value on the value stack.
         */
        void addValue();

        /**
         * Build the composite and put the result on the value stack.
         */
        void build();
    }

    /**
     * Concrete Builder implementation for objects.
     */
    private final class ObjectBuilder implements CompositeBuilder {
        /**
         * object builder for target API.
         */
        private final JBT builder;

        ObjectBuilder(JBT builder) {
            this.builder = builder;
        }

        @Override
        public void addValue() {
            V topval = valueStack.pop();
            String key = keyStack.pop();
            
            //
            // PMD - AccessorMethodGeneration
            //
            factory.put(builder, key, topval); // NOPMD
        }

        @Override
        public void build() {
            //
            // PMD - AccessorMethodGeneration
            //
            valueStack.push(factory.newObject(builder)); // NOPMD
        }
    }

    /**
     * Concrete Builder implementation for objects.
     */
    private final class ArrayBuilder implements CompositeBuilder {
        /**
         * array builder for target API.
         */
        private final ABT builder;

        ArrayBuilder(ABT builder) {
            this.builder = builder;
        }

        @Override
        public void addValue() {
            factory.add(builder, valueStack.pop()); // NOPMD
        }

        @Override
        public void build() {
            valueStack.push(factory.newArray(builder)); // NOPMD
        }
    }

    /**
     * Create a new ValueFactoryParseResultFacade.
     */
    public ParseResult(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory,
            ABT impliedArray,
            JBT impliedObject,
            MissingValueProvider<V> missingValueProvider) {

        this.factory = factory;
        this.missingValueProvider = missingValueProvider == null
                ? this::defaultMissingValueProvier : missingValueProvider;

        if (impliedArray != null) { // NOPMD - ConfusingTernary
            builderStack.push(new ArrayBuilder(impliedArray));
            impliedComposite = CompositeType.ARRAY;

        } else if (impliedObject != null) { // NOPMD - ConfusingTernary
            builderStack.push(new ObjectBuilder(impliedObject));
            impliedComposite = CompositeType.OBJECT;

        } else {
            impliedComposite = null;
        }
    }

    /**
     * Begin an array.
     */
    public void beginArray() {
        builderStack.push(new ArrayBuilder(factory.newArrayBuilder()));
    }

    /**
     * End an array.
     */
    public void endArray() {
        endComposite();
    }

    /**
     * Begin an object.
     */
    public void beginObject() {
        builderStack.push(new ObjectBuilder(factory.newObjectBuilder()));
    }

    /**
     * End an object.
     */
    public void endObject() {
        endComposite();
    }

    /**
     * Add an empty composite value.
     */
    public void addEmptyComposite() {
        valueStack.push(factory.getEmptyComposite());
        addValue();
    }

    /**
     * Add an empty literal value.
     */
    public void addEmptyLiteral() {
        valueStack.push(factory.getString(""));
        addValue();
    }

    /**
     * Add a true literal.
     */
    public void addTrue() {
        valueStack.push(factory.getTrue());
        addValue();
    }

    /**
     * Add a false literal.
     */
    public void addFalse() {
        valueStack.push(factory.getFalse());
        addValue();
    }

    /**
     * Add a null literal.
     */
    public void addNull() {
        valueStack.push(factory.getNull());
        addValue();
    }

    /**
     * Add a string literal.
     */
    public void addString(String text) {
        valueStack.push(factory.getString(text));
        addValue();
    }

    /**
     * Add a number literal.
     */
    public void addNumber(NumberText text) {
        valueStack.push(factory.getNumber(text));
        addValue();
    }

    /**
     * Add an object key.
     */
    public void addObjectKey(String text) {
        keyStack.push(text);                
    }

    /**
     * Add a missing value.
     */
    public void addMissingValue(ExceptionProvider exp) {
        valueStack.push(missingValueProvider.getValue(keyStack.peek(), exp));
        addValue();
    }

    /**
     * Get the result.
     */
    public V getResult() {
        if (impliedComposite == CompositeType.ARRAY) {
            endArray();
        } else if (impliedComposite == CompositeType.OBJECT) {
            endObject();
        }

        //
        // using getLast() rather than peek() because an implied value
        // means that the stack could have more than one element, and the
        // top element is not the result
        //
        // return factoryValueStack.getLast();
        return valueStack.peek();
    }

    /**
     * default implementation of MissingValueProvider.
     */
    private V defaultMissingValueProvier(String key, ExceptionProvider exp) {
        throw exp.newSyntaxException(
            MSG_EXPECT_OBJECT_VALUE,
            String.format("%s: %s", MSG_EXPECT_OBJECT_VALUE, key));
    }

    /**
     * End the current composite.
     */
    private void endComposite() {
        CompositeBuilder builder = builderStack.pop();
        builder.build();
        addValue();
    }

    /**
     * Add the top of the stack to the current composite.
     */
    private void addValue() {
        CompositeBuilder builder = builderStack.peek();

        if (builder != null) {
            builder.addValue();    
        }
    }
}
