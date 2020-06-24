package org.jsonurl.jsonorg;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.jsonurl.JsonTextBuilder;

/**
 * A utility class for serializing org.json objects, arrays, and values as JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public final class JsonUrlWriter {
    
    private JsonUrlWriter() {
        // EMPTY
    }
    
    private static final boolean isNull(Object in) {
        return in == null || in == JSONObject.NULL;
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    @SuppressWarnings("PMD")
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            Object in) throws IOException {

        if (isNull(in)) {
            out.addNull();
            return;
        }

        if (in instanceof JSONString) {
            try {
                String s = ((JSONString)in).toJSONString();
                out.add(s);
            } catch (Exception e) { //NOPMD - I want to re-throw as JSONException
                throw new JSONException(e);
            }
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
        
        if (in instanceof Enum<?>) {
            out.add(((Enum<?>)in).name());
            return;
        }
        
        if (in instanceof JSONObject) {
            write(out, (JSONObject)in);
            return;
        }
        
        if (in instanceof JSONArray) {
            write(out, (JSONArray)in);
            return;
        }

        if (in instanceof CharSequence) {
            out.add((CharSequence)in);
            return;
        }

        if (in instanceof Map<?,?>) {
            write(out, new JSONObject((Map<?,?>)in));
            return;
        }
        
        if (in instanceof Collection<?>) {
            write(out, new JSONArray((Collection<?>)in));
            return;
        }
        
        if (in.getClass().isArray()) {
            write(out, new JSONArray((Collection<?>)in));
            return;
        }

        throw new JSONException("unsupported class: " + in.getClass());
    }

    /**
     * Write the given JSONObject as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JSONObject in) throws IOException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }

        out.beginObject();

        //
        // this is the best we can do with the given interface
        //
        boolean comma = false; // NOPMD - I need to track this

        for (Iterator<String> it = in.keys(); it.hasNext();) { // NOPMD - not iterable
            String key = it.next();
            
            if (comma) {
                out.valueSeparator();
            }

            comma = true; // NOPMD - I need to track this
            
            out.addKey(key).nameSeparator();
            
            write(out, in.get(key));
        }
        
        out.endObject();
    }

    /**
     * Write the given JSONArray as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JSONArray in) throws IOException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }

        out.beginArray();

        boolean comma = false; // NOPMD - I need to track this

        for (int i = 0, length = in.length(); i < length; i++) { // NOPMD
            if (comma) {
                out.valueSeparator();
            }

            comma = true; // NOPMD - I need to track this

            write(out, in.get(i));
        }

        out.endArray();
    }
}
