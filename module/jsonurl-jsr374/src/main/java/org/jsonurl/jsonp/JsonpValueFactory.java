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

package org.jsonurl.jsonp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import org.jsonurl.BigMath;
import org.jsonurl.factory.ValueFactory;
import org.jsonurl.text.NumberBuilder;
import org.jsonurl.text.NumberText;

/**
 * A JSON&#x2192;URL ValueFactory which uses the JSR-374 JSONP interface.
 * 
 * <p>If you're using the reference implementation of JSR-374 then you probably
 * want to use {@link #BIGMATH64}, {@link #BIGMATH128}, or your own
 * {@link BigDecimalFactory} derivative.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */

//
// this is a factory - the whole point is pass-through objects
//
public interface JsonpValueFactory extends ValueFactory<
        JsonValue,
        JsonStructure,
        JsonArrayBuilder,
        JsonArray,
        JsonObjectBuilder,
        JsonObject,
        JsonValue,
        JsonNumber,
        JsonValue,
        JsonString> {


    /**
     * A singleton instance of {@link BigMathFactory} with 32-bit boundaries.
     */
    BigDecimalFactory BIGMATH32 = new BigDecimalFactory(MathContext.DECIMAL32);

    /**
     * A singleton instance of {@link BigMathFactory} with 64-bit boundaries.
     */
    BigDecimalFactory BIGMATH64 = new BigDecimalFactory(MathContext.DECIMAL64);

    /**
     * A singleton instance of {@link BigMathFactory} with 128-bit boundaries.
     */
    BigDecimalFactory BIGMATH128 = new BigDecimalFactory(MathContext.DECIMAL128);

    /**
     * A {@link JsonpValueFactory} that uses
     * {@link java.math.BigInteger BigInteger} and
     * {@link java.math.BigDecimal BigDecimal} when necessary.
     * 
     * <p>When using this factory, numbers without fractional parts that are
     * too big to be stored in a {@link java.lang.Long Long} will be stored
     * in a {@link java.math.BigInteger BigInteger}. Numbers with fractional
     * parts that are too big to stored in a {@link java.lang.Double Double}
     * will be stored in a {@link java.math.BigDecimal BigDecimal}.
     * 
     * <p>The reference implementation of JSR-374 does not actually store
     * double or BigInteger values. When
     * {@link javax.json.spi.JsonProvider#createValue(double)
     * JsonProvider.createValue(double)}
     * or {@link javax.json.spi.JsonProvider#createValue(BigInteger)
     * JsonProvider.createValue(BigInteger)} is called
     * the value is converted to a BigDecimal. This causes problems when
     * {@link org.jsonurl.BigMathProvider.BigIntegerOverflow#INFINITY
     * BigIntegerOverflow.INFINITY} is used because
     * {@link java.math.BigDecimal#BigDecimal(double)
     * BigDecimal(double)} will throw a NumberFormatException when given
     * {@code +Inf}/{@code -Inf}. So, don't use
     * {@code BigIntegerOverflow.INFINITY} unless you're sure your
     * JSR-374 implementation supports those values. In fact, if you're
     * using the reference implementation of JSR-374 (or other
     * implementation with a similar limitation) then use
     * {@link BigDecimalFactory} instead.
     */
    class BigMathFactory extends BigMath
            implements JsonpValueFactory, ValueFactory.BigMathFactory<
                JsonValue,
                JsonStructure,
                JsonArrayBuilder,
                JsonArray,
                JsonObjectBuilder,
                JsonObject,
                JsonValue,
                JsonNumber,
                JsonValue,
                JsonString> {
        
        /** My JsonProvider. */
        protected final JsonProvider jsonProvider;

        /**
         * Create a new BigMathFactory JsonpValueFactory using the given MathContext.
         * @param mctxt a valid MathContext or null
         * @param bigIntegerBoundaryNeg negative value boundary
         * @param bigIntegerBoundaryPos positive value boundary
         * @param bigIntegerOverflow action on boundary overflow
         * @param jsonProvider a valid JsonProvider
         */
        public BigMathFactory(
            MathContext mctxt,
            String bigIntegerBoundaryNeg,
            String bigIntegerBoundaryPos,
            BigIntegerOverflow bigIntegerOverflow,
            JsonProvider jsonProvider) {

            super(mctxt,
                bigIntegerBoundaryNeg,
                bigIntegerBoundaryPos,
                bigIntegerOverflow);

            this.jsonProvider = jsonProvider;
        }
        
        /**
         * Create a new BigMathFactory JsonpValueFactory using the given MathContext.
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

            this(mctxt,
                bigIntegerBoundaryNeg,
                bigIntegerBoundaryPos,
                bigIntegerOverflow,
                JsonProvider.provider());
        }

        @Override
        public JsonNumber getNumber(NumberText text) {
            Number num = NumberBuilder.build(text, false, this);

            if (num instanceof Long) {
                return jsonProvider.createValue(num.longValue());

            } else if (num instanceof BigInteger) {
                return jsonProvider.createValue((BigInteger)num);

            } else if (num instanceof BigDecimal) {
                return jsonProvider.createValue((BigDecimal)num);

            } else {
                return jsonProvider.createValue(num.doubleValue());
            }
        }

        @Override
        public JsonProvider getJsonProvider() {
            return jsonProvider;
        }
    }

    /**
     * A {@link JsonpValueFactory} that uses
     * {@link java.math.BigDecimal BigDecimal} when necessary.
     */
    class BigDecimalFactory extends BigMathFactory {
        /** Empty string. */
        private static final String EMPTY = "";

        /**
         * Create a new BigDecimalFactory JsonpValueFactory.
         * @param mctxt a valid MathContext or null
         */
        public BigDecimalFactory(MathContext mctxt) {
            super(mctxt, EMPTY, EMPTY, null);
        }
        
        /**
         * Create a new BigDecimalFactory JsonpValueFactory.
         * @param mctxt a valid MathContext or null
         * @param jsonProvider a valid JsonProvider
         */
        public BigDecimalFactory(MathContext mctxt, JsonProvider jsonProvider) {
            super(mctxt, EMPTY, EMPTY, null, jsonProvider);
        }
        
        @Override
        public JsonNumber getNumber(NumberText text) {
            if (NumberBuilder.isLong(text)) {
                return jsonProvider.createValue(// NOPMD AccessorMethodGeneration
                    NumberBuilder.toLong(text));
            }
            
            return jsonProvider.createValue(// NOPMD AccessorMethodGeneration
                NumberBuilder.toBigDecimal(text, this));
        }
    }

    /**
     * A singleton instance of {@link JsonpValueFactory}.
     * 
     * <p>This factory uses
     * {@link org.jsonurl.text.NumberBuilder#build(boolean)
     * NumberBuilder.build(text,true)}
     * to parse JSON&#x2192;URL numbers.
     */
    JsonpValueFactory PRIMITIVE = new JsonpValueFactory() {
        /** My JsonProvider. */
        private JsonProvider jsonProvider = JsonProvider.provider();

        @Override
        public JsonNumber getNumber(NumberText text) {
            Number num = NumberBuilder.build(text, true);

            if (num instanceof Long) {
                return jsonProvider.createValue(num.longValue());

            } else {
                return jsonProvider.createValue(num.doubleValue());
            }
        }

        @Override
        public JsonProvider getJsonProvider() {
            return jsonProvider;
        }
    };

    /**
     * A singleton instance of {@link JsonpValueFactory}.
     * 
     * <p>This factory uses
     * {@link java.lang.Double#valueOf(String) Double.valueOf(text)}
     * to parse JSON&#x2192;URL numbers.
     */
    JsonpValueFactory DOUBLE = new JsonpValueFactory() {
        /** My JsonProvider. */
        private JsonProvider jsonProvider = JsonProvider.provider();

        @Override
        public JsonNumber getNumber(NumberText text) {
            return jsonProvider.createValue(Double.parseDouble(text.toString()));
        }

        @Override
        public JsonProvider getJsonProvider() {
            return jsonProvider;
        }
    };

    /**
     * Get the JsonProvider for this factory.
     * @return a valid instance of JsonProvider
     */
    JsonProvider getJsonProvider();

    @Override
    default JsonStructure getEmptyComposite() {
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    @Override
    default JsonValue getNull() {
        return JsonValue.NULL;
    }

    @Override
    default JsonArray newArray(JsonArrayBuilder builder) {
        return builder.build();
    }

    @Override
    default JsonObject newObject(JsonObjectBuilder builder) {
        return builder.build();
    }

    @Override
    default void add(JsonArrayBuilder dest, JsonValue obj) {
        dest.add(obj);
    }

    @Override
    default void put(JsonObjectBuilder dest, String key, JsonValue value) {
        dest.add(key, value);
    }

    @Override
    default JsonValue getTrue() {
        return JsonValue.TRUE;
    }

    @Override
    default JsonValue getFalse() {
        return JsonValue.FALSE;
    }

    @Override
    default JsonString getString(CharSequence text, int start, int stop) {
        return getJsonProvider().createValue(
            String.valueOf(text.subSequence(start, stop)));
    }

    @Override
    default JsonString getString(String text) {
        return getJsonProvider().createValue(text);
    }

    @Override
    default JsonArrayBuilder newArrayBuilder() {
        return getJsonProvider().createArrayBuilder();
    }

    @Override
    default JsonObjectBuilder newObjectBuilder() {
        return getJsonProvider().createObjectBuilder();
    }
}
