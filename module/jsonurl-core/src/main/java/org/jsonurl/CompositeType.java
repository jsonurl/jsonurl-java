/*
 * Copyright 2020 David MacCormack
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

package org.jsonurl;

/**
 * An enumeration of JSON&#x2192;URL composite types.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public enum CompositeType {
    /**
     * JSON&#x2192;URL array.
     */
    ARRAY(ValueType.ARRAY),
    
    /**
     * JSON&#x2192;URL object.
     */
    OBJECT(ValueType.OBJECT);

    /**
     * ValueType.
     */
    private ValueType vtype;

    /**
     * Create a new CompositeType.
     */
    CompositeType(ValueType vtype) {
        this.vtype = vtype;
    }

    /**
     * Get the {@link ValueType} for this CompositeType.
     */
    public ValueType getValueType() {
        return vtype;
    }
}