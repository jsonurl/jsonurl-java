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

package org.jsonurl.j2se;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test for JsonUrlWriter.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
class JavaValueFactoryTest {

    @Test
    @DisplayName("JavaValueFactory.toJavaString")
    void testToJavaString() {
        String test = "Hello, World!";

        assertEquals(
            test,
            JavaValueFactory.toJavaString(test, 0, test.length()),
            test);
        
        assertEquals(
            test.substring(1),
            JavaValueFactory.toJavaString(test, 1, test.length()),
            test);
        
        assertEquals(
            test.substring(1),
            JavaValueFactory.toJavaString(
                new StringBuilder(test), 1, test.length()),
            test);
    }
}
