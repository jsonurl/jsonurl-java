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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * Unit test for writing JSON-&gt;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public abstract class AbstractWriteTest<V,C extends V,A extends C,J extends C> {

    /**
     * Write the given value.
     * @param out destination
     * @param value value to write
     */
    public abstract void write(
            JsonTextBuilder<?,?> out,
            V value) throws Exception; //NOPMD - Exception is correct here

    /**
     * Create a new implementation-defined Array instance.
     * @param s JSON-&gt;URL text
     * @return a valid Array instance
     */
    public abstract A newArray(String s);

    /**
     * Create a new implementation-defined Object instance.
     * @param s JSON-&gt;URL text
     * @return a valid Object instance
     */
    public abstract J newObject(String s);

    /**
     * Create a new implementation-defined JSON-&gt;URL parser.
     * @return a valid Parser instance
     */
    public abstract Parser<V,C,?,A,?,J,?,?,?,?> newParser();

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
    void testInlineObject(String s) throws Exception {
        testInline(s, newObject(s));
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
    void testInlineArray(String s) throws Exception {
        testInline(s, newArray(s));
    }
    
    void testInline(String s, V obj) throws Exception {
        JsonUrlStringBuilder sb = new JsonUrlStringBuilder();
        write(sb, obj);
        String jsonUriText = sb.build();
        
        Parser<V,C,?,A,?,J,?,?,?,?> p = newParser();
        V obj2 = p.parse(jsonUriText);
        String jsonText = valueToString(obj2);
        assertEquals(s, jsonText);
    }
}
