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

package org.jsonurl.jsonorg;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Unit test for JsonUrlParser.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-10-01
 */
class JsonUrlParserTest {

    @Test
    void testConstruct() {
        assertSame(
            JsonOrgValueFactory.PRIMITIVE,
            new JsonUrlParser().getFactory(),
            "new JsonUrlParser");

        assertSame(
            JsonOrgValueFactory.DOUBLE,
            new JsonUrlParser(JsonOrgValueFactory.DOUBLE).getFactory(),
            "new JsonUrlParser");
    }
}
