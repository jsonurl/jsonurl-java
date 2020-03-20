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


// CHECKSTYLE:OFF
import static org.junit.jupiter.api.Assertions.*;
// CHECKSTYLE:ON

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit test for NumberBuilder.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class NumberBuilderTest {
    /** Prefix used to test non-zero based start index. */
    private static final String PREFIX  = "prefix ";
    
    /** Suffix used to test non-zero based stop index. */
    private static final String SUFFIX = " suffix";

    /** Longs smaller than this will be parsed doubles. */
    private static final long MIN_LONG = -999999999999999999L;

    /** Longs bigger than this will be parsed doubles. */
    private static final long MAX_LONG = 999999999999999999L;
    
    /** tag annotation. */
    private static final String TAG_LONG = "long";

    /** tag annotation. */
    private static final String TAG_NUMBER = "number";

    /** tag annotation. */
    private static final String TAG_DOUBLE = "double";

    private NumberBuilder newNumberBuilder(String s) {
        return new NumberBuilder(
                PREFIX + s + SUFFIX,
                PREFIX.length(),
                PREFIX.length() + s.length());
    }

    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @ValueSource(longs = {
        0, -0,
        1, -1,
        123456, -123456,
        12345678905432132L,
        MIN_LONG,
        MIN_LONG + 1,
        MAX_LONG,
        MAX_LONG - 1,
    })
    void testLong(long g) {
        assertEquals(
                Long.valueOf(g),
                newNumberBuilder(String.valueOf(g)).build(true));
    }

    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "0", "-0",
        "1", "-1",
        "123456", "-123456",
        "12345678905432132",
    })
    void testLong(String s) {
        assertEquals(Long.valueOf(s), newNumberBuilder(s).build(true)); 
    }
    
    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @CsvSource({
        //
        // INPUT,OUTPUT
        //
        "'1e2',100",
        "'-2e1',-20",
        "'-3e0',-3",
        "'1e+2',100",
        "'-2e+1',-20",
        "'4e+15',4000000000000000",
    })
    void testLong(String in, long out) {
        assertEquals(Long.valueOf(out), newNumberBuilder(in).build(true));
    }
    
    @ParameterizedTest
    @Tag(TAG_DOUBLE)
    @Tag(TAG_NUMBER)
    @ValueSource(strings = {
        "0.0", "-0.0",
        "1.1", "-1.1",
        "123456.2", "-123456.2",
        "12345678905432132.3",
        "-12345678905432132.3",
        
        "0.0e1", "-0.0e2", "-0.0e+2", "0.0e-1",
        "1e-1", "0.2e1", "0.2e+2", "0.3e-3",
        "123.1e1", "-123.2e2", "-123.2e+2", "-321.4e-4",
        
        "123456789012345678901"
    })
    void testDouble(String s) {
        assertEquals(
                Double.valueOf(s),
                newNumberBuilder(s).build(true)); 
    }
    
    @ParameterizedTest
    @Tag(TAG_LONG)
    @Tag(TAG_NUMBER)
    @ValueSource(longs = {
        Long.MIN_VALUE,
        Long.MAX_VALUE,
        MAX_LONG + 1,
        MIN_LONG - 1,
    })
    void testDouble(long g) {
        assertEquals(
                Double.valueOf(g),
                newNumberBuilder(String.valueOf(g)).build(true)); 
    }
}
