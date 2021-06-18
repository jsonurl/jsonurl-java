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

package org.jsonurl.util;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.BitSet;
import org.jsonurl.stream.CharIterator;

/**
 * A utility class for encoding/decoding percent encoded UTF-8 sequences.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public final class PercentCodec { // NOPMD - ClassNamingConventions

    /**
     * BitSet of literal characters that are valid in a URL. 
     */
    private static final BitSet URL_QUERY_SAFE = new BitSet(128);

    static {
        URL_QUERY_SAFE.set(33);
        URL_QUERY_SAFE.set(36, 60);
        URL_QUERY_SAFE.set(61);
        URL_QUERY_SAFE.set(63, 91);
        URL_QUERY_SAFE.set(95);
        URL_QUERY_SAFE.set(97, 123);
        URL_QUERY_SAFE.set(126);
    }
    
    private PercentCodec() {
        // Utility class
    }

    /**
     * Get the next codepoint, percent decoding if necessary.
     * @param text a valid CharIterator 
     * @return a UNICODE codepoint
     */
    public static int decode(CharIterator text) throws IOException {
        return decode(text, true);
    }

    /**
     * Get the next codepoint, percent decoding if necessary.
     * @param text a valid CharIterator 
     * @param decodePlus if true, decode {@code +} as though it
     *     was {@code %20}
     * @return a UNICODE codepoint
     */
    @SuppressWarnings({
        // See SuppressWarnings.md#complexity
        "PMD.CyclomaticComplexity",
        "PMD.NPathComplexity",
        "java:S3776",
        
        // once you understand UTF-8 it's far more readable with the
        // literals there
        "PMD.AvoidLiteralsInIfCondition"
    })
    public static int decode(
            CharIterator text,
            boolean decodePlus) throws IOException {
        
        final int chr = text.nextChar();

        switch (chr) {
        case CharIterator.EOF:
            return CharIterator.EOF;
        case '+':
            return decodePlus ? ' ' : '+';
        case '%':
            break;
        default:
            //
            // Validate that the literal character (not the decoded value)
            // is allowed in a query string.
            //
            if (URL_QUERY_SAFE.get(chr)) {
                return chr;
            }

            throw new IOException("unexpected character");
        }

        int sum = 0;
        int more = -1;

        for (;;) {
            final int octet = hexDecodeOctet(text);

            //
            // Decode the octet literal as UTF-8
            //
            if ((octet & 0xc0) == 0x80) {             // 10xxxxxx (continuation byte)
                sum = (sum << 6) | (octet & 0x3f);    // Add 6 bits to sum
                if (--more == 0) {                    // NOPMD
                    return sum;                       // return
                }
            } else if ((octet & 0x80) == 0x00) {      // 0xxxxxxx (yields 7 bits)
                return octet;                         // return
            } else if ((octet & 0xe0) == 0xc0) {      // 110xxxxx (yields 5 bits)
                sum = octet & 0x1f;
                more = 1;                             // Expect 1 more byte
            } else if ((octet & 0xf0) == 0xe0) {      // 1110xxxx (yields 4 bits)
                sum = octet & 0x0f;
                more = 2;                             // Expect 2 more bytes
            } else if ((octet & 0xf8) == 0xf0) {      // 11110xxx (yields 3 bits)
                sum = octet & 0x07;
                more = 3;                             // Expect 3 more bytes
            } else {
                //
                // per rfc3629 everything else is invalid.
                //
                throw new MalformedInputException(more);
            }

            if (text.nextChar() != '%') {
                //
                // If I get here then more > 0 and I'm expecting more input
                //
                throw new MalformedInputException(more);
            }
        }
    }

    /**
     * Decode two hex digits and turn them into an octet.
     */
    private static int hexDecodeOctet(CharIterator text) throws IOException {
        int high = hexDecode(text.nextChar());
        int low = hexDecode(text.nextChar());
        return (high << 4) | low;
    }

    /**
     * Decode the given hex character to it's numerical value.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static int hexDecode(int chr) throws IOException {
        switch (chr) {
        case '0': return 0;
        case '1': return 1;
        case '2': return 2;
        case '3': return 3;
        case '4': return 4;
        case '5': return 5;
        case '6': return 6;
        case '7': return 7;
        case '8': return 8;
        case '9': return 9;

        case 'A':
        case 'a':
            return 10;
        case 'B':
        case 'b':
            return 11;
        case 'C':
        case 'c':
            return 12;
        case 'D':
        case 'd':
            return 13;
        case 'E':
        case 'e':
            return 14;
        case 'F':
        case 'f':
            return 15;
        default:
            throw new MalformedInputException(3);
        }
    }


    /**
     * Percent encode the given CharSequence as a sequence of UTF-8 octets.
     * @param dest destination
     * @param hexEncode lookup table for each character
     * @param text source text
     * @param start start position
     * @param end end position
     */
    @SuppressWarnings({
        // See SuppressWarnings.md
        "PMD.AvoidReassigningLoopVariables", "java:S127"
    })
    public static void encode(
            Appendable dest,
            String[] hexEncode,
            CharSequence text,
            int start,
            int end) throws IOException {
        
        for (int i = start; i < end; i++) {
            char chr = text.charAt(i);

            if (Character.isLowSurrogate(chr)) {
                throw new MalformedInputException(i);
            }

            final int codePoint;
            if (Character.isHighSurrogate(chr)) {
                i++;

                if (i == end) {
                    throw new MalformedInputException(i);
                }

                char low = text.charAt(i);

                if (!Character.isLowSurrogate(low)) {
                    throw new MalformedInputException(i);
                }

                codePoint = Character.toCodePoint(chr, low);

            } else {
                codePoint = chr;
            }

            encode(dest, codePoint, hexEncode, i);
        }
    }

    /**
     * Percent encode the given codepoint as a sequence of UTF-8 octets.
     * @param dest destination
     * @param codePoint a UNICODE codepoint
     * @param hexEncode lookup table
     * @param position text position
     *     (passed to
     *     {@link MalformedInputException#MalformedInputException(int)
     *     MalformedInputException})
     */
    public static void encode(
            Appendable dest,
            int codePoint,
            String[] hexEncode,
            int position) throws IOException {
        
        if (codePoint < 0x80) { // NOPMD - AvoidLiteralsInIfCondition
            dest.append(hexEncode[codePoint]);

        } else if (codePoint < 0x800) { // NOPMD - AvoidLiteralsInIfCondition
            dest.append(hexEncode[0xC0 | (codePoint >> 6)]);
            dest.append(hexEncode[0x80 | (codePoint & 0x3F)]);

        } else if (codePoint < 0x10000) { // NOPMD - AvoidLiteralsInIfCondition
            dest.append(hexEncode[0xE0 | (codePoint >> 12)]);
            dest.append(hexEncode[0x80 | ((codePoint >> 6) & 0x3F)]);
            dest.append(hexEncode[0x80 | (codePoint & 0x3F)]);

        } else if (codePoint < 0x200000) { // NOPMD - AvoidLiteralsInIfCondition
            dest.append(hexEncode[0xF0 | (codePoint >> 18)]);
            dest.append(hexEncode[0x80 | ((codePoint >> 12) & 0x3F)]);
            dest.append(hexEncode[0x80 | ((codePoint >> 6) & 0x3F)]);
            dest.append(hexEncode[0x80 | (codePoint & 0x3F)]);

        } else {
            throw new MalformedInputException(position);
        }
    }

}
