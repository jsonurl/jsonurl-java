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

package org.jsonurl.text;

import static org.jsonurl.text.CharUtil.APOS;
import static org.jsonurl.text.CharUtil.CHARBITS;
import static org.jsonurl.text.CharUtil.CHARBITS_LENGTH;
import static org.jsonurl.text.CharUtil.IS_ANY_STRSAFE;
import static org.jsonurl.text.CharUtil.IS_NSTRSAFE;
import static org.jsonurl.text.CharUtil.IS_QSTRSAFE;
import static org.jsonurl.text.CharUtil.IS_SPACE;

/**
 * Enumeration of string serialization strategies based on what characters
 * are in the string.
 */
enum StringStrategy {
    SAFE_ASIS,
    NO_QUOTE_WITH_SPACE,
    QUOTE_NO_SPACE,
    QUOTE_WITH_SPACE,
    FULL_ENCODING;

    /**
     * Get the StringEncoding value for the given text.
     * @param text a valid CharSequence
     * @param start first index
     * @param end last index
     */
    @SuppressWarnings({
        "PMD.CyclomaticComplexity",
        "PMD.DataflowAnomalyAnalysis"}) // maintain state across for loop
    public static StringStrategy getStringEncoding(
            CharSequence text,
            int start,
            int end) {

        if (text.charAt(start) == APOS) {
            //
            // edge case: if the string starts with a literal quote then it
            // must be percent encoded because a parser could not otherwise
            // tell the difference between that and a quoted string.
            //
            return StringStrategy.FULL_ENCODING;
        }

        final int strmask = IS_ANY_STRSAFE;
        final int spacemask = IS_SPACE;
        int strbits = strmask;
        int spacebits = 0;

        for (int i = start; i < end; i++) {
            char chr = text.charAt(i);
            if (chr > CHARBITS_LENGTH) {
                return StringStrategy.FULL_ENCODING;
            }

            //
            // track space and non-space values with separate masks.
            // CharUtil.CHARBITS[' '] is specifically setup to allow this.
            // If you change this code be sure to update that value as
            // necessary.
            //
            strbits &= CHARBITS[chr] & strmask;
            spacebits |= CHARBITS[chr] & spacemask;
        }

        switch (strbits | spacebits) {
        case IS_ANY_STRSAFE:
        case IS_NSTRSAFE:
            return StringStrategy.SAFE_ASIS;

        case IS_ANY_STRSAFE | IS_SPACE:
        case IS_NSTRSAFE | IS_SPACE:
            return StringStrategy.NO_QUOTE_WITH_SPACE;

        case IS_QSTRSAFE:
            return StringStrategy.QUOTE_NO_SPACE;

        case IS_QSTRSAFE | IS_SPACE:
            return StringStrategy.QUOTE_WITH_SPACE;

        default:
            // as currently written this will never happen
            return StringStrategy.FULL_ENCODING;
        }
    }
}