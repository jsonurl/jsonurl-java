package org.jsonurl;

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
 * A exception that occurs when a limit is exceeded.
 */
public class LimitException extends ParseException {

    @SuppressWarnings("PMD")
    static final String ERR_MSG_LIMIT_MAX_PARSE_CHARS =
            "input has too many characters";

    @SuppressWarnings("PMD")
    static final String ERR_MSG_LIMIT_MAX_PARSE_VALUES =
            "input has too many values";

    @SuppressWarnings("PMD")
    static final String ERR_MSG_LIMIT_MAX_PARSE_DEPTH =
            "input nesting is too deep";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new LimitException.
     * @param msg exception message
     */
    public LimitException(String msg) {
        super(msg);
    }

    /**
     * Create a new LimitException.
     * @param msg exception message
     * @param position position in input where the exception occurred.
     */
    public LimitException(String msg, int position) {
        super(msg, position);
    }
}
