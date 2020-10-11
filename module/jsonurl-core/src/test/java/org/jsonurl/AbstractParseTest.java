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

package org.jsonurl;

import static org.jsonurl.BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG;
import static org.jsonurl.BigMathProvider.BIG_INTEGER128_BOUNDARY_POS;
import static org.jsonurl.NumberBuilderTest.TAG_BIG;
import static org.jsonurl.NumberBuilderTest.TAG_DOUBLE;
import static org.jsonurl.NumberBuilderTest.TAG_LONG;
import static org.jsonurl.NumberBuilderTest.TAG_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.Objects;
import org.jsonurl.BigMathProvider.BigIntegerOverflow;
import org.jsonurl.j2se.JavaValueFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JSON&#x2192;URL parser test implementation.
 *
 * <p>Any implementation of the {@link ValueFactory} interface may use this
 * class to implement parser unit tests.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
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
    
    /** true. */
    private static final String TRUE = "true";

    /** false. */
    private static final String FALSE = "false";

    /** null. */
    private static final String NULL = "null";

    /** tag annotation. */
    public static final String TAG_ARRAY = "array";

    /** tag annotation. */
    public static final String TAG_BOOLEAN = "boolean";

    /** tag annotation. */
    public static final String TAG_EMPTY = "empty";

    /** tag annotation. */
    public static final String TAG_EXCEPTION = "exception";

    /** tag annotation. */
    public static final String TAG_NULL = NULL;

    /** tag annotation. */
    public static final String TAG_OBJECT = "object";

    /** tag annotation. */
    public static final String TAG_PARSE = "parse";

    /** tag annotation. */
    public static final String TAG_STRING = "string";

    /** Nonsense prefix to test parse start/end support. */
    private static final String PREFIX1 = "prefix 1";

    /** Nonsense prefix to test parse start/end support. */
    private static final String PREFIX2 = "number 2 prefix";

    /** Nonsense suffix to test parse start/end support. */
    private static final String SUFFIX1 = "suffix 1";
    
    /** Nonsense suffix to test parse start/end support. */
    private static final String SUFFIX2 = "number two suffix";

    /** ValueFactory. */
    protected ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;

    /** Create a new AbstractParseTest. */
    public AbstractParseTest(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        this.factory = factory;
    }


    private ValueFactoryParser<V,C,ABT,A,JBT,J,B,M,N,S> newFactoryParser() {
        return newParser(factory);
    }
    
    private ValueFactoryParser<V,C,ABT,A,JBT,J,B,M,N,S> newParser(
            ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        return new ValueFactoryParser<>(factory);
    }

    private void parseLiteral(
            ValueType allow,
            String text,
            Object expected) {
        parse(EnumSet.of(allow), text, expected, true);
    }
    
    private void parse(
            EnumSet<ValueType> allow,
            String text,
            Object expect,
            boolean isLiteral) {
        parse(allow, text, expect, isLiteral, false);
    }

    private void parse(
            EnumSet<ValueType> allow,
            String text,
            Object expect,
            boolean isLiteral,
            boolean allowEmptyString) {

        StringBuilder buf = new StringBuilder(1 << 10);

        //
        // Parser.parse()
        //
        buf.append(PREFIX1).append(text).append(SUFFIX1);

        V parseResult = newFactoryParser().parse(
            buf.toString(), PREFIX1.length(), text.length());

        //
        // just test that prefix and non/prefix are the same by default
        //
        final Object expectValue = expect == null
                ? newFactoryParser().parse(text) : expect;

        //
        // I should just use assertEquals() here but the json.org
        // implementation doesn't override Object.equals().
        //
        // assertEquals(expect, parseResult, in)
        assertTrue(isEqual(expectValue, parseResult), text);

        if (allow != null) {
            assertTrue(factory.isValid(allow, parseResult), text);            
        }

        //
        // JsonUrl.parseLiteral()
        //
        if (isLiteral) {
            buf.setLength(0);
            buf.append(PREFIX2).append(text).append(SUFFIX2);
    
            V litResult = JsonUrl.parseLiteral(
                buf.toString(), PREFIX2.length(), text.length(), factory, allowEmptyString);
    
            Object litCompare = expectValue;
            assertEquals(litCompare, litResult, text);
    
            if (allow != null) {
                assertTrue(factory.isValid(allow, litResult), text);
            }
    
            assertEquals(text.length(), JsonUrl.parseLiteralLength(text), text);
        }
    }

    private V parse(String text) {
        StringBuilder buf = new StringBuilder(4096)
            .append(PREFIX1)
            .append(text)
            .append(SUFFIX1);

        return newFactoryParser().parse(buf.toString(), PREFIX1.length(), text.length());
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

    private A parseFactoryArray(String jsonUrlText) {
        return newFactoryParser()
                .parseArray(jsonUrlText);
    }

    private A parseImpliedFactoryArray(String jsonUrlText) {
        return newFactoryParser().parseArray(
                makeImplied(jsonUrlText), factory.newArrayBuilder());
    }

    private A parseImpliedFactoryArray(String jsonUrlText, int off, int len) {
        return newFactoryParser().parseArray(
            makeImplied(jsonUrlText),
            off,
            Math.max(0, len - 2),
            factory.newArrayBuilder());
    }

    private J parseFactoryObject(String jsonUrlText) {
        ValueFactoryParser<V, C, ABT, A, JBT, J, B, M, N, S> p = newFactoryParser();
        return p.parseObject(jsonUrlText);
    }

    private J parseImpliedFactoryObject(String jsonUrlText) {
        ValueFactoryParser<V, C, ABT, A, JBT, J, B, M, N, S> p = newFactoryParser();
        return p.parseObject(makeImplied(jsonUrlText), factory.newObjectBuilder());
    }

    private J parseImpliedFactoryObject(String jsonUrlText, int off, int len) {
        ValueFactoryParser<V, C, ABT, A, JBT, J, B, M, N, S> p = newFactoryParser();
        return p.parseObject(
            makeImplied(jsonUrlText),
            off,
            Math.max(0, len - 2),
            factory.newObjectBuilder());
    }

    private void assertLong(
            String expect,
            String actual,
            M factoryValue) {

        assertEquals(expect, actual, expect);
        assertEquals(factoryValue, JsonUrl.parseLiteral(actual, factory), expect);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
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
        "'4e17',400000000000000000",
    })
    void testLong(String in, String out) throws IOException {
        M factoryValue = getFactoryLong(out);
        parseLiteral(ValueType.NUMBER, in, factoryValue);

        Long nativeValue = Long.valueOf(out);
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertLong(out, txt, factoryValue);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "0", "-1", "123456", "-123456", "12345678905432132",
    })
    void testLong(String s) throws IOException {
        M factoryValue = getFactoryLong(s);
        parseLiteral(ValueType.NUMBER, s, factoryValue);

        Long nativeValue = Long.parseLong(s);
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertLong(s, txt, factoryValue);
    }

    private M getFactoryLong(String s) {
        return factory.getNumber(new NumberBuilder(s));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_DOUBLE)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "0.1", "-1.1",
        "1e-2", "-2e-1",
        "1.9e2", "-2.8e1",
        "156.9e-2", "-276.8e-1",
        "156.911e+2", "-276.833e+4",
    })
    void testDouble(String s) throws IOException {
        M factoryValue = getFactoryDouble(s);
        parseLiteral(ValueType.NUMBER, s, factoryValue);

        Number nativeValue = Double.valueOf(s);

        //
        // test JsonUrlStringBuilder
        //
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(String.valueOf(nativeValue), txt, s);
        
        //
        // test that parseLiteral eventually returns the same value that I
        // get directly from the factory
        //
        assertEquals(factoryValue, JsonUrl.parseLiteral(s, factory), s);

        //
        // test that parse and parse literal return the same value.
        // If I simply try to compare the strings/text then the differences
        // in scientific notation (e.g. 100 vs. 1e2) and stripped zeros
        // after the decmial point (e.g. 1.0 vs 1) case some tests to fail.
        // So I'm passing this through Double.valueOf to account for those
        // differences.
        //
        assertEquals(
            Double.valueOf(factoryValue.toString()),
            Double.valueOf(JsonUrl.parseLiteral(txt, factory).toString()),
            txt);
    }

    private M getFactoryDouble(String s) {
        return factory.getNumber(new NumberBuilder(s));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_BOOLEAN)
    @ValueSource(strings = { TRUE, FALSE })
    void testBoolean(String s) throws IOException {
        B factoryValue = getFactoryBoolean(s);
        parseLiteral(ValueType.BOOLEAN, s, factoryValue);

        Boolean nativeValue = Boolean.valueOf(s);
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(String.valueOf(nativeValue), txt, s);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory), s);
    }

    private B getFactoryBoolean(String s) {
        return factory.getBoolean(Boolean.parseBoolean(s));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_NULL)
    @ValueSource(strings = { NULL })
    void testNull(String s) throws IOException {
        N factoryValue = factory.getNull();
        factory.isValid(ValueType.NULL, factoryValue);
        parse(EnumSet.of(ValueType.NULL), s, factoryValue, true);

        String txt = new JsonUrlStringBuilder().addNull().build();
        assertEquals(String.valueOf(s), txt, s);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory), s);

        assertEquals(factoryValue, newFactoryParser().parse(s, ValueType.NULL), s);
        assertEquals(
            factoryValue,
            newFactoryParser().parse(s, 0, s.length(), ValueType.NULL),
            s);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EMPTY)
    @ValueSource(strings = { "()" })
    void testEmptyComposite(String s) throws IOException {
        V expected = factory.getEmptyComposite();

        assertTrue(factory.isValid(ValueType.OBJECT, expected));
        assertTrue(factory.isValid(ValueType.ARRAY, expected));

        parse(EnumSet.of(ValueType.OBJECT, ValueType.ARRAY),
            s, expected, false);

        String txt = new JsonUrlStringBuilder().addEmptyComposite().build();
        assertEquals(s, txt, s);

        assertTrue(factory.isEmptyComposite(newFactoryParser().parse(s)), s);
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EMPTY)
    @ValueSource(strings = { "" })
    void testEmptyCompositeImplied(String s) throws IOException {
        J expectedObject = factory.newObject(factory.newObjectBuilder());

        J actualObject = new Parser().parseObject(
            makeImplied(s),
            factory,
            factory.newObjectBuilder());
        
        assertTrue(isEqual(expectedObject, actualObject));
        
        A expectedArray = factory.newArray(factory.newArrayBuilder());

        A actualArray = new Parser().parseArray(
            makeImplied(s),
            factory,
            factory.newArrayBuilder());
        
        assertTrue(isEqual(expectedArray, actualArray));
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EMPTY)
    @ValueSource(strings = { "", "''" })
    void testEmptyString(String text) throws IOException {
        assertEquals(text.length(), JsonUrl.parseLiteralLength(text));

        assertEquals(text.length(),
            JsonUrl.parseLiteralLength(text, 0, text.length()));

        assertEquals(text.length(), JsonUrl.parseLiteralLength(
            text, 0, text.length(), null));

        V expected = factory.getString("");
        assertTrue(factory.isValid(ValueType.STRING, expected));

        if (text.length() == 0) {
            assertThrows(
                SyntaxException.class,
                () -> parse(text));

            assertThrows(
                SyntaxException.class,
                () -> JsonUrl.parseLiteral(text));

            assertThrows(
                SyntaxException.class,
                () -> JsonUrl.parseLiteral(text, 0, text.length()));

            assertThrows(
                SyntaxException.class,
                () -> JsonUrl.parseLiteral(text, factory));

            assertThrows(
                SyntaxException.class,
                () -> JsonUrl.parseLiteral(text, 0, text.length(), factory, false));

        } else {
            if (factory == JavaValueFactory.PRIMITIVE) {
                assertEquals(expected, JsonUrl.parseLiteral(text));
                assertEquals(expected, JsonUrl.parseLiteral(text, 0, text.length())); 
            }
 
            assertEquals(expected, JsonUrl.parseLiteral(text, factory));
            assertEquals(expected, JsonUrl.parseLiteral(text, 0, text.length(), factory, false));
        }

        assertEquals(expected, JsonUrl.parseLiteral(text, 0, text.length(), factory, true));

        Parser p = new Parser();
        p.setEmptyUnquotedValueAllowed(true);
        assertEquals(expected, p.parse(text, factory));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_STRING)
    @CsvSource({
        //
        // INPUT,OUTPUT
        //
        "\'hello\',",
        "a,",
        "abc,",
        "1a,",
        "1e,",
        "1.,",
        "fals,",
        "hello+world,'hello world'",
        "hello%2Bworld,'hello+world'",
        "y+%3D+mx+%2B+b,y = mx + b",
        "a%3Db%26c%3Dd,a=b&c=d",
        // CHECKSTYLE:OFF
        "hello%F0%9F%8D%95world,'hello\uD83C\uDF55world'",
        // CHECKSTYLE:ON
        "-,",
        "-e,",
        "-e+,'-e '",
        "-e+1,'-e 1'",
        "-.,",
        "1e+,'1e '",
        "1e-,",
        "'1.2.3',",
        "Eddy\'s+in+the+space+time+continuum,Eddy\'s in the space time continuum",
    })
    void testString2(String in, String out) throws IOException {
        String nativeValue = out == null ? in : out;
        S factoryValue = getFactoryString(nativeValue); 
        parseLiteral(ValueType.STRING, in, factoryValue);

        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(in, txt, in);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory), in);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_STRING)
    @ValueSource(strings = {
            "hello", // NOPMD
            "Bob's House",
            "Hello, World!",
            "World: Hello!",
            "Hello (world).",
    })
    void testStringsWithStructCharsQuotedAndEncoded(String s)
            throws UnsupportedEncodingException {

        S value = getFactoryString(s);

        //
        // encoded
        //
        parseLiteral(ValueType.STRING, urlEncodeAndEscapeStructChars(s), value);

        //
        // quoted
        //
        String in = urlEncode(s).replace("'", "%27");
        parseLiteral(ValueType.STRING, "'" + in + "'", value);
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_STRING)
    @ValueSource(strings = {
            "hello",
            "t", "tr", "tru", "True", "tRue", "trUe", "truE",
            "f", "fa", "fal", "fals", "False", "fAlse", "faLse", "falSe", "falsE",
            "n", "nu", "nul", "Null", "nUll", "nuLl", "nulL",
    })
    void testString(String s) throws IOException {
        parseLiteral(ValueType.STRING, s, getFactoryString(s));
    }

    private S getFactoryString(String s) {
        return factory.getString(s);
    }
    
    private static final String urlEncode(String s)
            throws UnsupportedEncodingException {

        return URLEncoder
            //.encode(s, StandardCharsets.UTF_8)
            .encode(s, "UTF-8");
    }

    private static final String urlEncodeAndEscapeStructChars(String s)
            throws UnsupportedEncodingException {

        return urlEncode(s)
            .replace("(", "%28")
            .replace(")", "%29")
            .replace(",", "%2C")
            .replace(":", "%3A");
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
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
        assertThrows(
            SyntaxException.class,
            () -> parse(text));
        
        if (text.length() > 2) {
            assertThrows(
                SyntaxException.class,
                () -> parseImpliedFactoryObject(text));
            
            assertThrows(
                SyntaxException.class,
                () -> parseImpliedFactoryObject(text, 0, text.length()));
    
            assertThrows(
                SyntaxException.class,
                () -> parseImpliedFactoryArray(text));
            
            assertThrows(
                SyntaxException.class,
                () -> parseImpliedFactoryArray(text, 0, text.length()));
        }
    }

    private void assertAllowEmptyUnquotedKey(String text, J actual) {
        assertFalse(getBoolean("", actual), text);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
        "(:false)"
    })
    void testAllowEmptyUnquotedKey(String text) {
        Parser p = new Parser();

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, this.factory),
            text);
        
        p.setEmptyUnquotedKeyAllowed(true);
        
        assertTrue(p.isEmptyUnquotedKeyAllowed(), text);

        assertAllowEmptyUnquotedKey(text, p.parseObject(text, factory));

        assertAllowEmptyUnquotedKey(
            text,
            p.parseObject(
                makeImplied(text),
                factory,
                factory.newObjectBuilder()));
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
    @Tag(TAG_PARSE)
    @ValueSource(strings = {
        "(1,,3)",
    })
    void testAllowEmptyUnquotedArrayValue(String text) {
        Parser p = new Parser();

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, this.factory),
            text);
        
        p.setEmptyUnquotedValueAllowed(true);

        assertTrue(p.isEmptyUnquotedValueAllowed(), text);

        assertAllowEmptyUnquotedArrayValue(text, p.parseArray(text, factory));

        assertAllowEmptyUnquotedArrayValue(
            text,
            p.parseArray(
                makeImplied(text),
                factory,
                factory.newArrayBuilder()));
    }
    
    private void assertAllowEmptyUnquotedObjectValue(String text, J actual) {
        assertEquals(
            factory.getString(""),
            getString("a", actual),
            text);

        assertTrue(getBoolean("b", actual), text);
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @ValueSource(strings = {
        "(a:,b:true)"
    })
    void testAllowEmptyUnquotedObjectValue(String text) {
        Parser p = new Parser();

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, this.factory),
            text);
        
        p.setEmptyUnquotedValueAllowed(true);

        assertTrue(p.isEmptyUnquotedValueAllowed(), text);

        assertAllowEmptyUnquotedObjectValue(
            text,
            p.parseObject(text, this.factory));
        
        assertAllowEmptyUnquotedObjectValue(
            text,
            p.parseObject(makeImplied(text), factory, factory.newObjectBuilder()));
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
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @ValueSource(strings = {
        "(a:b&c:d)",
        "(a:b&c=d)",
        "(a=b&c:d)",
        "(a=b&c=d&e=false&f=1.234&g=null&h=()&i=(1,2,3)&j=(a:d,b:a))",
    })
    void testObjectWfu(String text) {
        Parser p = new Parser();

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, factory),
            text);
        
        assertThrows(
            SyntaxException.class,
            () -> p.parseObject(
                makeImplied(text),
                factory,
                factory.newObjectBuilder()),
            text);

        p.setFormUrlEncodedAllowed(true);

        assertTrue(p.isFormUrlEncodedAllowed(), text);

        assertObjectWfu(text, p.parseObject(text, factory));

        assertObjectWfu(
            text,
            p.parseObject(
                makeImplied(text),
                factory,
                factory.newObjectBuilder()));
    }

    private void assertArrayWfu(String text, A actual) {
        assertEquals(
            factory.getString("a"),
            getString(0, actual),
            text);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
    @ValueSource(strings = {
        "(a&b)",
        "(a&b&c&d)",
        "(a&false&1.234&null&()&(1,2,3)&(a:d,b:a))",
    })
    void testArrayWfu(String text) {
        Parser p = new Parser();

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, factory),
            text);
        
        assertThrows(
            SyntaxException.class,
            () -> p.parseArray(
                makeImplied(text),
                factory,
                factory.newArrayBuilder()),
            text);

        p.setFormUrlEncodedAllowed(true);

        assertTrue(p.isFormUrlEncodedAllowed(), text);

        assertArrayWfu(text, p.parseArray(text, factory));

        assertArrayWfu(
            text,
            p.parseArray(
                makeImplied(text),
                factory,
                factory.newArrayBuilder()));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    @CsvSource({
        "((1)), STRING",
        "(), STRING",
        "'(1,2)', STRING",
    })
    void testExceptionInvalidType(String text, String type) {
        Parser p = newFactoryParser();
        ValueType valueType = ValueType.valueOf(type);
        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, valueType, factory));
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
        "(1,",
        "(1,a,",
        "((a)1",
        "(a:b,",
        "'hello",
    })
    void testExceptionSyntax2(String s) {
        //
        // like testExceptionSyntax() but does not test implied values
        // (which would otherwise succeed).
        //
        assertThrows(
            SyntaxException.class,
            () -> parse(s));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
        "%2G",
        "%E2",
        "%FA%80%80%80%80", //0x200000
    })
    void testExceptionUtf8(String text) {
        Parser p = newFactoryParser();
        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, factory));
    }

    @Test
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    void testExceptionMaxParseChars() {
        Parser p = newFactoryParser();
        p.setMaxParseChars(2);
        
        assertThrows(
            LimitException.class,
            () -> p.parse(TRUE, factory));
    }

    @Test
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    void testExceptionMaxParseDepth() {
        Parser p = newFactoryParser();
        p.setMaxParseDepth(2);

        assertThrows(
            LimitException.class,
            () -> p.parse("(((1)))", factory));
    }

    @Test
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    void testExceptionMaxParseValues() {
        Parser p = newFactoryParser();
        p.setMaxParseValues(2);

        assertThrows(
            LimitException.class,
            () -> p.parse("(1,2,3)", factory));
    }

    @Test
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    void testParseException() {
        ParseException pe = null;
        try {
            newFactoryParser().parse("");

        } catch (ParseException e) {
            pe = e;
        }

        final String message = "empty string";
        assertEquals(0, pe.getPosition(), message);
        assertEquals("text missing", pe.getMessage(), message);
        assertTrue(pe.toString().endsWith("text missing at 0"), message);

        assertTrue(new ParseException("a").toString().endsWith("a"), message);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
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
    void testArray(String text) {
        A obj1 = newFactoryParser().parseArray(text);

        A obj2 = newFactoryParser().parseArray(
            PREFIX1 + text + SUFFIX1,
            PREFIX1.length(),
            text.length());

        for (ValueType t : ValueType.values()) {
            assertEquals(
                t == ValueType.ARRAY,
                factory.isValid(t, obj1),
                text);
            
            assertEquals(
                t == ValueType.ARRAY,
                factory.isValid(t, obj2),
                text);
        }

        parse(EnumSet.of(ValueType.ARRAY), text, null, false);
        
        //
        // direct call to Parser.parseArray()
        //
        // using assertTrue() because the json.org parser doesn't implement
        // JSONObject.equals()
        //
        A objA = new Parser().parseArray(text, factory);
        A objB = new Parser().parseArray(text, 0, text.length(), factory);
        assertTrue(isEqual(objA, objB), text);

        V objC = new Parser().parse(text, factory);
        assertTrue(isEqual(objA, objC), text);
        
        V objD = new Parser().parse(text, 0, text.length(), factory);
        assertTrue(isEqual(objA, objD), text);
        
        V objE = new Parser().parse(text, ValueType.ARRAY, factory);
        assertTrue(isEqual(objA, objE), text);

        V objF = new Parser().parse(text, 0, text.length(), ValueType.ARRAY, factory);
        assertTrue(isEqual(objA, objF), text);

        V objG = newFactoryParser().parseArray(makeImplied(text), factory.newArrayBuilder());
        assertTrue(isEqual(objA, objG), text);
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @ValueSource(strings = {
        "(a:b)",
        "(a:(b))",
        "(a:(b,c))",
    })
    void testObject(String text) {
        J obj1 = parseFactoryObject(text);

        J obj2 = newFactoryParser().parseObject(
            PREFIX1 + text + SUFFIX1,
            PREFIX1.length(),
            text.length());

        for (ValueType t : ValueType.values()) {
            assertEquals(
                t == ValueType.OBJECT,
                factory.isValid(t, obj1),
                text);

            assertEquals(
                t == ValueType.OBJECT,
                factory.isValid(t, obj2),
                text);
        }

        parse(EnumSet.of(ValueType.OBJECT), text, null, false);

        //
        // direct call to Parser.parseObject()
        //
        // using assertTrue() because org.json.JSONObject doesn't implement
        // an equals method
        //
        J objA = new Parser().parseObject(text, factory);
        J objB = new Parser().parseObject(text, 0, text.length(), factory);
        assertTrue(isEqual(objA, objB), text);

        V objC = new Parser().parse(text, factory);
        assertTrue(isEqual(objA, objC), text);

        V objD = new Parser().parse(text, 0, text.length(), factory);
        assertTrue(isEqual(objA, objD), text);

        V objE = new Parser().parse(text, ValueType.OBJECT, factory);
        assertTrue(isEqual(objA, objE), text);

        V objF = new Parser().parse(text, 0, text.length(), ValueType.OBJECT, factory);
        assertTrue(isEqual(objA, objF), text);

        V objG = newFactoryParser().parseObject(makeImplied(text), factory.newObjectBuilder());
        assertTrue(isEqual(objA, objG), text);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
            "hello",
            "1", "2.3",
            "true", "false",
            "null",
    })
    void testExceptionComposite(String text) {
        assertThrows(
            SyntaxException.class,
            () -> parseFactoryObject(text));
        assertThrows(
            SyntaxException.class,
            () -> parseFactoryArray(text));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
            "(1)",
            "(a(",
            "(a:",
            "(a:b(",
    })
    void testExceptionObject(String text) {
        assertThrows(
            SyntaxException.class,
            () -> parseFactoryObject(text));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
            "(a:b)",
            "(a(",
    })
    void testExceptionArray(String text) {
        assertThrows(
            SyntaxException.class,
            () -> parseFactoryArray(text));
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_EXCEPTION)
    @ValueSource(strings = {
        "(a&(b&c))",
        "(a&(b,c&d))",
        "(a=(b=c))",
        "(a=(b:c&d:e))",
        "(a=(b:c,d=e))",
    })
    void testExceptionWfu(String text) {
        Parser p = new Parser();

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, factory));


        p.setFormUrlEncodedAllowed(true);

        assertThrows(
            SyntaxException.class,
            () -> p.parse(text, factory));

        if (text.indexOf('=') != -1) {
            assertThrows(
                SyntaxException.class,
                () -> p.parseObject(
                    makeImplied(text),
                    factory,
                    factory.newObjectBuilder()));

        } else {
            assertThrows(
                SyntaxException.class,
                () -> p.parseArray(
                    makeImplied(text),
                    factory,
                    factory.newArrayBuilder()));
        }
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
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
    void testUtf8(String text, String expected) {
        assertEquals(expected, getStringValue(parse(text)), expected);
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_BIG)
    @Tag(TAG_NUMBER)
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
    void testMathContext(String s) {
        if (factory instanceof BigMathProvider) {
            MathContext mc = ((BigMathProvider)factory).getMathContext();

            if (mc == null) {
                mc = MathContext.UNLIMITED;
            }

            BigDecimal expect = new BigDecimal(s, mc);
            V parseResult = parse(s);

            assertEquals(expect, getNumberValue(parseResult), s);
        }
    }
    
    void testBigInteger(String s, MathContext mc, BigIntegerOverflow over) {
        if (over == BigIntegerOverflow.INFINITY
                && !this.isBigIntegerOverflowInfinityOK()) {
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
        BigInteger bi = new BigInteger(s);
        
        if (bi.bitLength() > 128 && over == null) {
            assertThrows(LimitException.class,
                () -> newParser(factory).parse(s));

        } else {
            V parseResult = newParser(factory).parse(s);
            Number n = getNumberValue(parseResult);

            if (bi.bitLength() > 128) {
                switch (over) {
                case BIG_DECIMAL:
                    assertEquals(
                        new BigDecimal(s, MathContext.DECIMAL128), n, s);
                    break;
                case DOUBLE:
                    assertEquals(
                        Double.parseDouble(s),
                        n.doubleValue(),
                        s);
                    break;
                case INFINITY:
                    if (bi.signum() == -1) {
                        assertEquals(BigMathProvider.NEGATIVE_INFINITY, n, s);
                    } else {
                        assertEquals(BigMathProvider.POSITIVE_INFINITY,n, s);
                    }
                    break;
                }
            } else if (bi.bitLength() > 64) {
                if (n instanceof BigInteger) {
                    assertEquals(bi, (BigInteger)n, s);
                } else {
                    assertEquals(new BigDecimal(bi), n, s);
                }
            } else {
                //
                // some gymnastics to handle JSR-374
                //
                assertEquals(
                    Long.parseLong(s),
                    n.longValue(),
                    s);
            }
        }
    }
    
    void testBigInteger(String s, MathContext mc) {
        testBigInteger(s, mc, null);

        for (BigIntegerOverflow over : BigIntegerOverflow.values()) {
            testBigInteger(s, mc, over);
        } 
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_BIG)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "1",
        "-1",
        Long.MAX_VALUE + "", // NOPMD - need a string literal
        Long.MIN_VALUE + "", // NOPMD - need a string literal
        Long.MAX_VALUE + "0",
        Long.MIN_VALUE + "0",
        '-' + BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG,
        BigMathProvider.BIG_INTEGER128_BOUNDARY_POS,
        BigMathProvider.BIG_INTEGER128_BOUNDARY_POS + '0',
        '-' + BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG + '0',
    })
    void testBigInteger(String s) {
        if (!(factory instanceof BigMathProvider)) {
            return;
        }

        testBigInteger(s, null);
        testBigInteger(s, MathContext.DECIMAL128);
    }

    // CHECKSTYLE:OFF
    protected abstract boolean getBoolean(String key, J value);
    protected abstract S getString(String key, J value);
    protected abstract S getString(int index, A value);
    protected abstract boolean getNull(String key, J value);
    protected abstract boolean getEmptyComposite(String key, J value);
    protected abstract boolean getEmptyComposite(int index, A value);
    protected abstract A getArray(String key, J value);
    protected abstract A getArray(int index, A value);
    protected abstract J getObject(String key, J value);
    protected abstract J getObject(int index, A value);
    protected abstract M getNumber(int index, A value);
    protected abstract M getNumber(String key, J value);
    protected abstract Number getNumberValue(V value);
    protected abstract String getStringValue(V value);
    protected abstract ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> newBigMathFactory(
        MathContext mc,
        String boundNeg,
        String boundPos,
        BigIntegerOverflow over);
    
    protected boolean isBigIntegerOverflowInfinityOK() {
        return true;
    }
    
    protected boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }
    // CHECKSTYLE:ON

    private void assertJsonUrlText1(String text, J actual) {
        assertTrue(getBoolean("true", actual), text);
        assertFalse(getBoolean("false", actual), text);
        assertTrue(getNull("null", actual), text);
        assertTrue(getEmptyComposite("empty", actual), text);
        assertTrue(getBoolean("1e+1", actual), text);

        assertEquals(
                factory.getNumber(new NumberBuilder("0")),
                getNumber(0, getArray("single", actual)),
                text);
        
        assertEquals(
                factory.getNumber(new NumberBuilder("-1")),
                getNumber(0, getArray("many", actual)),
                text);
        
        assertEquals(
                factory.getNumber(new NumberBuilder("2.0")),
                getNumber(1, getArray("many", actual)),
                text);
        
        assertEquals(
                factory.getNumber(new NumberBuilder("3e1")),
                getNumber(2, getArray("many", actual)),
                text);
        
        assertEquals(
                factory.getNumber(new NumberBuilder("4e-2")),
                getNumber(3, getArray("many", actual)),
                text);
        
        assertEquals(
                factory.getNumber(new NumberBuilder("5e+0")),
                getNumber(4, getArray("many", actual)),
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
            getString("hello", actual),
            text);
    }

    @Test
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    void testJsonUrlText1()  {
        final String text = "(true:true,false:false,null:null,empty:()"
            + ",1e+1:true"
            + ",single:(0),nested:((1)),nested2:((1,2))"
            + ",many:(-1,2.0,3e1,4e-2,5e+0),'hello':'world')";

        assertJsonUrlText1(text, parseFactoryObject(text));
        assertJsonUrlText1(text, parseImpliedFactoryObject(text));
        assertJsonUrlText1(text, parseImpliedFactoryObject(text, 0, text.length()));
    }

    private void assertJsonUrlText2(String text, A actual) {
        assertEquals(
            factory.getNumber(new NumberBuilder("1")),
            getNumber(0, actual),
            text);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
    @ValueSource(strings = {"(1)"})
    void testJsonUrlText2(String text) {
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
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
    @ValueSource(strings = {"(1,(2))"})
    void testJsonUrlText3(String text) {
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
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
    @ValueSource(strings = {"(1,(a:2),3)"})
    void testJsonUrlText4(String text) {
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
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @ValueSource(strings = {"(age:64,name:(first:Fred))"})
    void testJsonUrlText5(String text) {
        assertJsonUrlText5(text, parseFactoryObject(text));
        assertJsonUrlText5(text, parseImpliedFactoryObject(text));
        assertJsonUrlText5(text, parseImpliedFactoryObject(text, 0, text.length()));
    }
    
    private void assertJsonUrlText6(String text, A actual) {
        assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, getArray(0, getArray(0, getArray(0, actual)))),
                text);
        
    }
    
    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @ValueSource(strings = {"((((1))))"})
    @DisplayName("testJsonUrlText6: Deeply nested array")
    void testJsonUrlText6(String text) {
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
    @Tag(TAG_PARSE)
    @Tag(TAG_ARRAY)
    @ValueSource(strings = {"(1,())"})
    void testJsonUrlText7(String text) {
        assertJsonUrlText7(text, parseFactoryArray(text));
        assertJsonUrlText7(text, parseImpliedFactoryArray(text));
        assertJsonUrlText7(text, parseImpliedFactoryArray(text, 0, text.length()));
    }

    private void assertJsonUrlText8(String text, J actual) {
        assertTrue(getEmptyComposite("a", actual), text);
    }

    @ParameterizedTest
    @Tag(TAG_PARSE)
    @Tag(TAG_OBJECT)
    @ValueSource(strings = {"(a:())"})
    void testJsonUrlText8(String text) {
        assertJsonUrlText8(text, parseFactoryObject(text));
        assertJsonUrlText8(text, parseImpliedFactoryObject(text));
        assertJsonUrlText8(text, parseImpliedFactoryObject(text, 0, text.length()));
    }
}
