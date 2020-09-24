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
    
    /**
     * Enumeration of messages.
     */
    public enum Message {
        /** Input has too many characters. */
        MSG_LIMIT_MAX_PARSE_CHARS("input has too many characters"),

        /** Input has too many values. */
        MSG_LIMIT_MAX_PARSE_VALUES("input has too many values"),
        
        /** Input nesting is too deep. */
        MSG_LIMIT_MAX_PARSE_DEPTH("input nesting is too deep"),

        /** Integer overflow. */
        MSG_LIMIT_INTEGER("integer overflow");
        
        private final String text;

        private Message(String text) {
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
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * My message.
     */
    private final Message message;

    /**
     * Create a new LimitException.
     * @param msg exception message
     */
    public LimitException(Message msg, String txt) {
        super(txt);
        this.message = msg;
    }

    /**
     * Create a new LimitException.
     * @param msg exception message
     * @param position position in input where the exception occurred.
     */
    public LimitException(Message msg, String text, int position) {
        super(text, position);
        this.message = msg;
    }
    
    /**
     * Create a new LimitException.
     * @param msg exception message
     */
    public LimitException(Message msg) {
        super(msg.getMessageText());
        this.message = msg;
    }

    /**
     * Create a new LimitException.
     * @param msg exception message
     * @param position position in input where the exception occurred.
     */
    public LimitException(Message msg, int position) {
        super(msg.getMessageText(), position);
        this.message = msg;
    }

    /**
     * Get this exception's message value.
     */
    public Message getMessageValue() {
        return this.message;
    }
}
