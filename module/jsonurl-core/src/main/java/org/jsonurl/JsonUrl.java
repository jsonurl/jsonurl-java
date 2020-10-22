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

package org.jsonurl;

import static org.jsonurl.CharUtil.CHARBITS;
import static org.jsonurl.CharUtil.CHARBITS_LENGTH;
import static org.jsonurl.CharUtil.IS_LITCHAR;
import static org.jsonurl.CharUtil.IS_QSCHAR;
import static org.jsonurl.CharUtil.IS_QUOTE;
import static org.jsonurl.CharUtil.IS_STRUCTCHAR;
import static org.jsonurl.CharUtil.hexDecode;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_CHAR;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_PCT_ENC;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_QSTR;
import static org.jsonurl.SyntaxException.Message.MSG_BAD_UTF8;
import static org.jsonurl.SyntaxException.Message.MSG_EXPECT_LITERAL;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import org.jsonurl.j2se.JavaValueFactory;

/**
 * This class provides static methods for performing a number of
 * actions on JSON&#x2192;URL text.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public final class JsonUrl { // NOPMD - ClassNamingConventions

    /**
     * single quote/apostrophe.
     */
    private static final char APOS = '\'';

    /**
     * The empty string.
     */
    private static final String EMPTY_STRING = "";

    /**
     * a namespace to hide private, parse-specific static fields and methods.
     */
    static final class Parse { //NOPMD - internal utility class
      
        private Parse() {
        }
        
        static NumberBuilder newNumberBuilder(
            ValueFactory<?,?,?,?,?,?,?,?,?,?> factory) {
            
            final NumberBuilder ret = new NumberBuilder();

            if (factory instanceof BigMathProvider) {
                //
                // use the factory as a BigMathProvider
                //
                ret.setBigMathProvider((BigMathProvider)factory);
            }

            return ret;
        }

        private static int percentDecode(
                CharSequence text,
                int off,
                int len) {

            if (off + 2 > len) {
                throw new SyntaxException(MSG_BAD_PCT_ENC, off);
            }

            int ch1 = hexDecode(text.charAt(off));
            int ch2 = hexDecode(text.charAt(off + 1));

            if (ch1 < 0 || ch2 < 0) {
                throw new SyntaxException(MSG_BAD_PCT_ENC, off);
            }

            return (ch1 << 4) | ch2;
        }
        
        /**
         * parse true, false, and null literals.
         *
         * @return true if the string matches one of the following JSON&#x2192;URL
         *      literal values: true, false, null.
         */
        private static String getTrueFalseNull(// NOPMD - CyclomaticComplexity
                CharSequence text,
                int start,
                int stop) {

            switch (stop - start) {
            case 4:
                switch (text.charAt(start)) {
                case 't':
                    if (text.charAt(start + 1) != 'r'
                            || text.charAt(start + 2) != 'u'
                            || text.charAt(start + 3) != 'e') {
                        return null;
                    }
                    return "true";

                case 'n':
                    if (text.charAt(start + 1) != 'u'
                            || text.charAt(start + 2) != 'l'
                            || text.charAt(start + 3) != 'l') {
                        return null;
                    }
                    return "null";

                default:
                    return null;
                }

            case 5:
                if (text.charAt(start) != 'f'
                        || text.charAt(start + 1) != 'a'
                        || text.charAt(start + 2) != 'l'
                        || text.charAt(start + 3) != 's'
                        || text.charAt(start + 4) != 'e') {
                    return null;
                }
                return "false";

            default:
                return null;
            }
        }

        /**
         * parse a string literal.
         * 
         * @see <a href="https://www.w3.org/International/O-URL-code.html">
         * https://www.w3.org/International/O-URL-code.html</a>
         * @see <a href="https://www.w3.org/International/unescape.java">
         * https://www.w3.org/International/unescape.java</a>
         */
        @SuppressWarnings({
            "PMD.AvoidLiteralsInIfCondition",
            "PMD.AvoidReassigningLoopVariables", // needed to properly consume pct-encoding
            "PMD.DataflowAnomalyAnalysis", // NOPMD - state needs to span the for loop
            "PMD.CyclomaticComplexity", // NOPMD - yup, decoding UTF-8 branches a lot
            "PMD.ExcessiveMethodLength", // side-effect of shared state across many branches
            "PMD.ModifiedCyclomaticComplexity",
            "PMD.NPathComplexity",
            "PMD.ShortVariable", // NOPMD
            "PMD.StdCyclomaticComplexity"})
        private static String string(
                StringBuilder dest,
                CharSequence text,
                int start,
                int stop,
                boolean quoted) {

            boolean needEndQuote = quoted;

            //
            // buffer to accumulate the result as it's parsed
            //
            final StringBuilder buf = dest == null
                    ? new StringBuilder(Math.min((stop - start) * 2, 1 << 3)) : dest;

            buf.setLength(0);

            int sumb = 0;
            int more = -1;

            //
            // sonarcloud for codesmell for loop label and recommends
            // refactoring to remove it.
            //
            // The label facilitates consistent error handling along with
            // a switch to be used for per-character behavior.
            //
            loop: for (int i = start; i < stop; i++) {
                char c;
                int b;

                c = text.charAt(i);

                switch (c) {
                case '\'':
                    if (quoted) {
                        needEndQuote = false;
                        break loop;
                    }
                    b = c;
                    break;
                case '+':
                    b = ' ';
                    break;
                case '%':
                    b = percentDecode(text, i + 1, stop);
                    i += 2;
                    break;
                default:
                    //
                    // Note: I am not checking CharUtil.CHARBITS[c]
                    // against IS_LITCHAR and IS_QSCHAR here. This method is not
                    // public, so it may not be called directly. The way the code
                    // is currently structured there is no path to here that
                    // doesn't first call parseLiteralLength, and
                    // parseLiteralLength *does* check against those bits.
                    //
                    // If that changes then proper checking will need to be added.
                    // I have not added it now because it would be impossible to
                    // test.
                    //
                    b = c;
                    break;
                }
                
                //
                // This is largely based on unescape.java referenced above,
                // however, that code is old and doesn't take into account
                // changes in behavior for UTF-16. This code does. 
                //
                // Decode byte b as UTF-8, sumb collects incomplete chars
                if ((b & 0xc0) == 0x80) {               // 10xxxxxx (continuation byte)
                    sumb = (sumb << 6) | (b & 0x3f);    // Add 6 bits to sumb
                    if (--more == 0) {                  // NOPMD
                        buf.appendCodePoint(sumb);      // Add char to sbuf
                    }
                } else if ((b & 0x80) == 0x00) {        // 0xxxxxxx (yields 7 bits)
                    buf.appendCodePoint(b);             // Store in sbuf
                } else if ((b & 0xe0) == 0xc0) {        // 110xxxxx (yields 5 bits)
                    sumb = b & 0x1f;
                    more = 1;                           // Expect 1 more byte
                } else if ((b & 0xf0) == 0xe0) {        // 1110xxxx (yields 4 bits)
                    sumb = b & 0x0f;
                    more = 2;                           // Expect 2 more bytes
                } else if ((b & 0xf8) == 0xf0) {        // 11110xxx (yields 3 bits)
                    sumb = b & 0x07;
                    more = 3;                           // Expect 3 more bytes
                } else {
                    // per rfc3629 everything else is invalid.
                    //
                    // Ideally I'd throw a MalformedInputException, but
                    // I want to throw something that extends RuntimeException
                    // and it does not.
                    //
                    // Also, by throwing SyntaxException I can also include the
                    // position.
                    //
                    throw new SyntaxException(MSG_BAD_UTF8, stop);
                }
            }
            if (needEndQuote) {
                //
                // this isn't actually possible because parseLiteralLength()
                // will throw an exception first. But, just in case...
                //
                throw new SyntaxException(MSG_BAD_QSTR, stop);
            }
            if (more > 0) {
                throw new SyntaxException(MSG_BAD_UTF8, stop);
            }
            
            //
            // removed check:
            //
            // if !quoted && !isEmptyUnquotedStringOK && buf.length() == 0
            //     throw new SyntaxException(MSG_EXPECT_LITERAL, start);
            //
            // because it's dead code. Everything that calls this method
            // has to check the length itself anyway.
            //

            return buf.toString();
        }
        
        /**
         * parse a literal value.
         * 
         * <p>This will parse a literal value from JSON&#x2192;URL text and return
         * it as a java.lang.String (as opposed to a ValueFactory String).
         * It will perform input validation and string literal decoding.
         *
         * @param buf a temporary buffer used to parse the value
         * @param num a valid NumberBuilder instance or null
         * @param text the text to be parsed
         * @param start the start index
         * @param stop the stop index
         */
        static String literalToJavaString(
                StringBuilder buf,
                NumberBuilder num,
                CharSequence text,
                int start,
                int stop,
                JsonUrlOptions options) {

            if (stop <= start) {
                if (JsonUrlOptions.isEmptyUnquotedKeyAllowed(options)) {
                    return EMPTY_STRING;
                }

                throw new SyntaxException(MSG_EXPECT_LITERAL, start);
            }

            if (JsonUrlOptions.isImpliedStringLiterals(options)) {
                return string(buf, text, start, stop, false);
            }

            if (text.charAt(start) == APOS) {
                //
                // quoted string
                //
                return string(buf, text, start + 1, stop, true);
            }

            String ret = getTrueFalseNull(text, start, stop);
            if (ret != null) {
                return ret;
            }

            //
            // It is not possible (with the current codebase) for num to be
            // null, so that check has been removed and I simply reset it.
            //
            num.reset();

            if (num.parse(text, start, stop)) {
                return text.subSequence(start, stop).toString();
            }

            //
            // unquoted string
            //
            return string(buf, text, start, stop, false);
        }

        /**
         * parse a literal value
         * 
         * <p>This will parse a literal value from JSON&#x2192;URL text. You may use
         * {@link org.jsonurl.JsonUrl#parseLiteralLength(CharSequence, int, int)
         * parseLiteralLength} to calculate the stop index.
         *
         * @param <V> value type
         * @param buf a temporary buffer used to parse the value
         * @param nbuilder a valid NumberBuilder instance or null
         * @param text the text to be parsed
         * @param start the start index
         * @param stop the stop index
         * @param factory a valid value factory
         * @param isEmptyUnquotedStringOK if true allow a zero length value
         * @param isImpliedStringLiteral if true assume literals are strings
         */
        static <V> V literal(
                StringBuilder buf,
                NumberBuilder nbuilder,
                CharSequence text,
                int start,
                int stop,
                ValueFactory<V,?,?,?,?,?,?,?,?,?> factory,
                JsonUrlOptions options) {

            if (stop <= start) {
                if (JsonUrlOptions.isEmptyUnquotedValueAllowed(options)) {
                    return factory.getString("");
                }

                throw new SyntaxException(MSG_EXPECT_LITERAL, start);
            }
            
            if (JsonUrlOptions.isImpliedStringLiterals(options)) {
                return factory.getString(
                        string(buf, text, start, stop, false));
            }

            if (text.charAt(start) == APOS) {
                //
                // quoted string
                //
                return factory.getString(
                        string(buf, text, start + 1, stop, true));
            }

            V ret = factory.getTrueFalseNull(text, start, stop);
            if (ret != null) {
                return ret;
            }
            
            final NumberBuilder num = nbuilder == null
                    ? newNumberBuilder(factory) : nbuilder.reset(); 

            if (num.parse(text, start, stop)) {
                return factory.getNumber(num);
            }

            //
            // unquoted string
            //
            return factory.getString(string(buf, text, start, stop, false));
        }
    }

    /**
     * a namespace to hide private, encode-specific static fields and methods.
     */
    static final class Encode {

        /**
         * Enumeration of strings "types" based on what characters are in them.
         */
        enum StringEncoding {
            SAFE_ASIS,
            NO_QUOTE_WITH_SPACE,
            QUOTE_NO_SPACE,
            QUOTE_WITH_SPACE,
            FULL_ENCODING
        }

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

        @SuppressWarnings({
            "PMD.CyclomaticComplexity",
            "PMD.DataflowAnomalyAnalysis", // maintain state across for loop
            "PMD.ShortVariable"})
        private static StringEncoding getStringEncoding(
                CharSequence text,
                int start,
                int end) {

            if (text.charAt(start) == APOS) {
                //
                // edge case: if the string starts with a literal quote then it
                // must be percent encoded because a parser could not otherwise
                // tell the difference between that and a quoted string.
                //
                return StringEncoding.FULL_ENCODING;
            }

            final int strmask = CharUtil.BIS_ENC_STRSAFE;
            final int spacemask = CharUtil.IS_SPACE;
            int strbits = strmask;
            int spacebits = 0;

            for (int i = start; i < end; i++) {
                char c = text.charAt(i);
                if (c > CHARBITS_LENGTH) {
                    return StringEncoding.FULL_ENCODING;
                }

                //
                // track space and non-space values with separate masks.
                // CharUtil.CHARBITS[' '] is specifically setup to allow this.
                // If you change this code be sure to update that value as
                // necessary.
                //
                strbits &= CHARBITS[c] & strmask;
                spacebits |= CHARBITS[c] & spacemask;
            }

            switch (strbits | spacebits) {
            case CharUtil.BIS_ENC_STRSAFE:
            case CharUtil.IS_ENC_STRSAFE:
                return StringEncoding.SAFE_ASIS;

            case CharUtil.BIS_ENC_STRSAFE | CharUtil.IS_SPACE:
            case CharUtil.IS_ENC_STRSAFE | CharUtil.IS_SPACE:
                return StringEncoding.NO_QUOTE_WITH_SPACE;

            case CharUtil.IS_ENC_QSTRSAFE:
                return StringEncoding.QUOTE_NO_SPACE;

            case CharUtil.IS_ENC_QSTRSAFE | CharUtil.IS_SPACE:
                return StringEncoding.QUOTE_WITH_SPACE;

            default:
                return StringEncoding.FULL_ENCODING;
            }
        }

        /**
         * Hex encode as UTF-8 .
         */
        @SuppressWarnings({
            "PMD.AvoidLiteralsInIfCondition",
            "PMD.AvoidReassigningParameters", // needed for edge case
            "PMD.CyclomaticComplexity", // yup, encoding UTF-8 branches a lot
            "PMD.ModifiedCyclomaticComplexity",
            "PMD.NPathComplexity",
            "PMD.ShortVariable",
            "PMD.StdCyclomaticComplexity"})
        private static void encode(
                Appendable dest,
                CharSequence text,
                int start,
                int end,
                boolean quoted,
                boolean isImpliedStringLiteral) throws IOException {

            if (!isImpliedStringLiteral
                    && start < end
                    && text.charAt(start) == APOS) {

                //
                // edge case: if the first character is a literal quote then
                // it must always be encoded
                //
                dest.append("%27");
                start++;
            }
            
            final String[] hexEncode = quoted // NOPMD
                ? CharUtil.HEXENCODE_QUOTED : CharUtil.HEXENCODE_UNQUOTED;

            for (int i = start; i < end; i++) {
                char c = text.charAt(i);

                if (Character.isLowSurrogate(c)) {
                    throw new MalformedInputException(i);
                }

                final int cp;
                if (Character.isHighSurrogate(c)) {
                    i++; // NOPMD - needed to encode properly

                    if (i == end) {
                        throw new MalformedInputException(i);
                    }

                    char c2 = text.charAt(i);

                    if (!Character.isLowSurrogate(c2)) {
                        throw new MalformedInputException(i);
                    }

                    cp = Character.toCodePoint(c, c2);

                } else {
                    cp = c;
                }
                
                if (cp < 0x80) {
                    dest.append(hexEncode[cp]);

                } else if (cp < 0x800) {
                    dest.append(hexEncode[0xC0 | (cp >> 6)]);
                    dest.append(hexEncode[0x80 | (cp & 0x3F)]);

                } else if (cp < 0x10000) {
                    dest.append(hexEncode[0xE0 | (cp >> 12)]);
                    dest.append(hexEncode[0x80 | ((cp >> 6) & 0x3F)]);
                    dest.append(hexEncode[0x80 | (cp & 0x3F)]);

                } else if (cp < 0x200000) {
                    dest.append(hexEncode[0xF0 | (cp >> 18)]);
                    dest.append(hexEncode[0x80 | ((cp >> 12) & 0x3F)]);
                    dest.append(hexEncode[0x80 | ((cp >> 6) & 0x3F)]);
                    dest.append(hexEncode[0x80 | (cp & 0x3F)]);

                } else {
                    throw new MalformedInputException(i);
                }
            }
        }
    }

    private JsonUrl() {
    }

    /**
     * Parse the length of a quoted string literal.
     */
    private static int parseQuotedStringLength(
            CharSequence text,
            int start,
            int stop) {

        int ret = 0; // NOPMD

        for (int i = start; i < stop; i++) {
            char cur = text.charAt(i);

            if (cur < CHARBITS_LENGTH) {
                switch (CHARBITS[cur] & (IS_QSCHAR | IS_QUOTE)) {
                case IS_QSCHAR:
                    ret++;
                    continue;
                case IS_QUOTE:
                    return ret + 2;
                default:
                    break;
                }
            }
            throw new SyntaxException(MSG_BAD_CHAR, start);
        }
        throw new SyntaxException(MSG_BAD_QSTR, start);
    }
    
    /**
     * Parse the length of an unquoted literal.
     */
    private static int parseUnquotedLiteralLength(
            CharSequence text,
            int start,
            int stop) {

        int ret = 0; // NOPMD

        for (int i = start; i < stop; i++) {
            char cur = text.charAt(i);

            if (cur < CHARBITS_LENGTH) {
                switch (CHARBITS[cur] & (IS_LITCHAR | IS_STRUCTCHAR)) {
                case IS_LITCHAR:
                    ret++;
                    continue;
                case IS_STRUCTCHAR:
                    return ret;
                default:
                    break;
                }
            }
            throw new SyntaxException(MSG_BAD_CHAR, i);
        }

        return ret;
    }

    /**
     * Determine the length of a literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteralLength(CharSequence, int, int, org.jsonurl.SyntaxException.Message)
     * parseLiteralLength(s, start, stop, null)}.
     * 
     * @param text text to be parsed
     * @param start start position in text
     * @param stop stop position in text
     * @return the length of the literal value
     */
    public static int parseLiteralLength(
            CharSequence text,
            int start,
            int stop) {
        return parseLiteralLength(text, start, stop, null);
    }

    /**
     * Determine the length of a literal value.
     *
     * <p>This static method may be used to determine the length of a
     * literal value. The ``start'' parameter indicates the starting
     * index into the text and ``stop'' indicates the stop index.
     * 
     * <p>This method will stop when the first structural character
     * is found or when ``stop'' is reached, whichever comes first.
     * Note, because it is static no limits are enforced as is with
     * {@link org.jsonurl.Parser#parse(CharSequence, ValueFactory)
     * parse}.
     * 
     * @param text text to be parsed
     * @param start start position in text
     * @param stop stop position in text
     * @param errmsg error message for thrown {@link SyntaxException}
     * @return the length of the literal value
     */
    public static int parseLiteralLength(
            CharSequence text,
            int start,
            int stop,
            SyntaxException.Message errmsg) {

        if (stop <= start) {
            if (errmsg != null) {
                throw new SyntaxException(errmsg, start);
            }
            return 0;
        }

        if (text.charAt(start) == APOS) {
            return parseQuotedStringLength(text, start + 1, stop);
        }

        return parseUnquotedLiteralLength(text, start, stop);
    }

    /**
     * Determine the length of a literal value.
     * @see #parseLiteralLength(CharSequence, int, int, org.jsonurl.SyntaxException.Message)
     */
    public static int parseLiteralLength(CharSequence text) {
        return parseLiteralLength(text, 0, text.length(), null);
    }

    /**
     * Parse a single literal value.
     *
     * <p>This static method may be used to parse a single literal:
     * <ul>
     * <li>boolean (true or false)
     * <li>null
     * <li>number
     * <li>string
     * </ul>
     * No limits are enforced (as is with
     * {@link org.jsonurl.Parser#parse(CharSequence, ValueFactory)
     * parse}) because this method is static.
     * 
     * <p>If {@code isImpliedStringLiteral} is true then then
     * {@code true}, {@code false}, {@code null}, and number literals
     * will not be recognized. All literals are assumed to be strings.
     * 
     * <p>Note, the third argument is a length not a position. It indicates
     * the number of characters to be parsed.
     *
     * @param text text to be parsed
     * @param start start position in text
     * @param length number of characters to parse
     * @param factory a valid ValueFactory
     * @param options parse options
     * @return an object for the parsed literal
     */
    public static <V> V parseLiteral(
            CharSequence text,
            int start,
            int length,
            ValueFactory<V,?,?,?,?,?,?,?,?,?> factory,
            JsonUrlOptions options) {

        //
        // Note: not checking length == 0 here. That case is handled properly
        // by both parseLiteralLength() and Parse.literal().
        //
        
        int stop = start + length;
        
        final SyntaxException.Message errmsg =
                JsonUrlOptions.isEmptyUnquotedValueAllowed(options)
                    ? null : MSG_EXPECT_LITERAL;

        parseLiteralLength(text, start, stop, errmsg);

        return Parse.literal(
                null,
                null,
                text,
                start,
                stop,
                factory,
                options);
    }

    /**
     * Parse a single literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteral(CharSequence, int, int, ValueFactory, JsonUrlOptions)
     * parseLiteral(s, start, length, JavaValueFactory.PRIMITIVE, false, false)}.
     *
     * @see #parseLiteral(CharSequence, int, int, ValueFactory, JsonUrlOptions)
     * @see JavaValueFactory#PRIMITIVE
     */
    public static Object parseLiteral(
            CharSequence text,
            int start,
            int length) {

        return parseLiteral(
                text,
                start,
                length,
                JavaValueFactory.PRIMITIVE,
                null);
    }

    /**
     * Parse a single literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteral(CharSequence, int, int, ValueFactory, JsonUrlOptions)
     * parseLiteral(s, 0, s.length(), JavaValueFactory.PRIMITIVE, false, false)}.
     *
     * @see #parseLiteral(CharSequence, int, int, ValueFactory, JsonUrlOptions)
     * @see JavaValueFactory#PRIMITIVE
     */
    public static Object parseLiteral(CharSequence text) {
        return parseLiteral(
                text,
                0,
                text.length(),
                JavaValueFactory.PRIMITIVE,
                null);
    }

    /**
     * Parse a single literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteral(CharSequence, int, int, ValueFactory, JsonUrlOptions)
     * parseLiteral(s, 0, s.length(), factory, false, false)}.
     * @see #parseLiteral(CharSequence, int, int, ValueFactory, JsonUrlOptions)
     */
    public static <V> V parseLiteral(
            CharSequence text,
            ValueFactory<V,?,?,?,?,?,?,?,?,?> factory) {
        return parseLiteral(text, 0, text.length(), factory, null);
    }

    /**
     * Append the given CharSequence as a string literal.
     *
     * @param <T>   destination type
     * @param dest  destination
     * @param text  source
     * @param start offset in source
     * @param end   length of source
     * @return true if dest was modified
     */
    public static <T extends Appendable> boolean appendLiteral(// NOPMD - CyclomaticComplexity
            T dest,
            CharSequence text,
            int start,
            int end,
            boolean isKey,
            JsonUrlOptions options) throws IOException {

        if (end <= start) {
            //
            // empty string
            //
            boolean emptyOK = isKey
                    ? JsonUrlOptions.isEmptyUnquotedKeyAllowed(options)
                    : JsonUrlOptions.isEmptyUnquotedValueAllowed(options);

            if (emptyOK) {
                return false;
            }

            if (JsonUrlOptions.isImpliedStringLiterals(options)) {
                throw new IOException("implied strings: unexpected empty string");
            }

            //
            // the empty string must be quoted
            //
            dest.append("''");
            return true;
        }

        if (JsonUrlOptions.isImpliedStringLiterals(options)) {
            Encode.encode(dest, text, start, end, false, true);
            return true;
        }

        //
        // true, false, null, and number literals must be quoted
        //
        boolean isLiteral = Parse.getTrueFalseNull(text, start, end) != null
                || NumberBuilder.isNumber(text, start, end);

        if (isLiteral) {
            if (isKey) {
                //
                // keys are always assumed to be strings
                //
                dest.append(text, start, end);

            } else if (Encode.contains(text, start, end, '+')) {
                Encode.encode(dest, text, start, end, false, false);

            } else {
                //
                // this is a literal without a plus; it's safe to represent
                // it as a quoted string.
                //
                dest.append('\'').append(text, start, end).append('\'');
            }
            return true;
        }

        //
        // figure out the most efficient way to safely encode the string
        //
        Encode.StringEncoding enc = Encode.getStringEncoding(text, start, end);
        
        switch (enc) { // NOPMD - SwitchStmtsShouldHaveDefault
        case FULL_ENCODING:
        case NO_QUOTE_WITH_SPACE:
            Encode.encode(dest, text, start, end, false, false);
            break;
        case QUOTE_NO_SPACE:
            dest.append('\'').append(text, start, end).append('\'');
            break;
        case QUOTE_WITH_SPACE:
            dest.append('\'');
            Encode.encode(dest, text, start, end, true, false);
            dest.append('\'');
            break;
        case SAFE_ASIS:
            dest.append(text, start, end);
            break;
        }

        return true;
    }
}
