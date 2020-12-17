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

package org.jsonurl.jsonp;

import static org.jsonurl.JsonUrlOption.optionCoerceNullToEmptyString;
import static org.jsonurl.JsonUrlOption.optionEmptyUnquotedValue;
import static org.jsonurl.JsonUrlOption.optionSkipNulls;
import static org.jsonurl.JsonUrlOptionable.getJsonUrlOptions;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.text.JsonTextBuilder;

/**
 * A utility class for serializing javax.json objects, arrays, and values as JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public final class JsonUrlWriter { //NOPMD - ClassNamingConventions

    private JsonUrlWriter() {
        // EMPTY
    }

    private static boolean isNull(Object obj) {
        //
        // PMD - CompareObjectsWithEquals
        // this is intentional
        //
        return obj == null || obj == JsonValue.NULL; // NOPMD
    }

    /**
     * Write the given Object as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonValue
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Object value) throws IOException {
        return write(dest, getJsonUrlOptions(dest), value);
    }

    /**
     * Write the given Object as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonValue
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
        
        if (value instanceof JsonObject) {
            return write(dest, options, (JsonObject)value);
        }
        
        if (value instanceof JsonArray) {
            return write(dest, options, (JsonArray)value);
        }

        if (value instanceof JsonValue) {
            return write(dest, options, (JsonValue)value);
        }

        return org.jsonurl.j2se.JsonUrlWriter.write(
            dest, options, value);
    }

    /**
     * Write the given JsonValue as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonValue
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            JsonValue value) throws IOException {
        return write(dest, getJsonUrlOptions(dest), value);
    }

    /**
     * Write the given JsonValue as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonValue
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            JsonValue value) throws IOException {

        if (isNull(value)) {
            if (optionSkipNulls(options)) {
                return false;
            }

            return writeNull(dest, options);
        }
        
        //
        // PMD - CompareObjectsWithEquals
        // this is intentional
        //
        if (value == JsonValue.TRUE) { // NOPMD
            dest.add(true);
            return true;
        }
        
        //
        // PMD - CompareObjectsWithEquals
        // this is intentional
        //
        if (value == JsonValue.FALSE) { // NOPMD
            dest.add(false);
            return true;
        }

        if (value instanceof JsonString) {
            JsonString str = (JsonString)value;
            dest.add(str.getString());
            return true;
        }

        if (value instanceof JsonNumber) {
            JsonNumber num = (JsonNumber)value;
            dest.add(num.numberValue());
            return true;
        }

        if (value instanceof JsonObject) {
            return write(dest, options, (JsonObject)value);
        }
        
        if (value instanceof JsonArray) {
            return write(dest, options, (JsonArray)value);
        }

        return org.jsonurl.j2se.JsonUrlWriter.write(
                dest, options, value);
    }

    /**
     * Write the given JsonObjectBuilder as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JsonObjectBuilder
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            JsonObject obj) throws IOException {
        return write(dest, getJsonUrlOptions(dest), obj);
    }

    /**
     * Write the given JsonObjectBuilder as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JsonObjectBuilder
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            JsonObject obj) throws IOException {
        
        if (isNull(obj)) {
            if (optionSkipNulls(options)) {
                return false;
            }

            return writeNull(dest, options);
        }

        dest.beginObject();
        
        boolean ret = false;
        
        for (Entry<String, JsonValue> e : obj.entrySet()) {
            ret = write(dest, options, e.getKey(), e.getValue(), ret);
        }
        
        dest.endObject();
        
        return ret;
    }
    
    private static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            String key,
            JsonValue value,
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

    /**
     * Write the given JsonArrayBuilder as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonArrayBuilder
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            JsonArray value) throws IOException {
        return write(dest, getJsonUrlOptions(dest), value);
    }

    /**
     * Write the given JsonArrayBuilder as JSON&#x2192;URL text.
     * 
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonArrayBuilder
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    public static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            JsonArray value) throws IOException {
        
        if (isNull(value)) {
            if (optionSkipNulls(options)) {
                return false;
            }

            return writeNull(dest, options);
        }

        boolean ret = false;

        dest.beginArray();

        for (int i = 0, length = value.size(); i < length; i++) { // NOPMD - ForLoopVariableCount
            ret = write(dest, options, value.get(i), ret);
        }

        dest.endArray();
        
        return ret;
    }
    
    private static <R> boolean write(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options,
            JsonValue value,
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
    

    private static <R> boolean writeNull(
            JsonTextBuilder<R> dest,
            Set<JsonUrlOption> options) throws IOException {

        dest.addNull();

        return !(optionCoerceNullToEmptyString(options)
                && optionEmptyUnquotedValue(options));
    }
}
