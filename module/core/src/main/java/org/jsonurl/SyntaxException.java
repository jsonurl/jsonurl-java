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

public class SyntaxException extends ParseException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    static final String ERR_MSG_BADCHAR =
            "invalid character";
    static final String ERR_MSG_BADQSTR =
            "non-terminated string literal";
    static final String ERR_MSG_BADPCTENC =
            "invalid percent-encoded sequence";
    static final String ERR_MSG_BADUTF8 =
            "invalid encoded UTF-8 sequence";
    static final String ERR_MSG_NOTEXT =
            "text missing";
    static final String ERR_MSG_EXPECT_LITERAL =
            "expected literal value";
    static final String ERR_MSG_EXPECT_ARRAY =
            "expected array";
    static final String ERR_MSG_EXPECT_OBJECT =
            "expected object";
    static final String ERR_MSG_EXPECT_STRUCTCHAR =
            "expected structural character";
    static final String ERR_MSG_EXPECT_OBJVALUE =
            "expected object value";
    static final String ERR_MSG_STILLOPEN =
            "unexpected end-of-input inside composite";
    static final String ERR_MSG_EXTRACHARS =
            "unexpected unexpected text after composite";


    public SyntaxException(String msg) {
        super(msg);
    }
    
    public SyntaxException(String msg, int position) {
        super(msg, position);
    }

    protected String typeDescription() {
        return "syntax error";
    }
}
