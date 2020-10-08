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
public abstract class AbstractJsonTextBuilderTest<
    V,
    C extends V,
    ABT, 
    A extends C,
    JBT,
    J extends C> {

    /**
     * Test ValueFactory.
     */
    final ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> factory = getFactory();
    
    /**
     * Write the given value.
     * @param out destination
     * @param value value to write
     */
    protected abstract void write(JsonTextBuilder<?,?> out, V value)
        throws IOException;

    /**
     * Get an implementation-defined JSON&#x2192;URL ValueFactory.
     * @return a valid ValueFactory instance
     */
    protected abstract ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> getFactory();
    
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
        Parser p = new Parser();

        p.setAllowFormUrlEncoded(true);
        assertEquals(true, p.getAllowFormUrlEncoded(), text);

        //
        // !implied && wwwFormUrlEncoded
        //
        A value = p.parseArray(text, factory);
        assertTest(text, value, false, true);

        //
        // implied && wwwFormUrlEncoded
        //
        value = p.parseArray(
            makeImplied(text),
            factory,
            factory.newArrayBuilder());

        assertTest(makeImplied(text), value, true, true);

        text = replaceWfuChars(text);
        p.setAllowFormUrlEncoded(false);
        assertEquals(false, p.getAllowFormUrlEncoded(), text);

        //
        // !implied && !wwwFormUrlEncoded
        //
        value = p.parseArray(text, factory);
        assertTest(text, value, false, false);

        //
        // implied && !wwwFormUrlEncoded
        //
        value = p.parseArray(
            makeImplied(text),
            factory,
            factory.newArrayBuilder());

        assertTest(makeImplied(text), value, true, false);
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
        Parser p = new Parser();

        p.setAllowFormUrlEncoded(true);
        assertEquals(true, p.getAllowFormUrlEncoded(), text);

        //
        // !implied && wwwFormUrlEncoded
        //
        J value = p.parseObject(text, factory);
        assertTest(text, value, false, true);

        //
        // implied && wwwFormUrlEncoded
        //
        value = p.parseObject(
            makeImplied(text),
            factory,
            factory.newObjectBuilder());

        assertTest(makeImplied(text), value, true, true);

        text = replaceWfuChars(text);
        p.setAllowFormUrlEncoded(false);
        assertEquals(false, p.getAllowFormUrlEncoded(), text);

        //
        // !implied && !wwwFormUrlEncoded
        //
        value = p.parseObject(text, factory);
        assertTest(text, value, false, false);

        //
        // implied && !wwwFormUrlEncoded
        //
        value = p.parseObject(
            makeImplied(text),
            factory,
            factory.newObjectBuilder());

        assertTest(makeImplied(text), value, true, false);
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
        assertTest(expected, value, false, false);
    }
    
    void assertTest(
            String expected,
            V value,
            boolean implied,
            boolean wwwFormUrlEncoded) throws IOException {

        JsonUrlStringBuilder sb = new JsonUrlStringBuilder();
        sb.setImplied(implied);
        sb.setFormUrlEncoded(wwwFormUrlEncoded);

        write(sb, value);

        String actual = sb.build();
        assertEquals(expected, actual, expected);
    }
    
    private static final String replaceWfuChars(String s) {
        return s.replace('&', ',').replace('=', ':');
    }
}
