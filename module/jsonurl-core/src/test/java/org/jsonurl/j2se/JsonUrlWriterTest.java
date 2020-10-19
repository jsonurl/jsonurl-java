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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.jsonurl.JsonUrlStringBuilder;
import org.jsonurl.ValueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for write() cases that don't very with the factory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
class JsonUrlWriterTest {

    /**
     * null literal.
     */
    private static final String NULL = "null";
    
    /**
     * Array text case.
     */
    private static final class ArrayTestCase {
        /**
         * Test case input values.
         */
        final Object values;

        /**
         * Test case expected output.
         */
        final String expected;

        /**
         * Test case expected output when skipNullValues is true.
         */
        final String skipNullExpected;

        ArrayTestCase(Object values, String expected) {
            this.values = values;
            this.expected = expected;
            this.skipNullExpected = expected;
        }

        ArrayTestCase(Object values, String expected, String skipNullExpected) {
            this.values = values;
            this.expected = expected;
            this.skipNullExpected = skipNullExpected;
        }

    }

    @ParameterizedTest
    @EnumSource(ValueType.class)
    void testEnum(ValueType expected) throws IOException {
        assertEquals(
            expected.name(),
            new JsonUrlStringBuilder().add(expected).build(),
            expected.name());

        assertEquals(
            expected.name(),
            new JsonUrlStringBuilder().add(expected, true).build(),
            expected.name());

        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (Object)expected);

        assertEquals(
            expected,
            ValueType.valueOf(jsb.build()),
            expected.toString());
    }

    @ParameterizedTest
    @MethodSource
    void testArray(ArrayTestCase test) throws IOException {
        //
        // skipNullValues = false
        //
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, test.values);

        assertEquals(test.expected, jsb.build(), test.expected);

        //
        // skipNullValues = true
        //
        jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, true, test.values);

        assertEquals(
                test.skipNullExpected,
                jsb.build(),
                test.skipNullExpected);
    }
    
    static final Stream<ArrayTestCase> testArray() {
        return Stream.of(
            new ArrayTestCase(
                new String[] {"hello", null, "world"},
                "(hello,null,world)",
                "(hello,world)"),

            new ArrayTestCase(
                new boolean[] {true, false},
                "(true,false)"),

            new ArrayTestCase(
                new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE},
                "(" + Byte.MIN_VALUE + "," + Byte.MAX_VALUE + ")"),

            new ArrayTestCase(
                new char[] {'a', 'b', 'c'},
                "(a,b,c)"),

            new ArrayTestCase(
                new double[] {Double.MIN_VALUE, Double.MAX_VALUE},
                "(" + Double.MIN_VALUE + "," + Double.MAX_VALUE + ")"),

            //
            // JsonTextBuilder doesn't support writing floats explicitly, only
            // doubles. So, I need to compose "expected" using doubles.
            //
            new ArrayTestCase(
                new float[] {Float.MIN_VALUE, Float.MAX_VALUE},
                "(" + (double)Float.MIN_VALUE + ","
                    + (double)Float.MAX_VALUE + ")"),

            new ArrayTestCase(
                new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE},
                "(" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + ")"),

            new ArrayTestCase(
                new long[] {Long.MIN_VALUE, Long.MAX_VALUE},
                "(" + Long.MIN_VALUE + "," + Long.MAX_VALUE + ")"),

            new ArrayTestCase(
                new short[] {Short.MIN_VALUE, Short.MAX_VALUE},
                "(" + Short.MIN_VALUE + "," + Short.MAX_VALUE + ")")
            );
    }

    @Test
    void testNullBooleanArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (boolean[])null);
        assertEquals(NULL, jsb.build(), Boolean.class.toString());
    }

    @Test
    void testNullByteArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (byte[])null);
        assertEquals(NULL, jsb.build(), Byte.class.toString());
    }

    @Test
    void testNullCharArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (char[])null);
        assertEquals(NULL, jsb.build(), Character.class.toString());
    }

    @Test
    void testNullDoubleArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (double[])null);
        assertEquals(NULL, jsb.build(), Double.class.toString());
    }

    @Test
    void testNullFloatArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (float[])null);
        assertEquals(NULL, jsb.build(), Float.class.toString());
    }

    @Test
    void testNullIntArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (int[])null);
        assertEquals(NULL, jsb.build(), Integer.class.toString());
    }

    @Test
    void testNullLongArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (long[])null);
        assertEquals(NULL, jsb.build(), Long.class.toString());
    }

    @Test
    void testNullShortArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (short[])null);
        assertEquals(NULL, jsb.build(), Short.class.toString());
    }

    @Test
    void testNullObjectArray() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (Object[])null);
        assertEquals(NULL, jsb.build(), Object.class.toString());

        assertFalse(
                JsonUrlWriter.write(jsb.clear(), true, (Object[])null),
                Object.class.toString());
    }
    
    @Test
    void testNullIterable() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (Iterable<?>)null);
        assertEquals(NULL, jsb.build(), Iterable.class.toString());

        assertFalse(
                JsonUrlWriter.write(jsb.clear(), true, (Iterable<?>)null),
                Iterable.class.toString());
    }

    @Test
    void testNullMap() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (Map<?,?>)null);
        assertEquals(NULL, jsb.build(), Iterable.class.toString());

        assertFalse(
                JsonUrlWriter.write(jsb.clear(), true, (Map<?,?>)null),
                Iterable.class.toString());
    }

    @Test
    void testNullEnum() throws IOException {
        JsonUrlStringBuilder jsb = new JsonUrlStringBuilder();
        JsonUrlWriter.write(jsb, (Enum<?>)null);
        assertEquals(NULL, jsb.build(), Enum.class.toString());
        
        assertEquals(NULL,
            new JsonUrlStringBuilder().add((Enum<?>)null).build(),
            Enum.class.toString());
    }

    @Test
    void testException() throws IOException {
        assertThrows(
            IOException.class,
            () -> JsonUrlWriter.write(null, new Object()));

        assertThrows(
            IOException.class,
            () -> JsonUrlWriter.write(new JsonUrlStringBuilder(), newNonStringMap()));
    }
    
    private Map<?,?> newNonStringMap() {
        Map<Object, Object> ret = new HashMap<>(); 
        ret.put(new Object(), new Object());
        return ret;
    }
}
