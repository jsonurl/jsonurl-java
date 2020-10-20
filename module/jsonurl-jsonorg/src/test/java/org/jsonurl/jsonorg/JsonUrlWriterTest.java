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
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.JsonUrlStringBuilder;
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
        assertFalse(write(
            new JsonUrlStringBuilder(),
            true,
            new JSONString() {
                @Override
                public String toJSONString() {
                    return null;
                }
            }),
            NULL);

        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();
        assertTrue(write(
            dest,
            false,
            new JSONString() {
                @Override
                public String toJSONString() {
                    return null;
                }
            }),
            NULL);

        assertEquals(NULL, dest.build(), NULL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "hello",
    })
    void testJsonString(String text) throws IOException {
        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();
        assertTrue(write(
            dest,
            false,
            new JSONString() {
                @Override
                public String toJSONString() {
                    return text;
                }
            }),
            text);

        assertEquals(text, dest.build(), text);
    }

    @Test
    void testNullObject() throws IOException {
        assertFalse(
            JsonUrlWriter.write(
                new JsonUrlStringBuilder(),
                true,
                (JSONObject)null),
            NULL);

        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();
        assertTrue(
            JsonUrlWriter.write(dest, (JSONObject)null),
            NULL);

        assertEquals(NULL, dest.build(), NULL);

    }

    @Test
    void testNullArray() throws IOException {
        assertFalse(
            JsonUrlWriter.write(
                new JsonUrlStringBuilder(),
                true,
                (JSONArray)null),
            NULL);

        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();
        assertTrue(
            JsonUrlWriter.write(dest, (JSONArray)null),
            NULL);

        assertEquals(NULL, dest.build(), NULL);

    }
    
    static <I,R> boolean write(
            JsonTextBuilder<I, R> dest,
            boolean skipNullValues,
            Object value) throws IOException {

        return JsonUrlWriter.write(dest, skipNullValues, value);
    }
}
