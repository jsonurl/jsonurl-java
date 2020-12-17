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

import static org.jsonurl.factory.AbstractParseTest.makeImplied;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.text.JsonStringBuilder;
import org.jsonurl.text.JsonTextBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit test for writing JSON&#x2192;URL text.
 *
 * <p>These unit tests are designed to test JsonTextBuilder and friends
 * without the need for a working JSON parser. 
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
public abstract class AbstractWriteTest<
    V,
    C extends V,
    ABT, // NOPMD - GenericsNaming 
    A extends C,
    JBT, // NOPMD - GenericsNaming
    J extends C> {

    /**
     * Empty string.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Empty string test name.
     */
    private static final String EMPTY_STRING_TEST = "empty string";

    /**
     * Empty quoted string.
     */
    private static final String EMPTY_QSTRING = "''";

    /**
     * null literal.
     */
    private static final String NULL = "null";

    /**
     * Test ValueFactory.
     */
    final ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> factory = getFactory();

    /**
     * Write the given value.
     * @param dest destination
     * @param value value to write
     */
    protected abstract <R> boolean write(
        JsonTextBuilder<R> dest,
        V value) throws IOException;

    /**
     * Get a new JsonTextBuilder.
     */
    protected abstract JsonStringBuilder newJsonStringBuilder(
        Set<JsonUrlOption> options,
        CompositeType impliedType);

    /**
     * Get a new JsonTextBuilder.
     */
    protected JsonStringBuilder newJsonStringBuilder(
            Set<JsonUrlOption> options) {
        return newJsonStringBuilder(options, (CompositeType)null);
    }

    /**
     * Get a new JsonTextBuilder.
     */
    private JsonStringBuilder newJsonStringBuilder() {
        return newJsonStringBuilder(null, (CompositeType)null);
    }

    /**
     * Get a new JsonTextBuilder.
     */
    private JsonStringBuilder newJsonStringBuilder(
            JsonUrlOption first,
            JsonUrlOption... rest) {
        return newJsonStringBuilder(JsonUrlOption.newSet(first, rest), null);
    }

    /**
     * Get an implementation-defined JSON&#x2192;URL ValueFactory.
     * @return a valid ValueFactory instance
     */
    protected abstract ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> getFactory();

    @Test
    void testEmptyStringImpliedStringLiterals() throws IOException {
        Set<JsonUrlOption> options = JsonUrlOption.newSet(
            JsonUrlOption.IMPLIED_STRING_LITERALS);

        assertThrows(
            IOException.class,
            () -> newJsonStringBuilder(options).add(EMPTY_STRING).build());

        assertThrows(
            IOException.class,
            () -> newJsonStringBuilder(options).addKey(EMPTY_STRING).build());
    }

    @Test
    void testEmptyStringUnquotedKey() throws IOException {
        assertEquals(
            EMPTY_STRING,
            newJsonStringBuilder(JsonUrlOption.EMPTY_UNQUOTED_KEY)
                .addKey(EMPTY_STRING)
                .build(),
            EMPTY_STRING_TEST);
    }

    @Test
    void testEmptyStringUnquotedValue() throws IOException {
        assertEquals(
            EMPTY_STRING,
            newJsonStringBuilder(JsonUrlOption.EMPTY_UNQUOTED_VALUE)
                .add(EMPTY_STRING)
                .build(),
            EMPTY_STRING_TEST);
    }

    @Test
    void testEmptyString() throws IOException {
        assertEquals(
            EMPTY_QSTRING,
            newJsonStringBuilder().add(EMPTY_STRING).build(),
            EMPTY_STRING_TEST);

        assertEquals(
            EMPTY_QSTRING,
            newJsonStringBuilder().addKey(EMPTY_STRING).build(),
            EMPTY_STRING_TEST);
    }

    @Test
    void testCoerceNullToEmptyString() throws IOException {
        V factoryValue = newParser().parse(NULL);

        assertEquals(
            EMPTY_QSTRING,
            newJsonStringBuilder(JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING)
                .add(factoryValue)
                .build(),
            NULL);

        JsonStringBuilder jup = newJsonStringBuilder(
            JsonUrlOption.EMPTY_UNQUOTED_VALUE,
            JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING);

        assertFalse(write(jup, factoryValue), NULL);
        assertEquals(
            EMPTY_STRING,
            jup.build(),
            NULL);
    }

    @Test
    void testSkipNullValue() throws IOException {
        assertEquals(
            EMPTY_STRING,
            newJsonStringBuilder(JsonUrlOption.SKIP_NULLS)
                .add(newParser().parse(NULL))
                .build(),
            NULL);

        assertFalse(write(newJsonStringBuilder(JsonUrlOption.SKIP_NULLS),
                newParser().parse(NULL)),
            NULL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(a,b)",
        "(a,null,b)",
        "(null,a,null,b,null)",
    })
    void testArraySkipNullValue(String text) throws IOException {
        final String expected = "(a,b)";
        
        assertEquals(
            expected,
            newJsonStringBuilder(JsonUrlOption.SKIP_NULLS)
                .add(newParser().parse(text))
                .build(),
            text);

        JsonStringBuilder jup = newJsonStringBuilder(
            JsonUrlOption.SKIP_NULLS);

        assertTrue(write(jup, newParser().parseArray(text)), text);
        assertEquals(expected, jup.build(), text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(a:b,c:d)",
        "(a:b,c:d,e:null)",
        "(e:null,a:b,c:d)",
        "(e:null,a:b,f:null,c:d,g:null)",
    })
    void testObjectSkipNullValue(String text) throws IOException {
        final String expected = "(a:b,c:d)";

        assertEquals(
            expected,
            newJsonStringBuilder(JsonUrlOption.SKIP_NULLS)
                .add(newParser().parseObject(text))
                .build(),
            text);

        JsonStringBuilder jup = newJsonStringBuilder(
            JsonUrlOption.SKIP_NULLS);

        assertTrue(write(jup, newParser().parseObject(text)), text);
        assertEquals(expected, jup.build(), text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "()",
        "(value)",
        "(value&value)",
        "(value&value2&value3)",
        "((value))",
        "((name:value))",
        "((name:value)&(name:value))",
        "(true)",
        "(false)",
        "(true&false&null)",
        "(()&false&null&true&1.0&(false,hello,world,(),2.0))",
    })
    void testArray(String text) throws IOException {
        //
        // !implied && wwwFormUrlEncoded
        //
        A value = newParser(JsonUrlOption.WFU_COMPOSITE).parseArray(text);
        assertTest(text, value, null, true, false);

        //
        // implied && wwwFormUrlEncoded
        //
        value = newParser(JsonUrlOption.WFU_COMPOSITE).parseArray(
            makeImplied(text),
            factory.newArrayBuilder());

        assertTest(
            makeImplied(text),
            value,
            CompositeType.ARRAY,
            true,
            false);

        final String wfuText = replaceWfuChars(text);

        //
        // !implied && !wwwFormUrlEncoded
        //
        value = newParser().parseArray(wfuText);
        assertTest(wfuText, value, null, false, false);

        //
        // implied && !wwwFormUrlEncoded
        //
        value = newParser().parseArray(
            makeImplied(wfuText),
            factory.newArrayBuilder());

        assertTest(
            makeImplied(wfuText),
            value,
            CompositeType.ARRAY,
            false,
            false);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "()",
        "(name=value)",
        "(name=value&name2=value)",
        "(name=())",
        "(name=(name:value))",
        "(name=(value,value))",
        "(name=true&name2=false)",
        "(1.234=false)",
    })
    void testObject(String text) throws IOException {
        //
        // !implied && wwwFormUrlEncoded
        //
        J value = newParser(JsonUrlOption.WFU_COMPOSITE).parseObject(text);
        assertTest(text, value, null, true, false);

        //
        // implied && wwwFormUrlEncoded
        //
        value = newParser(JsonUrlOption.WFU_COMPOSITE).parseObject(
            makeImplied(text),
            factory.newObjectBuilder());

        assertTest(
            makeImplied(text),
            value,
            CompositeType.OBJECT,
            true,
            false);

        final String wfuText = replaceWfuChars(text);

        //
        // !implied && !wwwFormUrlEncoded
        //
        value = newParser().parseObject(wfuText);
        assertTest(wfuText, value, null, false, false);

        //
        // implied && !wwwFormUrlEncoded
        //
        value = newParser().parseObject(
            makeImplied(wfuText),
            factory.newObjectBuilder());

        assertTest(
            makeImplied(wfuText),
            value,
            CompositeType.OBJECT,
            false,
            false);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1.234",
        "false",
        "null",
        "true",
    })
    void testLiteral(String expected) throws IOException {
        V value = newParser().parse(expected);
        assertTest(expected, value, null, false, false);
    }
    
    void assertTest(
            String expected,
            V value,
            CompositeType impliedType,
            boolean wwwFormUrlEncoded,
            boolean skipNullValues) throws IOException {

        Set<JsonUrlOption> options = JsonUrlOption.newSet();
        if (wwwFormUrlEncoded) {
            options.add(JsonUrlOption.WFU_COMPOSITE);
        }
        if (skipNullValues) {
            options.add(JsonUrlOption.SKIP_NULLS);
        }

        String actual = newJsonStringBuilder(options, impliedType)
            .add(value)
            .build();

        assertEquals(expected, actual, expected);
    }
    
    private static String replaceWfuChars(String text) {
        return text.replace('&', ',').replace('=', ':');
    }

    private Parser<V,C,ABT,A,JBT,J,?,?,?,?> newParser(
            JsonUrlOption option) {
        return new ValueFactoryParser<>(factory, null, EnumSet.of(option));
    }

    private Parser<V,C,ABT,A,JBT,J,?,?,?,?> newParser() {
        return new ValueFactoryParser<>(factory, null, null);
    }
}
