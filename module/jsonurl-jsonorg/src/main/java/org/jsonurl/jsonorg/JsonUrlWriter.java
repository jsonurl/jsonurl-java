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

import static org.jsonurl.JsonUrlOptions.isSkipNulls;

import java.io.IOException;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.JsonUrlOptions;

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
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
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
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            Object value) throws IOException {

        if (isNull(value)) {
            if (isSkipNulls(options)) {
                return false;
            }
            dest.addNull();
            return true;
        }

        if (value instanceof JSONString) {
            String str = ((JSONString)value).toJSONString();
            if (isNull(str)) {
                if (isSkipNulls(options)) { // NOPMD -- nested if
                    return false;
                }
                dest.addNull();
                return true;
            }
            dest.add(str);
            return true;
        }
        
        if (value instanceof JSONObject) {
            return write(dest, options, (JSONObject)value);
        }
        
        if (value instanceof JSONArray) {
            return write(dest, options, (JSONArray)value);
        }

        return org.jsonurl.j2se.JsonUrlWriter.write(
            dest, options, value);
    }

    /**
     * Write the given JSONObject as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JSONObject
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JSONObject obj) throws IOException {
        return write(dest, JsonUrlOptions.fromObject(dest), obj);
    }

    /**
     * Write the given JSONObject as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JSONObject
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            JSONObject obj) throws IOException {
        
        if (isNull(obj)) {
            if (isSkipNulls(options)) {
                return false;
            }
            
            dest.addNull();
            return true;
        }

        boolean ret = false;

        dest.beginObject();

        //
        // PMD gives a false positive: ForLoopCanBeForeach.
        //
        // This is an Iterator, not an Iterable. I can't use the "foreach"
        // syntax here.
        //
        for (Iterator<String> it = obj.keys(); it.hasNext();) { // NOPMD - false positive
            //
            // this is the best we can do with the given interface -- get the
            // key and then do a "get".
            //
            String key = it.next();
            ret = write(dest, options, key, obj.get(key), ret);
        }
        
        dest.endObject();
        
        return ret;
    }

    /**
     * Write the given JSONArray as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JSONArray
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JSONArray value) throws IOException {
        return write(dest, JsonUrlOptions.fromObject(dest), value);
    }

    /**
     * Write the given JSONArray as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JSONArray
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <A,R> boolean write(
            JsonTextBuilder<A,R> dest,
            JsonUrlOptions options,
            JSONArray value) throws IOException {
        
        if (isNull(value)) {
            if (isSkipNulls(options)) {
                return false;
            }
            
            dest.addNull();
            return true;
        }

        boolean ret = false;

        dest.beginArray();

        for (int i = 0, length = value.length(); i < length; i++) { // NOPMD
            ret = write(dest, options, value.get(i), ret);
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
    
    private static <A,R> boolean write(
        JsonTextBuilder<A,R> dest,
        JsonUrlOptions options,
        String key,
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
}
