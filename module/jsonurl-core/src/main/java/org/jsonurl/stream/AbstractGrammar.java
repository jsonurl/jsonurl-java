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

import static org.jsonurl.JsonUrlOption.optionEmptyUnquotedKey;
import static org.jsonurl.JsonUrlOption.optionEmptyUnquotedValue;
import static org.jsonurl.JsonUrlOption.optionSkipNulls;
import static org.jsonurl.JsonUrlOption.optionWfuComposite;
import static org.jsonurl.LimitException.Message.MSG_LIMIT_MAX_PARSE_DEPTH;
import static org.jsonurl.LimitException.Message.MSG_LIMIT_MAX_PARSE_VALUES;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_CHAR;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_LITERAL;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_OBJECT_VALUE;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_STRUCT_CHAR;
import static org.jsonurl.SyntaxException.Message.MSG_EXTRA_CHARS;
import static org.jsonurl.SyntaxException.Message.MSG_NO_TEXT;
import static org.jsonurl.SyntaxException.Message.MSG_STILL_OPEN;
import static org.jsonurl.stream.CharIterator.EOF;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.LimitException;
import org.jsonurl.ParseException;
import org.jsonurl.SyntaxException;
import org.jsonurl.ValueType;
import org.jsonurl.text.NumberBuilder;
import org.jsonurl.text.NumberText;

