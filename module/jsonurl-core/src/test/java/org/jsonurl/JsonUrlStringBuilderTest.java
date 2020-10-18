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

import static org.jsonurl.AbstractParseTest.TAG_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.MalformedInputException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JsonUrlStringBuilder unit test.
 */
class JsonUrlStringBuilderTest {

    /**
     * String used by multiple tests.
     */
    private static final String TEST_STRING = "bcde";

    @Test
    void testConstruct() {
        assertNotNull(new JsonUrlStringBuilder(), "construct");
    }

    @Test
    void testConstructSize() {
        assertNotNull(new JsonUrlStringBuilder(10), "construct(size)");
    }

    @Test
    void testIsFormUrlEncoded() {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();
        assertFalse(jup.isFormUrlEncoded(), "FormUrlEncoded");
        jup.setFormUrlEncoded(true);
        assertTrue(jup.isFormUrlEncoded(), "FormUrlEncoded");
    }

    @Test
    void testIsImpliedComposite() {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();
        assertFalse(jup.isImpliedComposite(), "ImpliedComposite");
        jup.setImpliedComposite(true);
        assertTrue(jup.isImpliedComposite(), "ImpliedComposite");
    }

    @Test
    void testIsImpliedStringLiterals() {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();
        assertFalse(jup.isImpliedStringLiterals(), "ImpliedStringLiterals");
        jup.setImpliedStringLiterals(true);
        assertTrue(jup.isImpliedStringLiterals(), "ImpliedStringLiterals");
    }

    @Test
    void testIsEmptyUnquotedKeyAllowed() {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();
        assertFalse(jup.isEmptyUnquotedKeyAllowed(), "EmptyUnquotedKeyAllowed");
        jup.setEmptyUnquotedKeyAllowed(true);
        assertTrue(jup.isEmptyUnquotedKeyAllowed(), "EmptyUnquotedKeyAllowed");
    }

    @Test
    void testIsEmptyUnquotedValueAllowed() {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();
        assertFalse(jup.isEmptyUnquotedValueAllowed(), "EmptyUnquotedValueAllowed");
        jup.setEmptyUnquotedValueAllowed(true);
        assertTrue(jup.isEmptyUnquotedValueAllowed(), "EmptyUnquotedValueAllowed");
    }

    @Test
    void testText() throws IOException {
        String expected = "(key1:(1,1,true,CharSequence,1.0,2))"; 
        assertEquals(expected,
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
                 .build(),
                 expected);
    }

    @Test
    void testText2() throws IOException {
        final char chr = 0xc0;

        String expected = chr + TEST_STRING + TEST_STRING.substring(1,  2);
        assertEquals(expected,
                new JsonUrlStringBuilder()
                 .append(chr)
                 .append(TEST_STRING)
                 .append(TEST_STRING, 1, 2)
                 .build(),
                 expected);
    }

    @Test
    void testText3() throws IOException {
        String expected = "(bcde:bcde,d:())";
        assertEquals(expected,
                new JsonUrlStringBuilder()
                 .beginObject()
                 .addKey(TEST_STRING)
                 .nameSeparator()
                 .add(TEST_STRING)
                 .valueSeparator()
                 .addKey(TEST_STRING, 2, 3)
                 .nameSeparator()
                 .addEmptyComposite()
                 .endObject()
                 .build(),
                 expected);
    }

    @Test
    void testText4() throws IOException {
        String expected = "(null,10,0)";
        assertEquals(expected,
                new JsonUrlStringBuilder()
                 .beginObject()
                 .add((Number)null).valueSeparator()
                 .add((Number)BigDecimal.TEN).valueSeparator()
                 .add((Number)BigInteger.ZERO)
                 .endObject()
                 .build(),
                 expected);
    }

    @Test
    void testText5() throws IOException {
        String expected = "''";
        assertEquals(
                expected,
                new JsonUrlStringBuilder().addKey("").build(),
                expected);
    }

    @ParameterizedTest
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
            Character.MIN_LOW_SURROGATE + "" + Character.MIN_HIGH_SURROGATE, // NOPMD
            Character.MIN_HIGH_SURROGATE + "", // NOPMD
            Character.MIN_HIGH_SURROGATE + "" + Character.MIN_HIGH_SURROGATE, // NOPMD
            Character.MAX_HIGH_SURROGATE + "" + (Character.MAX_LOW_SURROGATE + 1), // NOPMD
    })
    void testException(String text) throws IOException {
        assertThrows(
            MalformedInputException.class,
            () -> new JsonUrlStringBuilder().addKey(text).build());

        assertThrows(
            MalformedInputException.class,
            () -> new JsonUrlStringBuilder().add(text).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "hello",
        "t", "tr", "tru", "True", "tRue", "trUe", "truE",
        "f", "fa", "fal", "fals", "False", "fAlse", "faLse", "falSe", "falsE",
        "n", "nu", "nul", "Null", "nUll", "nuLl", "nulL",
    })
    void testNonQuotedString(String text) throws IOException {
        testValue(text, text, text, text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "true", "false", "null", "1", "1.0", "1e3", "1e-3",
    })
    void testQuotedString(String text) throws IOException {
        testValue(text, text, '\'' + text + '\'', text);
    }

    @ParameterizedTest
    @CsvSource({
        "1e+3,1e%2B3,1e%2B3",
    })
    void testEncodedString(
            String text,
            String expected,
            String expectedImpliedStringLiteral) throws IOException {
        testValue(text, text, expected, expectedImpliedStringLiteral);
    }

    @Test
    void testEncodedString() throws IOException { // NOPMD - JUnitTestsShouldIncludeAssert
        testValue("'hello", "%27hello", "%27hello", "'hello");
        testValue("hello,", "'hello,'", "'hello,'", "hello%2C");
        testValue("hello, ", "'hello,+'", "'hello,+'", "hello%2C+");
    }

    @ParameterizedTest
    @CsvSource({
        // CHECKSTYLE:OFF
        "'hello\u00A2world',hello%C2%A2world",
        "'hello\u20ACworld',hello%E2%82%ACworld",
        "'hello\uD83C\uDF55world',hello%F0%9F%8D%95world",
        "'hello\uD852\uDF62world',hello%F0%A4%AD%A2world",
        // CHECKSTYLE:ON
    })
    void testUtf8(String text, String expected) throws IOException {
        String actual = new JsonUrlStringBuilder().addKey(text).build();
        assertEquals(expected, actual, text);
    }
    
    private void testValue(
            String text,
            String keyOutput,
            String nonKeyOutput,
            String nonKeyImpliedStringLiteralOutput) throws IOException {
        
        assertEquals(
                keyOutput,
                new JsonUrlStringBuilder().addKey(text).build(),
                keyOutput);

        assertEquals(
                nonKeyOutput,
                new JsonUrlStringBuilder().add(text).build(),
                nonKeyOutput);

        assertEquals(
                nonKeyOutput,
                new JsonUrlStringBuilder().add(text, 0, text.length()).build(),
                nonKeyOutput);
        
        assertEquals(
                nonKeyImpliedStringLiteralOutput,
                new JsonUrlStringBuilder()
                    .setImpliedStringLiterals(true)
                    .add(text, 0, text.length())
                    .build(),
                nonKeyImpliedStringLiteralOutput);
    }
}
