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

package org.jsonurl.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * Unit test for {@link org.jsonurl.stream.JsonUrlCharSequence
 * JsonUrlCharSequence}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-11-01
 */
class JsonUrlCharSequenceTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "abcd\r\nabcd" 
    })
    void testText(String text) throws IOException {
        CharSequence ncs = text;

        JsonUrlCharSequence jcs = new JsonUrlCharSequence(text);
        assertEquals(ncs.length(), jcs.length(), text);
        assertEquals(ncs.charAt(0), jcs.charAt(0), text);
        assertEquals(ncs.subSequence(1, 2), jcs.subSequence(1, 2), text);
        assertEquals(ncs.toString(), jcs.toString(), text);
        assertEquals("<input>:0", CharIterator.toStringWithOffset(jcs), text);
        assertEquals("<input>:1:0", CharIterator.toStringWithLine(jcs), text);

        int pos = 0;

        for (;;) {
            int chr = jcs.nextChar();

            if (chr == CharIterator.EOF) {
                break;
            }

            if (ncs.charAt(pos) == '\r') { // NOPMD - AvoidLiteralsInIfCondition
                pos++;
            }

            assertEquals(ncs.charAt(pos), chr, "text");
            pos++;
        }
    }
}
