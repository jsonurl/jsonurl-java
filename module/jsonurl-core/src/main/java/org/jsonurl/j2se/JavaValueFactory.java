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

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsonurl.BigMath;
import org.jsonurl.NumberBuilder;
import org.jsonurl.NumberText;
import org.jsonurl.ValueFactory;
import org.jsonurl.ValueType;

/**
 * A {@link org.jsonurl.ValueFactory ValueFactory} based on Java SE data types.
 * 
 * <p>The following singletons are available:
 * <ul>
 * <li>{@link #PRIMITIVE}
 * <li>{@link #DOUBLE}
 * <li>{@link #BIGMATH32}
 * <li>{@link #BIGMATH64}
 * <li>{@link #BIGMATH128}
 * </ul>
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface JavaValueFactory extends ValueFactory<
        Object,
        Object,
        List<Object>,
        List<Object>,
        Map<String,Object>,
        Map<String,Object>,
        Boolean,
        Number,
        Object,
        String> {

    /**
     * The default null value.
     */
    Object NULL = new Object();

    /**
     * The default empty composite value.
     */
    Map<String,Object> EMPTY = Collections.emptyMap();

    /**
     * A singleton instance of {@link BigMathFactory} with 32-bit boundaries.
     */
    BigMathFactory BIGMATH32 = new BigMathFactory(
        MathContext.DECIMAL32,
        BigMath.BIG_INTEGER32_BOUNDARY_NEG,
        BigMath.BIG_INTEGER32_BOUNDARY_POS,
        null);
    
    /**
     * A singleton instance of {@link BigMathFactory} with 64-bit boundaries.
     */
    BigMathFactory BIGMATH64 = new BigMathFactory(
        MathContext.DECIMAL64,
        BigMath.BIG_INTEGER64_BOUNDARY_NEG,
        BigMath.BIG_INTEGER64_BOUNDARY_POS,
        null);
    
    /**
     * A singleton instance of {@link BigMathFactory} with 128-bit boundaries.
     */
    BigMathFactory BIGMATH128 = new BigMathFactory(
        MathContext.DECIMAL128,
        BigMath.BIG_INTEGER128_BOUNDARY_NEG,
        BigMath.BIG_INTEGER128_BOUNDARY_POS,
        null);

    /**
     * A {@link org.jsonurl.ValueFactory ValueFactory} based on Java SE data
     * types that uses {@link java.math.BigInteger BigInteger} and
     * {@link java.math.BigDecimal BigDecimal} when necessary.
     * 
     * <p>When using an instance of this factory numbers without fractional
     * parts that are too big to be stored in a {@link java.lang.Long Long}
     * will be stored in a {@link java.math.BigInteger BigInteger}. Numbers
     * with fractional parts that are too big to stored in a
     * {@link java.lang.Double Double} will be stored in a
     * {@link java.math.BigDecimal BigDecimal}.
     */
    class BigMathFactory extends BigMath 
            implements JavaValueFactory,
                ValueFactory.BigMathFactory<
                    Object,
                    Object,
                    List<Object>,
                    List<Object>,
                    Map<String,Object>,
                    Map<String,Object>,
                    Boolean,
                    Number,
                    Object,
                    String> {

        /**
         * Create a new BigMathFactory JavaValueFactory using the given MathContext.
         * @param context a valid MathContext or null
         * @param bigIntegerBoundaryNeg negative value boundary
         * @param bigIntegerBoundaryPos positive value boundary
         * @param bigIntegerOverflow action on boundary overflow
         */
        public BigMathFactory(
            MathContext context,
            String bigIntegerBoundaryNeg,
            String bigIntegerBoundaryPos,
            BigMath.BigIntegerOverflow bigIntegerOverflow) {

            super(context,
                bigIntegerBoundaryNeg,
                bigIntegerBoundaryPos,
                bigIntegerOverflow);
        }

        @Override
        public Number getNumber(NumberText text) {
            return NumberBuilder.build(text, false, this);
        }
    }

    /**
     * A singleton instance of {@link JavaValueFactory} that stores numbers as
     * boxed primitives.
     * 
     * <p>This factory uses an instance of {@link java.lang.Long Long} for
     * numbers with no fractional value that fall between {@link Long#MIN_VALUE}
     * and {@link Long#MAX_VALUE}, and  an instance of
     * {@link java.lang.Double Double} for everything else.
     */
    JavaValueFactory PRIMITIVE = new JavaValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return NumberBuilder.build(text, true);
        }
    };
    
    /**
     * A singleton instance of {@link JavaValueFactory} that uses instances of
     * {@link java.lang.Double Double} for all numbers.
     */
    JavaValueFactory DOUBLE = new JavaValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return Double.valueOf(text.toString());
        }
    };

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
        return new LinkedList<>();
    }

    @Override
    default Map<String,Object> newObjectBuilder() {
        return new HashMap<>(4);
    }
    
    @Override
    default List<Object> newArray(List<Object> builder) {
        //
        // create an ArrayList from a LinkedList; space optimized now that the
        // size is known.
        //
        return new ArrayList<>(builder);
    }
    
    @Override
    default Map<String,Object> newObject(Map<String,Object> builder) {
        //
        // just use the builder
        //
        return builder;
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
    default String getString(CharSequence text, int start, int stop) {
        return toJavaString(text, start, stop);
    }

    @Override
    default String getString(String value) {
        return value;
    }

    @Override
    default boolean isValid(Set<ValueType> types, Object value) {
        if (isNull(value)) {
            return types.contains(ValueType.NULL);
        }
        if (isEmptyComposite(value)) {
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
     * @param text input
     * @param start start index
     * @param stop stop index
     * @return a valid String
     */
    static String toJavaString(CharSequence text, int start, int stop) {
        if (text instanceof String) {
            String str = (String)text;

            if (start == 0 && stop == text.length()) {
                return str;
            }
            
            return str.substring(start, stop);
        }
        return text.subSequence(start, stop).toString();
    }
}
