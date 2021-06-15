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

package org.jsonurl.text;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A thing that can serialize the JSON&#x2192;URL data model. 
 * 
 * @param <R> Result type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface JsonTextBuilder<R> {

    /**
     * Begin an object.
     */
    JsonTextBuilder<R> beginObject() throws IOException;

    /**
     * End an object.
     */
    JsonTextBuilder<R> endObject() throws IOException;

    /**
     * Begin an array.
     */
    JsonTextBuilder<R> beginArray() throws IOException;

    /**
     * End an array.
     */
    JsonTextBuilder<R> endArray() throws IOException;

    /**
     * Separate two values.
     */
    JsonTextBuilder<R> valueSeparator() throws IOException;

    /**
     * Separate a name from its value.
     */
    JsonTextBuilder<R> nameSeparator() throws IOException;

    /**
     * Add a null value.
     */
    JsonTextBuilder<R> addNull() throws IOException;

    /**
     * Add a UNICODE codepoint.
     */
    JsonTextBuilder<R> addCodePoint(int codepoint) throws IOException;

    /**
     * Add a long value.
     */
    JsonTextBuilder<R> add(long value) throws IOException;

    /**
     * Add a double value.
     */
    JsonTextBuilder<R> add(double value) throws IOException;

    /**
     * Add a BigDecimal value.
     */
    JsonTextBuilder<R> add(BigDecimal value) throws IOException;

    /**
     * Add a BigInteger value.
     */
    JsonTextBuilder<R> add(BigInteger value) throws IOException;

    /**
     * Add a boolean value.
     */
    JsonTextBuilder<R> add(boolean value) throws IOException;

    /**
     * Add a string value.
     * @param text a valid CharSequence
     * @param start start index
     * @param end stop index
     * @param isKey true if this is an object key
     */
    JsonTextBuilder<R> add(
            CharSequence text,
            int start,
            int end,
            boolean isKey) throws IOException;

    /**
     * Add an object.
     * @param value an object or {@code null}.
     * @return this
     */
    JsonTextBuilder<R> add(Object value) throws IOException;

    /**
     * Add a number value.
     */
    default JsonTextBuilder<R> add(Number value) throws IOException {
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
     * Add an enum value.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, boolean)
     * add(value.name(), isKey)}.
     * @param value an enumerated value or null
     * @param isKey true if this is an object key 
     */
    default JsonTextBuilder<R> add(Enum<?> value, boolean isKey) throws IOException {
        if (value == null) {
            return addNull();
        }

        return add(value.name(), isKey);
    }

    /**
     * Add an enum value.
     *
     * <p>This is simply a convenience for
     * {@link #add(Enum, boolean)
     * add(value, false)}.
     * @param value an enumerated value or null 
     */
    default JsonTextBuilder<R> add(Enum<?> value) throws IOException {
        return add(value, false);    
    }

    /**
     * Add a string value.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(text,0,text.length(),false)}.
     * @param text the character sequence to add 
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<R> add(CharSequence text) throws IOException {
        return add(text, 0, text == null ? 0 : text.length(), false);
    }

    /**
     * Add a string value.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(text,0,text.length(),false)}.
     * @param text the character sequence to add 
     * @param isKey true if this is an object key
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<R> add(CharSequence text, boolean isKey) throws IOException {
        return add(text, 0, text.length(), isKey);
    }

    /**
     * Add a string value.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(text,start,end,false)}.
     * @param text the character sequence to add
     * @param start start index
     * @param end stop index
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<R> add(
            CharSequence text,
            int start,
            int end) throws IOException {
        return add(text, start, end, false);
    }

    /**
     * Add an object key.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(text,0,text.length(),true)}.
     * @param text the character sequence to add 
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<R> addKey(CharSequence text) throws IOException {
        return add(text, 0, text.length(), true);
    }

    /**
     * Add an object key.
     *
     * <p>This is simply a convenience for
     * {@link #add(CharSequence, int, int, boolean)
     * add(text,start,end,true)}.
     * @param text the character sequence to add 
     * @see #add(CharSequence, int, int, boolean)
     */
    default JsonTextBuilder<R> addKey(
            CharSequence text,
            int start,
            int end) throws IOException {
        return add(text, start, end, true);
    }

    /**
     * Add an empty composite value.
     *
     * <p>TODO: separate addEmptyObject() / addEmptyArray.
     * @return this
     */
    default JsonTextBuilder<R> addEmptyComposite() throws IOException {
        return beginObject().endObject();
    }

    /**
     * Build the result.
     * @return a valid object
     */
    R build();
}
