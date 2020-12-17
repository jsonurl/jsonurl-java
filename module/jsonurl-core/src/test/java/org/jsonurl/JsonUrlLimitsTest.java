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

package org.jsonurl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link org.jsonurl.JsonUrlLimits}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-10-01
 */
class JsonUrlLimitsTest {

    @Test
    void testMaxParseChars() {
        JsonUrlLimits limits = JsonUrlLimits
            .builder()
            .addMaxParseChars(13)
            .build();
        assertEquals(13, limits.getMaxParseChars(), "getMaxParseChars");
    }

    @Test
    void testMaxParseDepth() {
        JsonUrlLimits limits = JsonUrlLimits
            .builder()
            .addMaxParseDepth(13)
            .build();
        assertEquals(13, limits.getMaxParseDepth(), "getMaxParseDepth");
    }

    @Test
    void testMaxParseValues() {
        JsonUrlLimits limits = JsonUrlLimits
            .builder()
            .addMaxParseValues(13)
            .build();
        assertEquals(13, limits.getMaxParseValues(), "getMaxParseValues");
    }
    
    @Test
    void testCopy() {
        Limits expected = new Limits();
        expected.setMaxParseChars(42);
        expected.setMaxParseValues(42);
        expected.setMaxParseDepth(42);
        
        JsonUrlLimits actual = expected.copy();
        assertEquals(expected, actual, "copy");
        assertEquals(expected.hashCode(), actual.hashCode(), "hashCode");
        
        assertEquals(expected, expected, "equals");
        assertNotEquals(expected, JsonUrlLimits.copy(null), "equals");
    }
}
