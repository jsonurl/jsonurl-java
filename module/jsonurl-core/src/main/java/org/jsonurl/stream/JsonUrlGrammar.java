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
import static org.jsonurl.JsonUrlOption.optionImpliedStringLiterals;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_CHAR;
import static org.jsonurl.SyntaxException.Message.MSG_QUOTE_STILL_OPEN;
import static org.jsonurl.stream.CharIterator.EOF;
import static org.jsonurl.stream.CharUtil.CHARBITS;
import static org.jsonurl.stream.CharUtil.IS_LITCHAR;
import static org.jsonurl.stream.CharUtil.IS_QSCHAR;
import static org.jsonurl.stream.CharUtil.IS_QUOTE;
import static org.jsonurl.stream.CharUtil.IS_STRUCTCHAR;

import java.io.IOException;
import java.util.Set;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.SyntaxException;
import org.jsonurl.util.PercentCodec;

/**
 * Implements an iterator for the JSON&#x2192;URL grammar.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
class JsonUrlGrammar extends AbstractGrammar {

    /**
     * single quote/apostrophe.
     */
    private static final char APOS = '\'';

    /**
     * empty string.
     */
    private static final String EMPTY_STRING = "";

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

    @Override
    protected boolean readAndBufferLiteral() {
        final int cval = nextChar();

        switch (cval) {
        case EOF:
            //
            // this case is impossible at the moment because EOF is always
            // caught by my caller. But, leaving it here in case a future
            // code change invalidates that assumption.
            //
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

    @Override
    protected JsonUrlEvent readBufferedLiteral(
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

    @Override
    protected boolean isEmptyBufferedLiteral(boolean flag) {
        return flag
            ? this.decodedTextBuffer.length() == 0
            : this.rawTextBuffer.length() == 0;
    }

    @Override
    protected JsonUrlEvent readLiteral(boolean isKey) {
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
            return readUnquotedLiteral(readUnquotedLiteral(), isKey);
        }        
    }

    @Override
    public String getString() {
        return literalText.toString();
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

    @Override
    protected int nextStructChar(boolean peek) {
        int cur = peek ? peekChar() : nextChar();

        if (cur == EOF) {
            return EOF;
        }

        if (cur >= CharUtil.CHARBITS_LENGTH) {
            throw newSyntaxException(MSG_BAD_CHAR);
        }

        if ((CHARBITS[cur] & IS_STRUCTCHAR) == IS_STRUCTCHAR) {
            return cur;
        }

        return 0;
    }

    /**
     * Read and decode the next codepoint.
     */
    private int nextCodePoint() {
        try {
            return PercentCodec.decode(text);

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
    }
}
