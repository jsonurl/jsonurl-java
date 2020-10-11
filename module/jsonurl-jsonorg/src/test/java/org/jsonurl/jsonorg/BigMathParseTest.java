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

/**
 * Unit test using JsonOrgValueFactory.BigMathFactory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class BigMathParseTest extends AbstractJsonOrgParseTest {
    
    /**
     * Unit test using JsonOrgValueFactory.BIGMATH32.
     */
    static final class Test32 extends AbstractJsonOrgParseTest {
        /**
         * Create a new Test32.
         */
        public Test32() {
            super(JsonOrgValueFactory.BIGMATH32);
        }
    }
    
    /**
     * Unit test using JsonOrgValueFactory.BIGMATH64.
     */
    static final class Test64 extends AbstractJsonOrgParseTest {
        /**
         * Create a new Test64.
         */
        public Test64() {
            super(JsonOrgValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JsonOrgValueFactory.BIGMATH128.
     */
    static final class Test128 extends AbstractJsonOrgParseTest {
        /**
         * Create a new Test128.
         */
        public Test128() {
            super(JsonOrgValueFactory.BIGMATH128);
        }
    }

    /**
     * Create a new BigMathParseTest.
     */
    public BigMathParseTest() {
        super(new JsonOrgValueFactory.BigMathFactory(null, null, null, null));
    }
}
