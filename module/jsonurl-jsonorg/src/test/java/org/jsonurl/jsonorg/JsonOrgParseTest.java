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

package org.jsonurl.jsonorg;

import org.junit.jupiter.api.Nested;

/**
 * Unit tests for Parser + JsonOrgValueFactory.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class JsonOrgParseTest {

    /**
     * Unit test using JsonOrgValueFactory.PRIMITIVE.
     */
    @Nested
    class PrimitiveParseTest extends AbstractJsonOrgParseTest {
        /**
         * Create a new PrimitiveParseTest.
         */
        public PrimitiveParseTest() {
            super(JsonOrgValueFactory.PRIMITIVE);
        }
    }
    
    /**
     * Unit test using JsonOrgValueFactory.DOUBLE.
     */
    @Nested
    class DoubleParseTest extends AbstractJsonOrgParseTest {
        /**
         * Create a new DoubleParseTest.
         */
        public DoubleParseTest() {
            super(JsonOrgValueFactory.DOUBLE);
        }
    }
    
    /**
     * Unit test using JsonOrgValueFactory.BigMathFactory.
     */
    @Nested
    class BigMathParseTest extends AbstractJsonOrgParseTest {
        /**
         * Create a new BigMathParseTest.
         */
        public BigMathParseTest() {
            super(new JsonOrgValueFactory.BigMathFactory(null, null, null, null));
        }
    }
    

    /**
     * Unit test using JsonOrgValueFactory.BIGMATH32.
     */
    @Nested
    class BigMathParseTest32 extends AbstractJsonOrgParseTest {
        /**
         * Create a new BigMathParseTest32.
         */
        public BigMathParseTest32() {
            super(JsonOrgValueFactory.BIGMATH32);
        }
    }
    
    /**
     * Unit test using JsonOrgValueFactory.BIGMATH64.
     */
    @Nested
    class BigMathParseTest64 extends AbstractJsonOrgParseTest {
        /**
         * Create a new BigMathParseTest64.
         */
        public BigMathParseTest64() {
            super(JsonOrgValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JsonOrgValueFactory.BIGMATH128.
     */
    @Nested
    class BigMathParseTest128 extends AbstractJsonOrgParseTest {
        /**
         * Create a new BigMathParseTest128.
         */
        public BigMathParseTest128() {
            super(JsonOrgValueFactory.BIGMATH128);
        }
    }
}
