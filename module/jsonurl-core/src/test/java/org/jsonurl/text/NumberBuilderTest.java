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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.stream.Stream;
import org.jsonurl.BigMath;
import org.jsonurl.BigMathProvider;
import org.jsonurl.LimitException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit test for NumberBuilder.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class NumberBuilderTest { // NOPMD
    /** Prefix used to test non-zero based start index. */
    private static final String PREFIX  = "prefix ";
    
    /** Suffix used to test non-zero based stop index. */
    private static final String SUFFIX = " suffix";

    /** First positive number to be coerced from a long to a double. */
    private static final double POS_INTEGER_AS_DOUBLE =
        Long.valueOf(Long.MAX_VALUE).doubleValue() + 1; // NOPMD
    
    /** First negative number to be coerced from a long to a double. */
    private static final double NEG_INTEGER_AS_DOUBLE =
        Long.valueOf(Long.MIN_VALUE).doubleValue() - 1; // NOPMD

    /** tag annotation. */
    public static final String TAG_LONG = "long";

    /** tag annotation. */
    public static final String TAG_NUMBER = "number";

    /** tag annotation. */
    public static final String TAG_DOUBLE = "double";

    /** tag annotation. */
    public static final String TAG_BIG = "big";

    private NumberBuilder newNumberBuilder(String text) {
        return newNumberBuilder(text, null);
    }

    private NumberBuilder newNumberBuilder(String text, BigMathProvider mcp) {
        return new NumberBuilder(
                PREFIX + text + SUFFIX,
                PREFIX.length(),
                PREFIX.length() + text.length(),
                mcp, null);
    }
    
    private void assertEqualsIsNumber(boolean expect, String text) {
        assertEquals(expect, NumberBuilder.isNumber(text), text);
        assertEquals(expect, NumberBuilder.isNumber(text, false), text);

        assertEquals(expect, NumberBuilder.isNumber(
            PREFIX + text + SUFFIX,
            PREFIX.length(),
            PREFIX.length() + text.length()),
            text);

        assertEquals(expect, new NumberBuilder(text).isNumber(), text);

        assertEquals(expect, new NumberBuilder(
            PREFIX + text + SUFFIX,
            PREFIX.length(),
            PREFIX.length() + text.length()).isNumber(),
            text);

        assertEquals(expect, new NumberBuilder(text).hasIntegerPart(), text);
    }

    private void assertEqualsIsInteger(boolean expect, String text) {
        assertEquals(expect, NumberBuilder.isNonFractional(text), text);
        assertEquals(expect, NumberBuilder.isNumber(text, true), text);

        assertEquals(expect, NumberBuilder.isNonFractional(
            PREFIX + text + SUFFIX,
            PREFIX.length(),
            PREFIX.length() + text.length()),
            text);
        
        assertEquals(expect, new NumberBuilder(text).isNonFractional(), text);

        assertEquals(expect, new NumberBuilder(
            PREFIX + text + SUFFIX,
            PREFIX.length(),
            PREFIX.length() + text.length()).isNonFractional(),
            text);
    }

    private void assertToString(String text) {
        NumberBuilder numb = newNumberBuilder(text);

        assertArrayEquals(
                numb.toChars(),
                NumberBuilder.toChars(numb),
                text);

        assertEquals(
                numb.toString(),
                NumberBuilder.toString(numb),
                text);
    }

    @ParameterizedTest
    @Tag(TAG_BIG)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "123456.789"
    })
    void testConstruct(String text) {
        NumberText numt = new NumberBuilder(text);
        NumberBuilder numb = new NumberBuilder(numt);
        assertEquals(NumberBuilder.toDouble(numt), numb.toDouble(), text);
    }

    @ParameterizedTest
    @Tag(TAG_BIG)
    @Tag(TAG_NUMBER)
    @CsvSource({
        Math.PI + ", 3.141593"
    })
    void testBigDecimal(double actual, double expect) {
        final BigMathProvider mcp = new BigMath(
            MathContext.DECIMAL32,
            null,
            null,
            null);

        final String sin = String.valueOf(actual);
        NumberBuilder numb = newNumberBuilder(sin, mcp);
        assertEquals(mcp, numb.getBigMathProvider(), sin);
        BigDecimal dec = numb.toBigDecimal();
        assertEquals(expect, dec.doubleValue(), String.valueOf(actual));
    }

    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @ValueSource(longs = {
        0, -0,
        1, -1,
        123_456, -123_456,
        12_345_678_905_432_132L,
        Long.MAX_VALUE,
        Long.MAX_VALUE - 1,
        Long.MIN_VALUE,
        Long.MIN_VALUE - 1,
    })
    void testLong(long expected) {
        assertEquals(
                Long.valueOf(expected),
                newNumberBuilder(String.valueOf(expected)).build(true),
                String.valueOf(expected));

        assertEqualsIsNumber(true, String.valueOf(expected));
        assertEqualsIsInteger(true, String.valueOf(expected));
        assertToString(String.valueOf(expected));
    }

    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "0",
        "-0",
        "1",
        "-1",
        "123456",
        "-123456",
        "12345678905432132",
    })
    void testLong(String text) {
        assertEquals(
                Long.valueOf(text),
                newNumberBuilder(text).build(true),
                text);

        assertEqualsIsNumber(true, text);
        assertEqualsIsInteger(true, text);
        assertToString(text);
    }
    
    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @CsvSource({
        //
        // INPUT,OUTPUT
        //
        "'1e2',100",
        "'-2e1',-20",
        "'-3e0',-3",
        "'1e+2',100",
        "'-2e+1',-20",
        "'4e+15',4000000000000000",
    })
    void testLong(String text, long expected) {
        assertEquals(
                Long.valueOf(expected),
                newNumberBuilder(text).build(true),
                text);

        assertEqualsIsNumber(true, text);
        assertEqualsIsInteger(true, text);
        assertToString(text);
    }
    
    @ParameterizedTest
    @Tag(TAG_DOUBLE)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "0.0", "-0.0",
        "1.1", "-1.1",
        "123456.2", "-123456.2",
        "12345678905432132.3",
        "-12345678905432132.3",
        
        "0.0e1", "-0.0e2", "-0.0e+2", "0.0e-1",
        "1e-1", "0.2e1", "0.2e+2", "0.3e-3",
        "123.1e1", "-123.2e2", "-123.2e+2", "-321.4e-4",

        "20e-1",
        
        "123456789012345678901"
    })
    void testDouble(String text) {
        assertEquals(
                Double.valueOf(text),
                newNumberBuilder(text).build(true),
                text);

        assertEquals(
            Double.valueOf(text),
            newNumberBuilder(text).toDouble(),
            text);
        
        assertEquals(
            new BigDecimal(text),
            newNumberBuilder(text).toBigDecimal(),
            text);

        assertEqualsIsNumber(true, text);

        assertEqualsIsInteger(
            text.indexOf('.') == -1 && text.indexOf('e') == -1,
            text);
        
        assertEquals(text.indexOf('.') != -1,
            new NumberBuilder(text).hasFractionalPart(),
            text);

        assertToString(text);
    }
    
    @ParameterizedTest
    @Tag(TAG_DOUBLE)
    @Tag(TAG_NUMBER)
    @MethodSource("integerAsDoubleProvider")
    void testDouble(double value) {
        String asString = String.format("%.0f", value);
        assertEquals(
                Double.valueOf(value),
                newNumberBuilder(asString).build(),
                asString);

        assertEqualsIsNumber(true, asString);
        assertEqualsIsInteger(true, asString);
        assertToString(asString);
    }
    
    static Stream<Double> integerAsDoubleProvider() {
        return Stream.of(POS_INTEGER_AS_DOUBLE, NEG_INTEGER_AS_DOUBLE);
    }

    @ParameterizedTest
    @Tag(TAG_BIG)
    @Tag(TAG_NUMBER)
    @CsvSource({
        Long.MAX_VALUE + "0,",
        Long.MIN_VALUE + "0,",
        Long.MAX_VALUE + "e2," + Long.MAX_VALUE + "00",
        Long.MIN_VALUE + "e3," + Long.MIN_VALUE + "000",
    })
    void testBigInteger(String text, String expected) {
        String expectedText = expected == null ? text : expected;

        assertEquals(
                new BigInteger(expectedText),
                newNumberBuilder(String.valueOf(text)).build(false),
                text);

        assertEqualsIsNumber(true, text);
        assertEqualsIsInteger(true, text);
        assertEqualsIsNumber(true, expectedText);
        assertEqualsIsInteger(true, expectedText);
        assertToString(text);
        assertToString(expectedText);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "true", "false", "null",
        "1e", "-1e",
        "1.", "-1.",
        "1e+", "1e-",
    })
    void testNonNumberLiterals(String text) {
        assertEqualsIsNumber(false, text);
        assertEqualsIsInteger(false, text);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "1",
        Long.MIN_VALUE + "", // NOPMD - must be a literal
        Long.MAX_VALUE + "", // NOPMD - must be a literal
    })
    void testIsLong(String text) {
        assertTrue(NumberBuilder.isLong(new NumberBuilder(text)), text);
        assertEquals(
            Long.parseLong(text),
            NumberBuilder.toLong(new NumberBuilder(text)),
            text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        Long.MIN_VALUE + "0",
        Long.MAX_VALUE + "0",
        "1.0",
    })
    void testIsNotLong(String text) {
        assertFalse(NumberBuilder.isLong(new NumberBuilder(text)), text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        Long.MAX_VALUE + "0",
    })
    void testOverflowNoOp(String text) {
        BigMath math = new BigMath(
                MathContext.DECIMAL32,
                BigMathProvider.BIG_INTEGER32_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER32_BOUNDARY_POS,
                null);

        NumberBuilder number = new NumberBuilder(text, math);

        assertThrows(
            LimitException.class,
            () -> number.build(false),
            text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        Long.MAX_VALUE + "0",
    })
    void testOverflowNoLimit(String text) {
        NumberBuilder number = new NumberBuilder(text);
        assertEquals(new BigInteger(text), number.build(false), text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        Long.MAX_VALUE + "0",
        Long.MAX_VALUE + "00",
    })
    void testOverflowUnderLimit(String text) {
        BigMath math = new BigMath(
                MathContext.DECIMAL128,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG,
                Long.MAX_VALUE + "01",
                BigMathProvider.BigIntegerOverflow.BIG_DECIMAL);

        NumberBuilder number = new NumberBuilder(text, math);

        assertEquals(new BigInteger(text), number.build(false), text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        Long.MAX_VALUE + "0",
    })
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testOverflow(String text) {
        testOverflow(
            text,
            BigMathProvider.BigIntegerOverflow.INFINITY,
            Double.POSITIVE_INFINITY);

        testOverflow(
            text,
            BigMathProvider.BigIntegerOverflow.DOUBLE,
            Double.valueOf(text));

        testOverflow(
            text,
            BigMathProvider.BigIntegerOverflow.BIG_DECIMAL,
            new BigDecimal(text));

    }
    
    private void testOverflow(
            String text,
            BigMathProvider.BigIntegerOverflow overOp,
            Object expected) {

        BigMath math = new BigMath(
                MathContext.DECIMAL128,
                BigMathProvider.BIG_INTEGER64_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER64_BOUNDARY_POS,
                overOp);

        NumberBuilder number = new NumberBuilder(text, math);

        assertEquals(expected, number.build(false), text);
    }

    @Test
    void testEmptyString() { // NOPMD - CyclomaticComplexity
        NumberText text = new NumberText() { // NOPMD - DataflowAnomalyAnalysis

            @Override
            public CharSequence getText() {
                return null;
            }

            @Override
            public int getIntegerStartIndex() {
                return 0;
            }

            @Override
            public int getIntegerStopIndex() {
                return 0;
            }

            @Override
            public int getFractionalStartIndex() {
                return 0;
            }

            @Override
            public int getFractionalStopIndex() {
                return 0;
            }

            @Override
            public int getExponentStartIndex() {
                return 0;
            }

            @Override
            public int getExponentStopIndex() {
                return 0;
            }

            @Override
            public int getStartIndex() {
                return 0;
            }

            @Override
            public int getStopIndex() {
                return 0;
            }

            @Override
            public Exponent getExponentType() {
                return Exponent.NONE;
            }
            
        };

        assertEquals(0L, NumberBuilder.toLong(text), "empty string");
    }
}
