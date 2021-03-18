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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.LimitException;
import org.jsonurl.SyntaxException;
import org.jsonurl.ValueType;
import org.jsonurl.text.NumberBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link org.jsonurl.stream.JsonUrlIterator JsonUrlIterator}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-10-01
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class JsonUrlIteratorTest {

    /** empty string. */
    private static final String EMPTY_STRING = "";

    /** list of common tests. */
    private static final List<EventTest> COMMON_TESTS =
        new ArrayList<>(1 << 10);

    /**
     * An event test case.
     */
    private static class EventTest {
        /**
         * JSON&#x2192;URL text.
         */
        final String text;
        
        /**
         * Array of expected data.
         */
        final Object[] expected;

        /**
         * JsonUrlOptions.
         */
        final Set<JsonUrlOption> options;
        
        /**
         * JsonUrlLimits.
         */
        final JsonUrlLimits limits;

        /**
         * CompositeType.
         */
        final CompositeType impliedType;

        /**
         * expected exception.
         */
        final Class<? extends Exception> exceptionClass;

        EventTest(String text, Object... expected) {
            this(null, null, text, expected);
        }

        EventTest(Set<JsonUrlOption> options,
                String text,
                Object... expected) {
            this(options, null, text, expected);
        }

        EventTest(JsonUrlOption option,
                String text,
                Object... expected) {
            this(EnumSet.of(option), text, expected);
        }

        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        EventTest(Set<JsonUrlOption> options,
                CompositeType impliedType,
                String text,
                Object... expected) {
            this.text = text;
            this.expected = expected;
            this.options = options;
            this.impliedType = impliedType;
            this.exceptionClass = null; // NOPMD
            this.limits = null; // NOPMD
        }

        EventTest(String text, Class<? extends Exception> exception) {
            this(null, null, text, exception);
        }

        EventTest(CompositeType impliedType,
                String text,
                Class<? extends Exception> exception) {
            this(null, impliedType, text, exception);
        }

        EventTest(JsonUrlLimits limits,
                CompositeType impliedType,
                String text,
                Class<? extends Exception> exception) {
            this.text = text;
            this.expected = null; // NOPMD
            this.options = null; // NOPMD
            this.impliedType = impliedType;
            this.exceptionClass = exception;
            this.limits = limits;
        }

        @Override
        public String toString() {
            return text;
        }

        /**
         * Test options for WFU_COMPOSITE.
         */
        public boolean isFormUrlEncoded() {
            return this.options != null
                && options.contains(JsonUrlOption.WFU_COMPOSITE);
        }
    }
    
    private void testSuccess(
            EventTest test,
            JsonUrlIterator jui) throws IOException {

        int dataIndex = 0;

        loop: for (;;) {
            JsonUrlEvent event = jui.next();
            Object expected = test.expected[dataIndex++];

            assertEquals(expected, event, "event");

            switch (event) { // NOPMD
            case END_STREAM:
                break loop;
            case VALUE_NUMBER:
                expected = test.expected[dataIndex];
                assertEquals(
                    expected,
                    NumberBuilder.toString(jui.getNumberText()),
                    "NumberBuilder.toString");
                // fall through
            case VALUE_STRING:
            case KEY_NAME:
                expected = test.expected[dataIndex++];
                assertEquals(
                    expected,
                    jui.getString(),
                    "JsonUrlIterator.getString()");
                break;
            default:
                break;
            }
        }
    }
    
    private void testException(
            EventTest test,
            JsonUrlIterator jui) throws IOException {

        assertThrows(test.exceptionClass, () -> {
            while (jui.next() != JsonUrlEvent.END_STREAM) { // NOPMD
            }
        });
    }
    
    private void testEvents(EventTest test, JsonUrlIterator jui)
            throws IOException {

        //
        // not what I want for the call to testSuccess(), but
        // calling the variants of setType() lets me check each method
        // for NPE.
        //
        jui.setType(test.impliedType);
        jui.setType((ValueType)null);
        jui.setType((Set<ValueType>)null);

        jui.setType(
            ValueType.forCompositeType(test.impliedType),
            test.impliedType);

        if (test.exceptionClass == null) {
            testSuccess(test, jui);
        } else {
            testException(test, jui);
        }

    }
    
    @ParameterizedTest
    @MethodSource
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void testAQF(EventTest test) throws IOException {
        CharIterator text = new JsonUrlCharSequence(
            "testAQF",
            test.text,
            JsonUrlLimits.getMaxParseChars(test.limits));

        Set<JsonUrlOption> options = test.options == null
            ? EnumSet.noneOf(JsonUrlOption.class)
            : EnumSet.copyOf(test.options);

        options.add(JsonUrlOption.AQF);

        JsonUrlIterator jui = JsonUrlIterator.newInstance(
            text,
            test.limits,
            options);

        testEvents(test, jui);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    static Stream<EventTest> testAQF() {
        return Stream.concat(COMMON_TESTS.parallelStream(),
            Arrays.stream(new EventTest[] {
                new EventTest(
                    "1e!+2",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "1e+2",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "1e%2B1",
                    new Object[] {
                        JsonUrlEvent.VALUE_NUMBER,
                        "1e+1",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "1e!-2",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "1e-2",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "('')",
                    new Object[] {
                        JsonUrlEvent.START_ARRAY,
                        JsonUrlEvent.VALUE_STRING,
                        "''",
                        JsonUrlEvent.END_ARRAY,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "('a')",
                    new Object[] {
                        JsonUrlEvent.START_ARRAY,
                        JsonUrlEvent.VALUE_STRING,
                        "'a'",
                        JsonUrlEvent.END_ARRAY,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                    CompositeType.ARRAY,
                    "a&'1'",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "a",
                        JsonUrlEvent.VALUE_STRING,
                        "'1'",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "'",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "'",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "'abcd",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "'abcd",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "!!",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "!e",
                    new Object[] {
                        JsonUrlEvent.VALUE_EMPTY_LITERAL,
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                        "!e!e",
                        SyntaxException.class),

                new EventTest(
                    "!3",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "3",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "!a",
                    SyntaxException.class),
                new EventTest(
                    "a ",
                    SyntaxException.class),
                new EventTest(
                    "a+b",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "a b",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "a%2Bb",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "a+b",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "(!e:a)",
                    new Object[] {
                        JsonUrlEvent.START_OBJECT,
                        JsonUrlEvent.KEY_NAME,
                        "",
                        JsonUrlEvent.VALUE_STRING,
                        "a",
                        JsonUrlEvent.END_OBJECT,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "%21e",
                    new Object[] {
                        JsonUrlEvent.VALUE_EMPTY_LITERAL,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "%21%65",
                    new Object[] {
                        JsonUrlEvent.VALUE_EMPTY_LITERAL,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "%21+",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "+",
                        JsonUrlEvent.END_STREAM}),

            }));
    }

    @ParameterizedTest
    @MethodSource
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void testNotAQF(EventTest test) throws IOException {
        CharIterator text = new JsonUrlCharSequence(
            "testNotAqf",
            test.text,
            JsonUrlLimits.getMaxParseChars(test.limits));

        JsonUrlIterator jui = JsonUrlIterator.newInstance(
            text,
            test.limits,
            test.options);

        testEvents(test, jui);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    static Stream<EventTest> testNotAQF() {
        return Stream.concat(COMMON_TESTS.parallelStream(),
            Arrays.stream(new EventTest[] {
                new EventTest(
                    "1e!+2",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "1e! 2",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "1e%2B1",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "1e+1",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "1e!-2",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "1e!-2",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "('')",
                    new Object[] {
                        JsonUrlEvent.START_ARRAY,
                        JsonUrlEvent.VALUE_STRING,
                        "",
                        JsonUrlEvent.END_ARRAY,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "('a')",
                    new Object[] {
                        JsonUrlEvent.START_ARRAY,
                        JsonUrlEvent.VALUE_STRING,
                        "a",
                        JsonUrlEvent.END_ARRAY,
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                        EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                        CompositeType.ARRAY,
                        "a&'1'",
                        new Object[] {
                            JsonUrlEvent.VALUE_STRING,
                            "a",
                            JsonUrlEvent.VALUE_STRING,
                            "1",
                            JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "'",
                    SyntaxException.class),

                new EventTest(
                    "'abcd",
                    SyntaxException.class),

                new EventTest(
                    "!!",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!!",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "!e",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!e",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "!e!e",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!e!e",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "!3",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!3",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "!a",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!a",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                        "a ",
                        SyntaxException.class),

                new EventTest(
                    "a+b",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "a b",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                        "a%2Bb",
                        new Object[] {
                            JsonUrlEvent.VALUE_STRING,
                            "a+b",
                            JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "(!e:a)",
                    new Object[] {
                        JsonUrlEvent.START_OBJECT,
                        JsonUrlEvent.KEY_NAME,
                        "!e",
                        JsonUrlEvent.VALUE_STRING,
                        "a",
                        JsonUrlEvent.END_OBJECT,
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "%21e",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!e",
                        JsonUrlEvent.END_STREAM}),
                new EventTest(
                    "%21%65",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "!e",
                        JsonUrlEvent.END_STREAM}),

                new EventTest(
                    "%21+",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "! ",
                        JsonUrlEvent.END_STREAM}),

            }));
    }

    //
    // static initializer to populate COMMON_TESTS
    //
    static {
        final EventTest[] tests = {
            new EventTest(
                "true",
                new Object[] {
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "false",
                new Object[] {
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "null",
                new Object[] {
                    JsonUrlEvent.VALUE_NULL,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "1",
                new Object[] {
                    JsonUrlEvent.VALUE_NUMBER,
                    "1",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "true",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "true",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "'true'",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "'true'",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "false",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "false",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "null",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "null",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "1",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "1",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "()",
                new Object[] {
                    JsonUrlEvent.VALUE_EMPTY_COMPOSITE,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "1.234",
                new Object[] {
                    JsonUrlEvent.VALUE_NUMBER,
                    "1.234",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "1e-2",
                new Object[] {
                    JsonUrlEvent.VALUE_NUMBER,
                    "1e-2",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "1e+2",
                new Object[] {
                    JsonUrlEvent.VALUE_NUMBER,
                    "1e+2",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "1e+2",
                new Object[] {
                    JsonUrlEvent.VALUE_NUMBER,
                    "1e+2",
                    JsonUrlEvent.END_STREAM}),

            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "1e%2B1",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "1e+1",
                    JsonUrlEvent.END_STREAM}),

            new EventTest(
                    JsonUrlOption.IMPLIED_STRING_LITERALS,
                    "1e+2",
                    new Object[] {
                        JsonUrlEvent.VALUE_STRING,
                        "1e 2",
                        JsonUrlEvent.END_STREAM}),

            new EventTest(
                JsonUrlOption.EMPTY_UNQUOTED_VALUE,
                EMPTY_STRING,
                new Object[] {
                    JsonUrlEvent.VALUE_EMPTY_LITERAL,
                    JsonUrlEvent.END_STREAM}),

            // Array
            new EventTest(
                "(true)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.IMPLIED_STRING_LITERALS,
                "('a')",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_STRING,
                    "'a'",
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "(truE,falsE,nulL)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_STRING,
                    "truE",
                    JsonUrlEvent.VALUE_STRING,
                    "falsE",
                    JsonUrlEvent.VALUE_STRING,
                    "nulL",
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.SKIP_NULLS,
                "(true,null,false,null,true)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.COERCE_NULL_TO_EMPTY_STRING,
                "(true,null,false,null,true)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.VALUE_EMPTY_LITERAL,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.VALUE_EMPTY_LITERAL,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.WFU_COMPOSITE,
                "(true&false)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "(())",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_EMPTY_COMPOSITE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.WFU_COMPOSITE,
                "(true&())",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.VALUE_EMPTY_COMPOSITE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.WFU_COMPOSITE,
                "(()&false)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_EMPTY_COMPOSITE,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "((false))",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "((false,true))",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "((false),true)",
                new Object[] {
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_FALSE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.VALUE_TRUE,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.ARRAY,
                "",
                new Object[] {JsonUrlEvent.END_STREAM}),

            // Object
            new EventTest(
                JsonUrlOption.EMPTY_UNQUOTED_KEY,
                "(:b)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.WFU_COMPOSITE,
                "(a=b)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                JsonUrlOption.WFU_COMPOSITE,
                "(a=b&c=d)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.KEY_NAME,
                    "c",
                    JsonUrlEvent.VALUE_STRING,
                    "d",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                "a=b",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                "a=b&c=d",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.KEY_NAME,
                    "c",
                    JsonUrlEvent.VALUE_STRING,
                    "d",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.EMPTY_UNQUOTED_VALUE),
                CompositeType.OBJECT,
                "a:",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_EMPTY_LITERAL,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.EMPTY_UNQUOTED_KEY),
                CompositeType.OBJECT,
                ":b",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                "a&c=d",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_MISSING,
                    JsonUrlEvent.KEY_NAME,
                    "c",
                    JsonUrlEvent.VALUE_STRING,
                    "d",
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                "a=b&c",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.KEY_NAME,
                    "c",
                    JsonUrlEvent.VALUE_MISSING,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                EMPTY_STRING,
                new Object[] {JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                "a=(c:d)",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "c",
                    JsonUrlEvent.VALUE_STRING,
                    "d",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.OBJECT,
                "a=(b:(c:d))",
                new Object[] {
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "b",
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "c",
                    JsonUrlEvent.VALUE_STRING,
                    "d",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                EnumSet.of(JsonUrlOption.WFU_COMPOSITE),
                CompositeType.ARRAY,
                "a&(b,(c,(d)))",
                new Object[] {
                    JsonUrlEvent.VALUE_STRING,
                    "a",
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_STRING,
                    "b",
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_STRING,
                    "c",
                    JsonUrlEvent.START_ARRAY,
                    JsonUrlEvent.VALUE_STRING,
                    "d",
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_ARRAY,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "(a:hello%C2%A2world,b:hello%E2%82%ACworld)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "hello\u00A2world", // NOPMD
                    JsonUrlEvent.KEY_NAME,
                    "b",
                    JsonUrlEvent.VALUE_STRING,
                    "hello\u20ACworld", // NOPMD
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "(a:hello%F0%9F%8D%95world,b:hello%20world)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "hello\uD83C\uDF55world", // NOPMD
                    JsonUrlEvent.KEY_NAME,
                    "b",
                    JsonUrlEvent.VALUE_STRING,
                    "hello world", // NOPMD
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),
            new EventTest(
                "(a:%41%42%43%44%45%46%47%48%49%4A%4B%4C%4D%4E%4F%50)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "ABCDEFGHIJKLMNOP",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),

            new EventTest(
                "(a:%41%42%43%44%45%46%47%48%49%4a%4b%4c%4d%4e%4f%50)",
                new Object[] {
                    JsonUrlEvent.START_OBJECT,
                    JsonUrlEvent.KEY_NAME,
                    "a",
                    JsonUrlEvent.VALUE_STRING,
                    "ABCDEFGHIJKLMNOP",
                    JsonUrlEvent.END_OBJECT,
                    JsonUrlEvent.END_STREAM}),

            // Exception
            new EventTest(
                EMPTY_STRING,
                SyntaxException.class),

            new EventTest(
                null,
                CompositeType.ARRAY,
                "a&b",
                SyntaxException.class),

            new EventTest(
                null,
                CompositeType.ARRAY,
                "()a",
                SyntaxException.class),

            new EventTest(
                "()a",
                SyntaxException.class),

            new EventTest(
                null,
                CompositeType.ARRAY,
                "())",
                SyntaxException.class),

            new EventTest(
                null,
                CompositeType.ARRAY,
                "(a,b))",
                SyntaxException.class),

            new EventTest(
                newLimits(100, 100, 2),
                null,
                "(true,false,false)",
                LimitException.class),

            new EventTest(
                newLimits(100, 1, 100),
                null,
                "((true,false,false))",
                LimitException.class),

            new EventTest(
                newLimits(4, 100, 100),
                null,
                "(true)",
                LimitException.class),

            new EventTest(
                "(:world)",
                SyntaxException.class),

            new EventTest(
                "true),a",
                SyntaxException.class),

            new EventTest(
                "(a:b)a",
                SyntaxException.class),

            new EventTest(
                "(a:b(",
                SyntaxException.class),

            new EventTest(
                "(a:b,c(",
                SyntaxException.class),

            new EventTest(
                "(a:b,c,d:e)",
                SyntaxException.class),

            new EventTest(
                CompositeType.OBJECT,
                "a,b:(a:b,c,d:e)",
                SyntaxException.class),

            new EventTest(
                "(a,b,c),d",
                SyntaxException.class),

            new EventTest(
                "(a,b,c",
                SyntaxException.class),

            new EventTest(
                "(a,b,c(",
                SyntaxException.class),

            new EventTest(
                "(a",
                SyntaxException.class),

            new EventTest(
                "((a:b",
                SyntaxException.class),

            new EventTest(
                "((a,",
                SyntaxException.class),

            new EventTest(
                "('a'b",
                SyntaxException.class),

            new EventTest(
                "ab\u0080",
                SyntaxException.class),

            new EventTest(
                "'ab\u0080'",
                SyntaxException.class),

            new EventTest(
                "[1]",
                SyntaxException.class),

            new EventTest(
                "'[1]'",
                SyntaxException.class),

            new EventTest(
                "(a:)",
                SyntaxException.class),

            new EventTest(
                "(a:%F0%9F%8D)",
                SyntaxException.class),

            new EventTest(
                "(a:%FA%80%80%80%80)", //0x200000
                SyntaxException.class),

            new EventTest(
                "(a:%4G)",
                SyntaxException.class),

            new EventTest(
                "(a:\r\r\n)",
                SyntaxException.class),
        };

        StringBuilder buf = new StringBuilder(512);

        for (EventTest test : tests) {
            String altImpliedText = getAltImpliedText(test);

            if (test.impliedType != null && altImpliedText != null) {
                buf.setLength(0);
                buf.append("&&").append(altImpliedText).append("&&");

                COMMON_TESTS.add(new EventTest(// NOPMD
                        test.options,
                        test.impliedType,
                        buf.toString(),
                        test.expected));
            }
        }

        COMMON_TESTS.addAll(Arrays.asList(tests));
    }

    private static String getAltImpliedText(EventTest test) {
        if (test.isFormUrlEncoded()) {
            return test.text.replaceAll("&", ",").replaceAll("=", ":");
        }

        return null;
    }

    private static JsonUrlLimits newLimits(
            long maxParseChars,
            int maxParseDepth,
            int maxParseValues) {

        return JsonUrlLimits.builder()
            .addMaxParseChars(maxParseChars)
            .addMaxParseDepth(maxParseDepth)
            .addMaxParseValues(maxParseValues)
            .build();
    }
}
