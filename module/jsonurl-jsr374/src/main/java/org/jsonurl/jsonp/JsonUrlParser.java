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

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.jsonurl.Parser;

/**
 * A JSON&#x2192;URL parser which uses the JSR-374 interface.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonUrlParser extends Parser<
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
     * Instantiate a new Parser.
     */
    public JsonUrlParser() {
        this(JsonpValueFactory.PRIMITIVE);
    }

    /**
     * Instantiate a new Parser.
     *
     * @param factory a valid JsonOrgValueFactory
     */
    public JsonUrlParser(JsonpValueFactory factory) {
        super(factory);
    }
}
