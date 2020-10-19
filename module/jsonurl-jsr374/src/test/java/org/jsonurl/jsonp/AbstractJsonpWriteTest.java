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

import java.io.IOException;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.jsonurl.AbstractWriteTest;
import org.jsonurl.JsonTextBuilder;
import org.junit.jupiter.api.Nested;

/**
 * Unit test for writing JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
public abstract class AbstractJsonpWriteTest
        extends AbstractWriteTest<
            JsonValue,
            JsonStructure,
            JsonArrayBuilder,
            JsonArray,
            JsonObjectBuilder,
            JsonObject> {

    /**
     * JavaValueFactory.PRIMITIVE JavaValueWriteTest.
     */
    @Nested
    static class PrimitiveWriteTest extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.PRIMITIVE;
        }
        
    }

    /**
     * JavaValueFactory.DOUBLE JavaValueWriteTest.
     */
    @Nested
    static class DoubleWriteTest extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.DOUBLE;
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return new JsonpValueFactory.BigMathFactory(null, null, null, null);
        }
    }

    /**
     * JavaValueFactory.BIGMATH32 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest32 extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.BIGMATH32;
        }
    }

    /**
     * JavaValueFactory.BIGMATH64 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest64 extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.BIGMATH64;
        }
    }

    /**
     * JavaValueFactory.BIGMATH128 JavaValueWriteTest.
     */
    @Nested
    static class BigMathWriteTest128 extends AbstractJsonpWriteTest {

        @Override
        public JsonpValueFactory getFactory() {
            return JsonpValueFactory.BIGMATH128;
        }
    }

    @Override
    public <I,R> boolean write(
            JsonTextBuilder<I, R> dest,
            boolean skipNullValues,
            JsonValue value) throws IOException {

        return JsonUrlWriter.write(dest, skipNullValues, value);
    }
}
