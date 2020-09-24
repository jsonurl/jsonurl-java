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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jsonurl.AbstractJsonTextBuilderTest;
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.ValueFactory;
import org.junit.jupiter.api.Nested;

/**
 * Unit test for writing JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
public abstract class JavaValueWriteTest extends AbstractJsonTextBuilderTest<
    Object,
    Object,
    List<Object>,
    List<Object>,
    Map<String,Object>,
    Map<String,Object>> {

    /**
     * JavaValueFactory.PRIMITIVE JavaValueWriteTest.
     */
    @Nested
    static class PrimitiveWriteTest extends JavaValueWriteTest {

        @Override
        public ValueFactory<
                Object,
                Object,
                List<Object>,
                List<Object>,
                Map<String,Object>,
                Map<String,Object>, ?, ?, ?, ?> getFactory() {

            return JavaValueFactory.PRIMITIVE;
        }
        
    }

    /**
     * JavaValueFactory.DOUBLE JavaValueWriteTest.
     */
    @Nested
    static class DoubleWriteTest extends JavaValueWriteTest {

        @Override
        public ValueFactory<
            Object,
            Object,
            List<Object>,
            List<Object>,
            Map<String,Object>,
            Map<String,Object>, ?, ?, ?, ?> getFactory() {

            return JavaValueFactory.DOUBLE;
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest extends JavaValueWriteTest {

        @Override
        public ValueFactory<
            Object,
            Object,
            List<Object>,
            List<Object>,
            Map<String,Object>,
            Map<String,Object>, ?, ?, ?, ?> getFactory() {

            return new JavaValueFactory.BigMathFactory(null, null, null, null);
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest32 extends JavaValueWriteTest {

        @Override
        public ValueFactory<
            Object,
            Object,
            List<Object>,
            List<Object>,
            Map<String,Object>,
            Map<String,Object>, ?, ?, ?, ?> getFactory() {

            return JavaValueFactory.BIGMATH32;
        }
    }

    /**
     * JavaValueFactory.BIGMATH64 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest64 extends JavaValueWriteTest {

        @Override
        public ValueFactory<
            Object,
            Object,
            List<Object>,
            List<Object>,
            Map<String,Object>,
            Map<String,Object>, ?, ?, ?, ?> getFactory() {

            return JavaValueFactory.BIGMATH64;
        }
    }

    /**
     * JavaValueFactory.BIGMATH128 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest128 extends JavaValueWriteTest {

        @Override
        public ValueFactory<
            Object,
            Object,
            List<Object>,
            List<Object>,
            Map<String,Object>,
            Map<String,Object>, ?, ?, ?, ?> getFactory() {

            return JavaValueFactory.BIGMATH128;
        }
    }

    @Override
    public void write(JsonTextBuilder<?, ?> out, Object value) throws IOException {
        JsonUrlWriter.write(out, value);
    }
}
