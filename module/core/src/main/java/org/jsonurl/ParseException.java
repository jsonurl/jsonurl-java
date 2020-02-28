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

public class ParseException extends RuntimeException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private int position;

    public ParseException(String msg) {
        this(msg,-1);
    }

    public ParseException(String msg, int position) {
        super(msg);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    protected String typeDescription() {
        return "parse error";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);

        sb.append("JSON->URL ")
            .append(typeDescription())
            .append(": ")
            .append(getMessage());

        int position = getPosition();
        if (position > -1) {
            sb.append(" at ").append(position);
        }

        return sb.toString();
    }
}
