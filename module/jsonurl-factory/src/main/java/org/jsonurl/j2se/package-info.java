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
 * text generator based J2SE datatypes.
 *
 * <p>The primary entry point for most use cases is
 * {@link org.jsonurl.j2se.JsonUrlParser JsonUrlParser}, which parses
 * JSON&#x2192;URL text as instances of Java SE types (e.g.
 * {@link java.util.Map Map}, {@link java.util.List List}, etc).
 *
 * <p>JSON&#x2192;URL text may be generated for
 * {@link java.util.Map Maps}
 * and
 * {@link java.util.List Lists}
 * via
 * {@link org.jsonurl.j2se.JsonUrlStringBuilder JsonUrlStringBuilder}.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
package org.jsonurl.j2se;
