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
 * A functional interface that provides a value for the given key. This may
 * be used with an implied-object or wfu-implied-object that allows missing,
 * top-level values.
 * @param <V> the return type
 */
@FunctionalInterface
public interface MissingValueProvider<V> {
    /**
     * Provides a value for the given key.
     * This function may also throw a or throws a {@link ParseException} (or derivative)
     * if it can not provide a value.
     *
     * @param key a valid key
     * @param pos current parse position
     * @return a valid value
     */
    V getValue(String key, int pos);
}