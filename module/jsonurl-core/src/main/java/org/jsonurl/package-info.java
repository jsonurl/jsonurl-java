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
 * This package provides an extensible JSON&#x2192;URL parser core and
 * static utility methods.
 *
 * <p>If you need a parser bound to your own JSON model API then
 * a {@link org.jsonurl.ValueFactory ValueFactory} may be created and
 * supplied to an instance of {@link org.jsonurl.Parser Parser}. Otherwise,
 * if a parser based on J2SE datatypes is sufficient then
 * take a look at {@link org.jsonurl.j2se.JsonUrlParser JsonUrlParser} which
 * does just that. 
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
package org.jsonurl;
