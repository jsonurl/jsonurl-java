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
 */
public enum ValueType {
    /**
     * A {@code null} literal.
     */
    NULL(true),
    /**
     * A {@code true} or {@code false} literal.
     */
    BOOLEAN(true),
    /**
     * A number literal (e.g. 1, 1.2, 1e3, etc).
     */
    NUMBER(true),
    /**
     * A string literal.
     */
    STRING(true),
    /**
     * An array.
     */
    ARRAY(false),
    /**
     * An object.
     */
    OBJECT(false);

    /**
     * see {@link #isPrimitive()}.
     */
    private final boolean isPrimitive;

    ValueType(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    /**
     * Test if this ValueType is a primitive type.
     */
    public boolean isPrimitive() {
        return isPrimitive && this != NULL;
    }

    /**
     * Test if this ValueType is a primitive type or NULL.
     */
    public boolean isPrimitiveOrNull() {
        return isPrimitive;
    }

    /**
     * Test if this ValueType is a composite type.
     */
    public boolean isComposite() {
        return !isPrimitive;
    }

    /**
     * Test if this ValueType is a composite type or NULL.
     */
    public boolean isCompositeOrNull() {
        return !isPrimitive || this == NULL;
    }

    /**
     * Test if the given EnumSet contains a composite value.
     * @param set the set to test
     */
    public static final boolean containsComposite(Set<ValueType> set) {
        return set.contains(OBJECT) || set.contains(ARRAY); 
    }
}