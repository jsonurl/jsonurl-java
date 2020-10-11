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

package org.jsonurl.j2se;

import java.io.IOException;
import java.util.Map;
import org.jsonurl.JsonTextBuilder;

/**
 * A utility class for serializing J2SE values as JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
public final class JsonUrlWriter { // NOPMD
    
    private JsonUrlWriter() {
        // EMPTY
    }
    
    private static boolean isNull(Object obj) {
        return obj == null || obj == JavaValueFactory.NULL;
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     */
    public static <A,R> void write(// NOPMD
            JsonTextBuilder<A,R> dest,
            Object value) throws IOException {

        if (isNull(value)) {
            dest.addNull();
            return;
        }

        if (value instanceof Number) {
            dest.add((Number)value);
            return;
        }

        if (value instanceof Boolean) {
            dest.add(((Boolean)value).booleanValue());
            return;
        }

        if (value instanceof Enum) {
            dest.add(((Enum<?>)value).name());
            return;
        }

        if (value instanceof Map) {
            write(dest, (Map<?,?>)value);
            return;
        }

        if (value instanceof Iterable) {
            write(dest, (Iterable<?>)value);
            return;
        }

        if (value instanceof CharSequence) {
            dest.add((CharSequence)value);
            return;
        }

        final Class<?> clazz = value.getClass();
        if (clazz.isArray()) {
            Class<?> type = clazz.getComponentType();
            if (type == Boolean.TYPE) {
                write(dest, (boolean[])value);
            } else if (type == Byte.TYPE) {
                write(dest, (byte[])value);
            } else if (type == Character.TYPE) {
                write(dest, (char[])value);
            } else if (type == Double.TYPE) {
                write(dest, (double[])value);
            } else if (type == Float.TYPE) {
                write(dest, (float[])value);
            } else if (type == Integer.TYPE) {
                write(dest, (int[])value);
            } else if (type == Long.TYPE) {
                write(dest, (long[])value);
            } else if (type == Short.TYPE) {
                write(dest, (short[])value);
            } else {
                write(dest, (Object[])value);
            }
            return;
        }

        throw new IOException("unsupported class: " + value.getClass());
    }

    /**
     * Write the given Map as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            Map<?,?> value) throws IOException {
        
        if (isNull(value)) {
            dest.addNull();
            return;
        }

        dest.beginObject();

        boolean comma = false; // NOPMD - state across for loop

        for (Map.Entry<?,?> e : value.entrySet())  {
            Object key = e.getKey();
            
            if (!(key instanceof CharSequence)) {
                throw new IOException("invalid JSON object");
            }

            if (comma) {
                dest.valueSeparator();
            }

            comma = true; // NOPMD - state across for loop

            dest.addKey(key.toString()).nameSeparator();

            write(dest, e.getValue());
        }

        dest.endObject();
    }

    /**
     * Write the given Iterable as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or an iterable object
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            Iterable<?> value) throws IOException {

        if (isNull(value)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        boolean comma = false; // NOPMD - state across for loop

        for (Object obj : value) {
            if (comma) {
                dest.valueSeparator();
            }

            comma = true; // NOPMD - state across for loop

            write(dest, obj);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            boolean... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            byte... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            char... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            double... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            float... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            int... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            long... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            short... array) // NOPMD - AvoidUsingShortType
                throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            Object... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            write(dest, array[i]);
        }

        dest.endArray();
    }

}
