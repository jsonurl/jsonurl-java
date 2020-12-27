/*
 * Copyright 2020 David MacCormack
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

import java.util.Set;

/**
 * An enumeration of JSON&#x2192;URL value types.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-07-05
 */
public enum ValueType {
    /**
     * A {@code null} literal.
     */
    NULL,
    /**
     * A {@code true} or {@code false} literal.
     */
    BOOLEAN,
    /**
     * A number literal (e.g. 1, 1.2, 1e3, etc).
     */
    NUMBER,
    /**
     * A string literal.
     */
    STRING,
    /**
     * An array.
     */
    ARRAY,
    /**
     * An object.
     */
    OBJECT;

    /**
     * Test if the given EnumSet contains a composite value.
     * @param set the set to test
     */
    public static final boolean containsComposite(Set<ValueType> set) {
        return set.contains(OBJECT) || set.contains(ARRAY); 
    }

    /**
     * Get the ValueType for the given {@link CompositeType}. 
     */
    public static final ValueType forCompositeType(CompositeType type) {
        return type == null ? null : type.getValueType();
    }
}