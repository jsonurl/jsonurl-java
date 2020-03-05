package org.jsonurl.jsonp;

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
import java.util.Map.Entry;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.jsonurl.JsonTextBuilder;

public class JsonUrlWriter {
    
    private static final boolean isNull(Object in) {
        return in == null || in == JsonValue.NULL;
    }
    
    /**
     * Write the given JsonValue as JSON->URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or JsonValue
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JsonValue in) throws IOException, JsonException {

        if (isNull(in)) {
            out.addNull();
            return;
        }
        
        if (in == JsonValue.TRUE) {
            out.add(true);
            return;
        }
        if (in == JsonValue.FALSE) {
            out.add(false);
            return;
        }

        if (in instanceof JsonString) {
            JsonString js = (JsonString)in;
            out.add(js.getString());
            return;
        }

        if (in instanceof JsonNumber) {
            JsonNumber n = (JsonNumber)in;
            out.add(n.numberValue());
            return;
        }

        if (in instanceof JsonObject) {
            write(out, (JsonObject)in);
            return;
        }
        
        if (in instanceof JsonArray) {
            write(out, (JsonArray)in);
            return;
        }

        throw new JsonException("unsupported JsonValue: " + in.getClass());
    }

    /**
     * Write the given JsonObject as JSON->URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or JsonObject
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JsonObject in) throws IOException, JsonException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }
        
        boolean comma = false;
        
        out.beginObject();
        
        in.entrySet();
        
        for (Entry<String, JsonValue> e : in.entrySet()) {
            String key = e.getKey();

            if (comma) {
                out.valueSeparator();
            }
            
            comma = true;
            
            out.addKey(key).nameSeparator();
            
            write(out, e.getValue());
        }
        
        out.endObject();
    }

    /**
     * Write the given JsonArray as JSON->URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or JsonArray
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JsonArray in) throws IOException, JsonException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }

        boolean comma = false;

        out.beginArray();

        for (int i = 0, length = in.size(); i < length; i++) {
            if (comma) {
                out.valueSeparator();
            }
            
            comma = true;

            write(out, in.get(i));
        }

        out.endArray();
    }
}
