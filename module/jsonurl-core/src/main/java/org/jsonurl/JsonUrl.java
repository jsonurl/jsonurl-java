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

import static org.jsonurl.CharUtil.CHARBITS;
import static org.jsonurl.CharUtil.IS_LITCHAR;
import static org.jsonurl.CharUtil.IS_QSCHAR;
import static org.jsonurl.CharUtil.IS_QUOTE;
import static org.jsonurl.CharUtil.IS_STRUCTCHAR;
import static org.jsonurl.CharUtil.hexDecode;
import static org.jsonurl.SyntaxException.ERR_MSG_BADCHAR;
import static org.jsonurl.SyntaxException.ERR_MSG_BADPCTENC;
import static org.jsonurl.SyntaxException.ERR_MSG_BADQSTR;
import static org.jsonurl.SyntaxException.ERR_MSG_BADUTF8;
import static org.jsonurl.SyntaxException.ERR_MSG_EXPECT_LITERAL;
import static org.jsonurl.SyntaxException.ERR_MSG_NOTEXT;

import java.io.IOException;
import java.nio.charset.MalformedInputException;

/**
 * This class provides static methods for performing a number of
 * actions on JSON&#x2192;URL text.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public final class JsonUrl {

    /**
     * a namespace to hide private, parse-specific static fields and methods.
     */
    static final class Parse { //NOPMD - internal utility class
      
        private Parse() {
        }

        private static final int percentDecode(
                CharSequence s,
                int off,
                int len) {

            if (off + 2 > len) {
                throw new SyntaxException(ERR_MSG_BADPCTENC, off);
            }

            int c1 = hexDecode(s.charAt(off));
            int c2 = hexDecode(s.charAt(off + 1));

            if (c1 < 0 || c2 < 0) {
                throw new SyntaxException(ERR_MSG_BADPCTENC, off);
            }

            return ((c1 << 4) | c2);
        }
        
        /**
         * parse true, false, and null literals.
         *
         * @return true if the string matches one of the following JSON&#x2192;URL
         *      literal values: true, false, null.
         */
        @SuppressWarnings("PMD")
        private static final String getTrueFalseNull(
                CharSequence s,
                int start,
                int stop) {

            switch (stop - start) {
            case 4:
                switch (s.charAt(start)) {
                case 't':
                    if (s.charAt(start + 1) != 'r'
                            || s.charAt(start + 2) != 'u'
                            || s.charAt(start + 3) != 'e') {
                        return null;
                    }
                    return "true";

                case 'n':
                    if (s.charAt(start + 1) != 'u'
                            || s.charAt(start + 2) != 'l'
                            || s.charAt(start + 3) != 'l') {
                        return null;
                    }
                    return "null";

                default:
                    return null;
                }

            case 5:
                if (s.charAt(start) != 'f'
                        || s.charAt(start + 1) != 'a'
                        || s.charAt(start + 2) != 'l'
                        || s.charAt(start + 3) != 's'
                        || s.charAt(start + 4) != 'e') {
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
        @SuppressWarnings("PMD")
        private static final String string(
                StringBuilder buf,
                CharSequence s,
                int start,
                int stop,
                boolean quoted) {

            boolean needEndQuote = quoted;

            if (buf == null) {
                buf = new StringBuilder(Math.min((stop - start) * 2, 1 << 3));
            } else {
                buf.setLength(0);
            }
            
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

                c = s.charAt(i);

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
                    b = percentDecode(s, i + 1, stop);
                    i += 2;
                    break;
                default:
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
                    if (--more == 0) {
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
                    throw new IllegalArgumentException("utf-8 decode error");
                }
            }
            if (needEndQuote) {
                throw new SyntaxException(ERR_MSG_BADQSTR, stop);
            }
            if (more > 0) {
                throw new SyntaxException(ERR_MSG_BADUTF8, stop);
            }

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
         * @param s the text to be parsed
         * @param start the start index
         * @param stop the stop index
         */
        @SuppressWarnings("PMD")
        static final String literalToJavaString(
                StringBuilder buf,
                NumberBuilder num,
                CharSequence s,
                int start,
                int stop) {

            String ret;
            
            if (s.charAt(start) == '\'') {
                return string(buf, s, start + 1, stop, true);
            }

            if ((ret = getTrueFalseNull(s, start, stop)) != null) {
                return ret;
            }
            
            if (num == null) {
                num = new NumberBuilder();

            } else {
                num.reset();
            }

            if (num.parse(s, start, stop)) {
                return num.toString();
            }

            return string(buf, s, start, stop, false);
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
         * @param num a valid NumberBuilder instance or null
         * @param s the text to be parsed
         * @param start the start index
         * @param stop the stop index
         * @param factory a valid value factory
         */
        static final <V> V literal(
                StringBuilder buf,
                NumberBuilder num,
                CharSequence s,
                int start,
                int stop,
                ValueFactory<V,?,?,?,?,?,?,?,?,?> factory) {

            if (s.charAt(start) == '\'') {
                return factory.getString(string(buf, s, start + 1, stop, true));
            }

            V ret = factory.getTrueFalseNull(s, start, stop);

            if (ret != null) {
                return ret;
            }

            if (num == null) {
                num = new NumberBuilder();

            } else {
                num.reset();
            }

            if (num.parse(s, start, stop)) {
                return factory.getNumber(num);
            }

            return factory.getString(string(buf, s, start, stop, false));
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

        private static final boolean contains(
                CharSequence s,
                int start,
                int end,
                char c) {
            
            for (int i = start; i < end; i++) {
                if (c == s.charAt(i)) {
                    return true;
                }
            }
            
            return false;
        }

        @SuppressWarnings("PMD")
        private static final StringEncoding getStringEncoding(
                CharSequence s,
                int start,
                int end) {

            final int strmask = CharUtil.BIS_ENC_STRSAFE;
            final int spacemask = CharUtil.IS_SPACE;
            int strbits = strmask;
            int spacebits = 0;

            if (s.charAt(start) == '\'') {
                //
                // edge case: if the string starts with a quote then it must
                // be percent encoded because a parser could not otherwise tell
                // the difference between that and a quoted string.
                //
                return StringEncoding.FULL_ENCODING;
            }

            for (int i = start; i < end; i++) {
                char c = s.charAt(i);
                if (c > 127) {
                    return StringEncoding.FULL_ENCODING;
                }
                strbits &= CharUtil.CHARBITS[c] & strmask;
                spacebits |= CharUtil.CHARBITS[c] & spacemask;
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
        @SuppressWarnings("PMD")
        private static final void encode(
                Appendable dest,
                CharSequence s,
                int start,
                int end) throws IOException {
            
            for (int i = start; i < end; i++) {
                char c = s.charAt(i);
                int cp;
                
                if (Character.isLowSurrogate(c)) {
                    throw new MalformedInputException(i);
                }
                if (Character.isHighSurrogate(c)) {
                    if (++i == end) {
                        throw new MalformedInputException(i);
                    }

                    char c2 = s.charAt(i);

                    if (!Character.isLowSurrogate(c2)) {
                        throw new MalformedInputException(i);
                    }

                    cp = Character.toCodePoint(c, c2);

                } else {
                    cp = c;
                }
                
                if (cp < 0x80) {
                    dest.append(CharUtil.HEXENCODE[cp]);

                } else if (cp < 0x800) {
                    dest.append(CharUtil.HEXENCODE[(0xC0 | (cp >> 6))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | (cp & 0x3F))]);

                } else if (cp < 0x10000) {
                    dest.append(CharUtil.HEXENCODE[(0xE0 | (cp >> 12))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 6) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | (cp & 0x3F))]);

                } else if (cp < 0x200000) {
                    dest.append(CharUtil.HEXENCODE[(0xF0 | (cp >> 18))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 12) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 6) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | (cp & 0x3F))]);

                } else if (cp < 0x4000000) {
                    dest.append(CharUtil.HEXENCODE[(0xF8 | (cp >> 24))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 18) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 12) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 6) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | (cp & 0x3F))]);

                } else if (cp < 0x8000000) {
                    dest.append(CharUtil.HEXENCODE[(0xFC | (cp >> 30))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 24) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 18) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 12) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | ((cp >> 6) & 0x3F))]);
                    dest.append(CharUtil.HEXENCODE[(0x80 | (cp & 0x3F))]);

                } else {
                    throw new MalformedInputException(i);
                }
            }
        }
    }

    private JsonUrl() {
    }

    /**
     * Determine the length of a literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteralLength(CharSequence, int, int, String)
     * parseLiteralLength(s, start, stop, null)}.
     * 
     * @param s text to be parsed
     * @param start start position in text
     * @param stop stop position in text
     * @return the length of the literal value
     */
    public static final int parseLiteralLength(
            CharSequence s,
            int start,
            int stop) {
        return parseLiteralLength(s, start, stop, null);
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
     * {@link org.jsonurl.Parser#parse(CharSequence, int, int) parse}.
     * 
     * @param s text to be parsed
     * @param start start position in text
     * @param stop stop position in text
     * @param errmsg error message for thrown {@link SyntaxException}
     * @return the length of the literal value
     */
    @SuppressWarnings("PMD")
    public static final int parseLiteralLength(
            CharSequence s,
            int start,
            int stop,
            String errmsg) {
        
        int ret = 0;

        if (start == stop) {
            return 0;
        }

        if (s.charAt(start) == '\'') {
            //
            // quoted string
            //
            for (start++; start < stop; start++) {
                char c = s.charAt(start);

                if (c < 128) {
                    switch (CHARBITS[c] & (IS_QSCHAR | IS_QUOTE)) {
                    case IS_QSCHAR:
                        ret++;
                        continue;
                    case IS_QUOTE:
                        return ret + 2;
                    default:
                        break;
                    }
                }
                throw new SyntaxException(ERR_MSG_BADCHAR, start);
            }
            throw new SyntaxException(ERR_MSG_BADQSTR, start);
        }

        for (; start < stop; start++) {
            char c = s.charAt(start);

            if (c < 128) {
                switch (CHARBITS[c] & (IS_LITCHAR | IS_STRUCTCHAR)) {
                case IS_LITCHAR:
                    ret++;
                    continue;
                case IS_STRUCTCHAR:
                    return ret;
                default:
                    break;
                }
            }
            throw new SyntaxException(ERR_MSG_BADCHAR, start);
        }
        
        if (start != stop && errmsg != null) {
            throw new SyntaxException(errmsg, stop);
        }
        return ret;
    }

    /**
     * Determine the length of a literal value.
     * @see #parseLiteralLength(CharSequence, int, int, String)
     */
    public static final int parseLiteralLength(CharSequence s) {
        return parseLiteralLength(s, 0, s.length(), null);
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
     * {@link org.jsonurl.Parser#parse(CharSequence, int, int) parse})
     * because this method is static.
     * 
     * <p>Note, the third argument is a length not a position. It indicates
     * the number of characters to be parsed.
     *
     * @param s text to be parsed
     * @param start start position in text
     * @param length number of characters to parse
     * @return an object for the parsed literal
     */
    public static final <V> V parseLiteral(
            CharSequence s,
            int start,
            int length,
            ValueFactory<V,?,?,?,?,?,?,?,?,?> factory) {

        if (length == 0) {
            throw new SyntaxException(ERR_MSG_NOTEXT);
        }

        int stop = start + length;
        parseLiteralLength(s, start, stop, ERR_MSG_EXPECT_LITERAL);

        return Parse.literal(null, null, s, start, stop, factory);
    }

    /**
     * Parse a single literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteral(CharSequence, int, int, ValueFactory)
     * parseLiteral(s, start, length, JavaValueFactory.PRIMITIVE)}.
     * @see JsonUrl#parseLiteral(CharSequence, int, int, ValueFactory)
     */
    public static final Object parseLiteral(
            CharSequence s,
            int start,
            int length) {
        return parseLiteral(s, start, length, JavaValueFactory.PRIMITIVE);
    }

    /**
     * Parse a single literal value.
     *
     * <p>This simply calls
     * {@link #parseLiteral(CharSequence, int, int, ValueFactory)
     * parseLiteral(s, 0, s.length(), JavaValueFactory.PRIMITIVE)}.
     * @see JsonUrl#parseLiteral(CharSequence, int, int, ValueFactory)
     */
    public static final Object parseLiteral(CharSequence s) {
        return parseLiteral(s, 0, s.length(), JavaValueFactory.PRIMITIVE);
    }

    /**
     * Parse a single literal value.
     *
     * @see JsonUrl#parseLiteral(CharSequence, int, int, ValueFactory)
     */
    public static final <V> V parseLiteral(
            CharSequence s,
            ValueFactory<V,?,?,?,?,?,?,?,?,?> factory) {
        return parseLiteral(s, 0, s.length(), factory);
    }

    /**
     * Append a string literal
     *
     * <p>This method will append the given CharSequence as a string literal.
     *
     * @param <T>   destination type
     * @param dest  destination
     * @param s     source
     * @param start offset in source
     * @param end   length of source
     * @return dest
     */
    @SuppressWarnings("PMD")
    public static <T extends Appendable> T appendLiteral(
            T dest,
            CharSequence s,
            int start,
            int end,
            boolean isKey) throws IOException {

        //
        // the empty string must be quoted
        //
        if (end <= start) {
            dest.append("''");
            return dest;
        }

        //
        // true, false, null, and number values must be quoted
        //
        boolean isLiteral = Parse.getTrueFalseNull(s, start, end) != null
                || NumberBuilder.isNumber(s, start, end);

        if (isLiteral) {
            if (isKey) {
                //
                // keys are always assumed to be strings
                //
                dest.append(s, start, end);

            } else if (!Encode.contains(s, start, end, '+')) {
                //
                // this is a literal without a plus; it's safe to represent
                // it as a quoted string.
                //
                dest.append('\'').append(s, start, end).append('\'');

            } else {
                Encode.encode(dest, s, start, end);
            }
            return dest;
        }

        //
        // figure out the most efficient way to safely encode the string
        //
        Encode.StringEncoding enc = Encode.getStringEncoding(s, start, end);
        
        switch (enc) {
        case FULL_ENCODING:
        case NO_QUOTE_WITH_SPACE:
            Encode.encode(dest, s, start, end);
            break;
        case QUOTE_NO_SPACE:
            dest.append('\'').append(s, start, end).append('\'');
            break;
        case QUOTE_WITH_SPACE:
            dest.append('\'');
            Encode.encode(dest, s, start, end);
            dest.append('\'');
            break;
        case SAFE_ASIS:
            dest.append(s, start, end);
            break;
        }

        return dest;
    }
}
