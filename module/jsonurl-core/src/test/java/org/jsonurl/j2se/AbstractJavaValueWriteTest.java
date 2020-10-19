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
import org.jsonurl.AbstractWriteTest;
import org.jsonurl.JsonTextBuilder;
import org.junit.jupiter.api.Nested;

/**
 * Unit test for writing JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
public abstract class AbstractJavaValueWriteTest
        extends AbstractWriteTest<
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
    static class PrimitiveWriteTest extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.PRIMITIVE;
        }
        
    }

    /**
     * JavaValueFactory.DOUBLE JavaValueWriteTest.
     */
    @Nested
    static class DoubleWriteTest extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.DOUBLE;
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return new JavaValueFactory.BigMathFactory(null, null, null, null);
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest32 extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.BIGMATH32;
        }
    }

    /**
     * JavaValueFactory.BIGMATH64 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest64 extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.BIGMATH64;
        }
    }

    /**
     * JavaValueFactory.BIGMATH128 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest128 extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.BIGMATH128;
        }
    }

    @Override
    protected <I,R> boolean write(
            JsonTextBuilder<I, R> dest,
            boolean skipNullValues,
            Object value) throws IOException {

        return JsonUrlWriter.write(dest, skipNullValues, value);
    }
}
