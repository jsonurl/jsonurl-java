/**
 * An implementation of the JSON&#x2192;URL interface based on the JSON-P
 * interface defined by JSR-374.
 * 
 * <p>The primary entry point for most use cases is
 * {@link org.jsonurl.jsonp.JsonUrlParser JsonUrlParser}, which parses
 * JSON&#x2192;URL text into instances of {@link javax.json.JsonObject
 * JsonObject} and {@link javax.json.JsonArray JsonArray}.
 *
 * <p>{@link org.jsonurl.jsonp.JsonUrlWriter JsonUrlWriter} has static methods
 * for creating JSON&#x2192;URL text from instances of
 * {@link javax.json.JsonObject JsonObject} and
 * {@link javax.json.JsonArray JsonArray}.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 * @see <a href="https://javaee.github.io/jsonp/">About</a>
 * @see <a href="https://github.com/javaee/jsonp/">GitHub</a>
 * @see <a href=
 * "https://mvnrepository.com/artifact/org.glassfish/javax.json">Maven</a>
 */
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
