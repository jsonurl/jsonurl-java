/*
 * Copyright 2019-2021 David MacCormack
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

package org.jsonurl.deploytest;

import static org.jsonurl.deploytest.DeployTestConstants.HELLO;
import static org.jsonurl.deploytest.DeployTestConstants.TEST_CASE;
import static org.jsonurl.deploytest.DeployTestConstants.WORLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jsonurl.stream.JsonUrlCharSequence;
import org.jsonurl.stream.JsonUrlEvent;
import org.jsonurl.stream.JsonUrlIterator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * <a href="https://jsonurl.org/">JSON&#x2192;URL</a> deployment
 * test for the jsonurl-core artifact.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2021-05-01
 */
class CoreTest {

    @ParameterizedTest
    @ValueSource(strings = {
        TEST_CASE, 
    })
    void test(String text) {
        JsonUrlIterator iter = JsonUrlIterator.newInstance(
            new JsonUrlCharSequence(text), null, null);

        assertNotNull(iter, text);

        assertEquals(
            JsonUrlEvent.START_ARRAY,
            iter.next(),
            JsonUrlEvent.START_ARRAY.toString());

        assertEquals(
            JsonUrlEvent.VALUE_STRING,
            iter.next(),
            JsonUrlEvent.VALUE_STRING.toString());

        assertEquals(HELLO, iter.getString(), HELLO);

        assertEquals(
            JsonUrlEvent.VALUE_STRING,
            iter.next(),
            JsonUrlEvent.VALUE_STRING.toString());

        assertEquals(WORLD, iter.getString(), WORLD);

        assertEquals(
            JsonUrlEvent.END_ARRAY,
            iter.next(),
            JsonUrlEvent.END_ARRAY.toString());

        assertEquals(
            JsonUrlEvent.END_STREAM,
            iter.next(),
            JsonUrlEvent.END_STREAM.toString());
    }
}
