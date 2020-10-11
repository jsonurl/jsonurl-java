package org.jsonurl.j2se;

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

/**
 * Unit test using JavaValueFactory.PRIMITIVE
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class PrimitiveParseTest extends AbstractJavaValueFactoryParseTest {

    /**
     * Create a new PrimitiveParseTest.
     */
    public PrimitiveParseTest() {
        super(JavaValueFactory.PRIMITIVE);
    }
}
