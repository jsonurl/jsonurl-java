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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    void testConstruct() {
        final String testName = "new JsonUrlParser";

        assertSame(
            JsonpValueFactory.BIGMATH64,
            new JsonUrlParser().factory(),
            testName);

        assertSame(
            JsonpValueFactory.PRIMITIVE,
            new JsonUrlParser(JsonpValueFactory.PRIMITIVE).factory(),
            testName);
        
        Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.WFU_COMPOSITE);

        assertEquals(
            options,
            new JsonUrlParser(options).options(),
            testName);
        
        assertEquals(
            options,
            new JsonUrlParser(JsonUrlOption.WFU_COMPOSITE).options(),
            testName);

        JsonUrlLimits limits = JsonUrlLimits.builder()
            .addMaxParseDepth(12)
            .addMaxParseValues(1978)
            .build();

        assertEquals(
            limits,
            new JsonUrlParser(limits).limits(),
            testName);

        assertEquals(
            limits,
            new JsonUrlParser(limits, options).limits(),
            testName);

        assertEquals(
            limits,
            new JsonUrlParser(limits, JsonUrlOption.WFU_COMPOSITE).limits(),
            testName);

        assertEquals(
            options,
            new JsonUrlParser(limits, options).options(),
            testName);

        assertEquals(
            options,
            new JsonUrlParser(limits, JsonUrlOption.WFU_COMPOSITE).options(),
            testName);

        assertEquals(
            limits,
            new JsonUrlParser(
                JsonpValueFactory.DOUBLE,
                limits,
                options).limits(),
            testName);

        assertEquals(
            limits,
            new JsonUrlParser(JsonpValueFactory.DOUBLE, limits).limits(),
            testName);

        assertEquals(
            options,
            new JsonUrlParser(JsonpValueFactory.DOUBLE, options).options(),
            testName);

        assertEquals(
            options,
            new JsonUrlParser(
                    JsonpValueFactory.DOUBLE,
                    JsonUrlOption.WFU_COMPOSITE)
                .options(),
            testName);
    }
}
