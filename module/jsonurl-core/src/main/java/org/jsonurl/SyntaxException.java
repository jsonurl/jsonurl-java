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
 * A syntax error in JSON&#x2192;URL text. 
 */
public class SyntaxException extends ParseException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Enumeration of messages.
     */
    public enum Message {
        /** Invalid character. */
        MSG_BAD_CHAR("invalid character"),
        /** Non-terminated string literal. */
        MSG_BAD_QSTR("non-terminated string literal"),
        /** Invalid percent-encoded sequence. */
        MSG_BAD_PCT_ENC("invalid percent-encoded sequence"),
        /** Invalid encoded UTF-8 sequence. */
        MSG_BAD_UTF8("invalid encoded UTF-8 sequence"),
        /** Text missing. */
        MSG_NO_TEXT("text missing"),
        /** Expected literal value. */
        MSG_EXPECT_LITERAL("expected literal value"),
        /** Expected array. */
        MSG_EXPECT_ARRAY("expected array"),
        /** Expected object. */
        MSG_EXPECT_OBJECT("expected object"),
        /** Expected type. */
        MSG_EXPECT_TYPE("expected type"),
        /** Expected structural character. */
        MSG_EXPECT_STRUCT_CHAR("expected structural character"),
        /** Expected object value. */
        MSG_EXPECT_OBJECT_VALUE("expected object value"),
        /** Unexpected array. */
        MSG_UNEXPECTED_ARRAY("unexpected array"),
        /** Unexpected boolean. */
        MSG_UNEXPECTED_BOOLEAN("unexpected boolean"),
        /** Unexpected empty composite. */
        MSG_UNEXPECTED_EMPTY_COMPOSITE("unexpected empty composite"),
        /** Unexpected null. */
        MSG_UNEXPECTED_NULL("unexpected null"),
        /** Unexpected number. */
        MSG_UNEXPECTED_NUMBER("unexpected number"),
        /** Unexpected object. */
        MSG_UNEXPECTED_OBJECT("unexpected object"),
        /** Unexpected string. */
        MSG_UNEXPECTED_STRING("unexpected string"),
        /** Unexpected end-of-input inside composite. */
        MSG_STILL_OPEN("unexpected end-of-input inside composite"),
        /** Unexpected text after composite. */
        MSG_EXTRA_CHARS("unexpected text after composite");

        private final String text;

        Message(String text) {
            this.text = text;
        }

        /**
         * Get the message text.
         */
        public String getMessageText() {
            return text;
        }
    }

    /**
     * My message.
     */
    private final Message message; // NOPMD - not a bean

    /**
     * Create a new SyntaxException.
     * @param msg exception message
     */
    public SyntaxException(Message msg) {
        super(msg.getMessageText());
        this.message = msg;
    }

    /**
     * Create a new SyntaxException.
     * @param msg exception message
     * @param position position in input where the exception occurred.
     */
    public SyntaxException(Message msg, int position) {
        super(msg.getMessageText(), position);
        this.message = msg;
    }

    /**
     * Create a new SyntaxException.
     * @param msg exception message
     * @param position position in input where the exception occurred.
     * @param text exception message text
     */
    public SyntaxException(Message msg, String text, int position) {
        super(text, position);
        this.message = msg;
    }

    /**
     * Create a new SyntaxException.
     * @param msg exception message
     * @param text exception message text
     */
    public SyntaxException(Message msg, String text) {
        super(text);
        this.message = msg;
    }

    @Override
    protected String typeDescription() {
        return "syntax error";
    }

    /**
     * Get this exception's message value.
     */
    public Message getMessageValue() {
        return this.message;
    }
}
