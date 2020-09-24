package org.jsonurl.j2se;

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
import java.util.Arrays;
import java.util.Map;
import org.jsonurl.JsonTextBuilder;

/**
 * A utility class for serializing J2SE values as JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
public final class JsonUrlWriter {
    
    private JsonUrlWriter() {
        // EMPTY
    }
    
    private static final boolean isNull(Object in) {
        return in == null || in == JavaValueFactory.NULL;
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    @SuppressWarnings({
        "PMD.NPathComplexity",
        "PMD.CyclomaticComplexity"
    })
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            Object in) throws IOException {

        if (isNull(in)) {
            out.addNull();
            return;
        }

        if (in instanceof Number) {
            out.add((Number)in);
            return;
        }

        if (in instanceof Boolean) {
            out.add(((Boolean)in).booleanValue());
            return;
        }

        if (in instanceof Enum) {
            out.add(((Enum<?>)in).name());
            return;
        }

        if (in instanceof Map) {
            write(out, (Map<?,?>)in);
            return;
        }

        if (in instanceof Iterable) {
            write(out, (Iterable<?>)in);
            return;
        }

        if (in instanceof CharSequence) {
            out.add((CharSequence)in);
            return;
        }

        if (in.getClass().isArray()) {
            write(out, Arrays.asList(in));
            return;
        }

        throw new IOException("unsupported class: " + in.getClass());
    }

    /**
     * Write the given Map as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            Map<?,?> in) throws IOException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }

        out.beginObject();

        boolean comma = false; // NOPMD - I need to track this

        for (Map.Entry<?,?> e : in.entrySet())  {
            Object key = e.getKey();
            
            if (!(key instanceof CharSequence)) {
                throw new IOException("invalid JSON object");
            }

            if (comma) {
                out.valueSeparator();
            }

            comma = true; // NOPMD - I need to track this

            out.addKey(key.toString()).nameSeparator();

            write(out, e.getValue());
        }

        out.endObject();
    }

    /**
     * Write the given Iterable as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param it null or an iterable object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            Iterable<?> it) throws IOException {

        if (isNull(it)) {
            out.addNull();
            return;
        }

        out.beginArray();

        boolean comma = false; // NOPMD - I need to track this

        for (Object obj : it) {
            if (comma) {
                out.valueSeparator();
            }

            comma = true; // NOPMD - I need to track this

            write(out, obj);
        }

        out.endArray();
    }
}
