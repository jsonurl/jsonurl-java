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

import static org.jsonurl.JsonUrlOption.optionCoerceNullToEmptyString;
import static org.jsonurl.JsonUrlOption.optionEmptyUnquotedValue;
import static org.jsonurl.JsonUrlOption.optionSkipNulls;
import static org.jsonurl.JsonUrlOptionable.getJsonUrlOptions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.text.JsonTextBuilder;

/**
 * A utility class for serializing org.json objects, arrays, and values as
 * JSON&#x2192;URL text.
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
        //
        // PMD - CompareObjectsWithEquals
        // this is intentional
        //
        return obj == null || obj == JSONObject.NULL; // NOPMD
    }
    
    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Object value) throws IOException {
        return write(dest, getJsonUrlOptions(dest), value);
    }

    /**
     * Write the given Java Object as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or Java Object
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            Object value) throws IOException {

        if (isNull(value)) {
            if (optionSkipNulls(options)) {
                return false;
            }
            return writeNull(dest, options);
        }

        if (value instanceof JSONString) {
            String str = ((JSONString)value).toJSONString();
            if (isNull(str)) {
                if (optionSkipNulls(options)) { // NOPMD -- nested if
                    return false;
                }
                return writeNull(dest, options);
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
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JSONObject
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            JSONObject obj) throws IOException {
        return write(dest, getJsonUrlOptions(dest), obj);
    }

    /**
     * Write the given JSONObject as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JSONObject
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            JSONObject obj) throws IOException {
        
        if (isNull(obj)) {
            if (optionSkipNulls(options)) {
                return false;
            }
            
            return writeNull(dest, options);
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
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JSONArray
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            JSONArray value) throws IOException {
        return write(dest, getJsonUrlOptions(dest), value);
    }

    /**
     * Write the given JSONArray as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JSONArray
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            JSONArray value) throws IOException {
        
        if (isNull(value)) {
            if (optionSkipNulls(options)) {
                return false;
            }
            
            return writeNull(dest, options);
        }

        boolean ret = false;

        dest.beginArray();

        for (int i = 0, length = value.length(); i < length; i++) { // NOPMD
            ret = write(dest, options, value.get(i), ret);
        }

        dest.endArray();
        return ret;
    }
    
    private static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            Object value,
            boolean comma) throws IOException {

        if (optionSkipNulls(options) && isNull(value)) {
            return comma;
        }

        if (comma) {
            dest.valueSeparator();
        }

        write(dest, options, value);

        return true;
    }
    
    private static <R> boolean write(
        JsonTextBuilder<R> dest,
        Set<JsonUrlOption> options,
        String key,
        Object value,
        boolean comma) throws IOException {

        if (optionSkipNulls(options) && isNull(value)) {
            return comma;
        }

        if (comma) {
            dest.valueSeparator();
        }
        
        dest.addKey(key).nameSeparator();
        
        write(dest, options, value);   
        return true;
    }

    private static <R> boolean writeNull(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options) throws IOException {

        dest.addNull();

        return !(optionCoerceNullToEmptyString(options)
                && optionEmptyUnquotedValue(options));
    }
}
