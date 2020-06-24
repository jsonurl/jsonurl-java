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
import org.jsonurl.Parser;

/**
 * A JSON&#x2192;URL parser which uses classes from the
 * {@link org.json.JSONObject org.json} package.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonUrlParser extends Parser.TransparentBuilder<
        Object,
        Object,
        JSONArray,
        JSONObject,
        Boolean,
        Number,
        Object,
        String> {

    /**
     * Instantiate a new Parser.
     *
     * <p>The returned parser uses the
     * {@link JsonOrgValueFactory#PRIMITIVE PRIMITIVE} factory.
     */
    public JsonUrlParser() {
        this(JsonOrgValueFactory.PRIMITIVE);
    }

    /**
     * Instantiate a new Parser.
     * @param factory a valid JsonOrgValueFactory
     * @see JsonOrgValueFactory#PRIMITIVE
     * @see JsonOrgValueFactory#DOUBLE
     * @see JsonOrgValueFactory#BIGMATH
     */
    public JsonUrlParser(JsonOrgValueFactory factory) {
        super(factory);
    }
}
