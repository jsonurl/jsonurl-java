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
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.factory.AbstractWriteTest;
import org.jsonurl.text.JsonStringBuilder;
import org.jsonurl.text.JsonTextBuilder;

/**
 * Abstract base class for JsonUrlStringBuilder + JsonOrgValueFactory unit tests.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
abstract class AbstractJsonOrgWriteTest
        extends AbstractWriteTest<
            Object,
            Object,
            JSONArray,
            JSONArray,
            JSONObject,
            JSONObject> {

    @Override
    protected <R> boolean write(
            JsonTextBuilder<R> dest,
            Object value) throws IOException {

        return JsonUrlWriter.write(dest, value);
    }

    @Override
    protected JsonStringBuilder newJsonStringBuilder(
            Set<JsonUrlOption> options,
            CompositeType impliedType) {

        return new JsonUrlStringBuilder(impliedType, options);
    }
}
