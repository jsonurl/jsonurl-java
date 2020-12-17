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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;
import org.junit.jupiter.api.Test;

/**
 * JsonUrlStringBuilder unit test.
 */
class JsonUrlStringBuilderTest {

    @Test
    void testConstruct() {
        final String testName = "JsonUrlStringBuilder";

        Set<JsonUrlOption> options = JsonUrlOption.newSet(
            JsonUrlOption.SKIP_NULLS);

        assertNotNull(new JsonUrlStringBuilder(options), testName);
        assertNotNull(new JsonUrlStringBuilder(), testName);

        assertNotNull(
            new JsonUrlStringBuilder(JsonUrlOption.SKIP_NULLS),
            testName);

        assertNotNull(new JsonUrlStringBuilder(1), testName);
        assertNotNull(new JsonUrlStringBuilder(1, options), testName);

        assertNotNull(
            new JsonUrlStringBuilder(
                new StringBuilder(64),
                JsonUrlOption.SKIP_NULLS),
            testName);

        assertNotNull(
            new JsonUrlStringBuilder(
                new StringBuilder(64),
                CompositeType.ARRAY,
                options),
            testName);
    }
}
