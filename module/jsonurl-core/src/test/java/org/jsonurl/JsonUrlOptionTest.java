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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * ValueTypeTest.
 */
class JsonUrlOptionTest {

    @ParameterizedTest
    @EnumSource(JsonUrlOption.class)
    void testOption(JsonUrlOption type) {
        Set<JsonUrlOption> options = JsonUrlOption.newSet();
        assertFalse(options.contains(type), type.toString());
        options.add(type);
        assertTrue(options.contains(type), type.toString());
    }
    
    void assertOption(Set<JsonUrlOption> set, JsonUrlOption expected) {
        assertTrue(set.contains(expected), expected.name());
    }
    
    @Test
    void testEnableImpliedStringLiterals() {
        Set<JsonUrlOption> options =
            JsonUrlOption.enableImpliedStringLiterals(
                JsonUrlOption.newSet());
        
        assertOption(options, JsonUrlOption.EMPTY_UNQUOTED_KEY);
        assertOption(options, JsonUrlOption.EMPTY_UNQUOTED_VALUE);
        assertOption(options, JsonUrlOption.SKIP_NULLS);
        assertOption(options, JsonUrlOption.IMPLIED_STRING_LITERALS);
    }
}
