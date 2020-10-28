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

import static org.jsonurl.JsonUrlOptions.isCoerceNullToEmptyString;
import static org.jsonurl.JsonUrlOptions.isEmptyUnquotedValueAllowed;
import static org.jsonurl.JsonUrlOptions.isSkipNulls;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.Map;
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.JsonUrlOptions;

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
     * @return true if dest was modified
     */
    public static <A,R> boolean write(// NOPMD
            JsonTextBuilder<A,R> dest,
            Object value) throws IOException {
        return write(dest, JsonUrlOptions.fromObject(dest), value);
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(// NOPMD
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            Object value) throws IOException {

        if (isNull(value)) {
            if (isSkipNulls(options)) {
                return false;
            }

            return writeNull(dest, options);
        }

        if (value instanceof Number) {
            dest.add((Number)value);
            return true;
        }

        if (value instanceof Boolean) {
            dest.add(((Boolean)value).booleanValue());
            return true;
        }

        if (value instanceof Enum) {
            dest.add(((Enum<?>)value).name());
            return true;
        }

        if (value instanceof Map) {
            return write(dest, options, (Map<?,?>)value);
        }

        if (value instanceof Iterable) {
            return write(dest, options, (Iterable<?>)value);
        }

        if (value instanceof CharSequence) {
            dest.add((CharSequence)value);
            return true;
        }

        final Class<?> clazz = value.getClass();
        if (clazz.isArray()) {
            Class<?> type = clazz.getComponentType();
            if (type == Boolean.TYPE) {
                return write(dest, options, (boolean[])value);
            }
            if (type == Byte.TYPE) {
                return write(dest, options, (byte[])value);
            }
            if (type == Character.TYPE) {
                return write(dest, options, (char[])value);
            }
            if (type == Double.TYPE) {
                return write(dest, options, (double[])value);
            }
            if (type == Float.TYPE) {
                return write(dest, options, (float[])value);
            }
            if (type == Integer.TYPE) {
                return write(dest, options, (int[])value);
            }
            if (type == Long.TYPE) {
                return write(dest, options, (long[])value);
            }
            if (type == Short.TYPE) {
                return write(dest, options, (short[])value);
            }
            return write(dest, options, (Object[])value);
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
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            Map<?,?> value) throws IOException {
        return write(dest, JsonUrlOptions.fromObject(dest), value);
    }
    
    /**
     * Write the given Map as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            Map<?,?> value) throws IOException {
        
        if (isNull(value)) {
            if (isSkipNulls(options)) {
                return false;
            }
            
            dest.addNull();
            return true;
        }

        boolean ret = false;

        dest.beginObject();

        for (Map.Entry<?,?> e : value.entrySet())  {
            Object key = e.getKey();
            if (!(key instanceof CharSequence)) {
                throw new IOException("object contains non-string key");
            }

            ret = write(
                    dest,
                    options,
                    (CharSequence)key,
                    e.getValue(),
                    ret);
        }

        dest.endObject();

        return ret;
    }
    
    private static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            CharSequence key,
            Object value,
            boolean comma) throws IOException {

        if (isSkipNulls(options) && isNull(value)) {
            return comma;
        }

        if (comma) {
            dest.valueSeparator();
        }
        
        dest.addKey(key).nameSeparator();
        
        write(dest, options, value);   
        return true;
    }

    /**
     * Write the given Iterable as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or an iterable object
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            Iterable<?> value) throws IOException {
        return write(dest, JsonUrlOptions.fromObject(dest), value);
    }

    /**
     * Write the given Iterable as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or an iterable object
     * @param options a valid JsonUrlOptions or null
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            Iterable<?> value) throws IOException {

        if (isNull(value)) {
            if (isSkipNulls(options)) {
                return false;
            }
            
            return writeNull(dest, options);
        }

        boolean ret = false;

        dest.beginArray();

        for (Object obj : value) {
            ret = write(dest, options, obj, ret);
        }

        dest.endArray();
        
        return ret;
    }
    
    private static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            Object value,
            boolean comma) throws IOException {

        if (isSkipNulls(options) && isNull(value)) {
            return comma;
        }

        if (comma) {
            dest.valueSeparator();
        }

        write(dest, options, value);

        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            boolean... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            byte... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(// NOPMD - CyclomaticComplexity
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            char... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();
        
        final int length = array.length;

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }

            char chr = array[i];

            if (Character.isLowSurrogate(chr)) {
                throw new MalformedInputException(i);
            }

            int codePoint;

            if (Character.isHighSurrogate(chr)) {
                i++; // NOPMD - consumed two characters

                if (i == length) {
                    throw new MalformedInputException(i);
                }

                char low = array[i];

                if (Character.isHighSurrogate(low)) {
                    throw new MalformedInputException(i);
                }

                codePoint = Character.toCodePoint(chr, low);

            } else {
                codePoint = chr;
            }

            dest.addCodePoint(codePoint);
        }

        dest.endArray();

        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            double... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
        
        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            float... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();

        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            int... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();
        
        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            long... array) throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();

        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            short... array) // NOPMD - AvoidUsingShortType
                throws IOException {

        if (isNull(array)) {
            return writeNull(dest, options);
        }

        dest.beginArray();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }
            dest.add(array[i]);
        }

        dest.endArray();

        return true;
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            Object... array) throws IOException {
        return write(dest, JsonUrlOptions.fromObject(dest), array);
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            Object... array) throws IOException {

        if (isNull(array)) {
            if (isSkipNulls(options)) {
                return false;
            }

            return writeNull(dest, options);
        }

        dest.beginArray();
        
        boolean ret = false;

        for (int i = 0; i < array.length; i++) { // NOPMD - ForLoopVariableCount
            ret = write(dest, options, array[i], ret);
        }

        dest.endArray();
        
        return ret;
    }

    private static <A,R> boolean writeNull(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options) throws IOException {

        dest.addNull();

        return !(isCoerceNullToEmptyString(options)
                && isEmptyUnquotedValueAllowed(options));
    }
}
