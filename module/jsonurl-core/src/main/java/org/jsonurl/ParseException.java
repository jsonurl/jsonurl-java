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
 * An exception that occurs while parsing JSON-&gt;URL text.
 */
public class ParseException extends RuntimeException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * the position in the input text.
     */
    private final int position;

    /**
     * Create a new ParseException.
     * @param msg exception text
     */
    public ParseException(String msg) {
        this(msg,-1);
    }

    /**
     * Create a new ParseException.
     * @param msg exception text
     * @param position the position in the input text
     */
    public ParseException(String msg, int position) {
        super(msg);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    /**
     * Exception type description.
     */
    protected String typeDescription() {
        return "parse error";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);

        sb.append("JSON-&gt;URL ")
            .append(typeDescription())
            .append(": ")
            .append(getMessage());

        int p = getPosition();
        if (p > -1) {
            sb.append(" at ").append(p);
        }

        return sb.toString();
    }
}
