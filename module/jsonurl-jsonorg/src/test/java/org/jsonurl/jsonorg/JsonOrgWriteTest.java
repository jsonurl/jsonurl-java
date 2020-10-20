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

package org.jsonurl.jsonorg;

import org.junit.jupiter.api.Nested;

/**
 * Unit tests for JsonUrlStringBuilder + JsonOrgValueFactory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
class JsonOrgWriteTest {

    /**
     * JsonOrgValueFactory.PRIMITIVE JsonOrgWriteTest.
     */
    @Nested
    class PrimitiveWriteTest extends AbstractJsonOrgWriteTest {

        @Override
        public JsonOrgValueFactory getFactory() {
            return JsonOrgValueFactory.PRIMITIVE;
        }
        
    }

    /**
     * JsonOrgValueFactory.DOUBLE JsonOrgWriteTest.
     */
    @Nested
    class DoubleWriteTest extends AbstractJsonOrgWriteTest {

        @Override
        public JsonOrgValueFactory getFactory() {
            return JsonOrgValueFactory.DOUBLE;
        }
    }

    /**
     * JsonOrgValueFactory.BIGMATH32 JsonOrgWriteTest.
     */
    @Nested
    class BigMathWriteTest extends AbstractJsonOrgWriteTest {

        @Override
        public JsonOrgValueFactory getFactory() {
            return new JsonOrgValueFactory.BigMathFactory(null, null, null, null);
        }
    }

    /**
     * JsonOrgValueFactory.BIGMATH32 JsonOrgWriteTest.
     */
    @Nested
    class BigMathWriteTest32 extends AbstractJsonOrgWriteTest {

        @Override
        public JsonOrgValueFactory getFactory() {
            return JsonOrgValueFactory.BIGMATH32;
        }
    }

    /**
     * JsonOrgValueFactory.BIGMATH64 JsonOrgWriteTest.
     */
    @Nested
    class BigMathWriteTest64 extends AbstractJsonOrgWriteTest {

        @Override
        public JsonOrgValueFactory getFactory() {
            return JsonOrgValueFactory.BIGMATH64;
        }
    }

    /**
     * JsonOrgValueFactory.BIGMATH128 JsonOrgWriteTest.
     */
    @Nested
    class BigMathWriteTest128 extends AbstractJsonOrgWriteTest {

        @Override
        public JsonOrgValueFactory getFactory() {
            return JsonOrgValueFactory.BIGMATH128;
        }
    }
}
