/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

/**
 * Unit test using JsonpValueFactory.BigMathFactory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class BigMathParseTest extends JsonpParseTest {

    /**
     * Unit test using JsonpValueFactory.BIGMATH32.
     */
    static final class Math32 extends JsonpParseTest {

        /**
         * Create a new Math32.
         */
        Math32() {
            super(JsonpValueFactory.BIGMATH32);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH64.
     */
    static final class Math64 extends JsonpParseTest {

        /**
         * Create a new Math64.
         */
        Math64() {
            super(JsonpValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH128.
     */
    static final class Math128 extends JsonpParseTest {

        /**
         * Create a new Math128.
         */
        Math128() {
            super(JsonpValueFactory.BIGMATH128);
        }
    }
    
    /**
     * Unit test using JsonpValueFactory.BIGMATH32.
     */
    static final class Decimal32 extends JsonpParseTest {

        /**
         * Create a new Decimal32.
         */
        Decimal32() {
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
    static final class Decimal64 extends JsonpParseTest {

        /**
         * Create a new Decimal64.
         */
        Decimal64() {
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
    static final class Decimal128 extends JsonpParseTest {

        /**
         * Create a new Decimal128.
         */
        Decimal128() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL128,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_POS,
                null));
        }
    }

    /**
     * Create a new BigMathParseTest.
     */
    BigMathParseTest() {
        super(new JsonpValueFactory.BigMathFactory(null, null, null, null));
    }
}
