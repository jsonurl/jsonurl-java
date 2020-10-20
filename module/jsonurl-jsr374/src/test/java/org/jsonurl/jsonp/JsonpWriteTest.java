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

import org.junit.jupiter.api.Nested;

/**
 * Unit tests for JsonUrlStringBuilder + JsonpValueFactory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
class JsonpWriteTest {

    /**
     * JsonpValueFactory.PRIMITIVE JsonpWriteTest.
     */
    @Nested
    class PrimitiveWriteTest extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.PRIMITIVE;
        }
        
    }

    /**
     * JsonpValueFactory.DOUBLE JsonpWriteTest.
     */
    @Nested
    class DoubleWriteTest extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.DOUBLE;
        }
    }

    /**
     * JsonpValueFactory.BIGMATH32 JsonpWriteTest.
     */
    @Nested
    class BigMathWriteTest extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return new JsonpValueFactory.BigMathFactory(null, null, null, null);
        }
    }

    /**
     * JsonpValueFactory.BIGMATH32 JsonpWriteTest.
     */
    @Nested
    class BigMathWriteTest32 extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.BIGMATH32;
        }
    }

    /**
     * JsonpValueFactory.BIGMATH64 JsonpWriteTest.
     */
    @Nested
    class BigMathWriteTest64 extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.BIGMATH64;
        }
    }

    /**
     * JsonpValueFactory.BIGMATH128 JsonpWriteTest.
     */
    @Nested
    class BigMathWriteTest128 extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.BIGMATH128;
        }
    }
}
