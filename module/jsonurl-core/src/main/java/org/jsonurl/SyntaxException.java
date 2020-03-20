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
 * A syntax error in JSON->URL text. 
 */
public class SyntaxException extends ParseException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** ERR_MSG_BADCHAR. */
    static final String ERR_MSG_BADCHAR = "invalid character";

    /** ERR_MSG_BADQSTR. */
    static final String ERR_MSG_BADQSTR = "non-terminated string literal";

    /** ERR_MSG_BADPCTENC. */
    static final String ERR_MSG_BADPCTENC = "invalid percent-encoded sequence";

    /** ERR_MSG_BADUTF8. */
    static final String ERR_MSG_BADUTF8 = "invalid encoded UTF-8 sequence";

    /** ERR_MSG_NOTEXT. */
    static final String ERR_MSG_NOTEXT = "text missing";

    /** ERR_MSG_EXPECT_LITERAL. */
    static final String ERR_MSG_EXPECT_LITERAL = "expected literal value";

    /** ERR_MSG_EXPECT_ARRAY. */
    static final String ERR_MSG_EXPECT_ARRAY = "expected array";

    /** ERR_MSG_EXPECT_OBJECT. */
    static final String ERR_MSG_EXPECT_OBJECT = "expected object";

    /** ERR_MSG_EXPECT_STRUCTCHAR. */
    static final String ERR_MSG_EXPECT_STRUCTCHAR = "expected structural character";

    /** ERR_MSG_EXPECT_OBJVALUE. */
    static final String ERR_MSG_EXPECT_OBJVALUE = "expected object value";

    /** ERR_MSG_STILLOPEN. */
    static final String ERR_MSG_STILLOPEN = "unexpected end-of-input inside composite";

    /** ERR_MSG_EXTRACHARS. */
    static final String ERR_MSG_EXTRACHARS = "unexpected unexpected text after composite";

    /**
     * Create a new SyntaxException.
     * @param msg exception message
     */
    public SyntaxException(String msg) {
        super(msg);
    }

    /**
     * Create a new LimitException.
     * @param msg exception message
     * @param position position in input where the exception occurred.
     */
    public SyntaxException(String msg, int position) {
        super(msg, position);
    }

    @Override
    protected String typeDescription() {
        return "syntax error";
    }
}
