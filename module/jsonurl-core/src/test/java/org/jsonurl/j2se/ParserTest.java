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

package org.jsonurl.j2se;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jsonurl.Parser;
import org.jsonurl.ValueFactoryParser;
import org.junit.jupiter.api.Test;

/**
 * ParserTest.
 */
class ParserTest {

    @Test
    void testConstruct() {
        assertNotNull(new JsonUrlParser(),
            "new JsonUrlParser");

        assertNotNull(
            new JsonUrlParser(JavaValueFactory.PRIMITIVE),
            "new JsonUrlParser");
    }
    
    @Test
    void testGetFactory() {
        assertSame(
            JavaValueFactory.PRIMITIVE,
            new ValueFactoryParser<>(JavaValueFactory.PRIMITIVE).getFactory(),
            "getFactory");        
    }

    @Test
    void testMaxParseChars() {
        Parser p = new Parser();
        p.setMaxParseChars(13);
        assertEquals(13, p.getMaxParseChars(), "getMaxParseChars");
    }

    @Test
    void testMaxParseDepth() {
        Parser p = new Parser();
        p.setMaxParseDepth(13);
        assertEquals(13, p.getMaxParseDepth(), "getMaxParseDepth");
    }

    @Test
    void testMaxParseValues() {
        Parser p = new Parser();
        p.setMaxParseValues(13);
        assertEquals(13, p.getMaxParseValues(), "getMaxParseValues");
    }
}
