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

package org.jsonurl.jsonp;

import java.math.MathContext;
import org.jsonurl.BigMathProvider;
import org.junit.jupiter.api.Nested;


/**
 * Unit tests for Parser + JsonpValueFactory.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class JsonpParseTest {

    /**
     * Unit test using JsonpValueFactory.PRIMITIVE.
     */
    @Nested
    class PrimitiveParseTest extends AbstractJsonpParseTest {
        /**
         * Create a new PrimitiveParseTest.
         */
        public PrimitiveParseTest() {
            super(JsonpValueFactory.PRIMITIVE);
        }
    }
    
    /**
     * Unit test using JsonpValueFactory.DOUBLE.
     */
    @Nested
    class DoubleParseTest extends AbstractJsonpParseTest {
        /**
         * Create a new DoubleParseTest.
         */
        public DoubleParseTest() {
            super(JsonpValueFactory.DOUBLE);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BigMathFactory.
     *
     * @author jsonurl.org
     * @author David MacCormack
     * @since 2019-09-01
     */
    @Nested
    class BigMathParseTest extends AbstractJsonpParseTest {

        /**
         * Create a new BigMathParseTest.
         */
        BigMathParseTest() {
            super(new JsonpValueFactory.BigMathFactory(null, null, null, null));
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH32.
     */
    @Nested
    class BigMathParseTest32 extends AbstractJsonpParseTest {
        /**
         * Create a new BigMathParseTest32.
         */
        BigMathParseTest32() {
            super(JsonpValueFactory.BIGMATH32);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH64.
     */
    @Nested
    class BigMathParseTest64 extends AbstractJsonpParseTest {
        /**
         * Create a new BigMathParseTest64.
         */
        BigMathParseTest64() {
            super(JsonpValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH128.
     */
    @Nested
    class BigMathParseTest128 extends AbstractJsonpParseTest {
        /**
         * Create a new BigMathParseTest128.
         */
        BigMathParseTest128() {
            super(JsonpValueFactory.BIGMATH128);
        }
    }
    
    /**
     * Unit test using JsonpValueFactory.BIGMATH32.
     */
    @Nested
    class BigDecimalParseTest32 extends AbstractJsonpParseTest {
        /**
         * Create a new BigDecimalParseTest32.
         */
        BigDecimalParseTest32() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL32,
                BigMathProvider.BIG_INTEGER32_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER32_BOUNDARY_POS,
                null));
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH64.
     */
    @Nested
    class BigDecimalParseTest64 extends AbstractJsonpParseTest {
        /**
         * Create a new BigDecimalParseTest64.
         */
        BigDecimalParseTest64() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL64,
                BigMathProvider.BIG_INTEGER64_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER64_BOUNDARY_POS,
                null));
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH128.
     */
    @Nested
    class BigDecimalParseTest128 extends AbstractJsonpParseTest {

        /**
         * Create a new BigDecimalParseTest128.
         */
        BigDecimalParseTest128() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL128,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_POS,
                null));
        }
    }
}
