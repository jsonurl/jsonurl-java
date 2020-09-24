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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test LimitException.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class SyntaxExceptionTest {

    @ParameterizedTest
    @Tag("exception")
    @EnumSource(SyntaxException.Message.class)
    void testSyntaxException(SyntaxException.Message msg) {
        final String text = "exception text";

        assertEquals(
            text,
            new SyntaxException(msg, text).getMessage(),
            text);

        assertEquals(
            42,
            new SyntaxException(msg, text, 42).getPosition(),
            text);

        assertEquals(
            msg,
            new SyntaxException(msg).getMessageValue(),
            text);        
    }
}
