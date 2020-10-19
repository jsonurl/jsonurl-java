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

package org.jsonurl.jsonp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.jsonurl.JsonUrlStringBuilder;
import org.junit.jupiter.api.Test;

/**
 * Unit test JsonUrlWriter.
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
    void testNullObject() throws IOException {
        assertFalse(
            JsonUrlWriter.write(
                new JsonUrlStringBuilder(),
                true,
                (JsonObject)null),
            NULL);

        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();
        assertTrue(
            JsonUrlWriter.write(dest, (JsonObject)null),
            NULL);

        assertEquals(NULL, dest.build(), NULL);

    }

    @Test
    void testNullArray() throws IOException {
        assertFalse(
            JsonUrlWriter.write(
                new JsonUrlStringBuilder(),
                true,
                (JsonArray)null),
            NULL);

        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();
        assertTrue(
            JsonUrlWriter.write(dest, (JsonArray)null),
            NULL);

        assertEquals(NULL, dest.build(), NULL);

    }
}
