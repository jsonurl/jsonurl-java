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
     * @return a valid Parser instance
     */
    protected abstract ValueFactory<V,C,ABT,A,JBT,J,?,?,?,?> getFactory();
    
    @ParameterizedTest
    @ValueSource(strings = {
        "()",
        "(value)",
        "(value,value)",
        "(value,value2,value3)",
        "((value))",
        "((name:value))",
        "((name:value),(name:value))",
        "(true)",
        "(false)",
        "(true,false,null)",
        "((),false,null,true,1.0,(false,hello,world,(),2.0))",
    })
    void testArray(String expected) throws IOException {
        A value = new ValueFactoryParser<>(getFactory()).parseArray(expected);
        assertTest(expected, value);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "()",
        "(name:value)",
        "(name:value,name2:value)",
        "(name:())",
        "(name:(name:value))",
        "(name:(value,value))",
        "(name:true,name2:value)",
        "(1.234:false)",
    })
    void testObject(String expected) throws IOException {
        J value = new ValueFactoryParser<>(factory).parseObject(expected);
        assertTest(expected, value);

        String impledText = makeImplied(expected);
        
        if (impledText.length() > 0) {
            value = new ValueFactoryParser<>(factory).parseObject(
                impledText, factory.newObjectBuilder());

            assertTest(expected, value);    
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1.234",
    })
    void testLiteral(String expected) throws IOException {
        V value = new ValueFactoryParser<>(getFactory()).parse(expected);
        assertTest(expected, value);
    }
    
    void assertTest(String expected, V value) throws IOException {
        JsonUrlStringBuilder sb = new JsonUrlStringBuilder();
        write(sb, value);

        String actual = sb.build();
        assertEquals(expected, actual, expected);
    }
}
