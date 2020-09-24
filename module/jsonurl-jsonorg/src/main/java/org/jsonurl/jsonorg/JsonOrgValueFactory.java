package org.jsonurl.jsonorg;

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

import java.math.MathContext;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsonurl.BigMath;
import org.jsonurl.NumberBuilder;
import org.jsonurl.NumberText;
import org.jsonurl.ValueFactory;
import org.jsonurl.ValueType;
import org.jsonurl.j2se.JavaValueFactory;


/**
 * A JSON&#x2192;URL ValueFactory which uses classes from the org.json package.
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface JsonOrgValueFactory extends ValueFactory.TransparentBuilder<
        Object,
        Object,
        JSONArray,
        JSONObject,
        Boolean,
        Number,
        Object,
        String> {
    
    /**
     * A {@link JsonOrgValueFactory} that uses
     * {@link java.math.BigInteger BigInteger} and
     * {@link java.math.BigDecimal BigDecimal} when necessary.
     * 
     * <p>When using this factory, numbers without fractional parts that are
     * too big to be stored in a {@link java.lang.Long Long} will be stored
     * in a {@link java.math.BigInteger BigInteger}. Numbers with fractional
     * parts that are too big to stored in a {@link java.lang.Double Double}
     * will be stored in a {@link java.math.BigDecimal BigDecimal}.
     */
    public static class BigMathFactory extends BigMath
            implements JsonOrgValueFactory, ValueFactory.BigMathFactory<
                Object,
                Object,
                JSONArray,
                JSONArray,
                JSONObject,
                JSONObject,
                Boolean,
                Number,
                Object,
                String> {
        /**
         * Create a new BigMathFactory JsonOrgValueFactory using the given MathContext.
         * @param mc a valid MathContext or null
         * @param bigIntegerBoundaryNeg negative value boundary
         * @param bigIntegerBoundaryPos positive value boundary
         * @param bigIntegerOverflow action on boundary overflow
         */
        public BigMathFactory(
            MathContext mc,
            String bigIntegerBoundaryNeg,
            String bigIntegerBoundaryPos,
            BigIntegerOverflow bigIntegerOverflow) {

            super(mc,
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
     * This represents the empty composite value.
     */
    public static final Object EMPTY = new JSONObject();
    
    /**
     * A singleton instance of {@link JsonOrgValueFactory}.
     * 
     * <p>This factory uses
     * {@link org.jsonurl.NumberBuilder#build(boolean)
     * NumberBuilder.build(text,true)}
     * to parse JSON&#x2192;URL numbers.  
     */
    public static final JsonOrgValueFactory PRIMITIVE = new JsonOrgValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return NumberBuilder.build(text, true);
        }
    };
    
    /**
     * A singleton instance of {@link JsonOrgValueFactory}.
     *
     * <p>This factory uses
     * {@link java.lang.Double#valueOf(String) Double.valueOf(text)}
     * to parse JSON&#x2192;URL numbers.
     */
    public static final JsonOrgValueFactory DOUBLE = new JsonOrgValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return Double.valueOf(text.toString());
        }
    };
    
    /**
     * A singleton instance of {@link BigMathFactory} with 32-bit boundaries.
     */
    public static final BigMathFactory BIGMATH32 = new BigMathFactory(
        MathContext.DECIMAL32,
        BigMath.BIG_INTEGER32_BOUNDARY_NEG,
        BigMath.BIG_INTEGER32_BOUNDARY_POS,
        null);
    
    /**
     * A singleton instance of {@link BigMathFactory} with 64-bit boundaries.
     */
    public static final BigMathFactory BIGMATH64 = new BigMathFactory(
        MathContext.DECIMAL64,
        BigMath.BIG_INTEGER64_BOUNDARY_NEG,
        BigMath.BIG_INTEGER64_BOUNDARY_POS,
        null);
    
    /**
     * A singleton instance of {@link BigMathFactory} with 128-bit boundaries.
     */
    public static final BigMathFactory BIGMATH128 = new BigMathFactory(
        MathContext.DECIMAL128,
        BigMath.BIG_INTEGER128_BOUNDARY_NEG,
        BigMath.BIG_INTEGER128_BOUNDARY_POS,
        null);

    @Override
    default JSONArray newArrayBuilder() {
        return new JSONArray();
    }

    @Override
    default JSONObject newObjectBuilder() {
        return new JSONObject();
    }

    @Override
    default void add(JSONArray dest, Object obj) {
        dest.put(obj);
    }

    @Override
    default void put(JSONObject dest, String key, Object value) {
        dest.put(key, value);
    }

    @Override
    default Object getEmptyComposite() {
        return EMPTY;
    }

    @Override
    default Object getNull() {
        return JSONObject.NULL;
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
        return JavaValueFactory.toJavaString(s, start, stop);
    }
    
    @Override
    default boolean isValid(Set<ValueType> types, Object value) {
        if (value instanceof String) {
            return types.contains(ValueType.STRING);
        }
        if (value instanceof Number) {
            return types.contains(ValueType.NUMBER);
        }
        if (value instanceof Boolean) {
            return types.contains(ValueType.BOOLEAN);
        }
        if (value instanceof JSONArray) {
            return types.contains(ValueType.ARRAY);
        }
        if (value instanceof JSONObject) {
            return types.contains(ValueType.OBJECT);
        }
        if (isNull(value)) {
            return types.contains(ValueType.NULL);
        }
        if (isEmptyComposite(value)) {
            return types.contains(ValueType.OBJECT)
                || types.contains(ValueType.ARRAY);
        }
        return false;
    }
}
