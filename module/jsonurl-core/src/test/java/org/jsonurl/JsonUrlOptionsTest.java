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

import org.junit.jupiter.api.Test;

/**
 * ValueTypeTest.
 */
class JsonUrlOptionsTest {

    @Test
    void testIsFormUrlEncoded() {
        String test = "IsFormUrlEncoded";

        assertFalse(JsonUrlOptions.isFormUrlEncoded(null), test);
        assertFalse(new JsonUrlOptions() {}.isFormUrlEncoded(), test);
        
        JsonUrlOptions options = new JsonUrlOptions() {
            @Override
            public boolean isFormUrlEncoded() {
                return true;
            }
        };

        assertTrue(JsonUrlOptions.isFormUrlEncoded(options), test);
        assertTrue(options.isFormUrlEncoded(), test);
    }

    @Test
    void testImpliedStringLiterals() {
        String test = "ImpliedStringLiterals";

        assertFalse(JsonUrlOptions.isImpliedStringLiterals(null), test);
        assertFalse(new JsonUrlOptions() {}.isImpliedStringLiterals(), test);
        
        JsonUrlOptions options = new JsonUrlOptions() {
            @Override
            public boolean isImpliedStringLiterals() {
                return true;
            }
        };

        assertTrue(JsonUrlOptions.isImpliedStringLiterals(options), test);
        assertTrue(options.isImpliedStringLiterals(), test);
    }

    @Test
    void testEmptyUnquotedKeyAllowed() {
        String test = "EmptyUnquotedKeyAllowed";

        assertFalse(JsonUrlOptions.isEmptyUnquotedKeyAllowed(null), test);
        assertFalse(new JsonUrlOptions() {}.isEmptyUnquotedKeyAllowed(), test);
        
        JsonUrlOptions options = new JsonUrlOptions() {
            @Override
            public boolean isEmptyUnquotedKeyAllowed() {
                return true;
            }
        };

        assertTrue(JsonUrlOptions.isEmptyUnquotedKeyAllowed(options), test);
        assertTrue(options.isEmptyUnquotedKeyAllowed(), test);
    }

    @Test
    void testEmptyUnquotedValueAllowed() {
        String test = "EmptyUnquotedValueAllowed";

        assertFalse(JsonUrlOptions.isEmptyUnquotedValueAllowed(null), test);
        assertFalse(new JsonUrlOptions() {}.isEmptyUnquotedValueAllowed(), test);
        
        JsonUrlOptions options = new JsonUrlOptions() {
            @Override
            public boolean isEmptyUnquotedValueAllowed() {
                return true;
            }
        };

        assertTrue(JsonUrlOptions.isEmptyUnquotedValueAllowed(options), test);
        assertTrue(options.isEmptyUnquotedValueAllowed(), test);
    }

    @Test
    void testSkipNulls() {
        String test = "SkipNulls";

        assertFalse(JsonUrlOptions.isSkipNulls(null), test);
        assertFalse(new JsonUrlOptions() {}.isSkipNulls(), test);

        JsonUrlOptions options = new JsonUrlOptions() {
            @Override
            public boolean isSkipNulls() {
                return true;
            }
        };

        assertTrue(JsonUrlOptions.isSkipNulls(options), test);
        assertTrue(options.isSkipNulls(), test);
    }

}
