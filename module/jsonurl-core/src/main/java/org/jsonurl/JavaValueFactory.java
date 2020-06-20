package org.jsonurl;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link org.jsonurl.ValueFactory ValueFactory} based on J2SE8 data types.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface JavaValueFactory extends ValueFactory.TransparentBuilder<
    Object,
    Object,
    List<Object>,
    Map<String,Object>,
    Boolean,
    Number,
    Object,
    String> {

    /**
     * A singleton instance of {@link JavaValueFactory}.
     */
    public static final JavaValueFactory PRIMITIVE = new JavaValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return NumberBuilder.build(text, true);
        }
    };
    
    /**
     * A singleton instance of {@link JavaValueFactory}.
     */
    public static final JavaValueFactory DOUBLE = new JavaValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return Double.valueOf(text.toString());
        }
    };
    
    /**
     * A singleton instance of {@link JavaValueFactory}.
     */
    public static final JavaValueFactory BIGMATH = new JavaValueFactory() {
        @Override
        public Number getNumber(NumberText text) {
            return NumberBuilder.build(text, false);
        }
    };

    /**
     * The null value.
     */
    public static final String NULL = "null";

    /**
     * The empty composite value.
     */
    public static final Map<String,Object> EMPTY = Collections.emptyMap();

    @Override
    default Object getEmptyComposite() {
        return EMPTY;
    }

    @Override
    default Object getNull() {
        return NULL;
    }

    @Override
    default List<Object> newArrayBuilder() {
        return new ArrayList<>(4);
    }

    @Override
    default Map<String,Object> newObjectBuilder() {
        return new HashMap<>(4);
    }

    @Override
    default void add(List<Object> dest, Object obj) {
        dest.add(obj);
    }

    @Override
    default void put(
            Map<String,Object> dest,
            String key,
            Object value) {

        dest.put(key, value);
    }

    @Override
    default Boolean getTrue() {
        return Boolean.TRUE;
    }

    @Override
    default Boolean getFalse() {
        return Boolean.FALSE;
    }

    @Override
    default String getString(CharSequence s, int start, int stop) {
        return toJavaString(s, start, stop);
    }

    @Override
    default String getString(String s) {
        return s;
    }

    @Override
    default boolean isValid(Set<ValueType> types, Object value) {
        if (isNull(value)) {
            return types.contains(ValueType.NULL);
        }
        if (isEmpty(value)) {
            return ValueType.containsComposite(types);
        }
        if (value instanceof String) {
            return types.contains(ValueType.STRING);
        }
        if (value instanceof Number) {
            return types.contains(ValueType.NUMBER);
        }
        if (value instanceof Boolean) {
            return types.contains(ValueType.BOOLEAN);
        }
        if (value instanceof List) {
            return types.contains(ValueType.ARRAY);
        }
        if (value instanceof Map) {
            return types.contains(ValueType.OBJECT);
        }
        return false;
    }

    /**
     * Get a java.lang.String from a java.lang.CharSequence
     * @param cs input
     * @param start start index
     * @param stop stop index
     * @return a valid String
     */
    public static String toJavaString(CharSequence cs, int start, int stop) {
        if (cs instanceof String) {
            String s = (String)cs;

            if (start == 0 && stop == cs.length()) {
                return s;
            }
            
            return s.substring(start, stop);
        }
        return cs.subSequence(start, stop).toString();
    }
}
