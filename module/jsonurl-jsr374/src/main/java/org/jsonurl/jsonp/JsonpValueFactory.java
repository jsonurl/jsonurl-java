package org.jsonurl.jsonp;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.jsonurl.NumberBuilder;
import org.jsonurl.NumberText;
import org.jsonurl.ValueFactory;
import org.jsonurl.ValueType;

/**
 * A JSON&#x2192;URL ValueFactory which uses the JSR-374 JSONP interface. 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public abstract class JsonpValueFactory implements ValueFactory<
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
     * A singleton instance of {@link JsonpValueFactory}.
     * 
     * <p>This factory uses
     * {@link org.jsonurl.NumberBuilder#build(boolean)
     * NumberBuilder.build(text,true)}
     * to parse JSON&#x2192;URL numbers.
     */
    public static final JsonpValueFactory PRIMITIVE = new JsonpValueFactory() {
        @Override
        public JsonNumber getNumber(NumberText text) {
            Number m = NumberBuilder.build(text, true);

            if (m instanceof Long) {
                return Json.createValue(m.longValue());

            } else {
                return Json.createValue(m.doubleValue());
            }
        }
    };
    
    /**
     * A singleton instance of {@link JsonpValueFactory}.
     * 
     * <p>This factory uses
     * {@link java.lang.Double#valueOf(String) Double.valueOf(text)}
     * to parse JSON&#x2192;URL numbers.
     */
    public static final JsonpValueFactory DOUBLE = new JsonpValueFactory() {
        @Override
        public JsonNumber getNumber(NumberText text) {
            return Json.createValue(Double.parseDouble(text.toString()));
        }
    };

    /**
     * A singleton instance of {@link JsonpValueFactory}.
     * 
     * <p>This factory uses
     * {@link java.math.BigInteger#BigInteger(String) new BigInteger()} or 
     * {@link java.math.BigDecimal#BigDecimal(String) new BigDecimal()}
     * to parse JSON&#x2192;URL numbers.
     */
    public static final JsonpValueFactory BIGMATH = new JsonpValueFactory() {
        @Override
        public JsonNumber getNumber(NumberText text) {
            if (!text.hasFractionalPart()) {
                switch (text.getExponentType()) { // NOPMD - don't want default
                case NONE:
                    return Json.createValue(new BigInteger(text.toString()));

                case JUST_VALUE:
                case POSITIVE_VALUE:
                    BigDecimal d = new BigDecimal(text.toString());
                    return Json.createValue(d.toBigIntegerExact());

                case NEGATIVE_VALUE:
                    break;
                }
            }

            return Json.createValue(new BigDecimal(text.toString()));
        }
    };

    @Override
    public JsonStructure getEmptyComposite() {
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    @Override
    public JsonValue getNull() {
        return JsonValue.NULL;
    }

    @Override
    public JsonArray newArray(JsonArrayBuilder builder) {
        return builder.build();
    }

    @Override
    public JsonObject newObject(JsonObjectBuilder builder) {
        return builder.build();
    }

    @Override
    public void add(JsonArrayBuilder dest, JsonValue obj) {
        dest.add(obj);
    }

    @Override
    public void put(JsonObjectBuilder dest, String key, JsonValue value) {
        dest.add(key, value);
    }

    @Override
    public JsonValue getTrue() {
        return JsonValue.TRUE;
    }

    @Override
    public JsonValue getFalse() {
        return JsonValue.FALSE;
    }

    @Override
    public JsonString getString(CharSequence s, int start, int stop) {
        return Json.createValue(String.valueOf(s.subSequence(start, stop)));
    }

    @Override
    public JsonString getString(String s) {
        return Json.createValue(s);
    }

    @Override
    public JsonArrayBuilder newArrayBuilder() {
        return Json.createArrayBuilder();
    }

    @Override
    public JsonObjectBuilder newObjectBuilder() {
        return Json.createObjectBuilder();
    }
    
    @Override
    public boolean isValid(Set<ValueType> types, JsonValue value) {
        if (value instanceof JsonString) {
            return types.contains(ValueType.STRING);
        }
        if (value instanceof JsonNumber) {
            return types.contains(ValueType.NUMBER);
        }
        if (value == JsonValue.FALSE || value == JsonValue.TRUE) {
            return types.contains(ValueType.BOOLEAN);
        }
        if (value instanceof JsonArray) {
            return types.contains(ValueType.ARRAY);
        }
        if (value instanceof JsonObject) {
            return types.contains(ValueType.OBJECT);
        }
        if (JsonValue.NULL == value) {
            return types.contains(ValueType.NULL);
        }
        if (isEmpty(value)) {
            return types.contains(ValueType.OBJECT)
                || types.contains(ValueType.ARRAY);
        }
        return false;
    }
}
