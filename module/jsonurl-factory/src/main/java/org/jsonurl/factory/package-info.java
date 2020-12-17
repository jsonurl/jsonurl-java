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
 * Defines generic value
 * {@link org.jsonurl.factory.ValueFactory factory} and
 * {@link org.jsonurl.factory.Parser parser} interfaces, and provides a parser
 * {@link org.jsonurl.factory.ValueFactoryParser implementation}
 * built on the {@link org.jsonurl.stream.JsonUrlIterator JsonUrlIterator}
 * interface.
 *
 * <p>If you need a parser bound to your own JSON object model API then
 * you may implement a new
 * {@link org.jsonurl.factory.ValueFactory ValueFactory}
 * and supply it to an
 * instance of
 * {@link org.jsonurl.factory.ValueFactoryParser ValueFactoryParser}.
 * This is already done for J2SE datatypes and implemented in
 * {@link org.jsonurl.j2se.JsonUrlParser JsonUrlParser}. Additionally, other
 * artifacts do this for other object model APIs such as
 * <a href="../jsonp/package-summary.html">JSR-374</a> and
 * <a href="../jsonorg/package-summary.html">json.org</a>.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
package org.jsonurl.factory;