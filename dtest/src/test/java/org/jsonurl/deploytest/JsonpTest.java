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

import javax.json.JsonArray;
import org.jsonurl.jsonp.JsonUrlParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * <a href="https://jsonurl.org/">JSON&#x2192;URL</a> deployment
 * test for the jsonurl-jsr374 artifact.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2021-05-01
 */
class JsonpTest {

    @ParameterizedTest
    @ValueSource(strings = {
        TEST_CASE, 
    })
    void test(String text) {
        JsonArray data = new JsonUrlParser().parseArray(text);
        assertNotNull(data);
        assertEquals(2, data.size(), text);
        assertEquals(HELLO, data.getString(0), text);
        assertEquals(WORLD, data.getString(1), text);
    }

}
