/*
 * Copyright 2020 David MacCormack
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
 * An instance of this interface can create parse, limit, and syntax
 * exceptions with parser-specific position information.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public interface ExceptionProvider {

    /**
     * Create a new SyntaxException for the current parse position.
     * @param msg the exception message
     * @param text the exception text
     */
    SyntaxException newSyntaxException(
            SyntaxException.Message msg,
            String text);

    /**
     * Create a new SyntaxException for the current parse position.
     * This simply calls {@link
     * #newSyntaxException(org.jsonurl.SyntaxException.Message, String)
     * newSyntaxException(msg, msg.getMessageText())}.
     *
     * @see SyntaxException.Message#getMessageText()
     */
    default SyntaxException newSyntaxException(SyntaxException.Message msg) {
        return newSyntaxException(msg, msg.getMessageText());
    }

    /**
     * Create a new LimitException for the current parse position.
     * @param msg the exception message
     * @param text the exception text
     */
    LimitException newLimitException(
            LimitException.Message msg,
            String text);

    /**
     * Create a new LimitException for the current parse position.
     * This simply calls {@link
     * #newLimitException(org.jsonurl.LimitException.Message, String)
     * newLimitException(msg, msg.getMessageText())}.
     *
     * @see LimitException.Message#getMessageText()
     */
    default LimitException newLimitException(LimitException.Message msg) {
        return newLimitException(msg, msg.getMessageText());
    }

    /**
     * Create a new ParseException for the current parse position.
     * @param text the exception text
     */
    ParseException newParseException(String text);

}