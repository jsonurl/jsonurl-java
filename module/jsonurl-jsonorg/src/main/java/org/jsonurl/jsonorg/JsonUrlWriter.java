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

package org.jsonurl.jsonorg;

import java.io.IOException;
import java.util.Iterator;
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
public final class JsonUrlWriter { //NOPMD - ClassNamingConventions
    
    private JsonUrlWriter() {
        // EMPTY
    }
    
    private static  boolean isNull(Object obj) {
        return obj == null || obj == JSONObject.NULL;
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            Object value) throws IOException {

        if (isNull(value)) {
            dest.addNull();
            return;
        }

        if (value instanceof JSONString) {
            try {
                String str = ((JSONString)value).toJSONString();
                dest.add(str);
            } catch (IOException e) {
                throw new JSONException(e);
            }
            return;
        }
        
        if (value instanceof JSONObject) {
            write(dest, (JSONObject)value);
            return;
        }
        
        if (value instanceof JSONArray) {
            write(dest, (JSONArray)value);
            return;
        }

        org.jsonurl.j2se.JsonUrlWriter.write(dest, value);
    }

    /**
     * Write the given JSONObject as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JSONObject
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            JSONObject obj) throws IOException {
        
        if (isNull(obj)) {
            dest.addNull();
            return;
        }

        dest.beginObject();

        //
        // this is the best we can do with the given interface
        //
        boolean comma = false; // NOPMD - state across for loop

        for (Iterator<String> it = obj.keys(); it.hasNext();) { // NOPMD - ForLoopCanBeForeach
            String key = it.next();
            
            if (comma) {
                dest.valueSeparator();
            }

            comma = true; // NOPMD - state across for loop
            
            dest.addKey(key).nameSeparator();
            
            write(dest, obj.get(key));
        }
        
        dest.endObject();
    }

    /**
     * Write the given JSONArray as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JSONArray
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            JSONArray value) throws IOException {
        
        if (isNull(value)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        boolean comma = false; // NOPMD - I need to track this

        for (int i = 0, length = value.length(); i < length; i++) { // NOPMD
            if (comma) {
                dest.valueSeparator();
            }

            comma = true; // NOPMD - I need to track this

            write(dest, value.get(i));
        }

        dest.endArray();
    }
}
