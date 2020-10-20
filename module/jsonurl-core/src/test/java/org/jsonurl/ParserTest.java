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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link org.jsonurl.Parser}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-10-01
 */
class ParserTest {

    @Test
    void testMaxParseChars() {
        Parser par = new Parser();
        par.setMaxParseChars(13);
        assertEquals(13, par.getMaxParseChars(), "getMaxParseChars");
    }

    @Test
    void testMaxParseDepth() {
        Parser par = new Parser();
        par.setMaxParseDepth(13);
        assertEquals(13, par.getMaxParseDepth(), "getMaxParseDepth");
    }

    @Test
    void testMaxParseValues() {
        Parser par = new Parser();
        par.setMaxParseValues(13);
        assertEquals(13, par.getMaxParseValues(), "getMaxParseValues");
    }

    @Test
    void testEmptyUnquotedKeyAllowed() {
        final String name = "isEmptyUnquotedKeyAllowed";

        Parser par = new Parser();
        assertFalse(par.isEmptyUnquotedKeyAllowed(), name);
        par.setEmptyUnquotedKeyAllowed(true);
        assertTrue(par.isEmptyUnquotedKeyAllowed(), name);
    }

    @Test
    void testEmptyUnquotedValueAllowed() {
        final String name = "isEmptyUnquotedValueAllowed";

        Parser par = new Parser();
        assertFalse(par.isEmptyUnquotedValueAllowed(), name);
        par.setEmptyUnquotedValueAllowed(true);
        assertTrue(par.isEmptyUnquotedValueAllowed(), name);
    }

    @Test
    void testFormUrlEncodedAllowed() {
        final String name = "isFormUrlEncodedAllowed";

        Parser par = new Parser();
        assertFalse(par.isFormUrlEncoded(), name);
        par.setFormUrlEncoded(true);
        assertTrue(par.isFormUrlEncoded(), name);
    }

    @Test
    void testImpliedStringLiterals() {
        final String name = "isEmptyImpliedStringLiterals";

        Parser par = new Parser();
        assertFalse(par.isImpliedStringLiterals(), name);
        par.setImpliedStringLiterals(true);
        assertTrue(par.isImpliedStringLiterals(), name);
    }
}
