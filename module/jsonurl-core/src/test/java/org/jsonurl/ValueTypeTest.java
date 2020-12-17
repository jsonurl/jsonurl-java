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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * ValueTypeTest.
 */
class ValueTypeTest {

    @ParameterizedTest
    @EnumSource(ValueType.class)
    @Disabled
    void test(ValueType type) {
        if (type == ValueType.ARRAY) {
            assertEquals(
                CompositeType.ARRAY,
                type.getCompositeType(),
                type.name());

            assertTrue(
                ValueType.containsComposite(EnumSet.of(type)),
                type.name());

        } else if (type == ValueType.OBJECT) {
            assertEquals(
                CompositeType.OBJECT,
                type.getCompositeType(),
                type.name());

            assertTrue(
                ValueType.containsComposite(EnumSet.of(type)),
                type.name());

        } else {
            assertNull(
                type.getCompositeType(),
                type.name());

            assertFalse(
                ValueType.containsComposite(EnumSet.of(type)),
                type.name());
        }
    }
}
