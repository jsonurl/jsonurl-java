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

public class JsonUrlWriter {
    
    private static final boolean isNull(Object in) {
        return in == null || in == JSONObject.NULL;
    }

    /**
     * Write the given Java Object as JSON->URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            Object in) throws IOException, JSONException {

        if (isNull(in)) {
            out.addNull();
            return;
        }

        if (in instanceof JSONString) {
            try {
                String s = ((JSONString)in).toJSONString();
                out.add(s);
            } catch (Exception e) {
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
     * Write the given JSONObject as JSON->URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JSONObject in) throws IOException, JSONException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }
        
        boolean comma = false;
        
        out.beginObject();

        //
        // this is the best we can do with the given interface
        //
        for (Iterator<String> it = in.keys(); it.hasNext(); comma = true) {
            String key = it.next();
            
            if (comma) {
                out.valueSeparator();
            }
            
            out.addKey(key).nameSeparator();
            
            write(out, in.get(key));
        }
        
        out.endObject();
    }

    /**
     * Write the given JSONArray as JSON->URL text.
     * 
     * @param <A> Accumulator type
     * @param <R> Result type
     * @param out non-null JsonTextBuilder
     * @param in null or Java Object
     */
    public static final <A,R> void write(
            JsonTextBuilder<A,R> out,
            JSONArray in) throws IOException, JSONException {
        
        if (isNull(in)) {
            out.addNull();
            return;
        }

        boolean comma = false;

        out.beginArray();

        for (int i = 0, length = in.length(); i < length; i++, comma = true) {
            if (comma) {
                out.valueSeparator();
            }
            
            write(out, in.get(i));
        }

        out.endArray();
    }
}
