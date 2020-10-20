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

import static org.jsonurl.AbstractParseTest.makeImplied;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
     * Test ValueFactory.
     */
    final ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> factory = getFactory();
    
    /**
     * Write the given value.
     * @param dest destination
     * @param value value to write
     */
    protected abstract <I,R> boolean write(
            JsonTextBuilder<I,R> dest,
            boolean skipNullValues,
            V value) throws IOException;

    /**
     * Get an implementation-defined JSON&#x2192;URL ValueFactory.
     * @return a valid ValueFactory instance
     */
    protected abstract ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> getFactory();

    @ParameterizedTest
    @ValueSource(strings = {
        "null",
    })
    void testSkipNullValue(String text) throws IOException {
        assertFalse(write(
                new JsonUrlStringBuilder(),
                true,
                new Parser().parse(text, factory)),
                text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(a,b)",
        "(a,null,b)",
        "(null,a,null,b,null)",
    })
    void testArraySkipNullValue(String text) throws IOException {
        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();

        assertTrue(write(
                dest,
                true,
                new Parser().parseArray(text, factory)));

        assertEquals("(a,b)", dest.build(), text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(a:b,c:d)",
        "(a:b,c:d,e:null)",
        "(e:null,a:b,c:d)",
        "(e:null,a:b,f:null,c:d,g:null)",
    })
    void testObjectSkipNullValue(String text) throws IOException {
        JsonUrlStringBuilder dest = new JsonUrlStringBuilder();

        assertTrue(write(
                dest,
                true,
                new Parser().parseObject(text, factory)));

        assertEquals("(a:b,c:d)", dest.build(), text);
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
        Parser par = new Parser();

        par.setFormUrlEncoded(true);
        assertTrue(par.isFormUrlEncoded(), text);

        //
        // !implied && wwwFormUrlEncoded
        //
        A value = par.parseArray(text, factory);
        assertTest(text, value, false, true, false);

        //
        // implied && wwwFormUrlEncoded
        //
        value = par.parseArray(
            makeImplied(text),
            factory,
            factory.newArrayBuilder());

        assertTest(makeImplied(text), value, true, true, false);

        final String wfuText = replaceWfuChars(text);
        par.setFormUrlEncoded(false);
        assertFalse(par.isFormUrlEncoded(), wfuText);

        //
        // !implied && !wwwFormUrlEncoded
        //
        value = par.parseArray(wfuText, factory);
        assertTest(wfuText, value, false, false, false);

        //
        // implied && !wwwFormUrlEncoded
        //
        value = par.parseArray(
            makeImplied(wfuText),
            factory,
            factory.newArrayBuilder());

        assertTest(makeImplied(wfuText), value, true, false, false);
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
        Parser par = new Parser();

        par.setFormUrlEncoded(true);
        assertTrue(par.isFormUrlEncoded(), text);

        //
        // !implied && wwwFormUrlEncoded
        //
        J value = par.parseObject(text, factory);
        assertTest(text, value, false, true, false);

        //
        // implied && wwwFormUrlEncoded
        //
        value = par.parseObject(
            makeImplied(text),
            factory,
            factory.newObjectBuilder());

        assertTest(makeImplied(text), value, true, true, false);

        final String wfuText = replaceWfuChars(text);
        par.setFormUrlEncoded(false);
        assertFalse(par.isFormUrlEncoded(), wfuText);

        //
        // !implied && !wwwFormUrlEncoded
        //
        value = par.parseObject(wfuText, factory);
        assertTest(wfuText, value, false, false, false);

        //
        // implied && !wwwFormUrlEncoded
        //
        value = par.parseObject(
            makeImplied(wfuText),
            factory,
            factory.newObjectBuilder());

        assertTest(makeImplied(wfuText), value, true, false, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1.234",
        "false",
        "null",
        "true",
    })
    void testLiteral(String expected) throws IOException {
        V value = new ValueFactoryParser<>(getFactory()).parse(expected);
        assertTest(expected, value, false, false, false);
    }
    
    void assertTest(
            String expected,
            V value,
            boolean implied,
            boolean wwwFormUrlEncoded,
            boolean skipNullValues) throws IOException {

        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        jsb.setImpliedComposite(implied);
        jsb.setFormUrlEncoded(wwwFormUrlEncoded);

        write(jsb, skipNullValues, value);

        String actual = jsb.build();
        assertEquals(expected, actual, expected);
    }
    
    private static String replaceWfuChars(String text) {
        return text.replace('&', ',').replace('=', ':');
    }
}
