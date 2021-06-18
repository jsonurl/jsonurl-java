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

package org.jsonurl.jsonorg;

import java.math.MathContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsonurl.BigMath;
import org.jsonurl.BigMathProvider;
import org.jsonurl.factory.ValueFactory;
import org.jsonurl.j2se.JavaValueFactory;
import org.jsonurl.text.NumberBuilder;
import org.jsonurl.text.NumberText;


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
     * This represents the empty composite value.
     */
    Object EMPTY = new JSONObject();

    /**
     * A singleton instance of {@link BigMathFactory} with 32-bit boundaries.
     */
    BigMathFactory BIGMATH32 = new BigMathFactory(
        MathContext.DECIMAL32,
        BigMathProvider.BIG_INTEGER32_BOUNDARY_NEG,
        BigMathProvider.BIG_INTEGER32_BOUNDARY_POS,
        null);
    
    /**
     * A singleton instance of {@link BigMathFactory} with 64-bit boundaries.
     */
    BigMathFactory BIGMATH64 = new BigMathFactory(
        MathContext.DECIMAL64,
        BigMathProvider.BIG_INTEGER64_BOUNDARY_NEG,
        BigMathProvider.BIG_INTEGER64_BOUNDARY_POS,
        null);
    
    /**
     * A singleton instance of {@link BigMathFactory} with 128-bit boundaries.
     */
    BigMathFactory BIGMATH128 = new BigMathFactory(
        MathContext.DECIMAL128,
        BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG,
        BigMathProvider.BIG_INTEGER128_BOUNDARY_POS,
        null);

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
    class BigMathFactory extends BigMath
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
         * @param mctxt a valid MathContext or null
         * @param bigIntegerBoundaryNeg negative value boundary
         * @param bigIntegerBoundaryPos positive value boundary
         * @param bigIntegerOverflow action on boundary overflow
         */
        public BigMathFactory(
            MathContext mctxt,
            String bigIntegerBoundaryNeg,
            String bigIntegerBoundaryPos,
            BigIntegerOverflow bigIntegerOverflow) {

            super(mctxt,
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
     * A singleton instance of {@link JsonOrgValueFactory}.
     * 
     * <p>This factory uses
     * {@link org.jsonurl.text.NumberBuilder#build(boolean)
     * NumberBuilder.build(text,true)}
     * to parse JSON&#x2192;URL numbers.  
     */
    JsonOrgValueFactory PRIMITIVE = new JsonOrgValueFactory() {

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
    JsonOrgValueFactory DOUBLE = new JsonOrgValueFactory() {

        @Override
        public Number getNumber(NumberText text) {
            return Double.valueOf(text.toString());
        }
    };

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
    default String getString(CharSequence text, int start, int stop) {
        return JavaValueFactory.toJavaString(text, start, stop);
    }
}
