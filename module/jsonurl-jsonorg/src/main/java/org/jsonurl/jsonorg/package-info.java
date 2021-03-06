
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
 * A <a href="https://jsonurl.org/">JSON&#x2192;URL</a> parser and
 * text generator based on Douglas Crockford's original Java implementation
 * of JSON.
 *
 * <p>The primary entry point for most use cases is
 * {@link org.jsonurl.jsonorg.JsonUrlParser JsonUrlParser}, which parses
 * JSON&#x2192;URL text as instances of
 * {@link org.json.JSONObject JSONObject}
 * and
 * {@link org.json.JSONArray JSONArray}.
 *
 * <p>JSON&#x2192;URL text may be generated for instances of
 * {@link org.json.JSONObject JSONObject}
 * and
 * {@link org.json.JSONArray JSONArray}
 * via
 * {@link org.jsonurl.jsonorg.JsonUrlStringBuilder JsonUrlStringBuilder}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 * @see <a href="https://www.json.org/">json.org</a>
 * @see <a href="https://github.com/stleary/JSON-java">GitHub</a>
 * @see <a href="https://mvnrepository.com/artifact/org.json/json">Maven</a>
 */
package org.jsonurl.jsonorg;
