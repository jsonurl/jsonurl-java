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

package org.jsonurl.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.MalformedInputException;
import java.util.EnumSet;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;
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

    /**
     * The empty string.
     */
    private static final String EMPTY_QSTRING = "''";

    /**
     * The empty string.
     */
    private static final String EMPTY_STRING = "";

    @Test
    void testImpliedComposite() throws IOException {
        String testName = "ImpliedComposite";
        assertFalse(
            new JsonUrlStringBuilder().isImpliedComposite(),
            testName);

        assertTrue(
            new JsonUrlStringBuilder(
                CompositeType.OBJECT).isImpliedComposite(),
            testName);

        assertEquals(
            "true,1",
            new JsonUrlStringBuilder(CompositeType.OBJECT)
                .beginArray()
                .add(true)
                .valueSeparator()
                .add(1)
                .endArray()
                .build(),
            testName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "hello",
    })
    void testClear(String text) throws IOException {
        JsonUrlStringBuilder jup = new JsonUrlStringBuilder();
        assertEquals(text, jup.add(text).build(), text);
        assertEquals(text, jup.clear().add(text).build(), text);
    }

    private void assertCodePoint(
            String expected,
            int codePoint) throws IOException {
        assertCodePoint(expected, codePoint, false);
    }

    private void assertCodePoint(
            String expected,
            int codePoint,
            boolean impliedStringLiteral) throws IOException {

        EnumSet<JsonUrlOption> options = EnumSet.noneOf(JsonUrlOption.class);
        if (impliedStringLiteral) {
            options.add(JsonUrlOption.IMPLIED_STRING_LITERALS);
        }

        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder(options);
        String actual = jsb.addCodePoint(codePoint).build();
        assertEquals(expected, actual, expected);
    }

    @Test
    void testAddCodePointSingleQuote() throws IOException {
        assertCodePoint("%27", '\'', false);
        assertCodePoint("'", '\'', true);
    }

    @Test
    void testAddCodePointSpace() throws IOException {
        assertCodePoint("+", ' ');
    }
    
    @ParameterizedTest
    @ValueSource(chars = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '!', '-', '.', '_', '~', '$', '*', '/', ';', '?', '@',
    })
    void testAddCodePointLiteral(char codePoint) throws IOException {
        String expected = String.valueOf(codePoint);
        String actual = new JsonUrlStringBuilder()
                .addCodePoint(codePoint)
                .build();

        assertEquals(expected, actual, expected);
    }

    @ParameterizedTest
    @ValueSource(chars = {
        '+', '\'', '(', ')', ',', ':'
    })
    void testAddCodePointEncoded(char codePoint) throws IOException {
        String expected = URLEncoder.encode(
            String.valueOf(codePoint), "UTF-8");

        assertCodePoint(expected, codePoint);
    }

    @ParameterizedTest
    @ValueSource(chars = {
        '+', '(', ')', ',', ':', '!'
    })
    void testAddCodePointEscaped(char codePoint) throws IOException {
        String actual = new JsonUrlStringBuilder(JsonUrlOption.AQF)
            .addCodePoint(codePoint)
            .build();

        assertEquals(
            "!" + codePoint,
            actual,
            "addCodePoint(AQF," + codePoint + ") is escaped");
    }

    @Test
    void testEmptyObject() throws IOException {
        final String testName = "empty object";

        assertEquals(
            "()",
            new JsonUrlStringBuilder().beginObject().endObject().build(),
            testName);

        assertEquals(
            "()",
            new JsonUrlStringBuilder(JsonUrlOption.AQF)
                .beginObject()
                .endObject()
                .build(),
            testName);

        assertEquals(
            "(:)",
            new JsonUrlStringBuilder(JsonUrlOption.NO_EMPTY_COMPOSITE)
                .beginObject()
                .endObject()
                .build(),
            testName);

        assertEquals(
            "(:)",
            new JsonUrlStringBuilder(
                    JsonUrlOption.AQF,
                    JsonUrlOption.NO_EMPTY_COMPOSITE)
                .beginObject()
                .endObject()
                .build(),
            testName);
    }

    @Test
    void testEmptyString() throws IOException {
        final String testName = "empty string";

        assertEquals(
            "%27'",
            new JsonUrlStringBuilder().add(EMPTY_QSTRING).build(),
            testName);

        assertEquals(
            EMPTY_QSTRING,
            new JsonUrlStringBuilder().add(EMPTY_STRING).build(),
            testName);

        assertEquals(
            EMPTY_QSTRING,
            new JsonUrlStringBuilder().addKey(EMPTY_STRING).build(),
            testName);

        assertEquals(
            EMPTY_QSTRING,
            new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .add(EMPTY_QSTRING).build(),
            testName);

        assertEquals(
            EMPTY_STRING,
            new JsonUrlStringBuilder(
                    JsonUrlOption.EMPTY_UNQUOTED_VALUE)
                .add(EMPTY_STRING).build(),
            testName);

        assertEquals(
            EMPTY_STRING,
            new JsonUrlStringBuilder(
                    JsonUrlOption.EMPTY_UNQUOTED_KEY)
                .addKey(EMPTY_STRING).build(),
            testName);

        assertEquals(
            "!e",
            new JsonUrlStringBuilder(
                    JsonUrlOption.AQF)
                .add(EMPTY_STRING).build(),
            testName);

        assertEquals(
            "!e",
            new JsonUrlStringBuilder(
                    JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING,
                    JsonUrlOption.IMPLIED_STRING_LITERALS,
                    JsonUrlOption.AQF)
                .add((String)null).build(),
            testName);

        assertEquals(
            "",
            new JsonUrlStringBuilder(
                    JsonUrlOption.AQF,
                    JsonUrlOption.EMPTY_UNQUOTED_VALUE)
                .add(EMPTY_STRING).build(),
            testName);

        assertThrows(
            IOException.class,
            () -> new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .add(EMPTY_STRING).build(),
            testName);

    }

    @Test
    void testNull() throws IOException {
        final String nullText = "null";

        assertEquals(
            nullText,
            new JsonUrlStringBuilder().add((BigDecimal)null).build(),
            nullText);

        assertEquals(
            nullText,
            new JsonUrlStringBuilder().add((BigInteger)null).build(),
            nullText);

        assertEquals(
            nullText,
            new JsonUrlStringBuilder().add(null, 0, 0).build(),
            nullText);

        assertEquals(
            nullText,
            new JsonUrlStringBuilder().add((Object)null).build(),
            nullText);

        assertEquals(
            EMPTY_QSTRING,
            new JsonUrlStringBuilder(
                    JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING)
                .add(null, 0, 0).build(),
            nullText);

        assertEquals(
            EMPTY_STRING,
            new JsonUrlStringBuilder(
                    JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING,
                    JsonUrlOption.EMPTY_UNQUOTED_VALUE)
                .add(null, 0, 0).build(),
            nullText);

        assertThrows(
            IOException.class,
            () -> new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .addNull(),
            nullText);

        assertThrows(
            IOException.class,
            () -> new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .addKey(null, 0, 0),
            nullText);

    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0",
        "0.0",
        "1",
        "1.1",
        "1.2345678912345567891234567890123456789012345678901234567890",
    })
    void testBigDecimal(String text) throws IOException {
        BigDecimal expected = new BigDecimal(text);

        assertEquals(
            text,
            new JsonUrlStringBuilder().add(expected).build(),
            text);

        assertEquals(
            text,
            new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .add(expected).build(),
            text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0",
        "1",
        "12345678912345567891234567890123456789012345678901234567890",
    })
    void testBigInteger(String text) throws IOException {
        BigInteger expected = new BigInteger(text);

        assertEquals(
            text,
            new JsonUrlStringBuilder().add(expected).build(),
            text);
        
        assertEquals(
            text,
            new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .add(expected).build(),
            text);
    }

    @ParameterizedTest
    @ValueSource(longs = {
        0,
        1,
        -1,
        Long.MIN_VALUE,
        Long.MAX_VALUE,
    })
    void testLong(long value) throws IOException {
        String expected = String.valueOf(value);

        assertEquals(
            expected,
            new JsonUrlStringBuilder().add(value).build(),
            expected);
        
        assertEquals(
            expected,
            new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .add(value).build(),
            expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {
        0,
        1,
        -1,
        1.0,
        -1.0,
        Float.MIN_VALUE,
        Float.MAX_VALUE,
        Double.MIN_VALUE,
        Double.MAX_VALUE,
    })
    void testDouble(double value) throws IOException {
        String expected = String.valueOf(value);

        assertEquals(
            expected,
            new JsonUrlStringBuilder().add(value).build(),
            expected);
        
        assertEquals(
            expected,
            new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
                .add(value).build(),
            expected);
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
        String expected = "(null,1,2.0,10,0)";
        assertEquals(expected,
            new JsonUrlStringBuilder()
             .beginArray()
             .add((Number)null).valueSeparator()
             .add((Number)Long.valueOf(1)).valueSeparator()
             .add((Number)Double.valueOf(2)).valueSeparator()
             .add((Number)BigDecimal.TEN).valueSeparator()
             .add((Number)BigInteger.ZERO)
             .endArray()
             .build(),
             expected);
    }

    @Test
    void testText5() throws IOException {
        String expected = "(a:(:))";
        assertEquals(
            expected,
            new JsonUrlStringBuilder(JsonUrlOption.NO_EMPTY_COMPOSITE)
            .beginObject()
            .addKey("a")
            .nameSeparator()
            .beginObject()
            .endObject()
            .endObject()
            .build(),
            expected);
    }
    
    @Test
    void testText6() throws IOException {
        String expected = "(null,ARRAY,ARRAY)";
        assertEquals(expected,
            new JsonUrlStringBuilder()
             .beginArray()
             .add((Enum<?>)null).valueSeparator()
             .add(CompositeType.ARRAY, false).valueSeparator()
             .add(CompositeType.ARRAY, true)
             .endArray()
             .build(),
             expected);
    }

    @ParameterizedTest
    @Tag("exception")
    @ValueSource(strings = {
        Character.MIN_LOW_SURROGATE + "" + Character.MIN_HIGH_SURROGATE, // NOPMD
        Character.MIN_HIGH_SURROGATE + "", // NOPMD
        Character.MIN_HIGH_SURROGATE + "" + Character.MIN_HIGH_SURROGATE, // NOPMD
        Character.MAX_HIGH_SURROGATE + "" + (Character.MAX_LOW_SURROGATE + 1), // NOPMD
    })
    void testExceptionUtf8(String text) throws IOException {
        assertThrows(
            MalformedInputException.class,
            () -> new JsonUrlStringBuilder().addKey(text).build());

        assertThrows(
            MalformedInputException.class,
            () -> new JsonUrlStringBuilder().add(text).build());
    }
    
    @ParameterizedTest
    @Tag("exception")
    @ValueSource(ints = {
        0x200000
    })
    void testAddCodePointException(int badCodePoint) throws IOException {
        assertThrows(
            MalformedInputException.class,
            () -> new JsonUrlStringBuilder().addCodePoint(badCodePoint));

        assertThrows(
            MalformedInputException.class,
            () -> new JsonUrlStringBuilder().addCodePoint(badCodePoint));
    }

    @Test
    @Tag("exception")
    void testException() {
        assertThrows(
            IOException.class,
            () -> new JsonUrlStringBuilder().add(new Object()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "hello",
        "t", "tr", "tru", "True", "tRue", "trUe", "truE",
        "f", "fa", "fal", "fals", "False", "fAlse", "faLse", "falSe", "falsE",
        "n", "nu", "nul", "Null", "nUll", "nuLl", "nulL",
    })
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testNonQuotedString(String text) throws IOException {
        testValue(text, text, text, text, text, text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "true", "false", "null", "1", "1.0", "1e3", "1e-3",
    })
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testQuotedString(String text) throws IOException {
        testValue(text, text, '\'' + text + '\'', text, '!' + text, text);
    }

    @ParameterizedTest
    @CsvSource({
        //
        // text - a string literal
        //
        // expected - with no options it determines that the plus needs to
        //     be encoded so that it doesn't look like a number
        //
        // expectedISL - Plus needs to be encoded so it isn't decoded as a
        //     space
        //
        // expectedAqf - this is a valid literal in the AQF syntax so
        //     the first character must be escaped.
        //
        // expectedISLAqf - the leading one is not escaped because it's
        //      an implied string. However, the plus needs to be escaped so it
        //      isn't interpreted as a space.
        //
        "1e+3,1e%2B3,1e%2B3,1e!+3,1e!+3",
    })
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testEncodedString(
            String text,
            String expected,
            String expectedImpliedStringLiteral,
            String expectedAqf,
            String expectedImpliedStringAqf) throws IOException {

        testValue(
                text,
                text,
                expected,
                expectedImpliedStringLiteral,
                expectedAqf,
                expectedImpliedStringAqf);
    }

    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void testEncodedString() throws IOException { // NOPMD - JUnitTestsShouldIncludeAssert
        testValue("'hello", "%27hello", "%27hello", "'hello", "'hello", null);
        testValue("hello,", "'hello,'", "'hello,'", "hello%2C", "hello!,", null);
        testValue("hello, ", "'hello,+'", "'hello,+'", "hello%2C+", "hello!,+", null);
        testValue("a b", "a+b", "a+b", "a+b", "a+b", null);
        testValue("a(", "'a('", "'a('", "a%28", "a!(", null);
        testValue("bob's burgers", 
                "bob's+burgers",
                "bob's+burgers",
                "bob's+burgers",
                "bob's+burgers",
                null);
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
        assertEquals(
            expected,
            new JsonUrlStringBuilder().addKey(text).build(),
            text);

        assertEquals(
            expected,
            new JsonUrlStringBuilder().add(text).build(),
            text);

        assertEquals(
            expected,
            new JsonUrlStringBuilder(JsonUrlOption.AQF).addKey(text).build(),
            text);

        assertEquals(
            expected,
            new JsonUrlStringBuilder(JsonUrlOption.AQF).add(text).build(),
            text);

    }
    
    private void testValue(
            String text,
            String keyOutput,
            String nonKeyOutput,
            String nonKeyImpliedStringLiteralOutput,
            String nonKeyAqfOutput,
            String nonKeyImpliedStringAqfOutput) throws IOException {
        
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
            new JsonUrlStringBuilder(
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
               .add(text, 0, text.length())
               .build(),
            nonKeyImpliedStringLiteralOutput);

        assertEquals(
            nonKeyAqfOutput,
            new JsonUrlStringBuilder(JsonUrlOption.AQF)
               .add(text, 0, text.length())
               .build(),
            nonKeyAqfOutput);

        assertEquals(
            coalesce(nonKeyImpliedStringAqfOutput, nonKeyAqfOutput),
            new JsonUrlStringBuilder(
                    JsonUrlOption.AQF,
                    JsonUrlOption.IMPLIED_STRING_LITERALS)
               .add(text, 0, text.length())
               .build(),
            coalesce(nonKeyImpliedStringAqfOutput, nonKeyAqfOutput));

    }
    
    private static String coalesce(String a, String b) { // NOPMD - ShortVariable
        return a == null ? b : a;
    }
}
