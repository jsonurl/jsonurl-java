/*
 * Copyright 2019-2020 David MacCormack
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.text.JsonTextBuilder;
import org.jsonurl.text.JsonUrlStringBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit test for JsonUrlWriter.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
class JsonUrlWriterTest {

    /**
     * null.
     */
    private static final String NULL = "null";

    @Test
    void testNullJsonString() throws IOException {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder(
            JsonUrlOption.SKIP_NULLS);

        assertFalse(write(
            jup,
            new JSONString() {
                @Override
                public String toJSONString() {
                    return null;
                }
            }),
            NULL);

        jup = new JsonUrlStringBuilder();
        assertTrue(write(
            jup,
            new JSONString() {
                @Override
                public String toJSONString() {
                    return null;
                }
            }),
            NULL);

        assertEquals(NULL, jup.build(), NULL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "hello",
    })
    void testJsonString(String text) throws IOException {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();

        assertTrue(write(
            jup,
            new JSONString() {
                @Override
                public String toJSONString() {
                    return text;
                }
            }),
            text);

        assertEquals(text, jup.build(), text);
    }

    @Test
    void testNullObject() throws IOException {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder(
            JsonUrlOption.SKIP_NULLS);

        assertFalse(
            JsonUrlWriter.write(jup, (JSONObject)null),
            NULL);

        jup = new JsonUrlStringBuilder();
        assertTrue(
            JsonUrlWriter.write(jup, (JSONObject)null),
            NULL);

        assertEquals(NULL, jup.build(), NULL);

    }

    @Test
    void testNullArray() throws IOException {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder(
            JsonUrlOption.SKIP_NULLS);

        assertFalse(
            JsonUrlWriter.write(
                jup, (JSONArray)null),
            NULL);

        jup = new JsonUrlStringBuilder();
        assertTrue(
            JsonUrlWriter.write(jup, (JSONArray)null),
            NULL);

        assertEquals(NULL, jup.build(), NULL);

    }
    
    static <R> boolean write(
            JsonTextBuilder<R> dest,
            Object value) throws IOException {

        return JsonUrlWriter.write(dest, value);
    }
}
