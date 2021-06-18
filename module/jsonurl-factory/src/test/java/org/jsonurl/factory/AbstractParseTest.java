/*
 * Copyright 2019-2020 David MacCormack
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

package org.jsonurl.factory;

import static org.jsonurl.BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG;
import static org.jsonurl.BigMathProvider.BIG_INTEGER128_BOUNDARY_POS;
import static org.jsonurl.BigMathProvider.BigIntegerOverflow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.jsonurl.BigMathProvider;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.LimitException;
import org.jsonurl.ParseException;
import org.jsonurl.SyntaxException;
import org.jsonurl.text.JsonUrlStringBuilder;
import org.jsonurl.text.NumberBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JSON&#x2192;URL parser test implementation.
 *
 * <p>Any implementation of the {@link ValueFactory} interface may use this
 * class to implement parser unit tests. Each module should extend
 * AbstractParseTest and implemented the requisite protected factory methods.
 * 
 *  <p>This class is long. It's long because I want each module to only have
 *  to extend one class to inherit the parser test suite. Therefore, several
 *  inner classes have been defined to group tests, per JUnit's docs. This
 *  makes navigating the results a bit easier.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
@SuppressWarnings({
    "checkstyle:AbbreviationAsWordInName",

    //
    // though it is generally good to avoid duplicate literals,
    // inline literals in unit tests often makes them much easier to read
    //
    "PMD.AvoidDuplicateLiterals"
})
public abstract class AbstractParseTest<
        V,
        C extends V,
        ABT, // NOPMD - GenericsNaming
        A extends C,
        JBT, // NOPMD - GenericsNaming
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> {

    /** empty string. */
    private static final String EMPTY_STRING = "";

    /** true. */
    private static final String TRUE = "true";

    /** false. */
    private static final String FALSE = "false";

    /** null. */
    private static final String NULL = "null";

    /** hello. */
    private static final String HELLO = "hello";

    /** Nonsense prefix to test parse start/end support. */
    private static final String PREFIX1 = "prefix 1";

    /** Nonsense suffix to test parse start/end support. */
    private static final String SUFFIX1 = "suffix 1";

    /** left paren. */
    private static final char LPAREN = '(';

    /** ValueFactory. */
    protected ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;

    /**
     * Create a new parser.
     */
    private Parser<V,C,ABT,A,JBT,J,B,M,N,S> newParser() {
        return new ValueFactoryParser<>(factory, null, null);
    }

    /**
     * Create a new parser.
     */
    private Parser<V,C,ABT,A,JBT,J,B,M,N,S> newParser(
            JsonUrlOption option) {
        return new ValueFactoryParser<>(factory, null, EnumSet.of(option));
    }

    /**
     * Create a new parser.
     */
    private Parser<V,C,ABT,A,JBT,J,B,M,N,S> newParser(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return new ValueFactoryParser<>(factory, null, null);
    }

    /**
     * Create a new parser.
     */
    private Parser<V,C,ABT,A,JBT,J,B,M,N,S> newParser(
            Set<JsonUrlOption> options) {
        return new ValueFactoryParser<>(factory, null, options);
    }

    /**
     * Create a new parser.
     */
    private Parser<V,C,ABT,A,JBT,J,B,M,N,S> newParser(
            JsonUrlLimits limits) {
        return new ValueFactoryParser<>(factory, limits, null);
    }
    
    private static JsonUrlLimits newLimits(
            long maxParseChars,
            int maxParseDepth,
            int maxParseValues) {

        return JsonUrlLimits.builder()
            .addMaxParseChars(maxParseChars)
            .addMaxParseDepth(maxParseDepth)
            .addMaxParseValues(maxParseValues)
            .build();
    }

    
    /**
     * Exception tests.
     */
    @Nested
    @Tag("exception")
    @SuppressWarnings("PMD.AccessorMethodGeneration") // NOPMD
    class ExceptionTests {

        @ParameterizedTest
        @ValueSource(strings = {
            HELLO,
            "1",
            "2.3",
            TRUE,
            FALSE,
            NULL,
        })
        void testExceptionComposite(String text) {
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryObject(text, false));
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryArray(text));
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryObject(
                        text,
                        newOptions(JsonUrlOption.AQF)));
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryArray(
                        text,
                        newOptions(JsonUrlOption.AQF)));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "(1)",
                "(a(",
                "(a:",
                "(a:b(",
                "(a:b,a",
                "(a:b,c&)",
        })
        void testExceptionObject(String text) {
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryObject(text, false));
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryObject(
                        text,
                        newOptions(JsonUrlOption.AQF)));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "(a:b)",
                "(a(",
        })
        void testExceptionArray(String text) {
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryArray(text));
            assertThrows(
                SyntaxException.class,
                () -> parseFactoryArray(text, newOptions(JsonUrlOption.AQF)));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(a&(b&c))",
            "(a&(b,c&d))",
            "(a=(b=c))",
            "(a=(b:c&d:e))",
            "(a=(b:c,d=e))",
        })
        void testExceptionWfu(String text) {
            assertThrows(SyntaxException.class, () -> newParser().parse(text));

            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.WFU_COMPOSITE);

            assertThrows(
                SyntaxException.class,
                () -> newParser(options).parse(text));

            if (text.indexOf('=') == -1) {
                assertThrows(
                    SyntaxException.class,
                    () -> newParser(options).parseArray(
                        makeImplied(text),
                        factory.newArrayBuilder()));

            } else {
                assertThrows(
                    SyntaxException.class,
                    () -> newParser(options).parseObject(
                        makeImplied(text),
                        factory.newObjectBuilder()));

            }
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "(1,",
            "(1,a,",
            "((a)1",
            "(a:b,",
            "'hello",
        })
        void testExceptionSyntax2(String text) {
            //
            // like testExceptionSyntax() but does not test implied values
            // (which would otherwise succeed).
            //
            assertThrows(SyntaxException.class, () -> parse(text));

            if (text.charAt(0) == LPAREN) {
                assertThrows(
                    SyntaxException.class,
                    () -> parse(text, newOptions(JsonUrlOption.AQF)));
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "%2G",
            "%E2",
            "%FA%80%80%80%80", //0x200000
        })
        void testExceptionUtf8(String text) {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parse(text));

            assertThrows(
                SyntaxException.class,
                () -> newParser(newOptions(JsonUrlOption.AQF)).parse(text));
        }

        @Test
        void testExceptionMaxParseChars() {
            JsonUrlLimits limits = newLimits(
                2,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE);

            assertThrows(
                LimitException.class,
                () -> newParser(limits).parse(TRUE));
        }

        @Test
        void testExceptionMaxParseDepth() {
            JsonUrlLimits limits = newLimits(
                Integer.MAX_VALUE,
                2,
                Integer.MAX_VALUE);

            assertThrows(
                LimitException.class,
                () -> newParser(limits).parse("(((1)))"));
        }

        @Test
        void testExceptionMaxParseValues() {
            JsonUrlLimits limits = newLimits(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                2);

            assertThrows(
                LimitException.class,
                () -> newParser(limits).parse("(1,2,3)"));
        }

        @Test
        void testParseException() throws IOException {
            ParseException pex = null;
            try {
                newParser().parse("");

            } catch (ParseException e) {
                pex = e;
            }

            final String message = "empty string";
            assertEquals(0, pex.getOffset(), message);
            assertEquals("text missing", pex.getMessage(), message);
            assertTrue(pex.toString().endsWith("text missing at 0"), message);

            assertTrue(new ParseException("a").toString().endsWith("a"), message);
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "(a,b:true)",
            "b:(a,b:true)",
            "a,b:(a,b:true)",
            "(a&b=true)",
            "b=(a,b:true)",
            "a&b=(a,b:true)",
        })
        void testExceptionMissingObjectValue(String text) {
            assertThrows(
                SyntaxException.class,
                () -> newParser(JsonUrlOption.WFU_COMPOSITE).parseObject(text),
                text);
            
            assertThrows(
                SyntaxException.class,
                () -> newParser(JsonUrlOption.WFU_COMPOSITE).parseObject(
                    text),
                text);

            assertThrows(
                SyntaxException.class,
                () -> newParser(JsonUrlOption.WFU_COMPOSITE).parseObject(
                    text,
                    factory.newObjectBuilder(),
                    (key, pos) -> factory.getTrue()),
                text);
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "",
            "%2",
            "1,",
            "(",
            "(1",
            "(1,a",
            "(1,a,(",
            "(1,a,()",
            "((",
            "((a",
            "((a)",
            "(((1))",
            "()1",
            "(1)1",
            "('1'1)",
            "(1,'2'1)",
            "(1,2,3)a",
            "(a:b",
            "(a:b,)",
            "(a:b,c)",
            "(a:b)a",
            "(a:'b'a)",
            "(a:b,'c'd)",
            "(a:()a)",
            "(a:(b:(1))",
            "1,2)",
            "\u0100", //NOPMD
            "'\u0100'", //NOPMD
        })
        void testExceptionSyntax(String text) {
            assertThrows(SyntaxException.class, () -> parse(text));

            if (text.length() > 2) { // NOPMD - AvoidLiteralsInIfCondition
                assertThrows(
                    SyntaxException.class,
                    () -> parseImpliedFactoryObject(text, false));

                assertThrows(
                    SyntaxException.class,
                    () -> parseImpliedFactoryObject(
                            text, 0, text.length(), false));

                assertThrows(
                    SyntaxException.class,
                    () -> parseImpliedFactoryArray(text));

                assertThrows(
                    SyntaxException.class,
                    () -> parseImpliedFactoryArray(text, 0, text.length()));
            }
        }

    }

    /**
     * Literal tests.
     */
    @Nested
    @SuppressWarnings("PMD.AccessorMethodGeneration")
    class LiteralTests {
        void assertLong(
                String text,
                String expected,
                Set<JsonUrlOption> options) throws IOException {

            assertEquals(
                expected,
                new JsonUrlStringBuilder(options).add(
                    Long.valueOf(expected)).build(),
                expected);

            assertEquals(
                expected,
                new JsonUrlStringBuilder(options).add(
                    Long.parseLong(expected)).build(),
                expected);

            assertParse(text, options, getFactoryLong(expected));
        }

        @ParameterizedTest
        @CsvSource({
            //
            // INPUT,OUTPUT
            //
            "'1e2',100,true",
            "'-2e1',-20,true",
            "'-3e0',-3,true",
            "'1e+2',100,false",
            "'-2e+1',-20,false",
            "'4e17',400000000000000000,true",
        })
        void testLong(
                String text,
                String expected,
                boolean aqf) throws IOException {
            
            assertLong(text, expected, null);
            
            if (aqf) {
                assertLong(text, expected, newOptions(JsonUrlOption.AQF));
            }

            assertEquals(
                getFactoryString(urlDecode(text)),
                newParser(newOptionsISL()).parse(text),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "0", "-1", "123456", "-123456", "12345678905432132",
        })
        void testLong(String text) throws IOException {
            assertLong(text, text, null);

            assertEquals(
                getFactoryString(text),
                newParser(newOptionsISL()).parse(text),
                text);
        }
        
        void assertDouble(String text, Set<JsonUrlOption> options) throws IOException {
            Number numberValue = Double.valueOf(text);
            assertEquals(
                String.valueOf(numberValue),
                new JsonUrlStringBuilder(options).add(numberValue).build(),
                text);

            double doubleValue = Double.parseDouble(text);
            assertEquals(
                String.valueOf(doubleValue),
                new JsonUrlStringBuilder(options).add(doubleValue).build(),
                text);

            assertParse(text, options, getFactoryDouble(text));
        }

        @ParameterizedTest
        @CsvSource({
            "0.1,true",
            "-1.1,true",
            "1e-2,true",
            "-2e-1,true",
            "1.9e2,true",
            "-2.8e1,true",
            "156.9e-2,true",
            "-276.8e-1,true",
            "156.911e+2,false",
            "-276.833e+4,false",
        })
        void testDouble(String text, boolean aqf) throws IOException {
            assertDouble(text, null);
            
            if (aqf) {
                assertDouble(text, newOptions(JsonUrlOption.AQF));
            }

            assertEquals(
                getFactoryString(urlDecode(text)),
                newParser(newOptionsISL()).parse(text),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = { TRUE, FALSE })
        void testBoolean(String text) throws IOException {
            assertEquals(
                text,
                new JsonUrlStringBuilder().add(Boolean.parseBoolean(text)).build(),
                text);

            assertParse(text, getFactoryBoolean(text));

            assertEquals(
                text,
                getStringValue(newParser(newOptionsISL()).parse(text)),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "''" })
        void testEmptyString(String text) throws IOException {
            if (text.length() == 0) {
                assertThrows(
                    SyntaxException.class,
                    () -> parse(text));

                assertThrows(
                    SyntaxException.class,
                    () -> parse(text, newOptions(JsonUrlOption.AQF)));
            }

            assertEquals(
                factory.getString(EMPTY_STRING),
                newParser(JsonUrlOption.EMPTY_UNQUOTED_VALUE).parse(text));

            assertEquals(
                factory.getString(text),
                newParser(newOptions(
                        JsonUrlOption.IMPLIED_STRING_LITERALS,
                        JsonUrlOption.EMPTY_UNQUOTED_VALUE))
                    .parse(text),
                text);
        }
        
        @ParameterizedTest
        @NullSource
        @EnumSource(names = "AQF")
        @DisplayName("testLiteral1: 1e+1")
        void testLiteral1(JsonUrlOption option) throws IOException {
            final String desc = "1e%2B1";

            // parse("1e+1") -> number(10)
            assertEquals(
                getFactoryLong("10"),
                newParser(newOptions(option)).parse("1e+1"),
                desc);
            
            // stringify("1e 1") -> string("1e+1")
            assertEquals(
                "1e+1",
                new JsonUrlStringBuilder(
                    newOptions(option)).add("1e 1").build(),
                desc);

            // parse("1e+1") -> implied-string("1e 1")
            assertEquals(
                getFactoryString("1e 1"),
                newParser(newOptions(
                        JsonUrlOption.IMPLIED_STRING_LITERALS,
                        option))
                    .parse("1e+1"),
                desc);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(names = "AQF")
        @DisplayName("testLiteral2: 1e%2B1")
        void testLiteral2(JsonUrlOption option) throws IOException {
            final String desc = "1e%2B1";

            if (option == JsonUrlOption.AQF) {
                // parse("1e%2B1") -> number(10)
                assertEquals(
                    factory.getNumber(new NumberBuilder("10")),
                    newParser(newOptions(option)).parse("1e%2B1"),
                    desc);

                // stringify("1e+1") -> string("1e!+1")
                assertEquals(
                    "1e!+1",
                    new JsonUrlStringBuilder(
                        newOptions(option)).add("1e+1").build(),
                    desc);
            } else {
                // parse("1e%2B1") -> string("1e+1")
                assertEquals(
                    getFactoryString("1e+1"),
                    newParser().parse("1e%2B1"),
                    desc);

                // stringify("1e+1") -> string("1e%2B1")
                assertEquals(
                    "1e%2B1",
                    new JsonUrlStringBuilder().add("1e+1").build(),
                    desc);
            }

            // parse("1e%2B1") -> implied-string("1e+1")
            assertEquals(
                getFactoryString("1e+1"),
                newParser(newOptions(JsonUrlOption.IMPLIED_STRING_LITERALS, option))
                    .parse("1e%2B1"),
                desc);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(names = "AQF")
        @DisplayName("testImpliedStringLiteral3: 1e!%2B1")
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        void testImpliedStringLiteral3(JsonUrlOption option) throws IOException {
            final String desc = "1e!%2B1";

            Set<JsonUrlOption> options = newOptions(option);

            for (int i = 0; i < 2; i++) {
                if (option == JsonUrlOption.AQF) {
                    assertEquals(
                        getFactoryString("1e+1"),
                        newParser(options).parse("1e!%2B1"),
                        desc);

                    assertEquals(
                        "1e!!!+1",
                        new JsonUrlStringBuilder(options).add("1e!+1").build(),
                        desc);

                } else {
                    assertEquals(
                        getFactoryString("1e!+1"),
                        newParser(options).parse("1e!%2B1"),
                        desc);

                    assertEquals(
                        "1e!%2B1",
                        new JsonUrlStringBuilder(options).add("1e!+1").build(),
                        desc);
                }

                //
                // Run the same test again.
                // IMPLIED_STRING_LITERALS should have no effect.
                //
                options.add(JsonUrlOption.IMPLIED_STRING_LITERALS);
            }
        }
        
        void assertString2(
                String text,
                String nativeValue,
                Set<JsonUrlOption> options) throws IOException {
            assertEquals(
                text,
                new JsonUrlStringBuilder().add(nativeValue).build(),
                text);

            assertParse(text, getFactoryString(nativeValue));
            assertParse(text,
                newOptions(JsonUrlOption.AQF),
                getFactoryString(nativeValue));

        }

        @ParameterizedTest
        @CsvSource({
            //
            // INPUT,OUTPUT
            //
            "hello,",
            "a,",
            "abc,",
            "1a,",
            "1e,",
            "1.,",
            "fals,",
            "hello+world,hello world",
            "hello%2Bworld,hello+world",
            "y+%3D+mx+%2B+b,y = mx + b",
            "a%3Db%26c%3Dd,a=b&c=d",
            // CHECKSTYLE:OFF
            "hello%F0%9F%8D%95world,hello\uD83C\uDF55world",
            // CHECKSTYLE:ON
            "-,",
            "-e,",
            "-e+,'-e '",
            "-e+1,-e 1",
            "-.,",
            "1e+,'1e '",
            "1e-,",
            "1.2.3,",
            "Eddy\'s+in+the+space+time+continuum,Eddy\'s in the space time continuum",
        })
        void testString2(String text, String out) throws IOException {
            String nativeValue = out == null ? text : out;
            assertString2(text, nativeValue, null);
            assertString2(text, nativeValue, newOptions(JsonUrlOption.AQF));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            HELLO,
            "Bob's House",
            "Hello, World!",
            "World: Hello!",
            "Hello (world).",
        })
        void testStringsWithStructCharsQuotedAndEncoded(String text)
                throws IOException {

            S value = getFactoryString(text);

            //
            // encoded
            //
            assertParse(urlEncodeAndEscapeStructChars(text), value);
            assertParse(urlEncodeAndEscapeAQF(text),
                    newOptions(JsonUrlOption.AQF),
                    value);

            //
            // quoted
            //
            String etext = urlEncode(text).replace("'", "%27");
            assertParse("'" + etext + "'", value);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
                HELLO,
                "t", "tr", "tru", "True", "tRue", "trUe", "truE",
                "f", "fa", "fal", "fals", "False", "fAlse", "faLse", "falSe", "falsE",
                "n", "nu", "nul", "Null", "nUll", "nuLl", "nulL",
        })
        void testString(String text) throws IOException {
            assertParse(text, getFactoryString(text));
            assertParse(text,
                newOptions(JsonUrlOption.AQF),
                getFactoryString(text));
        }

        @ParameterizedTest
        @ValueSource(strings = { NULL })
        void testNull(String text) throws IOException {
            N factoryValue = factory.getNull();

            assertParse(text, factoryValue);

            assertParse(text, newOptions(JsonUrlOption.AQF), factoryValue);

            assertEquals(
                String.valueOf(text),
                new JsonUrlStringBuilder().addNull().build(),
                text);

            assertEquals(factoryValue, newParser().parse(text), text);
            assertEquals(
                factoryValue,
                newParser().parse(text, 0, text.length()),
                text);

            assertEquals(
                getFactoryString(text),
                newParser(newOptionsISL()).parse(text),
                text);
        }

        @ParameterizedTest
        @CsvSource({
            //
            // INPUT,OUTPUT
            //
            // CHECKSTYLE:OFF
            "hello%C2%A2world,'hello\u00A2world'",
            "hello%E2%82%ACworld,'hello\u20ACworld'",
            "hello%F0%9F%8D%95world,'hello\uD83C\uDF55world'",
            // CHECKSTYLE:ON
        })
        void testUtf8(String text, String expected) throws IOException {
            assertEquals(expected, getStringValue(parse(text)), expected);

            assertEquals(expected, getStringValue(
                    parse(text, newOptions(JsonUrlOption.AQF))), expected);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            //
            // https://mathshistory.st-andrews.ac.uk/HistTopics/2000_places/
            // https://www.piday.org/million/
            //
            "3.14159265358979323846264338327950288419716939937510582097494459230781"
            + "64062862089986280348253421170679821480865132823066470938446095505822"
            + "31725359408128481117450284102701938521105559644622948954930381964428"
            + "81097566593344612847564823378678316527120190914564856692346034861045"
            + "43266482133936072602491412737245870066063155881748815209209628292540"
            + "91715364367892590360011330530548820466521384146951941511609433057270"
            + "36575959195309218611738193261179310511854807446237996274956735188575"
            + "2724891227938183011949"
        })
        void testMathContext(String text) throws IOException {
            if (factory instanceof BigMathProvider) {
                MathContext mctx =
                    ((BigMathProvider)factory).getMathContext();

                if (mctx == null) {
                    mctx = MathContext.UNLIMITED;
                }

                BigDecimal expect = new BigDecimal(text, mctx);
                V parseResult = parse(text);

                assertEquals(expect, getNumberValue(parseResult), text);
            }
        }

        @SuppressWarnings("PMD.CyclomaticComplexity")
        void testBigInteger(
                String text,
                MathContext mctx,
                BigIntegerOverflow over) throws IOException {

            if (over == BigIntegerOverflow.INFINITY
                    && !isBigIntegerOverflowInfinityOK()) {
                //
                // this is a hack to handle the fact that the reference
                // implementation of JSR-374 doesn't support +Inf/-Inf.
                //
                return;
            }

            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory = newBigMathFactory(
                MathContext.DECIMAL128,
                BIG_INTEGER128_BOUNDARY_NEG,
                BIG_INTEGER128_BOUNDARY_POS,
                over);

            BigInteger bigint = new BigInteger(text);
            
            if (bigint.bitLength() > 128 && over == null) {
                assertThrows(LimitException.class,
                    () -> newParser(factory).parse(text));

            } else {
                V parseResult = newParser(factory).parse(text);
                Number num = getNumberValue(parseResult);

                if (bigint.bitLength() > 128) { // NOPMD
                    switch (over) {
                    case BIG_DECIMAL:
                        assertEquals(
                            new BigDecimal(text, MathContext.DECIMAL128), num, text);
                        break;
                    case DOUBLE:
                        assertEquals(
                            Double.parseDouble(text),
                            num.doubleValue(),
                            text);
                        break;
                    case INFINITY:
                        if (bigint.signum() == -1) {
                            assertEquals(BigMathProvider.NEGATIVE_INFINITY, num, text);
                        } else {
                            assertEquals(BigMathProvider.POSITIVE_INFINITY,num, text);
                        }
                        break;
                    }
                } else if (bigint.bitLength() > Long.SIZE) {
                    if (num instanceof BigInteger) {
                        assertEquals(bigint, (BigInteger)num, text);
                    } else {
                        assertEquals(new BigDecimal(bigint), num, text);
                    }
                } else {
                    //
                    // some gymnastics to handle JSR-374
                    //
                    assertEquals(
                        Long.parseLong(text),
                        num.longValue(),
                        text);
                }
            }

            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                    JsonUrlOption.IMPLIED_STRING_LITERALS);

            assertEquals(
                    getFactoryString(text),
                    newParser(options).parse(text),
                    text);
        }
        
        void testBigInteger(String text, MathContext mctx)
                throws IOException {

            testBigInteger(text, mctx, null);

            for (BigIntegerOverflow over : BigIntegerOverflow.values()) {
                testBigInteger(text, mctx, over);
            } 
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "1",
            "-1",
            Long.MAX_VALUE + "", // NOPMD - need a string literal
            Long.MIN_VALUE + "", // NOPMD - need a string literal
            Long.MAX_VALUE + "0",
            Long.MIN_VALUE + "0",
            '-' + BIG_INTEGER128_BOUNDARY_NEG,
            BIG_INTEGER128_BOUNDARY_POS,
            BIG_INTEGER128_BOUNDARY_POS + '0',
            '-' + BIG_INTEGER128_BOUNDARY_NEG + '0',
        })
        void testBigInteger(String text) throws IOException {
            if (!(factory instanceof BigMathProvider)) {
                return;
            }

            testBigInteger(text, null);
            testBigInteger(text, MathContext.DECIMAL128);
        }

        @Test
        void testCoerceNullToEmptyString() throws IOException {
            assertEquals(
                factory.getString(EMPTY_STRING),
                newParser(JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING)
                    .parse(NULL),
                NULL);
        }
    }

    /**
     * Text tests.
     */
    @Nested
    @SuppressWarnings("PMD.AccessorMethodGeneration")
    class TextTests {
        private void assertJsonUrlText1(String text, J actual) {
            final String many = "many";

            assertTrue(getBoolean(TRUE, actual), text);
            assertFalse(getBoolean(FALSE, actual), text);
            assertTrue(getNull(NULL, actual), text);
            assertTrue(getEmptyComposite("empty", actual), text);
            assertTrue(getBoolean("1e+1", actual), text);

            assertEquals(
                    factory.getNumber(new NumberBuilder("0")),
                    getNumber(0, getArray("single", actual)),
                    text);
            
            assertEquals(
                    factory.getNumber(new NumberBuilder("-1")),
                    getNumber(0, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getNumber(new NumberBuilder("2.0")),
                    getNumber(1, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getNumber(new NumberBuilder("3e1")),
                    getNumber(2, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getNumber(new NumberBuilder("4e-2")),
                    getNumber(3, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getNumber(new NumberBuilder("5e+0")),
                    getNumber(4, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getNumber(new NumberBuilder("1")),
                    getNumber(0, getArray(0, getArray("nested", actual))),
                    text);

            assertEquals(
                factory.getNumber(new NumberBuilder("2")),
                getNumber(1, getArray(0, getArray("nested2", actual))),
                text);
            
            assertEquals(
                factory.getString("world"),
                getString(HELLO, actual),
                text);
        }

        private void assertJsonUrlText1ImpliedStringLiteral(
                String text,
                J actual) {

            final String many = "many";

            assertEquals(
                    factory.getString(TRUE),
                    getString(TRUE, actual),
                    text);

            assertEquals(
                    factory.getString(FALSE),
                    getString(FALSE, actual),
                    text);
            
            assertEquals(
                    factory.getString(NULL),
                    getString(NULL, actual),
                    text);

            assertEquals(
                    factory.getString(TRUE),
                    getString("1e 1", actual),
                    text);

            assertEquals(
                    factory.getString("1e 2"),
                    getString("1e 2", actual),
                    text);

            assertEquals(
                    factory.getString("0"),
                    getString(0, getArray("single", actual)),
                    text);
            
            assertEquals(
                    factory.getString("-1"),
                    getString(0, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getString("2.0"),
                    getString(1, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getString("3e1"),
                    getString(2, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getString("4e-2"),
                    getString(3, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getString("5e 0"),
                    getString(4, getArray(many, actual)),
                    text);
            
            assertEquals(
                    factory.getString("1"),
                    getString(0, getArray(0, getArray("nested", actual))),
                    text);

            assertEquals(
                    factory.getString("2"),
                    getString(1, getArray(0, getArray("nested2", actual))),
                    text);
            
            assertEquals(
                factory.getString("'world'"),
                getString("'hello'", actual),
                text);
        }

        @Test
        void testJsonUrlText1() throws IOException  {
            final String text = "(true:true,false:false,null:null,empty:()"
                + ",1e+1:true"
                + ",1e+2:1e+2"
                + ",single:(0),nested:((1)),nested2:((1,2))"
                + ",many:(-1,2.0,3e1,4e-2,5e+0),'hello':'world')";

            assertJsonUrlText1(text, parseFactoryObject(text, false));
            assertJsonUrlText1(text, parseImpliedFactoryObject(text,false));

            assertJsonUrlText1(
                    text,
                    parseImpliedFactoryObject(text, 0, text.length(), false));

            //
            // implied string literals
            //
            assertJsonUrlText1ImpliedStringLiteral(
                    text,
                    parseFactoryObject(text, true));

            assertJsonUrlText1ImpliedStringLiteral(
                    text,
                    parseImpliedFactoryObject(text,true));

            assertJsonUrlText1ImpliedStringLiteral(
                    text,
                    parseImpliedFactoryObject(text, 0, text.length(), true));
        }

        private void assertJsonUrlText2(String text, A actual) {
            assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, actual),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"(1)"})
        void testJsonUrlText2(String text) throws IOException {
            assertJsonUrlText2(text, parseFactoryArray(text));
            assertJsonUrlText2(text, parseImpliedFactoryArray(text));
            assertJsonUrlText2(text, parseImpliedFactoryArray(text, 0, text.length()));
        }
        
        private void assertJsonUrlText3(String text, A actual) {
            assertEquals(
                    factory.getNumber(new NumberBuilder("2")),
                    getNumber(0, getArray(1, actual)),
                    text);
            
        }

        @ParameterizedTest
        @ValueSource(strings = {"(1,(2))"})
        void testJsonUrlText3(String text) throws IOException {
            assertJsonUrlText3(text, parseFactoryArray(text));
            assertJsonUrlText3(text, parseImpliedFactoryArray(text));
            assertJsonUrlText3(text, parseImpliedFactoryArray(text, 0, text.length()));
        }
        
        private void assertJsonUrlText4(String text, A actual) {
            assertEquals(
                factory.getNumber(new NumberBuilder("2")),
                getNumber("a", getObject(1, actual)),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"(1,(a:2),3)"})
        void testJsonUrlText4(String text) throws IOException {
            assertJsonUrlText4(text, parseFactoryArray(text));
            assertJsonUrlText4(text, parseImpliedFactoryArray(text));
            assertJsonUrlText4(text, parseImpliedFactoryArray(text, 0, text.length()));

        }

        private void assertJsonUrlText5(String text, J actual) {
            assertEquals(
                factory.getString("Fred"),
                getString("first", getObject("name", actual)),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"(age:64,name:(first:Fred))"})
        void testJsonUrlText5(String text) throws IOException {
            assertJsonUrlText5(text, parseFactoryObject(text, false));
            assertJsonUrlText5(text, parseImpliedFactoryObject(text, false));
            assertJsonUrlText5(text, parseImpliedFactoryObject(text, 0, text.length(), false));
        }
        
        private void assertJsonUrlText6(String text, A actual) {
            assertEquals(
                    factory.getNumber(new NumberBuilder("1")),
                    getNumber(0, getArray(0, getArray(0, getArray(0, actual)))),
                    text);
            
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"((((1))))"})
        @DisplayName("testJsonUrlText6: Deeply nested array")
        void testJsonUrlText6(String text) throws IOException {
            assertJsonUrlText6(text, parseFactoryArray(text));
            assertJsonUrlText6(text, parseImpliedFactoryArray(text));
            assertJsonUrlText6(text, parseImpliedFactoryArray(text, 0, text.length()));
        }

        private void assertJsonUrlText7(String text, A actual) {
            assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, actual),
                text);
            
            assertTrue(getEmptyComposite(1, actual), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"(1,())"})
        void testJsonUrlText7(String text) throws IOException {
            assertJsonUrlText7(text, parseFactoryArray(text));
            assertJsonUrlText7(text, parseImpliedFactoryArray(text));
            assertJsonUrlText7(text, parseImpliedFactoryArray(text, 0, text.length()));
        }

        private void assertJsonUrlText8(String text, J actual) {
            assertTrue(getEmptyComposite("a", actual), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"(a:())"})
        void testJsonUrlText8(String text) throws IOException {
            assertJsonUrlText8(text, parseFactoryObject(text, false));
            assertJsonUrlText8(text, parseImpliedFactoryObject(text, false));
            assertJsonUrlText8(text, parseImpliedFactoryObject(text, 0, text.length(), false));
        }

        @ParameterizedTest
        @ValueSource(strings = {"(:)"})
        void testJsonUrlText9(String text) throws IOException {
            assertTrue(isEmptyObject(parseFactoryObject(text,
                EnumSet.of(JsonUrlOption.NO_EMPTY_COMPOSITE))), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"%28%3a%29"})
        void testJsonUrlText10(String text) throws IOException {
            assertTrue(
                isEmptyObject(parseFactoryObject(text,
                    EnumSet.of(JsonUrlOption.AQF,
                        JsonUrlOption.NO_EMPTY_COMPOSITE))),
                text);
        }

    }

    /**
     * Composite tests.
     */
    @Nested
    @SuppressWarnings("PMD.AccessorMethodGeneration")
    class CompositeTests {

        @ParameterizedTest
        @ValueSource(strings = { "()" })
        void testEmptyComposite(String text) throws IOException {
            assertParse(text, factory.getEmptyComposite());
            assertParse(text,
                newOptions(JsonUrlOption.AQF),
                factory.getEmptyComposite());

            String txt = new JsonUrlStringBuilder().addEmptyComposite().build();
            assertEquals(text, txt, text);
            assertTrue(factory.isEmptyComposite(
                newParser().parse(text)), text);
        }

        @ParameterizedTest
        @ValueSource(strings = { "(:)" })
        void testNoEmptyCompositeObject(String text) throws IOException {
            J expected = factory.newObject(factory.newObjectBuilder());

            assertParse(text,
                newOptions(JsonUrlOption.NO_EMPTY_COMPOSITE),
                expected);

            assertParse(text,
                newOptions(JsonUrlOption.AQF, JsonUrlOption.NO_EMPTY_COMPOSITE),
                expected);
        }

        @ParameterizedTest
        @ValueSource(strings = { "()" })
        void testNoEmptyCompositeArray(String text) throws IOException {
            A expected = factory.newArray(factory.newArrayBuilder());

            assertParse(text,
                newOptions(JsonUrlOption.NO_EMPTY_COMPOSITE),
                expected);

            assertParse(text,
                newOptions(JsonUrlOption.AQF, JsonUrlOption.NO_EMPTY_COMPOSITE),
                expected);
        }
        
        @ParameterizedTest
        @ValueSource(strings = { "" })
        void testEmptyCompositeImplied(String text) throws IOException {
            J expectedObject = factory.newObject(factory.newObjectBuilder());

            J actualObject = newParser().parseObject(
                makeImplied(text),
                factory.newObjectBuilder());
            
            assertTrue(isEqual(expectedObject, actualObject));
            
            A expectedArray = factory.newArray(factory.newArrayBuilder());

            A actualArray = newParser().parseArray(
                makeImplied(text),
                factory.newArrayBuilder());
            
            assertTrue(isEqual(expectedArray, actualArray));
        }

        private void assertObjectWfu(String text, J actual) {
            assertEquals(
                factory.getString("b"),
                getString("a", actual),
                text);

            assertEquals(
                factory.getString("d"),
                getString("c", actual),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(a:b&c:d)",
            "(a:b&c=d)",
            "(a=b&c:d)",
            "(a=b&c=d&e=false&f=1.234&g=null&h=()&i=(1,2,3)&j=(a:d,b:a))",
        })
        void testObjectWfu(String text) throws IOException {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parse(text),
                text);
            
            assertThrows(
                SyntaxException.class,
                () -> newParser().parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()),
                text);

            assertObjectWfu(text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE))
                .parseObject(text));
            
            assertObjectWfu(text,
                newParser(newOptions(
                        JsonUrlOption.WFU_COMPOSITE,
                        JsonUrlOption.AQF))
                .parseObject(text));

            assertObjectWfu(
                text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE))
                .parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()));

            assertObjectWfu(
                text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE,
                    JsonUrlOption.AQF))
                .parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "a=b&&c:d",
            "a=b&c=d&",
            "a=b&c=d&&",
            "a=b&&c=d",
            "a&&c=d",
            "a=b&&c",
            "a=b&&c&",
            "&a=b&c=d",
            "a=b&&c:d&&e:f",
        })
        void testObjectWfuExtraAmps(String text) throws IOException {

            assertThrows(
                SyntaxException.class,
                () -> newParser().parseObject(
                    text,
                    factory.newObjectBuilder()),
                text);

            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.WFU_COMPOSITE);

            assertObjectWfu(
                text,
                newParser(options).parseObject(
                    text,
                    factory.newObjectBuilder(),
                    (key, pos) -> {
                        switch (key) {
                        case "a": return factory.getString("b");
                        case "c": return factory.getString("d");
                        default: return null;
                        }
                    }));
        }

        private void assertArrayWfu(String text, A actual) {
            assertEquals(
                factory.getString("a"),
                getString(0, actual),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(a&b)",
            "(a&b&c&d)",
            "(a&false&1.234&null&()&(1,2,3)&(a:d,b:a))",
        })
        void testArrayWfu(String text) throws IOException {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parse(text),
                text);
            
            assertThrows(
                SyntaxException.class,
                () -> newParser().parseArray(
                    makeImplied(text),
                    factory.newArrayBuilder()),
                text);

            assertArrayWfu(
                text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE))
                .parseArray(text));

            assertArrayWfu(
                text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE,
                    JsonUrlOption.AQF))
                .parseArray(text));

            assertArrayWfu(
                text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE))
                .parseArray(makeImplied(text), factory.newArrayBuilder()));

            assertArrayWfu(
                text,
                newParser(newOptions(
                    JsonUrlOption.WFU_COMPOSITE,
                    JsonUrlOption.AQF))
                .parseArray(makeImplied(text), factory.newArrayBuilder()));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "&a",
            "a&",
            "&a&",
            "a&&b",
            "&a&b",
            "&a&b&",
            "a&b&",
            "a&b&&c&d",
            "a&b&c&&d",
            "a&&false&&1.234&&null&&()&&(1)&&(1,2,3)&&(a:d,b:a)&&",
            "a&true&",
            "a&()&",
            "a&(1)&",
            "a&(1,2)&",
            "a&(a:b)&",
        })
        void testArrayWfuExtraAmps(String text) throws IOException {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parseArray(
                    text,
                    factory.newArrayBuilder()),
                text);


            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.WFU_COMPOSITE);

            assertArrayWfu(
                text,
                newParser(options).parseArray(
                    text,
                    factory.newArrayBuilder()));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(1)",
            "(hello)",
            "(true)",
            "(null)",
            "(())",
            "(1,2)",
            "(hello,world)",
            "(true,false)",
            "(null,null)",
            "((),())",
            "(1,(a:b))",
        })
        void testArray(String text) throws IOException {
            A obj1 = newParser().parseArray(text);

            A obj2 = newParser().parseArray(
                PREFIX1 + text + SUFFIX1,
                PREFIX1.length(),
                text.length());
            
            assertTrue(isEqual(obj1, obj2), text);

            parse(text, null);
            
            //
            // direct call to Parser.parseArray()
            //
            // using assertTrue() because the json.org parser doesn't implement
            // JSONObject.equals()
            //
            A objA = newParser().parseArray(text);
            A objB = newParser().parseArray(text, 0, text.length());
            assertTrue(isEqual(objA, objB), text);

            V objC = newParser().parse(text);
            assertTrue(isEqual(objA, objC), text);
            
            V objD = newParser().parse(text, 0, text.length());
            assertTrue(isEqual(objA, objD), text);
            
            V objG = newParser().parseArray(makeImplied(text), factory.newArrayBuilder());
            assertTrue(isEqual(objA, objG), text);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "(a:b)",
            "(a:(b))",
            "(a:(b,c))",
        })
        void testObject(String text) throws IOException {
            J obj1 = parseFactoryObject(text, false);

            J obj2 = newParser().parseObject(
                PREFIX1 + text + SUFFIX1,
                PREFIX1.length(),
                text.length());

            assertTrue(isEqual(obj1, obj2), text);

            assertParse(text, null);

            //
            // direct call to Parser.parseObject()
            //
            // using assertTrue() because org.json.JSONObject doesn't implement
            // an equals method
            //
            J objA = newParser().parseObject(text);
            J objB = newParser().parseObject(text, 0, text.length());
            assertTrue(isEqual(objA, objB), text);

            V objC = newParser().parse(text);
            assertTrue(isEqual(objA, objC), text);

            V objD = newParser().parse(text, 0, text.length());
            assertTrue(isEqual(objA, objD), text);

            V objG = newParser().parseObject(makeImplied(text), factory.newObjectBuilder());
            assertTrue(isEqual(objA, objG), text);
        }
        
        private void assertAllowEmptyUnquotedArrayValue(String text, A actual) {
            assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, actual),
                text);

            assertEquals(
                factory.getString(""),
                getString(1, actual),
                text);

            assertEquals(
                factory.getNumber(new NumberBuilder("3")),
                getNumber(2, actual),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(1,,3)",
        })
        void testAllowEmptyUnquotedArrayValue(String text) throws IOException {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parse(text),
                text);

            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.EMPTY_UNQUOTED_VALUE);

            assertAllowEmptyUnquotedArrayValue(
                text,
                newParser(options).parseArray(text));

            assertAllowEmptyUnquotedArrayValue(
                text,
                newParser(options).parseArray(
                    makeImplied(text),
                    factory.newArrayBuilder()));
        }

        private void assertSkipNullArrayValue(String text, A actual) {
            assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, actual),
                text);

            assertEquals(
                factory.getNumber(new NumberBuilder("3")),
                getNumber(1, actual),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(1,null,3)",
            "(1,null,null,null,null,3)",
            "(null,1,null,null,null,null,3,null)",
        })
        void testSkipNullArrayValue(String text) throws IOException {
            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.SKIP_NULLS);

            assertSkipNullArrayValue(
                text,
                newParser(options).parseArray(text));

            assertSkipNullArrayValue(
                text,
                newParser(options).parseArray(
                    makeImplied(text),
                    factory.newArrayBuilder()));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(null)",
            "(null,null,null,null)",
        })
        void testSkipNullSingleArrayValue(String text) throws IOException {
            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.SKIP_NULLS);

            assertEquals(0, getSize(
                newParser(options).parseArray(text)), text);

            assertEquals(0, getSize(
                    newParser(options).parseArray(
                        makeImplied(text),
                        factory.newArrayBuilder())),
                    text);
        }

        private void assertAllowEmptyUnquotedObjectValue(
                String text,
                J actual) {

            assertEquals(
                factory.getString(""),
                getString("a", actual),
                text);

            assertTrue(getBoolean("b", actual), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(a:,b:true)"
        })
        void testAllowEmptyUnquotedObjectValue(String text) throws IOException {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parse(text),
                text);

            assertAllowEmptyUnquotedObjectValue(
                    text,
                    newParser(newOptions(
                        JsonUrlOption.EMPTY_UNQUOTED_VALUE))
                    .parseObject(text));

            assertAllowEmptyUnquotedObjectValue(
                    text,
                    newParser(newOptions(
                        JsonUrlOption.AQF,
                        JsonUrlOption.EMPTY_UNQUOTED_VALUE))
                    .parseObject(text));
                
            assertAllowEmptyUnquotedObjectValue(
                text,
                newParser(newOptions(
                    JsonUrlOption.EMPTY_UNQUOTED_VALUE))
                .parseObject(makeImplied(text), factory.newObjectBuilder()));
            
            assertAllowEmptyUnquotedObjectValue(
                text,
                newParser(newOptions(
                    JsonUrlOption.AQF,
                    JsonUrlOption.EMPTY_UNQUOTED_VALUE))
                .parseObject(makeImplied(text), factory.newObjectBuilder()));
        }

        private void assertSkipNullObjectValue(String text, J actual) {
            assertTrue(getNull("a", actual), text);
            assertTrue(getBoolean("b", actual), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(a:null,b:true)",
            "(b:true,a:null)"
        })
        void testSkipNullObjectValue(String text) throws IOException {
            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.SKIP_NULLS);

            assertSkipNullObjectValue(
                text,
                newParser(options).parseObject(text));
                
            assertSkipNullObjectValue(
                text,
                newParser(options).parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()));
        }

        private void assertAllowEmptyUnquotedKey(String text, J actual) {
            assertFalse(getBoolean("", actual), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "(:false)"
        })
        void testAllowEmptyUnquotedKey(String text) throws IOException {
            assertThrows(
                SyntaxException.class,
                () -> newParser().parse(text),
                text);

            assertAllowEmptyUnquotedKey(
                text,
                newParser(newOptions(
                    JsonUrlOption.EMPTY_UNQUOTED_KEY))
                .parseObject(text));

            assertAllowEmptyUnquotedKey(
                text,
                newParser(newOptions(
                    JsonUrlOption.AQF,
                    JsonUrlOption.EMPTY_UNQUOTED_KEY))
                .parseObject(text));

            assertAllowEmptyUnquotedKey(
                text,
                newParser(newOptions(
                    JsonUrlOption.EMPTY_UNQUOTED_KEY))
                .parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()));

            assertAllowEmptyUnquotedKey(
                text,
                newParser(newOptions(
                    JsonUrlOption.AQF,
                    JsonUrlOption.EMPTY_UNQUOTED_KEY))
                .parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()));

        }
        
        @ParameterizedTest
        @CsvSource({
            "'a,b:true','(a:hello,b:true)'",
            "'a:hello,b','(a:hello,b:true)'",
            "'a:hello,b,c:1','(a:hello,b:true,c:1)'",
            "'a,b','(a:hello,b:true)'",
            "'a&b=true','(a:hello,b:true)'",
            "'a=hello&b','(a:hello,b:true)'",
            "'a=hello&b,c=1','(a:hello,b:true,c:1)'",
            "'a&b','(a:hello,b:true)'",

        })
        void testMissingObjectValueProvider(
                String text,
                String expectedText) throws IOException {

            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                JsonUrlOption.EMPTY_UNQUOTED_KEY,
                JsonUrlOption.WFU_COMPOSITE);

            assertThrows(
                SyntaxException.class,
                () -> newParser(options).parseObject(text),
                text);

            final MissingValueProvider<V> mvp = (key, pos) -> {
                switch (key) {
                case "a":
                    return factory.getString(HELLO);
                case "b":
                    return factory.getTrue();
                default:
                    break;
                }

                return null;
            };

            //
            // assertEquals is preferred, however, JSONObject doesn't implement
            // the equals() method. So, fallback to assertTrue.
            //
            J actual = newParser(options).parseObject(
                text,
                factory.newObjectBuilder(),
                mvp);

            assertTrue(
                isEqual(newParser(options).parse(expectedText), actual),
                text);
            
            actual = newParser(options).parseObject(
                text,
                0,
                text.length(),
                factory.newObjectBuilder(),
                mvp);

            assertTrue(
                isEqual(newParser(options).parse(expectedText), actual),
                text);
        }

        private void assertImpliedStringLiterals(String text, J actual) {
            assertEquals(
                getFactoryString("'hello back'"),
                getString("'hello world'", actual),
                text);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "('hello+world':'hello+back')"
        })
        void testImpliedStringLiterals(String text) throws IOException {
            Set<JsonUrlOption> options = JsonUrlOption.newSet(
                    JsonUrlOption.IMPLIED_STRING_LITERALS);

            assertImpliedStringLiterals(
                text,
                newParser(options).parseObject(text));

            assertImpliedStringLiterals(
                text,
                newParser(options).parseObject(
                    makeImplied(text),
                    factory.newObjectBuilder()));
        }

    }

    /**
     * Miscellaneous tests.
     */
    @Nested
    @SuppressWarnings("PMD.AccessorMethodGeneration")
    class MiscTests {
        @Test
        void testGetFactory() {
            assertSame(
                factory,
                newParser().factory(),
                factory.getClass().getName());        
        }
    }

    /**
     * Create a new AbstractParseTest.
     */
    public AbstractParseTest(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        this.factory = factory;
    }
    
    private void assertParse(
            String text,
            Set<JsonUrlOption> options,
            Object expect) throws IOException {
        
        StringBuilder buf = new StringBuilder(1 << 10);

        //
        // Parser.parse()
        //
        buf.append(PREFIX1).append(text).append(SUFFIX1);

        V parseResult = newParser(options).parse(
            buf.toString(),
            PREFIX1.length(),
            text.length());

        //
        // just test that prefix and non/prefix are the same by default
        //
        final Object expectValue = expect == null
            ? newParser(options).parse(text) : expect;

        //
        // I should just use assertEquals() here but the json.org
        // implementation doesn't override Object.equals().
        //
        // assertEquals(expect, parseResult, in)
        assertTrue(isEqual(expectValue, parseResult), text);
    }
    
    private void assertParse(String text, Object expect) throws IOException {
        assertParse(text, null, expect);
    }

    private V parse(String text) throws IOException {
        return parse(text, null);
    }
        
    private V parse(String text, Set<JsonUrlOption> options) throws IOException {
        StringBuilder buf = new StringBuilder(4096)
            .append(PREFIX1)
            .append(text)
            .append(SUFFIX1);

        return newParser(options).parse(
                buf.toString(),
                PREFIX1.length(),
                text.length());
    }

    /**
     * Remove leading and trailing chars to build an implied array or object.
     */
    static final String makeImplied(String text) {
        if (text == null || text.length() < 2) {
            return "";
        }

        StringBuilder buf = new StringBuilder(text.length())
                .append(text, 1, text.length() - 1);

        return buf.toString();
    }

    private A parseFactoryArray(String text) throws IOException {
        return newParser().parseArray(text);
    }

    private A parseFactoryArray(String text, Set<JsonUrlOption> options)
            throws IOException {
        return newParser(options).parseArray(text);
    }

    private A parseImpliedFactoryArray(String text) throws IOException {
        return newParser().parseArray(
                makeImplied(text),
                factory.newArrayBuilder());
    }

    private A parseImpliedFactoryArray(
            String text,
            int off,
            int len) throws IOException {
        return newParser().parseArray(
            makeImplied(text),
            off,
            Math.max(0, len - 2),
            factory.newArrayBuilder());
    }

    private J parseFactoryObject(
            String text,
            boolean impliedStringLiterals) throws IOException {

        Set<JsonUrlOption> options = JsonUrlOption.newSet();
        if (impliedStringLiterals) {
            options.add(JsonUrlOption.IMPLIED_STRING_LITERALS);
        }

        return parseFactoryObject(text, options);
    }
    
    private J parseFactoryObject(
            String text,
            Set<JsonUrlOption> options) throws IOException {
        return newParser(options).parseObject(text);
    }

    private J parseImpliedFactoryObject(
            String jsonUrlText,
            boolean impliedStringLiterals) throws IOException {

        Set<JsonUrlOption> options = JsonUrlOption.newSet();
        if (impliedStringLiterals) {
            options.add(JsonUrlOption.IMPLIED_STRING_LITERALS);
        }

        return newParser(options).parseObject(
            makeImplied(jsonUrlText),
            factory.newObjectBuilder());
    }

    private J parseImpliedFactoryObject(
            String text,
            int off,
            int len,
            boolean impliedStringLiterals) throws IOException {

        Set<JsonUrlOption> options = JsonUrlOption.newSet();
        if (impliedStringLiterals) {
            options.add(JsonUrlOption.IMPLIED_STRING_LITERALS);
        }

        return newParser(options).parseObject(
            makeImplied(text),
            off,
            Math.max(0, len - 2),
            factory.newObjectBuilder());
    }

    private B getFactoryBoolean(String text) {
        return factory.getBoolean(Boolean.parseBoolean(text));
    }

    private S getFactoryString(String text) {
        return factory.getString(text);
    }

    private M getFactoryLong(String text) {
        return factory.getNumber(new NumberBuilder(text));
    }

    private M getFactoryDouble(String text) {
        return factory.getNumber(new NumberBuilder(text));
    }

    private static String urlEncode(String text)
            throws UnsupportedEncodingException {

        return URLEncoder
            //.encode(s, StandardCharsets.UTF_8)
            .encode(text, "UTF-8");
    }
    
    private static String urlDecode(String text)
            throws UnsupportedEncodingException {
        return URLDecoder.decode(text.replace('+', ' '), "UTF-8");
    }

    private static String urlEncodeAndEscapeStructChars(String text)
            throws UnsupportedEncodingException {

        return urlEncode(text)
            .replace("(", "%28")
            .replace(")", "%29")
            .replace(",", "%2C")
            .replace(":", "%3A");
    }

    private static String urlEncodeAndEscapeAQF(String text)
            throws UnsupportedEncodingException {

        return text
            .replaceAll("([(),:!])", "!$1")
            .replace(' ', '+');
    }

    private static Set<JsonUrlOption> newOptionsISL() {
        return JsonUrlOption.enableImpliedStringLiterals(
            JsonUrlOption.newSet());
    }

    private static Set<JsonUrlOption> newOptions(
            JsonUrlOption first,
            JsonUrlOption... rest) {
        if (first == null) {
            return JsonUrlOption.newSet();
        }
        if (rest == null || rest.length == 0 || rest[0] == null) {
            return JsonUrlOption.newSet(first);
        }
        return JsonUrlOption.newSet(first, rest);
    }

    /** Get the boolean from the given object for the given key. */
    protected abstract boolean getBoolean(String key, J value);

    /** Get the String from the given object for the given key. */
    protected abstract S getString(String key, J value);

    /** Get the String at the index from the given array. */
    protected abstract S getString(int index, A value);

    /** Test for null in the given object for the given key. */
    protected abstract boolean getNull(String key, J value);

    /** Test for empty composite in the given object for the given key. */
    protected abstract boolean getEmptyComposite(String key, J value);

    /** Test for null at the given index in the given array. */
    protected abstract boolean getEmptyComposite(int index, A value);

    /** Get the array from the given object for the given key. */
    protected abstract A getArray(String key, J value);

    /** Get the array at the given index in the given array. */
    protected abstract A getArray(int index, A value);

    /** Get the object from the given object for the given key. */
    protected abstract J getObject(String key, J value);

    /** Get the object at the given index in the given array. */
    protected abstract J getObject(int index, A value);

    /** Get the number at the given index in the given array. */
    protected abstract M getNumber(int index, A value);

    /** Get the number from the given object for the given key. */
    protected abstract M getNumber(String key, J value);

    /** Get the Java Number for the given factory number. */
    protected abstract Number getNumberValue(V value);

    /** Get the Java String for the given factory number. */
    protected abstract String getStringValue(V value);

    /** Get the size of the given array. */
    protected abstract int getSize(A value);

    /** Test if the given object is empty. */
    protected abstract boolean isEmptyObject(J value);

    /** Create a new bing math factory. */
    protected abstract ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> newBigMathFactory(
        MathContext mctx,
        String boundNeg,
        String boundPos,
        BigIntegerOverflow over);
    

    /**
     * Test if BigIntegerOverflow supports {@link Double#POSITIVE_INFINITY}.
     */
    protected boolean isBigIntegerOverflowInfinityOK() {
        return true;
    }

    /**
     * Test two objects are equal. 
     */
    protected boolean isEqual(Object a, Object b) { // NOPMD
        return Objects.equals(a, b);
    }
    
}
