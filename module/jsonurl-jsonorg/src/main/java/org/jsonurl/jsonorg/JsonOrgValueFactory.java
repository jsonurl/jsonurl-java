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
public abstract class JsonOrgValueFactory implements ValueFactory.TransparentBuilder<
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
    public static class BigMath extends JsonOrgValueFactory {

        /**
         * MathContext for new BigDecimal instances. 
         */
        private final MathContext mc;

        /**
         * Create a new BigMath JsonOrgValueFactory using the default
         * MathContext {@link java.math.MathContext#DECIMAL128 DECIMAL128}.
         */
        public BigMath() {
            this(MathContext.DECIMAL128);
        }

        /**
         * Create a new BigMath JsonOrgValueFactory using the given MathContext.
         * @param mc a valid MathContext or null
         */
        public BigMath(MathContext mc) {
            this.mc = mc;
        }

        @Override
        public Number getNumber(NumberText text) {
            return NumberBuilder.build(text, false, mc);
        }
    }

    /**
     * This represents the empty composite value.
     */
    private static final Object EMPTY = new JSONObject();
    
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
     * A singleton instance of {@link BigMath}.
     */
    public static final JsonOrgValueFactory BIGMATH = new BigMath() {
    };

    @Override
    public JSONArray newArrayBuilder() {
        return new JSONArray();
    }

    @Override
    public JSONObject newObjectBuilder() {
        return new JSONObject();
    }

    @Override
    public void add(JSONArray dest, Object obj) {
        dest.put(obj);
    }

    @Override
    public void put(JSONObject dest, String key, Object value) {
        dest.put(key, value);
    }

    @Override
    public Object getEmptyComposite() {
        return EMPTY;
    }

    @Override
    public Object getNull() {
        return JSONObject.NULL;
    }

    @Override
    public Boolean getTrue() {
        return Boolean.TRUE;
    }

    @Override
    public Boolean getFalse() {
        return Boolean.FALSE;
    }

    @Override
    public String getString(CharSequence s, int start, int stop) {
        return JavaValueFactory.toJavaString(s, start, stop);
    }
    
    @Override
    public boolean isValid(Set<ValueType> types, Object value) {
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
        if (isEmpty(value)) {
            return types.contains(ValueType.OBJECT)
                || types.contains(ValueType.ARRAY);
        }
        return false;
    }
}
