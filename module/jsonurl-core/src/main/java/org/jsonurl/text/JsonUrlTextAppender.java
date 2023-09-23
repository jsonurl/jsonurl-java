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

package org.jsonurl.text;

import static org.jsonurl.JsonUrlOption.optionAQF;
import static org.jsonurl.JsonUrlOption.optionCoerceNullToEmptyString;
import static org.jsonurl.JsonUrlOption.optionEmptyUnquoted;
import static org.jsonurl.JsonUrlOption.optionImpliedStringLiterals;
import static org.jsonurl.JsonUrlOption.optionNoEmptyComposite;
import static org.jsonurl.JsonUrlOption.optionWfuComposite;
import static org.jsonurl.text.CharUtil.APOS;
import static org.jsonurl.text.CharUtil.HEXENCODE_AQF;
import static org.jsonurl.text.CharUtil.HEXENCODE_QUOTED;
import static org.jsonurl.text.CharUtil.HEXENCODE_UNQUOTED;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.JsonUrlOptionable;
import org.jsonurl.util.PercentCodec;

/**
 * A JsonTextAppendable that appends JSON&#x2192;URL text to any Appendable.
 *
 * <p>Note, like {@link java.lang.StringBuilder} an instance of this class is
 * not thread-safe.
 *
 * @param <A> Accumulator type
 * @param <R> Result type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public abstract class JsonUrlTextAppender<A extends Appendable, R> // NOPMD
        implements JsonTextAppendable<A, R>, JsonUrlOptionable {

    /**
     * The empty string.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Destination, provided in constructor.
     */
    protected final A out;

    /**
     * Do not output the top level parens.
     */
    private final CompositeType impliedType;

    /**
     * Set of JsonUrlOptions.
     */
    private final Set<JsonUrlOption> options; // NOPMD - final

    /**
     * Current print depth.
     */
    private int depth;

    /**
     * modified flag.
     * This is used to implement NO_EMPTY_COMPOSITE.
     */
    private int modifiedDepth;

    /**
     * Create a new JsonUrlTextAppender.
     * @param dest JSON&#x2192;URL text destination
     * @param options Set of JsonUrlOptions
     */
    protected JsonUrlTextAppender(
            A dest,
            CompositeType impliedType,
            Set<JsonUrlOption> options) {

        this.out = dest;
        this.impliedType = impliedType;
        this.options = options;
    }

    @Override
    public Set<JsonUrlOption> options() {
        return options;
    }

    @Override
    public JsonUrlTextAppender<A,R> beginObject() throws IOException {
        return beginComposite();
    }

    @Override
    public JsonUrlTextAppender<A,R> endObject() throws IOException {
        return endComposite(true);
    }

    @Override
    public JsonUrlTextAppender<A,R> beginArray() throws IOException {
        return beginComposite();
    }

    @Override
    public JsonUrlTextAppender<A,R> endArray() throws IOException {
        return endComposite(false);
    }

    @Override
    public JsonUrlTextAppender<A,R> valueSeparator() throws IOException {
        out.append(useWfuStructChars() ? '&' : ',');
        modifiedDepth = depth;
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> nameSeparator() throws IOException {
        out.append(useWfuStructChars() ? '=' : ':');
        modifiedDepth = depth;
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> addNull() throws IOException {
        if (optionCoerceNullToEmptyString(options)) {
            add(EMPTY_STRING);

        } else  if (optionImpliedStringLiterals(options)) {
            throw new IOException("implied strings: unexpected null");

        } else {
            out.append("null");
            modifiedDepth = depth;
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> addCodePoint(int codePoint) throws IOException {
        appendCodePoint(this, codePoint, options);
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(BigDecimal value) throws IOException {
        if (value == null) {
            return addNull();
        }

        if (optionImpliedStringLiterals(options)) {
            add(String.valueOf(value), false);

        } else {
            out.append(String.valueOf(value));
            modifiedDepth = depth;
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(BigInteger value) throws IOException {
        if (value == null) {
            return addNull();
        }

        if (optionImpliedStringLiterals(options)) {
            add(String.valueOf(value), false);

        } else {
            out.append(String.valueOf(value));
            modifiedDepth = depth;
        }

        
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(long value) throws IOException {
        if (optionImpliedStringLiterals(options)) {
            add(String.valueOf(value), false);
        } else {
            out.append(String.valueOf(value));
            modifiedDepth = depth;
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(double value) throws IOException {
        if (optionImpliedStringLiterals(options)) {
            add(String.valueOf(value), false);
        } else {
            out.append(String.valueOf(value));
            modifiedDepth = depth;
        }

        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(boolean value) throws IOException {
        out.append(String.valueOf(value));
        modifiedDepth = depth;
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(
            CharSequence text,
            int start,
            int end,
            boolean isKey) throws IOException {

        if (text == null) {
            if (isKey) {
                throw new IOException("object key can not be null");
            }
            return addNull();
        }

        boolean modified = appendLiteral(
            out,
            text,
            start,
            end,
            isKey,
            options);

        if (modified) {
            modifiedDepth = depth;    
        }

        return this;
    }
    
    @Override
    public JsonUrlTextAppender<A,R> add(Object value) throws IOException {
        if (value == null) {
            return addNull();
        }
        throw new IOException("unsupported object class: " + value.getClass());
    }
    
    @Override
    public JsonUrlTextAppender<A,R> append(CharSequence csq) throws IOException {
        out.append(csq);
        modifiedDepth = depth;
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> append(
            CharSequence csq,
            int start,
            int end) throws IOException {
        out.append(csq, start, end);
        modifiedDepth = depth;
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> append(char value) throws IOException {
        out.append(value);
        modifiedDepth = depth;
        return this;
    }

    /**
     * Returns true if this appender is writing an implied array or object.
     */
    public boolean isImpliedComposite() {
        return impliedType != null;
    }

    /**
     * Test if wwwFormUrlEncoded structural characters should be used.
     */
    private boolean useWfuStructChars() {
        return optionWfuComposite(options) && depth == 1;
    }

    private JsonUrlTextAppender<A,R> beginComposite() throws IOException {
        if (impliedType == null || depth > 0) {
            out.append('(');
        }

        modifiedDepth = depth;
        depth++;
        return this;
    }

    private JsonUrlTextAppender<A,R> endComposite(boolean isObject)
            throws IOException {

        depth--;

        if (isObject && isEmptyObject()) {
            out.append(':');
        }

        if (impliedType == null || depth > 0) {
            out.append(')');
        }
        return this;
    }
    
    private boolean isEmptyObject() {
        return modifiedDepth == depth && optionNoEmptyComposite(options());
    }

    /**
     * Append the empty value.
     * @param <T> destination type
     * @param dest destination
     * @param isKey true if this is an object key, false if it's a value
     * @param options JsonUrlOptions
     * @return true if {@code dest} was modified
     */
    private static <T extends Appendable> boolean appendEmptyString(
            T dest,
            boolean isKey,
            Set<JsonUrlOption> options) throws IOException {

        if (optionEmptyUnquoted(options, isKey)) {
            return false;
        }

        if (optionAQF(options)) {
            dest.append("!e");

        } else if (optionImpliedStringLiterals(options)) {
            throw new IOException("implied strings: unexpected empty string");

        } else {
            dest.append("''");
        }

        return true;
    }

    /**
     * Append the given UNICODE codepoint as a string literal.
     * Note, this method effectively appends a string of length 1 and
     * can't take advantage optimizations available to longer strings.
     * It's primarily used for appending the individual characters of
     * character arrays. You don't want to call this in a loop as a means
     * of appending a string. Use
     * {@link #appendLiteral(Appendable, CharSequence, int, int, boolean, Set)}
     * for that.
     *
     * @param <T>       destination type
     * @param dest      destination
     * @param codePoint a UNICODE codePoint
     * @param options   a valid JsonUrlOptions or null
     */
    private static <T extends Appendable> void appendCodePoint(
            T dest,
            int codePoint,
            Set<JsonUrlOption> options) throws IOException {

        if (!optionImpliedStringLiterals(options) && codePoint == APOS) {
            dest.append("%27");

        } else {
            PercentCodec.encode(
                dest,
                codePoint,
                optionAQF(options) ? HEXENCODE_AQF : HEXENCODE_UNQUOTED,
                0);
        }
    }


    /**
     * Test if the given text matches one of: true, false, null.
     */
    @SuppressWarnings({"java:S3776", "PMD.CyclomaticComplexity"})
    private static boolean isTrueFalseNull(CharSequence text, int start, int end) {
        switch (end - start) {
        case 4:
            switch (text.charAt(0)) {
            case 't':
                return text.charAt(1) == 'r'
                    && text.charAt(2) == 'u'
                    && text.charAt(3) == 'e';

            case 'n':
                return text.charAt(1) == 'u'
                    && text.charAt(2) == 'l'
                    && text.charAt(3) == 'l';

            default:
                return false;
            }
            // this can never happen but checkstyle gets angry, so:
            // fall through

        case 5:
            return text.charAt(0) == 'f'
                && text.charAt(1) == 'a'
                && text.charAt(2) == 'l'
                && text.charAt(3) == 's'
                && text.charAt(4) == 'e';

        default:
            return false;
        }
    }

    /**
     * Check haystack conains needle within the given bounds.
     */
    private static boolean contains(
            CharSequence haystack,
            int start,
            int end,
            char needle) {
        
        for (int i = start; i < end; i++) {
            if (needle == haystack.charAt(i)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Append the given CharSequence as a string literal.
     *
     * @param <T>   destination type
     * @param dest  destination
     * @param text  source
     * @param start offset in source
     * @param end   offset in source
     * @param options a valid JsonUrlOptions or null
     * @return true if dest was modified
     */
    @SuppressWarnings({
        // See SuppressWarnings.md#complexity
        "PMD.CyclomaticComplexity",
        "PMD.NPathComplexity",
        "PMD.CognitiveComplexity",
        "PMD.ExcessiveMethodLength",
        "java:S3776"
    })
    private static <T extends Appendable> boolean appendLiteral(
            T dest,
            CharSequence text,
            int start,
            int end,
            boolean isKey,
            Set<JsonUrlOption> options) throws IOException {

        if (end <= start) {
            return appendEmptyString(dest, isKey, options);
        }

        if (optionImpliedStringLiterals(options)) {
            if (optionAQF(options)) {
                encodeAqf(dest, text, start, end);
                return true;
            }

            encode(dest, text, start, end, false, true);
            return true;
        }

        //
        // true, false, null, and number literals must be quoted
        //
        boolean isLiteral = isTrueFalseNull(text, start, end)
                || NumberBuilder.isNumber(text, start, end, options);

        if (isLiteral) {
            if (isKey) {
                //
                // keys are always assumed to be strings
                //
                dest.append(text, start, end);

            } else if (optionAQF(options)) {
                if (contains(text, start, end, '+')) {
                    encodeAqf(dest, text, start, end);

                } else {
                    dest.append('!').append(text, start, end);                    
                }

            } else if (contains(text, start, end, '+')) {
                encode(dest, text, start, end, false, false);

            } else {
                //
                // this is a literal without a plus; it's safe to represent
                // it as a quoted string.
                //
                dest.append('\'').append(text, start, end).append('\'');
            }
            return true;
        }
        
        if (NumberBuilder.isNumber(text, start, end, false, true, options)) {
            //
            // Special handling if this would look like a number literal once
            // the space is replaced with '+' 
            //
            if (optionAQF(options)) {
                dest.append('!');
                encodeAqf(dest, text, start, end);

            } else {
                dest.append('\'');
                encode(dest, text, start, end, true, false);
                dest.append('\'');
            }
            return true;
        }

        if (optionAQF(options)) {
            encodeAqf(dest, text, start, end);
            return true;
        }

        //
        // figure out the most efficient way to safely encode the string
        //
        StringStrategy enc = StringStrategy.getStringEncoding(
                text, start, end);

        switch (enc) { // NOPMD - SwitchStmtsShouldHaveDefault
        case FULL_ENCODING:
        case NO_QUOTE_WITH_SPACE:
            encode(dest, text, start, end, false, false);
            break;
        case QUOTE_NO_SPACE:
            dest.append('\'').append(text, start, end).append('\'');
            break;
        case QUOTE_WITH_SPACE:
            dest.append('\'');
            encode(dest, text, start, end, true, false);
            dest.append('\'');
            break;
        case SAFE_ASIS:
            dest.append(text, start, end);
            break;
        }

        return true;
    }

    /**
     * Hex encode as UTF-8.
     */
    private static void encodeAqf(
            Appendable dest,
            CharSequence text,
            int start,
            int end) throws IOException {

        PercentCodec.encode(dest, HEXENCODE_AQF, text, start, end);
    }

    /**
     * Hex encode as UTF-8.
     */
    private static void encode(
            Appendable dest,
            CharSequence text,
            int start,
            int end,
            boolean quoted,
            boolean isImpliedStringLiteral) throws IOException {

        int position = start;

        if (!isImpliedStringLiteral
                && start < end
                && text.charAt(start) == APOS) {

            //
            // edge case: if the first character is a literal quote then
            // it must always be encoded
            //
            dest.append("%27");
            position++;
        }
        
        final String[] hexEncode = quoted
            ? HEXENCODE_QUOTED : HEXENCODE_UNQUOTED;
        
        PercentCodec.encode(dest, hexEncode, text, position, end);
    }
}
