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

    private void parse(Object out, Object out2, String in) {
        Parser<V, C, ABT, A, JBT, J, B, M, N, S> p = new Parser<>(factory);
        StringBuilder sb = new StringBuilder(1 << 10);

        sb.append(PREFIX1).append(in).append(SUFFIX1);
        V parseResult = p.parse(sb.toString(), PREFIX1.length(), in.length());

        if (out.equals(parseResult) || out2 == null) {
            assertEquals(out, parseResult);
        } else {
            assertEquals(out2, parseResult);
        }

        sb.setLength(0);
        sb.append(PREFIX2).append(in).append(SUFFIX2);

        V litResult = JsonUrl.parseLiteral(sb.toString(), PREFIX2.length(), in.length(), factory);

        if (out.equals(litResult)) {
            assertEquals(out, litResult);
        } else {
            assertEquals(out2, litResult);
        }
        assertEquals(in.length(), JsonUrl.parseLiteralLength(in));
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
        parse(factoryValue, null, in);

        Long nativeValue = Long.valueOf(out);
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(out, txt);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
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
        parse(factoryValue, null, s);

        Long nativeValue = Double.valueOf(s).longValue();
        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(s, txt);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
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
        parse(factoryValue, null, s);

        Number nativeValue = Double.valueOf(s);

        String txt = new JsonUrlStringBuilder().add(nativeValue).build();
        assertEquals(String.valueOf(nativeValue), txt);

        //
        // I'd like to do the following but differences in the way the
        // string is printed wrt scientific notation causes it to fail. 
        //
        // assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
        
        //
        // so I'm just checking the parseLiteral() eventually calls
        // factory.getNumber() like I expect
        //
        assertEquals(factoryValue, JsonUrl.parseLiteral(s, factory));
    }

    M parseDouble(String s) {
        return factory.getNumber(new NumberBuilder(s));
    }

    @ParameterizedTest
    @Tag("parse")
    @Tag("boolean")
    @ValueSource(strings = { "true", "false" })
    void testBoolean(String s) throws ParseException, IOException {
        B factoryValue = parseBoolean(s);
        parse(factoryValue, null, s);

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
        parse(factoryValue, null, s);

        String txt = new JsonUrlStringBuilder().addNull().build();
        assertEquals(String.valueOf("null"), txt);
        assertEquals(factoryValue, JsonUrl.parseLiteral(txt, factory));
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
        "'1.2.3',",
    })
    void testString(String in, String out) throws ParseException, IOException {
        String nativeValue = out == null ? in : out;
        S factoryValue = parseString(nativeValue); 
        parse(factoryValue, null, in);

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
            "Hello (world)."
    })
    void testMoreStrings(String s) throws UnsupportedEncodingException {
        String in = URLEncoder
                //.encode(s, StandardCharsets.UTF_8)
                .encode(s, "UTF-8")
                .replace("'", "%27");

        S value = parseString(s);

        parse(value, null, "'" + in + "'");

        in = URLEncoder
                //.encode(s, StandardCharsets.UTF_8)
                .encode(s, "UTF-8")
                .replace("(", "%28")
                .replace(")", "%29")
                .replace(",", "%2C")
                .replace(":", "%3A");

        parse(value, null, in);
    }

    @Test
    @Tag("parse")
    @Tag("static")
    void testStatics() throws ParseException {
        assertTrue(factory.isEmpty(newParser().parse("()")));
    }

    @Test
    @Tag("parse")
    @Tag("exception")
    void testExceptions() throws ParseException {
        assertThrows(
            SyntaxException.class,
            () -> newParser().parse(""));

        assertThrows(
            SyntaxException.class,
            () -> newParser().parse("%2G"));

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


