/*
 * Copyright 2019-2020 David MacCormack
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

import java.math.MathContext;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.jsonurl.AbstractParseTest;
import org.jsonurl.BigMathProvider;
import org.jsonurl.BigMathProvider.BigIntegerOverflow;
import org.jsonurl.ValueFactory;
import org.junit.jupiter.api.Nested;


/**
 * Abstract base class for parser tests.
 *
 * <p>A specialization of this class may be created for each
 * factory constant defined in {@link JsonOrgValueFactory}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
abstract class AbstractJsonpParseTest extends AbstractParseTest<
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
     * Unit test using JsonpValueFactory.PRIMITIVE.
     */
    @Nested
    static class PrimitiveParseTest extends AbstractJsonpParseTest {
        /**
         * Create a new PrimitiveParseTest.
         */
        public PrimitiveParseTest() {
            super(JsonpValueFactory.PRIMITIVE);
        }
    }
    
    /**
     * Unit test using JsonpValueFactory.DOUBLE.
     */
    @Nested
    static class DoubleParseTest extends AbstractJsonpParseTest {
        /**
         * Create a new DoubleParseTest.
         */
        public DoubleParseTest() {
            super(JsonpValueFactory.DOUBLE);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BigMathFactory.
     *
     * @author jsonurl.org
     * @author David MacCormack
     * @since 2019-09-01
     */
    @Nested
    static class BigMathParseTest extends AbstractJsonpParseTest {

        /**
         * Create a new BigMathParseTest.
         */
        BigMathParseTest() {
            super(new JsonpValueFactory.BigMathFactory(null, null, null, null));
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH32.
     */
    @Nested
    static final class BigMathParseTest32 extends AbstractJsonpParseTest {
        /**
         * Create a new BigMathParseTest32.
         */
        BigMathParseTest32() {
            super(JsonpValueFactory.BIGMATH32);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH64.
     */
    @Nested
    static final class BigMathParseTest64 extends AbstractJsonpParseTest {
        /**
         * Create a new BigMathParseTest64.
         */
        BigMathParseTest64() {
            super(JsonpValueFactory.BIGMATH64);
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH128.
     */
    @Nested
    static final class BigMathParseTest128 extends AbstractJsonpParseTest {
        /**
         * Create a new BigMathParseTest128.
         */
        BigMathParseTest128() {
            super(JsonpValueFactory.BIGMATH128);
        }
    }
    
    /**
     * Unit test using JsonpValueFactory.BIGMATH32.
     */
    static final class BigDecimalParseTest32 extends AbstractJsonpParseTest {
        /**
         * Create a new BigDecimalParseTest32.
         */
        BigDecimalParseTest32() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL32,
                BigMathProvider.BIG_INTEGER32_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER32_BOUNDARY_POS,
                null));
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH64.
     */
    static final class BigDecimalParseTest64 extends AbstractJsonpParseTest {
        /**
         * Create a new BigDecimalParseTest64.
         */
        BigDecimalParseTest64() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL64,
                BigMathProvider.BIG_INTEGER64_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER64_BOUNDARY_POS,
                null));
        }
    }

    /**
     * Unit test using JsonpValueFactory.BIGMATH128.
     */
    static final class BigDecimalParseTest128 extends AbstractJsonpParseTest {

        /**
         * Create a new BigDecimalParseTest128.
         */
        BigDecimalParseTest128() {
            super(new JsonpValueFactory.BigMathFactory(
                MathContext.DECIMAL128,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_NEG,
                BigMathProvider.BIG_INTEGER128_BOUNDARY_POS,
                null));
        }
    }

    /**
     * Create a new JsonpParseTest.
     */
    public AbstractJsonpParseTest(JsonpValueFactory factory) {
        super(factory);
    }
    
    @Override
    protected JsonArray getArray(String key, JsonObject value) {
        return (JsonArray)value.get(key);
    }

    @Override
    protected JsonArray getArray(int index, JsonArray value) {
        return (JsonArray)value.get(index);
    }

    @Override
    protected JsonObject getObject(int index, JsonArray value) {
        return (JsonObject)value.get(index);
    }
    
    @Override
    protected JsonObject getObject(String key, JsonObject value) {
        return (JsonObject)value.get(key);
    }

    @Override
    protected JsonNumber getNumber(int index, JsonArray value) {
        return (JsonNumber)value.get(index);
    }
    
    @Override
    protected JsonNumber getNumber(String key, JsonObject value) {
        return (JsonNumber)value.get(key);
    }

    @Override
    protected JsonString getString(int index, JsonArray value) {
        return (JsonString)value.get(index);
    }

    @Override
    protected JsonString getString(String key, JsonObject value) {
        return (JsonString)value.get(key);
    }
    
    @Override
    protected boolean getBoolean(String key, JsonObject value) {
        Object ret = value.get(key);

        if (ret == factory.getTrue()) {
            return true;
        }

        if (ret == factory.getFalse()) {
            return false;
        }

        throw new IllegalArgumentException("value not boolean: " + ret);
    }
    
    @Override
    protected boolean getNull(String key, JsonObject value) {
        Object ret = value.get(key);
        return factory.getNull() == ret;
    }
    
    @Override
    protected boolean getEmptyComposite(String key, JsonObject value) {
        return factory.isEmptyComposite(value.get(key));
    }

    @Override
    protected boolean getEmptyComposite(int index, JsonArray value) {
        return factory.isEmptyComposite(value.get(index));
    }

    @Override
    protected Number getNumberValue(JsonValue value) {
        return value instanceof JsonNumber ? ((JsonNumber)value).numberValue() : null;
    }

    @Override
    protected String getStringValue(JsonValue value) {
        return value instanceof JsonString ? ((JsonString)value).getString() : null;
    }

    @Override
    protected ValueFactory<
            JsonValue,
            JsonStructure,
            JsonArrayBuilder,
            JsonArray,
            JsonObjectBuilder,
            JsonObject,
            JsonValue,
            JsonNumber,
            JsonValue,
            JsonString> newBigMathFactory(
                MathContext mctxt,
                String boundNeg,
                String boundPos,
                BigIntegerOverflow over) {
        return new JsonpValueFactory.BigMathFactory(
            mctxt, boundNeg, boundPos, over);
    }

    @Override
    protected boolean isBigIntegerOverflowInfinityOK() {
        return false;
    }
}
