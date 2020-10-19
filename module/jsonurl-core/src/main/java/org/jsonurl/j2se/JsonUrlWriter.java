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
     * @return true if dest was modified
     */
    public static <A,R> boolean write(// NOPMD
            JsonTextBuilder<A,R> dest,
            Object value) throws IOException {
        return write(dest, false, value);
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     * @param skipNullValues do not write a value if it is null
     * @return true if dest was modified
     */
    public static <A,R> boolean  write(// NOPMD
            JsonTextBuilder<A,R> dest,
            boolean skipNullValues,
            Object value) throws IOException {

        if (isNull(value)) {
            if (skipNullValues) {
                return false;
            }
            
            dest.addNull();
            return true;
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
            return write(dest, skipNullValues, (Map<?,?>)value);
        }

        if (value instanceof Iterable) {
            return write(dest, skipNullValues, (Iterable<?>)value);
        }

        if (value instanceof CharSequence) {
            dest.add((CharSequence)value);
            return true;
        }

        final Class<?> clazz = value.getClass();
        if (clazz.isArray()) {
            Class<?> type = clazz.getComponentType();
            if (type == Boolean.TYPE) {
                return write(dest, (boolean[])value);
            }
            if (type == Byte.TYPE) {
                return write(dest, (byte[])value);
            }
            if (type == Character.TYPE) {
                return write(dest, (char[])value);
            }
            if (type == Double.TYPE) {
                return write(dest, (double[])value);
            }
            if (type == Float.TYPE) {
                return write(dest, (float[])value);
            }
            if (type == Integer.TYPE) {
                return write(dest, (int[])value);
            }
            if (type == Long.TYPE) {
                return write(dest, (long[])value);
            }
            if (type == Short.TYPE) {
                return write(dest, (short[])value);
            }
            return write(dest, skipNullValues, (Object[])value);
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
        return write(dest, false, value);
    }
    
    /**
     * Write the given Map as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     * @param skipNullValues do not write key/value pair if it's null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            boolean skipNullValues,
            Map<?,?> value) throws IOException {
        
        if (isNull(value)) {
            if (skipNullValues) {
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
                    skipNullValues,
                    (CharSequence)key,
                    e.getValue(),
                    ret);
        }

        dest.endObject();

        return ret;
    }
    
    private static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            boolean skipNullValues,
            CharSequence key,
            Object value,
            boolean comma) throws IOException {

        if (skipNullValues && isNull(value)) {
            return comma;
        }

        if (comma) {
            dest.valueSeparator();
        }
        
        dest.addKey(key).nameSeparator();
        
        write(dest, skipNullValues, value);   
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
        return write(dest, false, value);
    }

    /**
     * Write the given Iterable as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or an iterable object
     * @param skipNullValues do not write iterated value if it's null
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            boolean skipNullValues,
            Iterable<?> value) throws IOException {

        if (isNull(value)) {
            if (skipNullValues) {
                return false;
            }
            
            dest.addNull();
            return true;
        }

        boolean ret = false;

        dest.beginArray();

        for (Object obj : value) {
            ret = write(dest, skipNullValues, obj, ret);
        }

        dest.endArray();
        
        return ret;
    }
    
    private static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            boolean skipNullValues,
            Object value,
            boolean comma) throws IOException {

        if (skipNullValues && isNull(value)) {
            return comma;
        }

        if (comma) {
            dest.valueSeparator();
        }

        write(dest, skipNullValues, value);

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
            boolean... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();
    
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }
    
            dest.endArray();
        }
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
            byte... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
    
            dest.beginArray();
    
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }
    
            dest.endArray();
        }

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
            char... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }

            dest.endArray();
        }
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
            double... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }

            dest.endArray();
        }
        
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
            float... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }

            dest.endArray();
        }

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
            int... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }

            dest.endArray();
        }
        
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
            long... array) throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }

            dest.endArray();

        }

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
            short... array) // NOPMD - AvoidUsingShortType
                throws IOException {

        if (isNull(array)) {
            dest.addNull();

        } else {
            dest.beginArray();

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    dest.valueSeparator();
                }
                dest.add(array[i]);
            }

            dest.endArray();
        }

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
        return write(dest, false, array);
    }

    /**
     * Write the given array as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param array null or a valid array
     * @param skipNullValues do not write array member if it's null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            boolean skipNullValues,
            Object... array) throws IOException {

        if (isNull(array)) {
            if (skipNullValues) {
                return false;
            }
            
            dest.addNull();
            return true;
        }

        dest.beginArray();
        
        boolean ret = false;

        for (int i = 0; i < array.length; i++) { // NOPMD - ForLoopVariableCount
            ret = write(dest, skipNullValues, array[i], ret);
        }

        dest.endArray();
        
        return ret;
    }

}
