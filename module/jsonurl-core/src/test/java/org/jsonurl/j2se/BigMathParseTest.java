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

package org.jsonurl.j2se;

/**
 * Unit test using JavaValueFactory.BigMathFactory.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
class BigMathParseTest extends JavaValueFactoryParseTest {

    /**
     * Unit test using JavaValueFactory.BIGMATH32.
     */
    static final class Test32 extends JavaValueFactoryParseTest {
        /**
         * Create a new Test32.
         */
        Test32() {
            super(JavaValueFactory.BIGMATH32);
        }
    }

    /**
     * Unit test using JavaValueFactory.BIGMATH64.
     */
    static final class Test64 extends JavaValueFactoryParseTest {
        /**
         * Create a new Test64.
         */
        Test64() {
            super(JavaValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JavaValueFactory.BIGMATH128.
     */
    static final class Test128 extends JavaValueFactoryParseTest {
        /**
         * Create a new Test128.
         */
        Test128() {
            super(JavaValueFactory.BIGMATH128);
        }
    }

    /**
     * Create a new BigMathParseTest.
     */
    BigMathParseTest() {
        super(new JavaValueFactory.BigMathFactory(null, null, null, null));
    }
}
