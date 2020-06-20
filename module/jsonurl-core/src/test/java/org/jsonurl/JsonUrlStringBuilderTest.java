package org.jsonurl;

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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JsonUrlStringBuilder unit test.
 */
@SuppressWarnings("PMD")
class JsonUrlStringBuilderTest {

    @Test
    void testConstruct() {
        assertNotNull(new JsonUrlStringBuilder());
        assertNotNull(new JsonUrlStringBuilder(10));
    }

    @Test
    void testText() throws IOException {
        assertEquals("(key1:(1,1,true,CharSequence,1.0,2))",
                new JsonUrlStringBuilder()
                 .beginObject()
                 .append("key1")
                 .nameSeparator()
                   .beginArray()
                   .add(BigDecimal.ONE).valueSeparator()
                   .add(BigInteger.ONE).valueSeparator()
                   .add(true).valueSeparator()
                   .add("CharSequence").valueSeparator()
                   .add(1D).valueSeparator()
                   .add(2L)
                   .endArray()
                 .endObject()
                 .build());

        final char c = 0xc0;
        final String s = "bcde";

        assertEquals("" + c + s + s.substring(1,  2),
                new JsonUrlStringBuilder()
                 .append(c)
                 .append(s)
                 .append(s, 1, 2)
                 .build());
        
        assertEquals("(bcde:bcde,d:())",
                new JsonUrlStringBuilder()
                 .beginObject()
                 .addKey(s)
                 .nameSeparator()
                 .add(s)
                 .valueSeparator()
                 .addKey(s, 2, 3)
                 .nameSeparator()
                 .addEmptyComposite()
                 .endObject()
                 .build());
        
        assertEquals("(null,10,0)",
                new JsonUrlStringBuilder()
                 .beginObject()
                 .add((Number)null).valueSeparator()
                 .add((Number)BigDecimal.TEN).valueSeparator()
                 .add((Number)BigInteger.ZERO)
                 .endObject()
                 .build());

        assertEquals("''", new JsonUrlStringBuilder().addKey("").build());
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("string")
    @Tag("autostring")
    @ValueSource(strings = {
            "hello",
            "t", "tr", "tru", "True", "tRue", "trUe", "truE",
            "f", "fa", "fal", "fals", "False", "fAlse", "faLse", "falSe", "falsE",
            "n", "nu", "nul", "Null", "nUll", "nuLl", "nulL",
    })
    public void testNonLiteral(String text) throws IOException {
        testValue(text, false);
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("string")
    @Tag("autostring")
    @ValueSource(strings = {
            "true", "false", "null", "1", "1.0", "1e3",
    })
    public void testLiteral(String text) throws IOException {
        testValue(text, true);
    }
    
    public void testValue(String text, boolean isLiteral) throws IOException {
        String expected = isLiteral ? '\'' + text + '\'' : text;
        assertEquals(text, new JsonUrlStringBuilder().addKey(text).build());
        assertEquals(expected, new JsonUrlStringBuilder().add(text).build());
        assertEquals(expected,
            new JsonUrlStringBuilder().add(text, 0, text.length()).build());
    }
}
