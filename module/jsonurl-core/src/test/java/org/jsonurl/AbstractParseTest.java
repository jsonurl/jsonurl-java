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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.EnumSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JsonUrl parser test implementation.
 *
 * <p>Any implementation of the {@link ValueFactory} interface may use this
 * class to implement parser unit tests.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
@SuppressWarnings("PMD")
public abstract class AbstractParseTest<
        V,
        C extends V,
        ABT,
        A extends C,
        JBT,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> {
    
    protected ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory;
    
    private static final String PREFIX1 = "prefix 1";
    private static final String PREFIX2 = "number 2 prefix";
    private static final String SUFFIX1 = "suffix 1";
    private static final String SUFFIX2 = "number two suffix";

    private Parser<V,C,ABT,A,JBT,J,B,M,N,S> newParser() {
        return new Parser<>(factory);
    }

    public AbstractParseTest(ValueFactory<V,C,ABT,A,JBT,J,B,M,N,S> factory) {
        this.factory = factory;    
    }
    
    private void parse(
            ValueType allow,
            Object out,
            Object out2,
            String in) {
        parse(EnumSet.of(allow), out, out2, in, true);
    }

    private void parse(
            EnumSet<ValueType> allow,
            Object out,
            Object out2,
            String in,
            boolean isLiteral) {

        StringBuilder sb = new StringBuilder(1 << 10);

        //
        // Parser.parse()
        //
        sb.append(PREFIX1).append(in).append(SUFFIX1);

        V parseResult = newParser().parse(
            sb.toString(), PREFIX1.length(), in.length());

        Object parseCompare = out.equals(parseResult) ? out : out2;
        assertEquals(parseCompare, parseResult);

        if (allow != null) {
            assertTrue(factory.isValid(allow, parseResult));
        }

        //
        // JsonUrl.parseLiteral()
        //
        if (isLiteral) {
            sb.setLength(0);
            sb.append(PREFIX2).append(in).append(SUFFIX2);
    
            V litResult = JsonUrl.parseLiteral(
                sb.toString(), PREFIX2.length(), in.length(), factory);
    
            Object litCompare = out.equals(litResult) ? out : out2;
            assertEquals(litCompare, litResult);
    
            if (allow != null) {
                assertTrue(factory.isValid(allow, litResult));
            }
    
            assertEquals(in.length(), JsonUrl.parseLiteralLength(in));
        }
    }

    @SuppressWarnings("unchecked")
    private A parseArray(String jsonUrlText) {
        Parser<V, C, ABT, A, JBT, J, B, M, N, S> p = new Parser<>(factory);
        return (A)p.parse(jsonUrlText);
    }

    @SuppressWarnings("unchecked")
    private J parseObject(String jsonUrlText) {
        Parser<V, C, ABT, A, JBT, J, B, M, N, S> p = new Parser<>(factory);
        return (J)p.parse(jsonUrlText);
    }
    
    private void assertLong(
            String expect,
            String actual,
            M factoryValue) {

        assertEquals(expect, actual);
        assertEquals(factoryValue, JsonUrl.parseLiteral(actual, factory));
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("long")
    @Tag("number")
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
    void testLong(String in, String out) throws ParseException, IOException {
        M factoryValue = parseLong(out);
        parse(ValueType.NUMBER, factoryValue, null, in);

        Long nativeValue = Long.valueOf(out);
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertLong(out, txt, factoryValue);
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("long")
    @Tag("number")
    @ValueSource(strings = {
        "0", "-1", "123456", "-123456", "12345678905432132",
    })
    void testLong(String s) throws ParseException, IOException {
        M factoryValue = parseLong(s);
        parse(ValueType.NUMBER, factoryValue, null, s);

        Long nativeValue = Double.valueOf(s).longValue();
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertLong(s, txt, factoryValue);
    }

    M parseLong(String s) {
        return factory.getNumber(new NumberBuilder(s));
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("double")
    @Tag("number")
    @ValueSource(strings = {
        "0.1", "-1.1",
        "1e-2", "-2e-1",
        "1.9e2", "-2.8e1",
        "156.9e-2", "-276.8e-1",
        "156.911e+2", "-276.833e+4",
    })
    void testDouble(String s) throws ParseException, IOException {
        M factoryValue = parseDouble(s);
        parse(ValueType.NUMBER, factoryValue, null, s);

        Number nativeValue = Double.valueOf(s);

        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(String.valueOf(nativeValue), txt);

        //
        // I'd like to do the following but differences in the way the
        // string is printed (WRT scientific notation) causes it to fail. 
        //
        // assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
        
        //
        // so I'm just checking that parseLiteral() eventually calls
        // factory.getNumber() like I expect
        //
        assertEquals(factoryValue, JsonUrl.parseLiteral(s, factory));
        assertEquals(nativeValue, new NumberBuilder(s).toDouble());
    }

    M parseDouble(String s) {
        return factory.getNumber(new NumberBuilder(s));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "t", "tr", "tru",
    })
    void testTrueFalseNull(String s) {
        //assertNull(factory.getTrueFalseNull(s));
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("boolean")
    @ValueSource(strings = { "true", "false" })
    void testBoolean(String s) throws ParseException, IOException {
        B factoryValue = parseBoolean(s);
        parse(ValueType.BOOLEAN, factoryValue, null, s);

        Boolean nativeValue = Boolean.valueOf(s);
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(String.valueOf(nativeValue), txt);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
    }

    B parseBoolean(String s) {
        return factory.getBoolean(Boolean.parseBoolean(s));
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("null")
    @ValueSource(strings = { "null" })
    void testNull(String s) throws ParseException, IOException {
        N factoryValue = factory.getNull();
        factory.isValid(ValueType.NULL, factoryValue);
        parse(ValueType.NULL, factoryValue, null, s);

        String txt = new JsonUrlStringBuilder().addNull().build();
        assertEquals(String.valueOf(s), txt);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));

        assertEquals(factoryValue, newParser().parse(s, ValueType.NULL));
        assertEquals(
            factoryValue,
            newParser().parse(s, 0, s.length(), ValueType.NULL));
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("empty")
    @ValueSource(strings = { "()" })
    void testEmpty(String s) throws ParseException, IOException {
        V factoryValue = factory.getEmptyComposite();
        factory.isValid(ValueType.OBJECT, factoryValue);
        factory.isValid(ValueType.ARRAY, factoryValue);

        parse(
            EnumSet.of(ValueType.OBJECT, ValueType.ARRAY),
            factoryValue, null, s, false);

        String txt = new JsonUrlStringBuilder().addEmptyComposite().build();
        assertEquals(String.valueOf(s), txt);
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("string")
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
    })
    void testString2(String in, String out) throws ParseException, IOException {
        String nativeValue = out == null ? in : out;
        S factoryValue = parseString(nativeValue); 
        parse(ValueType.STRING, factoryValue, null, in);

        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(in, txt);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
    }

    S parseString(String s) {
        return factory.getString(s);
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("string")
    @Tag("autostring")
    @ValueSource(strings = {
            "hello",
            "Bob's House",
            "Hello, World!",
            "World: Hello!",
            "Hello (world).",
            "t", "tr", "tru", "True", "tRue", "trUe", "truE",
            "f", "fa", "fal", "fals", "False", "fAlse", "faLse", "falSe", "falsE",
            "n", "nu", "nul", "Null", "nUll", "nuLl", "nulL",
    })
    void testString(String s) throws UnsupportedEncodingException {
        String in = URLEncoder
                //.encode(s, StandardCharsets.UTF_8)
                .encode(s, "UTF-8")
                .replace("'", "%27");

        S value = parseString(s);

        parse(ValueType.STRING, value, null, "'" + in + "'");

        in = URLEncoder
                //.encode(s, StandardCharsets.UTF_8)
                .encode(s, "UTF-8")
                .replace("(", "%28")
                .replace(")", "%29")
                .replace(",", "%2C")
                .replace(":", "%3A");

        parse(ValueType.STRING, value, null, in);
    }

    @Test
    void testMisc() {
        assertTrue(factory.isEmpty(newParser().parse("()")));

        Parser<?,?,?,?,?,?,?,?,?,?> p = newParser();

        p.setMaxParseChars(13);
        p.setMaxParseDepth(13);
        p.setMaxParseValues(13);

        assertEquals(13, p.getMaxParseChars());
        assertEquals(13, p.getMaxParseDepth());
        assertEquals(13, p.getMaxParseValues());
    }

    @ParameterizedTest
    @Tag("exception")
    @ValueSource(strings = {
        "",
        "%2G",
        "%2",
        "1,",
        "(1,",
        "()1",
        "(1)1",
        "('1'1)",
        "(1,'2'1)",
        "(1,2,3)a",
        "(a:b)a",
        "(a:'b'a)",
        "(a:b,'c'd)",
    })
    void testSyntaxException(String s) throws ParseException {
        assertThrows(
            SyntaxException.class,
            () -> newParser().parse(s));
    }
    
    @ParameterizedTest
    @Tag("parse")
    @Tag("exception")
    @CsvSource({
        "((1)), STRING",
        "(), STRING",
        "'(1,2)', STRING",
    })
    void testSyntaxException2(String text, String type) throws ParseException {
        assertThrows(
            SyntaxException.class,
            () -> newParser().parse(text, ValueType.valueOf(type)));
    }


    @Test
    @Tag("parse")
    @Tag("exception")
    void testException() throws ParseException {
        assertThrows(
            LimitException.class,
            () -> {
                Parser<?,?,?,?,?,?,?,?,?,?> p = newParser();
                p.setMaxParseChars(2);
                p.parse("true");
            });

        assertThrows(
            LimitException.class,
            () -> {
                Parser<?,?,?,?,?,?,?,?,?,?> p = newParser();
                p.setMaxParseDepth(2);
                p.parse("(((1)))");
            });
        
        assertThrows(
            LimitException.class,
            () -> {
                Parser<?,?,?,?,?,?,?,?,?,?> p = newParser();
                p.setMaxParseValues(2);
                p.parse("(1,2,3)");
            });

        ParseException pe = null;
        try {
            newParser().parse("");

        } catch (ParseException e) {
            pe = e;
        }

        assertEquals(0, pe.getPosition());
        assertEquals("text missing", pe.getMessage());
        assertTrue(pe.toString().endsWith("text missing at 0"));

        assertTrue(new ParseException("a").toString().endsWith("a"));
        assertTrue(new SyntaxException("a").toString().endsWith("a"));
    }

    @ParameterizedTest
    @Tag("parse")
    @ValueSource(strings = {
        "(1)",
    })
    void testArray(String text) throws ParseException {
        A obj1 = newParser().parseArray(text);

        A obj2 = newParser().parseArray(
            PREFIX1 + text + SUFFIX1,
            PREFIX1.length(),
            text.length());

        for (ValueType t : ValueType.values()) {
            assertEquals(
                t == ValueType.ARRAY,
                factory.isValid(t, obj1));
            
            assertEquals(
                t == ValueType.ARRAY,
                factory.isValid(t, obj2));
        }

    }
    
    @ParameterizedTest
    @Tag("parse")
    @ValueSource(strings = {
        "(a:b)",
    })
    void testObject(String text) throws ParseException {
        J obj1 = newParser().parseObject(text);

        J obj2 = newParser().parseObject(
            PREFIX1 + text + SUFFIX1,
            PREFIX1.length(),
            text.length());

        for (ValueType t : ValueType.values()) {
            assertEquals(
                t == ValueType.OBJECT,
                factory.isValid(t, obj1));

            assertEquals(
                t == ValueType.OBJECT,
                factory.isValid(t, obj2));
        }
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("exception")
    @ValueSource(strings = {
            "hello",
            "1", "2.3",
            "true", "false",
            "null",
            "(1)",
    })
    void testObjectException(String text) throws ParseException {
        assertThrows(
            SyntaxException.class,
            () -> newParser().parseObject(text));
    }
    
    @ParameterizedTest
    @Tag("parse")
    @Tag("exception")
    @ValueSource(strings = {
            "hello",
            "1", "2.3",
            "true", "false",
            "null",
            "(a:b)",
    })
    void testArrayException(String text) throws ParseException {
        assertThrows(
            SyntaxException.class,
            () -> newParser().parseArray(text));
    }
    
    protected abstract boolean getBoolean(String key, J value);
    
    protected abstract String getString(String key, J value);

    protected abstract boolean getNull(String key, J value);
    
    protected abstract boolean getEmptyComposite(String key, J value);
    
    protected abstract A getArray(String key, J value);

    protected abstract A getArray(int index, A value);

    protected abstract J getObject(String key, J value);

    protected abstract J getObject(int index, A value);
    
    protected abstract M getNumber(int index, A value);
    
    protected abstract M getNumber(String key, J value);

    @Test
    void testJsonUrlText1() throws ParseException {
        J parseResult = (J)parseObject(
                "(true:true,false:false,null:null,empty:(),"
                + "single:(0),nested:((1)),many:(-1,2.0,3e1,4e-2,5e+0))");

        assertTrue(getBoolean("true", parseResult));
        assertFalse(getBoolean("false", parseResult));
        assertTrue(getNull("null", parseResult));
        assertTrue(getEmptyComposite("empty", parseResult));

        assertEquals(
                factory.getNumber(new NumberBuilder("0")),
                getNumber(0, getArray("single", parseResult)));
        
        assertEquals(
                factory.getNumber(new NumberBuilder("-1")),
                getNumber(0, getArray("many", parseResult)));
        
        assertEquals(
                factory.getNumber(new NumberBuilder("2.0")),
                getNumber(1, getArray("many", parseResult)));
        
        assertEquals(
                factory.getNumber(new NumberBuilder("3e1")),
                getNumber(2, getArray("many", parseResult)));
        
        assertEquals(
                factory.getNumber(new NumberBuilder("4e-2")),
                getNumber(3, getArray("many", parseResult)));
        
        assertEquals(
                factory.getNumber(new NumberBuilder("5e+0")),
                getNumber(4, getArray("many", parseResult)));
        
        assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, getArray(0, getArray("nested", parseResult))));
    }

    @Test
    void testJsonUrlText2() throws ParseException {
        A parseResult = parseArray("(1)");
        
        assertEquals(
                factory.getNumber(new NumberBuilder("1")),
                getNumber(0, parseResult));
    }
    
    @Test
    void testJsonUrlText3() throws ParseException {
        A parseResult = parseArray("(1,(2))");

        assertEquals(
                factory.getNumber(new NumberBuilder("2")),
                getNumber(0, getArray(1,parseResult)));
    }

    @Test
    void testJsonUrlText4() throws ParseException {
        A parseResult = parseArray("(1,(a:2),3)");

        assertEquals(
                factory.getNumber(new NumberBuilder("2")),
                getNumber("a", getObject(1, parseResult)));
    }

    @Test
    void testJsonUrlText5() throws ParseException {
        J parseResult = parseObject("(age:64,name:(first:Fred))");

        assertEquals(
                "Fred",
                getString("first", getObject("name", parseResult)));
    }
}
