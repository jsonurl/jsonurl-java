package org.jsonurl;

import java.util.Set;

/**
 * An enumeration of JSON&gt;URL value types.
 */
public enum ValueType {
    NULL(true),
    BOOLEAN(true),
    NUMBER(true),
    STRING(true),
    ARRAY(false),
    OBJECT(false);

    /**
     * see {@link #isPrimitive()}.
     */
    private final boolean isPrimitive;

    private ValueType(boolean isPrimitive) {
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