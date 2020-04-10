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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.jsonurl.AbstractWriteTest;
import org.jsonurl.JsonTextBuilder;
import org.jsonurl.Parser;

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

/**
 * Unit test for writing JSON-&gt;URL text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonOrgWriteTest extends AbstractWriteTest<
        Object,
        Object,
        JSONArray,
        JSONObject> {

    @Override
    public void write(JsonTextBuilder<?, ?> out, Object value) throws Exception {
        JsonUrlWriter.write(out, value);
    }

    @Override
    public JSONArray newArray(String s) {
        return new JSONArray(s);
    }

    @Override
    public JSONObject newObject(String s) {
        return new JSONObject(s);
    }

    @Override
    public Parser<
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
