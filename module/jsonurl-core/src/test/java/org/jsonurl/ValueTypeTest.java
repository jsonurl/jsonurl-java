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

package org.jsonurl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * ValueTypeTest.
 */
class ValueTypeTest {

    @ParameterizedTest
    @EnumSource(ValueType.class)
    void test(ValueType type) {
        assertEquals(
            type == ValueType.ARRAY || type == ValueType.OBJECT,
            type.isComposite(),
            type.name());
        
        assertEquals(
            type == ValueType.ARRAY || type == ValueType.OBJECT || type == ValueType.NULL,
            type.isCompositeOrNull(),
            type.name());
        
        assertEquals(
            type != ValueType.ARRAY && type != ValueType.OBJECT && type != ValueType.NULL,
            type.isPrimitive(),
            type.name());
        
        assertEquals(
            type != ValueType.ARRAY && type != ValueType.OBJECT,
            type.isPrimitiveOrNull(),
            type.name());
    }
}