/**
 * Base class for JSON&#x2192;URL grammar implementations.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
@SuppressWarnings("PMD.CyclomaticComplexity") // NOPMD
abstract class AbstractGrammar extends AbstractEventIterator { 

    /**
     * Parse state.
     */
    private enum State {
        START,
        SKIP_LEADING_AMPS,
        SAVED_EVENT,
        PAREN,
        IMPLIED_ARRAY,
        IN_ARRAY,
        ARRAY_AFTER_ELEMENT,
        IMPLIED_OBJECT,
        IN_OBJECT,
        OBJECT_HAVE_KEY,
        OBJECT_AFTER_ELEMENT,
        END_STREAM,
    }

    /**
     * The depth at which a parse can be done.
     */
    private static final int PARSE_DEPTH_DONE = 0;
    
    /**
     * The depth at which an implied object/array can be done.
     */
    private static final int PARSE_DEPTH_IMPLIED = 1;

    /**
     * begin-composite (open paren).
     */
    protected static final char BEGIN_COMPOSITE = '(';

    /**
     * end-composite (close paren).
     */
    protected static final char END_COMPOSITE = ')';

    /**
     * name-separator (colon).
     */
    protected static final char NAME_SEPARATOR = ':';

    /**
     * value-separator (comma).
     */
    protected static final char VALUE_SEPARATOR = ',';

    /*
     * empty string.
     *
    private static final String EMPTY_STRING = "";*/

    /**
     * Parse state stack.
     */
    @SuppressWarnings("PMD.LooseCoupling") // I need methods specific to LinkedList.
    private final LinkedList<State> stateStack = new LinkedList<>();

    /**
     * Buffer for parsing number literals.
     */
    protected final NumberBuilder numberBuilder = new NumberBuilder();

    /**
     * Implied array/object or null.
     */
    private CompositeType impliedType;

    /**
     * Buffered "next" event value.
     */
    private JsonUrlEvent savedEventValue;

    /**
     * Current parse/nesting depth.
     */
    private int parseDepth = 1;
    
    /**
     * Number of parsed values.
     */
    private int parseValueCount;

    /**
     * Construct a new JsonUrlGrammar.
     * @param text input text
     * @param limits a valid JsonUrlLimits or null
     * @param options valid JsonUrlOptions or null
     */
    public AbstractGrammar(
            CharIterator text,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {

        super(text, limits, options);
        stateStack.push(State.START);
    }

    /**
     * Return {@code KEY_NAME}, {@code VALUE_EMPTY_LITERAL}, or throw
     * an exception as appropriate.
     */
    protected JsonUrlEvent literalEmptyValue(boolean isKey) {
        if (isKey) {
            if (optionEmptyUnquotedKey(options())) {
                return JsonUrlEvent.KEY_NAME;
            }
        } else if (optionEmptyUnquotedValue(options())) {
            return JsonUrlEvent.VALUE_EMPTY_LITERAL;
        }

        throw newSyntaxException(MSG_EXPECT_LITERAL);
    }

    /**
     * Buffer a literal.
     * @return an implementation specific boolean flag
     */
    protected abstract boolean readAndBufferLiteral();

    /**
     * Read the buffered literal and return its event.
     * This is used to consume the result of
     * {@link #readAndBufferLiteral()}.
     *
     * @param flag the flag value returned by {@link #readAndBufferLiteral()}
     * @param isKey true if the context of the literal is an object key
     */
    protected abstract JsonUrlEvent readBufferedLiteral(
            boolean flag,
            boolean isKey);

    /**
     * Read a literal and return its event.
     */
    protected abstract JsonUrlEvent readLiteral(boolean isKey);

    private JsonUrlEvent stateSavedEvent() {
        stateStack.pop();
        JsonUrlEvent ret = this.savedEventValue;
        this.savedEventValue = null; // NOPMD
        return ret;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private JsonUrlEvent stateStart() {
        final int cval = nextChar();

        switch (cval) {
        case BEGIN_COMPOSITE:
            stateStack.set(0, State.PAREN);
            return null;
        case EOF:
            if (!optionEmptyUnquotedValue(options())) {
                throw this.newSyntaxException(MSG_NO_TEXT);
            }
            break;
        default:
            break;
        }

        stateStack.set(0, State.END_STREAM);
        text.pushbackChar(cval);
        incrementParseValueCount();
        JsonUrlEvent ret = readLiteral(false);

        switch (ret) {
        case VALUE_EMPTY_LITERAL:
        case VALUE_STRING:
            checkResultType(ValueType.STRING);
            break;
        case VALUE_FALSE:
        case VALUE_TRUE:
            checkResultType(ValueType.BOOLEAN);
            break;
        case VALUE_NULL:
            checkResultType(ValueType.NULL);
            break;
        case VALUE_NUMBER:
            checkResultType(ValueType.NUMBER);
            break;
        default:
            throw newParseException("interal parse error");
        }

        if (!eof()) {
            throw newSyntaxException(MSG_EXTRA_CHARS);
        }

        return ret;
    }

    private JsonUrlEvent stateParenStructChar() {
        final int cval = nextChar();

        switch (cval) {
        case BEGIN_COMPOSITE:
            incrementParseValueCount();

            parseDepth = incrementLimit(
                MSG_LIMIT_MAX_PARSE_DEPTH,
                JsonUrlLimits.getMaxParseDepth(limits()),
                parseDepth);

            //
            // two back-to-back open parens, the first one is definitely
            // an array
            //
            checkResultType(ValueType.ARRAY);
            stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
            stateStack.push(State.PAREN);
            return JsonUrlEvent.START_ARRAY;

        case END_COMPOSITE:
            //
            // open paren followed by close paren -- empty composite
            //
            incrementParseValueCount();

            parseDepth--;
            stateStack.pop();

            checkResultTypeIsComposite();

            if (parseDepth == PARSE_DEPTH_DONE) {
                if (eof()) {
                    stateStack.push(State.END_STREAM);

                } else {
                    throw newSyntaxException(MSG_EXTRA_CHARS);
                }
            }
            return JsonUrlEvent.VALUE_EMPTY_COMPOSITE;

        default:
            text.pushbackChar(cval);
            return null;
        }
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private JsonUrlEvent stateParen() {
        //
        // look for a structural character
        //
        JsonUrlEvent ret = stateParenStructChar();
        if (ret != null) {
            return ret;
        }

        //
        // paren followed by a literal.  I need to buffer the literal and
        // then lookahead one token to see if this is an object or array.
        //
        boolean bufLitFlag = readAndBufferLiteral();

        int sep = peekChar();

        switch (sep) { // NOPMD - false positive
        case EOF:
            throw newSyntaxException(MSG_STILL_OPEN);

        case WFU_VALUE_SEPARATOR:
            checkWfuSeparator(true);
            // fall through
        case VALUE_SEPARATOR:
        case END_COMPOSITE:
            //
            // array
            //
            incrementParseValueCount();
            savedEventValue = readBufferedLiteral(bufLitFlag, false);
            checkResultType(ValueType.ARRAY);
            stateStack.set(0, State.ARRAY_AFTER_ELEMENT);
            stateStack.push(State.SAVED_EVENT);
            return JsonUrlEvent.START_ARRAY;

        case WFU_NAME_SEPARATOR:
            checkWfuSeparator(false);
            // fall through
        case NAME_SEPARATOR:
            //
            // key name for object
            //
            savedEventValue = readBufferedLiteral(bufLitFlag, true);
            checkResultType(ValueType.OBJECT);
            stateStack.set(0, State.OBJECT_HAVE_KEY);
            this.savedEventValue = JsonUrlEvent.KEY_NAME;
            stateStack.push(State.SAVED_EVENT);
            return JsonUrlEvent.START_OBJECT;

        default:
            throw newSyntaxException(MSG_EXPECT_LITERAL);
        }
    }

    private JsonUrlEvent stateObjectMissingValue() {
        final boolean requireValue = parseDepth != PARSE_DEPTH_IMPLIED
                || impliedType != CompositeType.OBJECT;

        if (requireValue) {
            throw newSyntaxException(MSG_EXPECT_OBJECT_VALUE);
        }

        //
        // this may be a key that's missing a value; give
        // the result a chance to handle that case.
        //
        return JsonUrlEvent.VALUE_MISSING;
    }

    private JsonUrlEvent stateObjectKeySeparator() {

        //
        // look for a key or value separator
        //
        final int sep = nextChar();

        switch (sep) { // NOPMD - false positive
        case EOF:
            stateStack.set(0, State.END_STREAM);
            return stateObjectMissingValue();
            
        case WFU_NAME_SEPARATOR:
            checkWfuSeparator(false);
            // fall through
        case NAME_SEPARATOR:
            return null;

        case WFU_VALUE_SEPARATOR:
            checkWfuSeparator(true);
            if (eof()) {
                stateStack.set(0, State.END_STREAM);
                return stateObjectMissingValue();
            }
            // fall through
        case VALUE_SEPARATOR:
            stateStack.set(0, State.IN_OBJECT);
            return stateObjectMissingValue();

        default:
            throw newSyntaxException(MSG_EXPECT_OBJECT_VALUE);
        }
    }

    private JsonUrlEvent stateNextValue(boolean isObject, State state) {
        if (isObject) {
            JsonUrlEvent ret = stateObjectKeySeparator();
            if (ret != null) {
                return ret;
            }
        }

        final int cval = nextChar();

        if (cval == BEGIN_COMPOSITE) {
            //
            // nested composite
            //
            stateStack.set(0, state);
            stateStack.push(State.PAREN);

            parseDepth = incrementLimit(
                    MSG_LIMIT_MAX_PARSE_DEPTH,
                    JsonUrlLimits.getMaxParseDepth(limits()),
                    parseDepth);

            return null;
        }

        //
        // literal
        //
        stateStack.set(0, state);
        text.pushbackChar(cval);
        incrementParseValueCount();
        return readLiteral(false);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private JsonUrlEvent stateAfterValue(
            State state,
            JsonUrlEvent event,
            CompositeType type) {

        final int cval = nextChar();

        switch (cval) {
        case WFU_VALUE_SEPARATOR:
            checkWfuSeparator(true);
            if (eof()) {
                stateStack.set(0, State.END_STREAM);
                return JsonUrlEvent.END_STREAM;
            }
            // fall through
        case VALUE_SEPARATOR:
            stateStack.set(0, state);
            return null;

        case END_COMPOSITE:
            parseDepth--;

            if (parseDepth == PARSE_DEPTH_DONE) {
                if (eof() && impliedType == null) {
                    stateStack.set(0, State.END_STREAM);
                    return event;
                }

                throw newSyntaxException(MSG_EXTRA_CHARS);
            }

            stateStack.pop();
            return event;

        case EOF:
            if (parseDepth == PARSE_DEPTH_IMPLIED
                    && impliedType == type) { // NOPMD
                stateStack.set(0, State.END_STREAM);
                return JsonUrlEvent.END_STREAM;
            }

            throw newSyntaxException(MSG_STILL_OPEN);

        default:
            break;
        }

        throw newSyntaxException(MSG_EXPECT_STRUCT_CHAR);
    }

    private JsonUrlEvent stateImplied(State next) {
        final int cval = nextChar();

        if (cval == EOF) {
            stateStack.set(0, State.END_STREAM);
            return JsonUrlEvent.END_STREAM;
        }

        text.pushbackChar(cval);
        stateStack.set(0, next);
        return null;
    }

    private JsonUrlEvent stateObjectKey() {
        //
        // read the next key
        //
        readLiteral(true);

        stateStack.set(0, State.OBJECT_HAVE_KEY);
        return JsonUrlEvent.KEY_NAME;
    }
    
    private JsonUrlEvent filterLiteral(JsonUrlEvent event) {
        boolean skip = event == JsonUrlEvent.VALUE_NULL
                && optionSkipNulls(options());
        
        return skip ? null : event;
    }

    @Override
    public JsonUrlEvent next() { // NOPMD - CyclomaticComplexity
        for (;;) {
            JsonUrlEvent ret = null;

            //
            // default case not used because I want the compiler to tell me
            // if I've missed a case
            //
            switch (stateStack.peek()) { // NOPMD - SwitchStmtsShouldHaveDefault
            case START:
                ret = filterLiteral(stateStart());
                break;

            case SKIP_LEADING_AMPS:
                consumeAmps();
                stateStack.pop();
                continue;

            case SAVED_EVENT:
                ret = filterLiteral(stateSavedEvent());
                break;

            case PAREN:
                ret = filterLiteral(stateParen());
                break;

            case IN_ARRAY:
                ret = filterLiteral(stateNextValue(
                    false, State.ARRAY_AFTER_ELEMENT));
                break;

            case ARRAY_AFTER_ELEMENT:
                ret = stateAfterValue(
                    State.IN_ARRAY,
                    JsonUrlEvent.END_ARRAY,
                    CompositeType.ARRAY);
                break;

            case OBJECT_HAVE_KEY:
                ret = filterLiteral(stateNextValue(
                    true, State.OBJECT_AFTER_ELEMENT));
                break;

            case OBJECT_AFTER_ELEMENT: 
                ret = stateAfterValue(
                    State.IN_OBJECT,
                    JsonUrlEvent.END_OBJECT,
                    CompositeType.OBJECT);
                break;

            case IN_OBJECT:
                ret = stateObjectKey();
                break;

            case END_STREAM:
                return JsonUrlEvent.END_STREAM;

            case IMPLIED_ARRAY:
                ret = stateImplied(State.IN_ARRAY);
                break;

            case IMPLIED_OBJECT:
                ret = stateImplied(State.IN_OBJECT);
                break;
            }

            if (ret != null) {
                return ret;
            }
        }
    }

    @Override
    public void setType(Set<ValueType> resultType, CompositeType impliedType) {
        super.setType(resultType);

        this.impliedType = impliedType;

        stateStack.clear();

        if (impliedType == CompositeType.OBJECT) {
            stateStack.push(State.IMPLIED_OBJECT);
            stateStack.push(State.SKIP_LEADING_AMPS);
            checkResultType(ValueType.OBJECT);

        } else if (impliedType == CompositeType.ARRAY) {
            stateStack.push(State.IMPLIED_ARRAY);
            stateStack.push(State.SKIP_LEADING_AMPS);
            checkResultType(ValueType.ARRAY);

        } else {
            stateStack.push(State.START);
        }
    }

    @Override
    public NumberText getNumberText() {
        return numberBuilder;
    }

    @Override
    public String toString() {
        return CharIterator.toStringWithOffset(text);
    }

    @Override
    public SyntaxException newSyntaxException(
            SyntaxException.Message msg,
            String text) {
        return new SyntaxException(msg, text, this.text.getOffset());
    }

    @Override
    public LimitException newLimitException(
            LimitException.Message msg,
            String text) {
        return new LimitException(msg, text, this.text.getOffset());
    }

    @Override
    public ParseException newParseException(String text) {
        return new ParseException(text, this.text.getOffset());
    }

    private boolean consumeAmps() {
        boolean ret = false;

        if (optionWfuComposite(options()) && impliedType != null) {
            for (;;) {
                final int cur = nextChar();
    
                if (cur != WFU_VALUE_SEPARATOR) {
                    text.pushbackChar(cur);
                    break;
                }

                ret = true;
            }
        }

        return ret;
    }

    /**
     * increment a limit value, throwing an exception if the limit is reached.
     */
    private int incrementLimit(
            LimitException.Message msg,
            int limit,
            int value) {

        final int newValue = value + 1;

        if (newValue > limit) {
            throw newLimitException(msg);
        }
        return newValue;
    }

    private void checkWfuSeparator(boolean value) {
        if (!optionWfuComposite(options()) || parseDepth != 1) {
            throw newSyntaxException(MSG_BAD_CHAR);
        }
        if (value && impliedType != null) {
            //
            // value separator
            //
            consumeAmps();
        }
    }

    /**
     * Return {@code KEY_NAME} if isKey is true.
     * Otherwise, return {@code event}.
     */
    protected static JsonUrlEvent keyEvent(boolean isKey, JsonUrlEvent event) {
        return isKey ? JsonUrlEvent.KEY_NAME : event;
    }

    /**
     * Get the next character from {@link text} and consume it.
     */
    protected abstract int nextChar();

    /**
     * Peek at the next character from {@link text}, but done consume it.
     */
    protected abstract int peekChar();

    /**
     * Increment the count of parsed values, throwing an exception
     * if the limit is reached.
     */
    private void incrementParseValueCount() {
        parseValueCount = incrementLimit(
            MSG_LIMIT_MAX_PARSE_VALUES,
            JsonUrlLimits.getMaxParseValues(limits()),
            parseValueCount);
    }

    private boolean eof() {
        try {
            return text.atEnd();

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
    }
}
