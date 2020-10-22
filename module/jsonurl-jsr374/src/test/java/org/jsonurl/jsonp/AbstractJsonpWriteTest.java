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

/**
 * Abstract base class for JsonUrlStringBuilder + JsonpValueFactory unit tests.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-09-01
 */
abstract class AbstractJsonpWriteTest
        extends AbstractWriteTest<
            JsonValue,
            JsonStructure,
            JsonArrayBuilder,
            JsonArray,
            JsonObjectBuilder,
            JsonObject> {

    @Override
    public <I,R> boolean write(
            JsonTextBuilder<I, R> dest,
            JsonValue value) throws IOException {

        return JsonUrlWriter.write(dest, value);
    }
}
