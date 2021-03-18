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

package org.jsonurl.stream;

import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_TYPE;

import java.util.Set;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.JsonUrlOptionable;
import org.jsonurl.ValueType;

/**
 * Provides the boilerplate for JsonUrlIterator implementations.
 *
 * <p>This class houses options and limits, and provides some type checking
 * methods.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public abstract class AbstractEventIterator
        implements JsonUrlIterator, JsonUrlOptionable { // NOPMD

    /**
     * x-www-form-urlencoded name-separator (equals).
     */
    public static final char WFU_NAME_SEPARATOR = '=';

    /**
     * x-www-form-urlencoded value-separator (ampersand).
     */
    public static final char WFU_VALUE_SEPARATOR = '&';

    /**
     * x-www-form-urlencoded space (plus).
     */
    public static final char WFU_SPACE = '+';

    /**
     * JsonUrlOptions.
     */
    private final Set<JsonUrlOption> options; // NOPMD - final field

    /**
     * JsonUrlLimits.
     */
    private final JsonUrlLimits limits; // NOPMD - final field

    /**
     * The input text.
     */
    protected final CharIterator text;
    
    /**
     * Result type or null.
     */
    private Set<ValueType> resultType;

    /**
     * True if the result has been validated against resultType.
     */
    private boolean doneTypeCheck;

    /**
     * Construct a new AbstractIterator.
     * @param text input text
     */
    public AbstractEventIterator(
            CharIterator text,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {

        this.text = text;
        this.options = options;
        this.limits = limits;
    }

    @Override
    public void setType(Set<ValueType> resultType) {
        this.resultType = resultType;
    }

    @Override
    public Set<JsonUrlOption> options() {
        return this.options;
    }

    /**
     * Get the JsonUrlLimits.
     */
    public JsonUrlLimits limits() {
        return this.limits;
    }

    /**
     * Test the given string against the JSON literals {@code true},
     * {@code false}, and {@code null}.
     *
     * @param text a valid CharSequence
     * @return a valid JsonUrlEvent or {@code null}.
     */
    public static JsonUrlEvent getTrueFalseNull(CharSequence text) { // NOPMD
        switch (text.length()) {
        case 4:
            switch (text.charAt(0)) {
            case 't':
                if (text.charAt(1) == 'r'
                        && text.charAt(2) == 'u'
                        && text.charAt(3) == 'e') {
                    return JsonUrlEvent.VALUE_TRUE;
                }
                return null;

            case 'n':
                if (text.charAt(1) == 'u'
                        && text.charAt(2) == 'l'
                        && text.charAt(3) == 'l') {
                    return JsonUrlEvent.VALUE_NULL;
                }
                return null;

            default:
                return null;
            }
            // this can never happen but checkstyle gets angry, so:
            // fall through

        case 5:
            if (text.charAt(0) == 'f'
                    && text.charAt(1) == 'a'
                    && text.charAt(2) == 'l'
                    && text.charAt(3) == 's'
                    && text.charAt(4) == 'e') {
                return JsonUrlEvent.VALUE_FALSE;
            }
            return null;

        default:
            return null;
        }
    }

    /**
     * This method may be used to validate the parsed content against a
     * user-supplied requirement.
     *
     * @param type the type found during parse
     */
    protected void checkResultType(ValueType type) {
        if (!doneTypeCheck) {
            doneTypeCheck = true;

            Set<ValueType> resultType = this.resultType;

            if (resultType != null && !resultType.contains(type)) {
                throw newSyntaxException(
                    MSG_EXPECT_TYPE,
                    String.format("%s: expected one of %s",
                        MSG_EXPECT_TYPE.getMessageText(),
                        resultType));
            }
        }
    }

    /**
     * This method may be used to validate the parsed content against a
     * user-supplied requirement. This may be used when an
     * empty composite is parsed.
     *
     * @see #checkResultType(ValueType)
     */
    protected void checkResultTypeIsComposite() {
        if (!doneTypeCheck) {
            doneTypeCheck = true;

            Set<ValueType> resultType = this.resultType;

            if (resultType != null
                    && !ValueType.containsComposite(resultType)) {

                throw newSyntaxException(
                    MSG_EXPECT_TYPE,
                    String.format("%s: expected one of %s",
                        MSG_EXPECT_TYPE.getMessageText(),
                        resultType));
            }
        }
    }

}
