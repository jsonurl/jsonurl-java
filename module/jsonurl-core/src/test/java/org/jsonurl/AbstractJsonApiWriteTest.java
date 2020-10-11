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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * Unit test for writing JSON&#x2192;URL text via JSON text.
 *
 * <p>These unit tests are designed to test against an existing JSON API,
 * such as JSR-374. It requires a valid/functioning JSON parser as it uses it
 * to parse JSON to verify that semantically equivalent JSON&#x2192;URL produces
 * equivalent in-memory classes for the target API.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public abstract class AbstractJsonApiWriteTest<
    V,
    C extends V,
    A extends C,
    J extends C> {

    /**
     * Write the given value.
     * @param dest destination
     * @param value value to write
     */
    public abstract void write(
            JsonTextBuilder<?,?> dest,
            V value) throws IOException;

    /**
     * Create a new implementation-defined Array instance.
     * @param text JSON&#x2192;URL text
     * @return a valid Array instance
     */
    public abstract A newArray(String text);

    /**
     * Create a new implementation-defined Object instance.
     * @param text JSON&#x2192;URL text
     * @return a valid Object instance
     */
    public abstract J newObject(String text);

    /**
     * Create a new implementation-defined JSON&#x2192;URL parser.
     * @return a valid Parser instance
     */
    public abstract ValueFactoryParser<V,C,?,A,?,J,?,?,?,?> newParser();

    /**
     * Create a string for the given implementation-defined value.
     */
    public abstract String valueToString(V value);

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"name\":\"value\"}",
        "{\"name\":\"value\",\"name2\":\"value\"}",
        "{\"name\":{}}",
        "{\"name\":{\"name\":\"value\"}}",
        "{\"name\":[\"value\",\"value\"]}",
        "{\"name\":true,\"name2\":\"value\"}",
    })
    void testInlineObject(String text) throws Exception {
        testInline(text, newObject(text));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "[\"value\"]",
        "[\"value\",\"value\"]",
        "[\"value\",\"value2\",\"value3\"]",
        "[[\"value\"]]",
        "[{\"name\":\"value\"}]",
        "[{\"name\":\"value\"},{\"name\":\"value\"}]",
        "[true]",
        "[false]",
        "[true,false,null]",
    })
    void testInlineArray(String text) throws Exception {
        testInline(text, newArray(text));
    }
    
    void testInline(String text, V obj) throws Exception {
        JsonUrlStringBuilder buf = new JsonUrlStringBuilder();
        write(buf, obj);
        String jsonUriText = buf.build();
        
        ValueFactoryParser<V,C,?,A,?,J,?,?,?,?> vfp = newParser();

        //
        // newParser() is a factory method. The whole point is that I'm
        // asking the implementation to return one to me.
        //
        V obj2 = vfp.parse(jsonUriText);

        String jsonText = valueToString(obj2);
        assertEquals(text, jsonText);
    }
}
