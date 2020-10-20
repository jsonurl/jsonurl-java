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

import org.junit.jupiter.api.Nested;

/**
 * Unit tests for JsonUrlStringBuilder + JavaValueFactory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
class JavaValueWriteTest {

    /**
     * JavaValueFactory.PRIMITIVE JavaValueWriteTest.
     */
    @Nested
    class PrimitiveWriteTest extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.PRIMITIVE;
        }
        
    }

    /**
     * JavaValueFactory.DOUBLE JavaValueWriteTest.
     */
    @Nested
    class DoubleWriteTest extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.DOUBLE;
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    class BigMathWriteTest extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return new JavaValueFactory.BigMathFactory(null, null, null, null);
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    class BigMathWriteTest32 extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.BIGMATH32;
        }
    }

    /**
     * JavaValueFactory.BIGMATH64 JavaValueWriteTest.
     */
    @Nested
    class BigMathWriteTest64 extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.BIGMATH64;
        }
    }

    /**
     * JavaValueFactory.BIGMATH128 JavaValueWriteTest.
     */
    @Nested
    class BigMathWriteTest128 extends AbstractJavaValueWriteTest {

        @Override
        public JavaValueFactory getFactory() {
            return JavaValueFactory.BIGMATH128;
        }
    }
}
