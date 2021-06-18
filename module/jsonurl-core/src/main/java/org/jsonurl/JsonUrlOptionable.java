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


package org.jsonurl;

import java.util.Set;

/**
 * An instance of this interface is something that can provide
 * JsonUrlOptions.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public interface JsonUrlOptionable {
    
    /**
     * Get the set of options for this object.
     * @return a valid Set or {@code null}.
     */
    Set<JsonUrlOption> options();

    /**
     * Get the options from the given object, if possible.
     */
    @SuppressWarnings("java:S1168") // See SuppressWarnings.md - !API!
    static Set<JsonUrlOption> getJsonUrlOptions(Object obj) {
        if (obj instanceof JsonUrlOptionable) {
            return ((JsonUrlOptionable)obj).options();
        }
        return null;
    }
}
