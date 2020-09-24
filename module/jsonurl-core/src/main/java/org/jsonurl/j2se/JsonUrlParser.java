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

package org.jsonurl.j2se;

import java.util.List;
import java.util.Map;
import org.jsonurl.ValueFactoryParser;

/**
 * A {@link org.jsonurl.Parser Parser} based on Java SE data types.
 * 
 * <p>No state is maintained globally other than the {@link JavaValueFactory}
 * given in the constructor, which can not be changed. So as single instance
 * of this class is thread-safe as long as you follow the guidance outlined
 * for {@link org.jsonurl.Parser}.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class JsonUrlParser extends ValueFactoryParser.TransparentBuilder<
    Object,
    Object,
    List<Object>,
    Map<String,Object>,
    Boolean,
    Number,
    Object,
    String> {

    /**
     * Instantiate a new Parser.
     */
    public JsonUrlParser() {
        this(JavaValueFactory.PRIMITIVE);
    }
    
    /**
     * Instantiate a new Parser.
     */
    public JsonUrlParser(JavaValueFactory factory) {
        super(factory);
    }
}
