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

import java.io.IOException;
import java.util.Map.Entry;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.jsonurl.JsonTextBuilder;

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
        return obj == null || obj == JsonValue.NULL;
    }

    /**
     * Write the given JsonValue as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonValue
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            JsonValue value) throws IOException {

        if (isNull(value)) {
            dest.addNull();
            return;
        }
        
        if (value == JsonValue.TRUE) {
            dest.add(true);
            return;
        }
        if (value == JsonValue.FALSE) {
            dest.add(false);
            return;
        }

        if (value instanceof JsonString) {
            JsonString str = (JsonString)value;
            dest.add(str.getString());
            return;
        }

        if (value instanceof JsonNumber) {
            JsonNumber num = (JsonNumber)value;
            dest.add(num.numberValue());
            return;
        }

        if (value instanceof JsonObject) {
            write(dest, (JsonObject)value);
            return;
        }
        
        if (value instanceof JsonArray) {
            write(dest, (JsonArray)value);
            return;
        }

        org.jsonurl.j2se.JsonUrlWriter.write(dest, value);
    }

    /**
     * Write the given JsonObjectBuilder as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param obj null or JsonObjectBuilder
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            JsonObject obj) throws IOException {
        
        if (isNull(obj)) {
            dest.addNull();
            return;
        }
        
        boolean comma = false; // NOPMD - state across for loop

        dest.beginObject();
        
        for (Entry<String, JsonValue> e : obj.entrySet()) {
            String key = e.getKey();

            if (comma) {
                dest.valueSeparator();
            }
            
            comma = true; // NOPMD - state across for loop
            
            dest.addKey(key).nameSeparator();
            
            write(dest, e.getValue());
        }
        
        dest.endObject();
    }

    /**
     * Write the given JsonArrayBuilder as JSON&#x2192;URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param dest non-null JsonTextBuilder
     * @param value null or JsonArrayBuilder
     */
    public static <A,R> void write(
            JsonTextBuilder<A,R> dest,
            JsonArray value) throws IOException {
        
        if (isNull(value)) {
            dest.addNull();
            return;
        }

        dest.beginArray();

        final int length = value.size();

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                dest.valueSeparator();
            }

            write(dest, value.get(i));
        }

        dest.endArray();
    }
}
