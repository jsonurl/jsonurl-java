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
import static org.jsonurl.SyntaxException.Message.MSG_BAD_ESCAPE;
import static org.jsonurl.stream.CharIterator.EOF;
import static org.jsonurl.stream.CharUtil.CHARBITS;
import static org.jsonurl.stream.CharUtil.CHARBITS_LENGTH;
import static org.jsonurl.stream.CharUtil.IS_BANG;
import static org.jsonurl.stream.CharUtil.IS_CGICHAR;
import static org.jsonurl.stream.CharUtil.IS_LITCHAR;
import static org.jsonurl.stream.CharUtil.IS_SPACE;
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
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class JsonUrlGrammarAQF extends AbstractGrammar {

    /**
     * Escape character.
     */
    private static final char ESCAPE = '!';

    /**
     * Buffer for decoded/unescaped literal text.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField") // reused
    private final StringBuilder decodedTextBuffer = new StringBuilder(512);

    /**
     * Buffer for non-decoded/escaped literal text.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField") // reused
    private final StringBuilder numTextBuffer = new StringBuilder(512);
    
    /**
     * Reference to either {@link #decodedTextBuffer} or
     * {@link #numTextBuffer}.
     */
    private CharSequence literalText;

    /**
     * Construct a new JsonUrlGrammar.
     * @param text input text
     * @param limits a valid JsonUrlLimits or null
     * @param options valid JsonUrlOptions or null
     */
    public JsonUrlGrammarAQF(
            CharIterator text,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {

        super(text, limits, options);
    }

    /**
     * Decode an escaped character.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void decodeBang(StringBuilder decodedText, boolean isFirst) {
        int cur = nextCodePoint(false);

        switch (cur) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case '-':
        case '+':
        case ESCAPE:
        case 't':
        case 'f':
        case 'n':
        case BEGIN_COMPOSITE:
        case END_COMPOSITE:
        case NAME_SEPARATOR:
        case VALUE_SEPARATOR:
            decodedText.append((char)cur);
            break;

        case 'e':
            if (isFirst) {
                break;
            }
            // fall through
        default:
            throw newSyntaxException(MSG_BAD_ESCAPE);
        }
    }
    
    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    protected boolean readAndBufferLiteral() {
        final StringBuilder decodedText = this.decodedTextBuffer;
        decodedText.setLength(0);
        
        final StringBuilder numText = this.numTextBuffer;
        numText.setLength(0);
        
        //
        // return true if this has an escape sequence and therefore
        // must be parsed as a string value; otherwise, return false.
        //
        boolean ret = false;

        for (boolean isFirst = true;; isFirst = false) {
            final char rawPlus;

            //
            // The browser address bar *does* recognize a difference between
            // percent encoded vs. literal ampersand, equals, and plus, unlike
            // other sub-delims. So I have to check for those specifically,
            // here, because the call to nextCodePoint() will decode them and
            // I can't tell the difference at that point.
            //
            switch (peekAscii()) { // NOPMD - no default
            case EOF:
            case WFU_VALUE_SEPARATOR:
            case WFU_NAME_SEPARATOR:
                return ret;
            case WFU_SPACE:
                rawPlus = '+';
                break;
            default:
                rawPlus = ' ';
                break;
            }

            final int ucp = nextCodePoint();

            if (ucp >= CHARBITS_LENGTH) {
                decodedText.appendCodePoint(ucp);
                numText.appendCodePoint(ucp);
                continue;
            }

            switch (CHARBITS[ucp] & (IS_SPACE
                    | IS_BANG | IS_LITCHAR | IS_STRUCTCHAR | IS_CGICHAR)) {

            case IS_BANG | IS_LITCHAR:
                decodeBang(decodedText, isFirst);
                numText.appendCodePoint(ucp);
                ret = true;
                continue;
            case IS_SPACE:
                decodedText.appendCodePoint(ucp);
                numText.append(rawPlus);
                break;
            case IS_LITCHAR:
            case IS_STRUCTCHAR | IS_CGICHAR:
                decodedText.appendCodePoint(ucp);
                numText.appendCodePoint(ucp);
                continue;
            case IS_STRUCTCHAR:
                text.pushbackChar(ucp);
                return ret;
            default:
                // can't happen
                throw newSyntaxException(MSG_BAD_CHAR);
            }
        }
    }

    @Override
    protected JsonUrlEvent readBufferedLiteral(
            boolean isEscaped,
            boolean isKey) {

        final StringBuilder decodedText = this.decodedTextBuffer;
        literalText = decodedText;

        if (optionImpliedStringLiterals(options())) {
            //
            // VALUE_STRING (rather than VALUE_EMPTY_LITERAL) should be
            // returned if IMPLIED_STRING_LITERALS is enabled
            //
            return keyEvent(isKey, JsonUrlEvent.VALUE_STRING); 
        }

        if (isEscaped) {
            if (decodedText.length() == 0) {
                return keyEvent(isKey, JsonUrlEvent.VALUE_EMPTY_LITERAL);
            }

            return keyEvent(isKey, JsonUrlEvent.VALUE_STRING);
        }

        if (decodedText.length() == 0) {
            return literalEmptyValue(isKey);
        }

        JsonUrlEvent ret = getTrueFalseNull(decodedText);

        if (ret == JsonUrlEvent.VALUE_NULL
                && optionCoerceNullToEmptyString(options())) {

            decodedText.setLength(0);
            return keyEvent(isKey, JsonUrlEvent.VALUE_EMPTY_LITERAL);
        }

        if (ret != null) {
            return keyEvent(isKey, ret);
        }

        final StringBuilder numText = this.numTextBuffer;

        if (numberBuilder.reset().parse(numText, options())) {
            literalText = numText;
            return keyEvent(isKey, JsonUrlEvent.VALUE_NUMBER);
        }

        return keyEvent(isKey, JsonUrlEvent.VALUE_STRING);
    }

    @Override
    protected JsonUrlEvent readLiteral(boolean isKey) {
        return readBufferedLiteral(readAndBufferLiteral(), isKey);
    }

    @Override
    public String getString() {
        return literalText.toString();
    }

    @Override
    protected int nextChar() {
        return nextCodePoint();
    }

    @Override
    protected int peekChar() {
        int codePoint = nextCodePoint();
        text.pushbackChar(codePoint);
        return codePoint;
    }
    
    /**
     * Read and decode the next codepoint.
     */
    private int nextCodePoint() {
        return nextCodePoint(true);
    }

    /**
     * Read and decode the next codepoint.
     */
    private int nextCodePoint(boolean decodePlus) {
        try {
            return PercentCodec.decode(text, decodePlus);

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
    }

    /**
     * Peek at the next literal character.
     */
    private int peekAscii() {
        try {
            return text.peekChar();

        } catch (IOException e) {
            SyntaxException tex = newSyntaxException(MSG_BAD_CHAR);
            tex.initCause(e);
            throw tex;
        }
    }

}
