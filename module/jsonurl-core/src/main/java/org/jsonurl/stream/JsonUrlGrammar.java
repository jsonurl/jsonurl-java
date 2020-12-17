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

package org.jsonurl.stream; // NOPMD - ExcessiveImports

import static org.jsonurl.JsonUrlOption.optionCoerceNullToEmptyString;
import static org.jsonurl.JsonUrlOption.optionEmptyUnquotedKey;
import static org.jsonurl.JsonUrlOption.optionEmptyUnquotedValue;
import static org.jsonurl.JsonUrlOption.optionImpliedStringLiterals;
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
import static org.jsonurl.SyntaxException.Message.MSG_QUOTE_STILL_OPEN;
import static org.jsonurl.SyntaxException.Message.MSG_STILL_OPEN;
import static org.jsonurl.stream.CharIterator.EOF;
import static org.jsonurl.stream.CharUtil.CHARBITS;
import static org.jsonurl.stream.CharUtil.IS_LITCHAR;
import static org.jsonurl.stream.CharUtil.IS_QSCHAR;
import static org.jsonurl.stream.CharUtil.IS_QUOTE;
import static org.jsonurl.stream.CharUtil.IS_STRUCTCHAR;

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
import org.jsonurl.util.PercentCodec;

/**
 * Implements an iterator for the JSON&#x2192;URL grammar.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
class JsonUrlGrammar extends AbstractEventIterator { // NOPMD - CyclomaticComplexity

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
    private static final char BEGIN_COMPOSITE = '(';

    /**
     * end-composite (close paren).
     */
    private static final char END_COMPOSITE = ')';

    /**
     * name-separator (colon).
     */
    private static final char NAME_SEPARATOR = ':';

    /**
     * value-separator (comma).
     */
    private static final char VALUE_SEPARATOR = ',';

    /**
     * single quote/apostrophe.
     */
    private static final char APOS = '\'';

    /**
     * empty string.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Parse state stack.
     */
    @SuppressWarnings("PMD.LooseCoupling") // I need methods specific to LinkedList.
    private final LinkedList<State> stateStack = new LinkedList<>();

    /**
     * Buffer for decoded literal text.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField") // reused
    private final StringBuilder decodedTextBuffer = new StringBuilder(512);

    /**
     * Straight/raw copy of literal text; no decoding done.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField") // reused
    private final StringBuilder rawTextBuffer = new StringBuilder(512);

    /**
     * Text for the last key/literal.
     */
    private CharSequence literalText = decodedTextBuffer;

    /**
     * Buffer for parsing number literals.
     */
    private final NumberBuilder numberBuilder = new NumberBuilder();

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
    public JsonUrlGrammar(
            CharIterator text,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {

        super(text, limits, options);
        stateStack.push(State.START);
    }
    
    private JsonUrlEvent readQuotedLiteral(boolean isKey) {
        //
        // if user calls getString() the text will come from here
        //
        this.literalText = this.decodedTextBuffer;

        final StringBuilder buf = this.decodedTextBuffer;
        buf.setLength(0);

        for (;;) {
            int cur = nextChar();

            if (cur == EOF) {
                throw newSyntaxException(MSG_QUOTE_STILL_OPEN);
            }

            if (cur >= CharUtil.CHARBITS_LENGTH) {
                throw newSyntaxException(MSG_BAD_CHAR);
            }

            switch (CHARBITS[cur] & (IS_QSCHAR | IS_QUOTE)) {
            case IS_QSCHAR:
                text.pushbackChar(cur);
                buf.appendCodePoint(nextCodePoint());
                continue;
            case IS_QUOTE:
                return keyEvent(isKey, JsonUrlEvent.VALUE_STRING);
            default:
                throw newSyntaxException(MSG_BAD_CHAR);
            }
        }
    }

    /**
     * Store the decoded text in {@link #decodedTextBuffer} and the raw text
     * in {@link #rawTextBuffer}.
     * @return {@link #rawTextBuffer}
     */
    private CharSequence readUnquotedLiteral() {
        final StringBuilder decodedText = this.decodedTextBuffer;
        decodedText.setLength(0);

        final StringBuilder copyBuffer = this.rawTextBuffer;
        copyBuffer.setLength(0);

        for (;;) {
            int cur = peekChar();
            if (cur == EOF) {
                return copyBuffer;
            }

            if (cur >= CharUtil.CHARBITS_LENGTH) {
                throw newSyntaxException(MSG_BAD_CHAR);
            }

            switch (CHARBITS[cur] & (IS_LITCHAR | IS_STRUCTCHAR)) {
            case IS_LITCHAR:
                copyBuffer.append((char)cur);
                decodedText.appendCodePoint(nextCodePoint());
                continue;
            case IS_STRUCTCHAR:
                return copyBuffer;
            default:
                throw newSyntaxException(MSG_BAD_CHAR);
            }
        }
    }

    private JsonUrlEvent readUnquotedLiteral(boolean isKey) {
        return readUnquotedLiteral(readUnquotedLiteral(), isKey);
    }

    private JsonUrlEvent readUnquotedLiteral(
            CharSequence rawText,
            boolean isKey) {

        if (rawText.length() == 0) {
            return literalEmptyValue(isKey);
        }

        if (optionImpliedStringLiterals(options())) {
            return keyEvent(isKey, JsonUrlEvent.VALUE_STRING);
        }

        JsonUrlEvent tfn = getTrueFalseNull(rawText);
        if (tfn == JsonUrlEvent.VALUE_NULL
                && optionCoerceNullToEmptyString(options())) {

            this.literalText = EMPTY_STRING;
            return keyEvent(isKey, JsonUrlEvent.VALUE_EMPTY_LITERAL);
        }
        if (tfn != null) {
            this.literalText = rawText;
            return keyEvent(isKey, tfn);
        }

        if (numberBuilder.reset().parse(rawText)) {
            this.literalText = rawText;
            return keyEvent(isKey, JsonUrlEvent.VALUE_NUMBER);
        }

        this.literalText = this.decodedTextBuffer;
        return keyEvent(isKey, JsonUrlEvent.VALUE_STRING);
    }

    private JsonUrlEvent literalEmptyValue(boolean isKey) {
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
     * Buffer a literal. Returns true if the value was quoted.
     */
    private boolean readAndBufferLiteral() {
        final int cval = nextChar();

        switch (cval) {
        case EOF:
            return false;

        case APOS:
            if (!optionImpliedStringLiterals(options())) {
                readQuotedLiteral(false);
                return true;
            }
            // fall through
        default:
            text.pushbackChar(cval);
            readUnquotedLiteral();
            return false;
        }
    }
    

    /**
     * Read the buffered literal and return its event.
     * This is used to consume the result of
     * {@link #readAndBufferLiteral()}.
     */
    private JsonUrlEvent readBufferedLiteral(
            boolean isQuoted,
            boolean isKey) {
        
        if (isQuoted) {
            return keyEvent(isKey, JsonUrlEvent.VALUE_STRING);
        }

        CharSequence text = rawTextBuffer;

        if (text.length() == 0) {
            return literalEmptyValue(isKey);
        }

        return readUnquotedLiteral(text, isKey);
    }

    /**
     * Read a literal and return its event.
     */
    private JsonUrlEvent readLiteral(
            boolean checkValueLimit,
            boolean isKey) {
        
        if (checkValueLimit) {
            parseValueCount = incrementLimit(
                MSG_LIMIT_MAX_PARSE_VALUES,
                JsonUrlLimits.getMaxParseValues(limits()),
                parseValueCount);
        }

        final int cval = nextChar();

        switch (cval) {
        case EOF:
            return literalEmptyValue(isKey);

        case APOS:
            if (!optionImpliedStringLiterals(options())) {
                return readQuotedLiteral(isKey);                
            }
            // fall through
        default:
            text.pushbackChar(cval);
            return readUnquotedLiteral(isKey);
        }        
    }

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
        JsonUrlEvent ret = readLiteral(true, false); // NOPMD - false positive

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
            parseValueCount = incrementLimit(
                    MSG_LIMIT_MAX_PARSE_VALUES,
                    JsonUrlLimits.getMaxParseValues(limits()),
                    parseValueCount);

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
            parseDepth--;

            parseValueCount = incrementLimit(
                    MSG_LIMIT_MAX_PARSE_VALUES,
                    JsonUrlLimits.getMaxParseValues(limits()),
                    parseValueCount);

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

    @SuppressWarnings("PMD.CyclomaticComplexity") // NOPMD
    private JsonUrlEvent stateParen() {
        //
        // look for a structural character
        //
        JsonUrlEvent ret = stateParenStructChar();
        if (ret != null) {
            return ret;
        }

        //
        // paren followed by a literal.  I need to buffer the next literal
        // and then lookahead one token to see if this is an object or array.
        //
        boolean isQuoted = readAndBufferLiteral();

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
            parseValueCount = incrementLimit(
                    MSG_LIMIT_MAX_PARSE_VALUES,
                    JsonUrlLimits.getMaxParseValues(limits()),
                    parseValueCount);

            savedEventValue = readBufferedLiteral(isQuoted, false);
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
            savedEventValue = readBufferedLiteral(isQuoted, true);
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
        return readLiteral(true, false);
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

            throw newSyntaxException(MSG_EXTRA_CHARS);

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
        readLiteral(false, true);

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
    public String getString() {
        return literalText.toString();
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

    private static JsonUrlEvent keyEvent(boolean isKey, JsonUrlEvent event) {
        return isKey ? JsonUrlEvent.KEY_NAME : event;
    }

    private int nextCodePoint() {
        try {
            return PercentCodec.decode(text);

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
        
    }

    private int nextChar() {
        try {
            return text.nextChar();

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
    }

    private int peekChar() {
        try {
            return text.peekChar();

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
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
