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

package org.jsonurl.jsonorg;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.jsonurl.AbstractJsonApiWriteTest;
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.ValueFactoryParser;

/**
 * Unit test for writing JSON&#x2192;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonOrgWriteTest extends AbstractJsonApiWriteTest<
        Object,
        Object,
        JSONArray,
        JSONObject> {

    @Override
    public void write(JsonTextBuilder<?, ?> dest, Object value)
            throws IOException {
        JsonUrlWriter.write(dest, value);
    }

    @Override
    public JSONArray newArray(String text) {
        return new JSONArray(text);
    }

    @Override
    public JSONObject newObject(String text) {
        return new JSONObject(text);
    }

    @Override
    public ValueFactoryParser<
            Object,
            Object,
            ?,
            JSONArray,
            ?,
            JSONObject, ?, ?, ?, ?> newParser() {

        return new JsonUrlParser(JsonOrgValueFactory.PRIMITIVE);
    }

    @Override
    public String valueToString(Object value) {
        return JSONWriter.valueToString(value);
    }
}
