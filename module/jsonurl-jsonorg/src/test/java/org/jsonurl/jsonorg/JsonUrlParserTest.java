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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.EnumSet;
import java.util.Set;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.junit.jupiter.api.Test;

/**
 * Unit test for JsonUrlParser.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-10-01
 */
class JsonUrlParserTest {

    /**
     * Test description.
     */
    private static final String TEST_DESCRIPTION = "new JsonUrlParser";

    @Test
    void testConstructOptions() {
        Set<JsonUrlOption> expectedOptions = EnumSet.of(JsonUrlOption.SKIP_NULLS); 
        assertEquals(
            expectedOptions,
            new JsonUrlParser(JsonUrlOption.SKIP_NULLS).options(),
            TEST_DESCRIPTION);
        
        assertEquals(
            expectedOptions,
            new JsonUrlParser(expectedOptions).options(),
            TEST_DESCRIPTION);

        assertEquals(
            expectedOptions,
            new JsonUrlParser(
                    JsonOrgValueFactory.PRIMITIVE,
                    JsonUrlOption.SKIP_NULLS)
                .options(),
            TEST_DESCRIPTION);

        assertEquals(
            expectedOptions,
            new JsonUrlParser(
                    JsonOrgValueFactory.PRIMITIVE,
                    expectedOptions)
                .options(),
            TEST_DESCRIPTION);

        assertEquals(
            expectedOptions,
            new JsonUrlParser(
                    JsonOrgValueFactory.PRIMITIVE,
                    JsonUrlOption.SKIP_NULLS)
                .options(),
            TEST_DESCRIPTION);

    }

    @Test
    void testConstructLimits() {
        JsonUrlLimits expectedLimits = JsonUrlLimits
            .builder()
            .addMaxParseDepth(3)
            .build();

        assertEquals(
            expectedLimits,
            new JsonUrlParser(expectedLimits).limits(),
            TEST_DESCRIPTION);

        assertEquals(
            expectedLimits,
            new JsonUrlParser(
                    JsonOrgValueFactory.PRIMITIVE,
                    expectedLimits)
                .limits(),
            TEST_DESCRIPTION);
    }

    void assertConstructLimitsAndOptions(
            JsonUrlParser parser,
            JsonUrlLimits expectedLimits,
            Set<JsonUrlOption> expectedOptions) {
        
        assertEquals(
            expectedLimits,
            parser.limits(),
            TEST_DESCRIPTION);

        assertEquals(
            expectedOptions,
            parser.options(),
            TEST_DESCRIPTION);
    }

    @Test
    void testConstructLimitsAndOptions() {
        Set<JsonUrlOption> expectedOptions = EnumSet.of(
            JsonUrlOption.WFU_COMPOSITE);

        JsonUrlLimits expectedLimits = JsonUrlLimits
            .builder()
            .addMaxParseDepth(3)
            .build();

        assertConstructLimitsAndOptions(
            new JsonUrlParser(
                expectedLimits,
                expectedOptions),
            expectedLimits,
            expectedOptions);

        assertConstructLimitsAndOptions(
            new JsonUrlParser(
                expectedLimits,
                JsonUrlOption.WFU_COMPOSITE),
            expectedLimits,
            expectedOptions);

        assertConstructLimitsAndOptions(
            new JsonUrlParser(
                JsonOrgValueFactory.PRIMITIVE,
                expectedLimits,
                expectedOptions),
            expectedLimits,
            expectedOptions);

        assertConstructLimitsAndOptions(
            new JsonUrlParser(
                JsonOrgValueFactory.PRIMITIVE,
                expectedLimits,
                JsonUrlOption.WFU_COMPOSITE),
            expectedLimits,
            expectedOptions);

    }

    @Test
    void testConstruct() {
        assertSame(
            JsonOrgValueFactory.PRIMITIVE,
            new JsonUrlParser().factory(),
            TEST_DESCRIPTION);

        assertNull(
            new JsonUrlParser().options(),
            TEST_DESCRIPTION);

        assertNull(
            new JsonUrlParser().limits(),
            TEST_DESCRIPTION);

        assertSame(
            JsonOrgValueFactory.DOUBLE,
            new JsonUrlParser(JsonOrgValueFactory.DOUBLE).factory(),
            TEST_DESCRIPTION);
    }
}
