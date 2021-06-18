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

package org.jsonurl;

/**
 * An exception that occurs while parsing JSON&#x2192;URL text.
 */
public class ParseException extends RuntimeException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * the offset in the input text.
     */
    private final long offset;

    /**
     * the line number.
     */
    private final int line;

    /**
     * the column number.
     */
    private final int column;

    /**
     * Create a new ParseException.
     * @param msg exception text
     */
    public ParseException(String msg) {
        this(msg, -1, -1, -1);
    }

    /**
     * Create a new ParseException.
     * @param msg exception text
     * @param offset the offset in the input text
     */
    public ParseException(String msg, long offset) {
        this(msg, offset, -1, -1);
    }

    /**
     * Create a new ParseException.
     * @param msg exception text
     * @param line the line in the input text
     * @param column the column in the input text
     */
    public ParseException(String msg, int line, int column) {
        this(msg, -1, line, column);
    }

    /**
     * Create a new ParseException.
     * @param msg exception text
     * @param offset the offset in the input text
     * @param line the line in the input text
     * @param column the column in the input text
     */
    private ParseException(String msg, long offset, int line, int column) {
        super(msg);
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    /**
     * Get the offset in the text where the exception occurred.
     * @return a valid offset or -1.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Get the line in the text where the exception occurred.
     * @return a valid line or -1.
     */
    public int getLineNumber() {
        return line;
    }

    /**
     * Get the column in the text where the exception occurred.
     * @return a valid column or -1.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Exception type description.
     */
    @SuppressWarnings(
        // false positive; It's overridden by subclasses
        "java:S3400")
    protected String typeDescription() {
        return "parse error";
    }

    @Override
    @SuppressWarnings("java:S1117") // See SuppressWarnings.md
    public String toString() {
        StringBuilder buf = new StringBuilder(64);

        buf.append("jsonurl ")
            .append(typeDescription())
            .append(": ")
            .append(getMessage());

        int line = getLineNumber();
        if (line > -1) {
            buf.append(" at ").append(line);
            
            int col = getColumn();
            if (col > -1) {
                buf.append(':').append(col);
            }
        } else {
            long off = getOffset();
            if (off > -1) {
                buf.append(" at ").append(off);
            }            
        }

        return buf.toString();
    }
}
