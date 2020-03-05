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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A JSONURLBuilder builds JSON text.
 *
 * <p>This interface implements the builder pattern. 
 * 
 * @param <A> Accumulator type
 * @param <R> Result type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface JsonTextBuilder<A,R> {

    /**
     * Begin an object.
     */
    public JsonTextBuilder<A,R> beginObject() throws IOException;

    /**
     * End an object.
     */
    public JsonTextBuilder<A,R> endObject() throws IOException;

    /**
     * Begin an array.
     */
    public JsonTextBuilder<A,R> beginArray() throws IOException;

    /**
     * End an array.
     */
    public JsonTextBuilder<A,R> endArray() throws IOException;

    /**
     * Separate two values.
     */
    public JsonTextBuilder<A,R> valueSeparator() throws IOException;

    /**
     * Separate a name from its value.
     */
    public JsonTextBuilder<A,R> nameSeparator() throws IOException;

    /**
     * Add a null value.
     */
    public JsonTextBuilder<A,R> addNull() throws IOException;

    /**
     * Add a long value.
     */
    public JsonTextBuilder<A,R> add(long value) throws IOException;

    /**
     * Add a double value.
     */
    public JsonTextBuilder<A,R> add(double value) throws IOException;

    /**
     * Add a BigDecimal value.
     */
    public JsonTextBuilder<A,R> add(BigDecimal value) throws IOException;

    /**
     * Add a BigInteger value.
     */
    public JsonTextBuilder<A,R> add(BigInteger value) throws IOException;
    
    /**
     * Add a boolean value.
     */
    public JsonTextBuilder<A,R> add(boolean value) throws IOException;

    /**
     * Add a string value.
     * @param start start index
     * @param end stop index
     * @param isKey true if this is an object key
     */
    public JsonTextBuilder<A,R> add(
            CharSequence s,
            int start,
            int end,
            boolean isKey) throws IOException;

    /**
     * Add a number value.
     */
    default JsonTextBuilder<A,R> add(Number value) throws IOException {
        if (value == null) {
            return addNull();
        }

        Class<? extends Number> clazz = NumberCoercionMap.getType(
                value.getClass());

        if (clazz == Long.class) {
            return add(value.longValue());
        }
        if (clazz == Double.class) {
            return add(value.doubleValue());
        }
        if (value instanceof BigDecimal) {
            return add((BigDecimal)value);
        }
        if (value instanceof BigInteger) {
            return add((BigInteger)value);
        }

        throw new NumberFormatException(value.toString());
    }

    /**
     * Add a string value.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(s,0,s.length(),false)}.
     * @param s the character sequence to add 
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<A,R> add(CharSequence s) throws IOException {
        return add(s, 0, s.length(), false);
    }

    /**
     * Add a string value.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(s,start,end,false)}.
     * @param s the character sequence to add
     * @param start start index
     * @param end stop index
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<A,R> add(
            CharSequence s,
            int start,
            int end) throws IOException {
        return add(s, start, end, false);
    }

    /**
     * Add an object key.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(s,0,s.length(),true)}.
     * @param s the character sequence to add 
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<A,R> addKey(CharSequence s) throws IOException {
        return add(s, 0, s.length(), true);
    }

    /**
     * Add an object key.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(s,start,end,true)}.
     * @param s the character sequence to add 
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<A,R> addKey(
            CharSequence s,
            int start,
            int end) throws IOException {
        return add(s, start, end, true);
    }

    /**
     * Add an empty composite value.
     * @return this
     */
    default JsonTextBuilder<A,R> addEmptyComposite() throws IOException {
        return beginObject().endObject();
    }

    public R build();
}
