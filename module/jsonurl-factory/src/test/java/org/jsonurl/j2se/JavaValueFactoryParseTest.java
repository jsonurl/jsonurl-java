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
 * Unit tests for Parser + JavaValueFactory.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class JavaValueFactoryParseTest  {
    
    /**
     * Unit test using JavaValueFactory.PRIMITIVE.
     */
    @Nested
    class PrimitiveParseTest extends AbstractJavaValueFactoryParseTest {

        /**
         * Create a new PrimitiveParseTest.
         */
        public PrimitiveParseTest() {
            super(JavaValueFactory.PRIMITIVE);
        }
    }
    
    /**
     * Unit test using JavaValueFactory.DOUBLE.
     */
    @Nested
    class DoubleParseTest extends AbstractJavaValueFactoryParseTest {

        /**
         * Create a new DoubleParseTest.
         */
        public DoubleParseTest() {
            super(JavaValueFactory.DOUBLE);
        }
    }
    
    /**
     * Unit test using JavaValueFactory.BigMathFactory.
     */
    @Nested
    class BigMathParseTest extends AbstractJavaValueFactoryParseTest {
        /**
         * Create a new BigMathParseTest.
         */
        BigMathParseTest() {
            super(new JavaValueFactory.BigMathFactory(null, null, null, null));
        }
    }
    

    /**
     * Unit test using JavaValueFactory.BIGMATH32.
     */
    @Nested
    class BigMathParseTest32 extends AbstractJavaValueFactoryParseTest {
        /**
         * Create a new BigMathParseTest32.
         */
        BigMathParseTest32() {
            super(JavaValueFactory.BIGMATH32);
        }
    }

    /**
     * Unit test using JavaValueFactory.BIGMATH64.
     */
    @Nested
    class BigMathParseTest64 extends AbstractJavaValueFactoryParseTest {
        /**
         * Create a new BigMathParseTest64.
         */
        BigMathParseTest64() {
            super(JavaValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JavaValueFactory.BIGMATH128.
     */
    @Nested
    class BigMathParseTest128 extends AbstractJavaValueFactoryParseTest {
        /**
         * Create a new BigMathParseTest128.
         */
        BigMathParseTest128() {
            super(JavaValueFactory.BIGMATH128);
        }
    }
}
