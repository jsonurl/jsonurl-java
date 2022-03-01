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

/**
 * Base implementation of {@link JsonUrlLimits}.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
class Limits implements JsonUrlLimits { // NOPMD - DataClass

    /**
     * Maximum parse depth.
     */
    private int maxParseDepth = DEFAULT_MAX_PARSE_DEPTH;

    /**
     * Maximum number of parsed chars.
     */
    private long maxParseChars = DEFAULT_MAX_PARSE_CHARS;

    /**
     * Maximum number of parsed values.
     */
    private int maxParseValues = DEFAULT_MAX_PARSE_VALUES;

    @Override
    public long getMaxParseChars() {
        return this.maxParseChars;
    }

    /**
     * Set the maximum number of parsed characters.
     * @see #getMaxParseChars()
     */
    public void setMaxParseChars(long maxParseChars) {
        this.maxParseChars = maxParseChars;
    }

    @Override
    public int getMaxParseDepth() {
        return this.maxParseDepth;
    }

    /**
     * Set the maximum parse depth.
     * @see #getMaxParseDepth()
     */
    public void setMaxParseDepth(int maxParseDepth) {
        this.maxParseDepth = maxParseDepth;
    }

    @Override
    public int getMaxParseValues() {
        return this.maxParseValues;
    }

    /**
     * Set the maximum number of parsed values.
     * @see #getMaxParseValues()
     */
    public void setMaxParseValues(int maxParseValues) {
        this.maxParseValues = maxParseValues;
    }

    @Override
    public int hashCode() {
        final int prime = 31; // NOPMD - AvoidFinalLocalVariable
        int result = 1;
        result = prime * result + (int) (maxParseChars ^ (maxParseChars >>> 32));
        result = prime * result + maxParseDepth;
        result = prime * result + maxParseValues;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JsonUrlLimits
                && equals(this, (JsonUrlLimits)obj);
    }

    /**
     * Test two limits for equality.
     * @param lima a valid JsonUrlLimits or {@code null}
     * @param limb a valid JsonUrlLimits or {@code null}
     */
    public static boolean equals(JsonUrlLimits lima, JsonUrlLimits limb) {
        return lima == limb // NOPMD - false positive
                || lima.getMaxParseChars() == limb.getMaxParseChars()
                && lima.getMaxParseDepth() == limb.getMaxParseDepth()
                && lima.getMaxParseValues() == limb.getMaxParseValues();
    }
}
