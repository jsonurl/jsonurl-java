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

/**
 * Character metadata.
 */
final class CharUtil {

    /**
     * HEX encoding table for quoted strings.
     */
    static final String[] HEXENCODE_QUOTED = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
        "+",   "!",   "%22", "%23", "$",   "%25", "%26", "%27",
        "(",   ")",   "*",   "%2B", ",",   "-",   ".",   "/",
        "0",   "1",   "2",   "3",   "4",   "5",   "6",   "7",
        "8",   "9",   ":",   ";",   "%3C", "%3D", "%3E", "?",
        "@",   "A",   "B",   "C",   "D",   "E",   "F",   "G",
        "H",   "I",   "J",   "K",   "L",   "M",   "N",   "O",
        "P",   "Q",   "R",   "S",   "T",   "U",   "V",   "W",
        "X",   "Y",   "Z",   "%5B", "%5C", "%5D", "%5E", "_",
        "%60", "a",   "b",   "c",   "d",   "e",   "f",   "g",
        "h",   "i",   "j",   "k",   "l",   "m",   "n",   "o",
        "p",   "q",   "r",   "s",   "t",   "u",   "v",   "w",
        "x",   "y",   "z",   "%7B", "%7C", "%7D", "~",   "%7F",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
        "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
        "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
        "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7",
        "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
        "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7",
        "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
        "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7",
        "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
        "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7",
        "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
        "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7",
        "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
    };

    /**
     * HEX encoding table for unquoted strings.
     */
    static final String[] HEXENCODE_UNQUOTED = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
        "+",   "!",   "%22", "%23", "$",   "%25", "%26", "\'",
        "%28", "%29", "*",   "%2B", "%2C", "-",   ".",   "/",
        "0",   "1",   "2",   "3",   "4",   "5",   "6",   "7",
        "8",   "9",   "%3A", ";",   "%3C", "%3D", "%3E", "?",
        "@",   "A",   "B",   "C",   "D",   "E",   "F",   "G",
        "H",   "I",   "J",   "K",   "L",   "M",   "N",   "O",
        "P",   "Q",   "R",   "S",   "T",   "U",   "V",   "W",
        "X",   "Y",   "Z",   "%5B", "%5C", "%5D", "%5E", "_",
        "%60", "a",   "b",   "c",   "d",   "e",   "f",   "g",
        "h",   "i",   "j",   "k",   "l",   "m",   "n",   "o",
        "p",   "q",   "r",   "s",   "t",   "u",   "v",   "w",
        "x",   "y",   "z",   "%7B", "%7C", "%7D", "~",   "%7F",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
        "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
        "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
        "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7",
        "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
        "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7",
        "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
        "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7",
        "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
        "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7",
        "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
        "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7",
        "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
    };

    /** bit for letters. */
    static final int IS_LETTER = 1 << 0;

    /** bit for digits. */
    static final int IS_DIGIT = 1 << 1;

    /** bit for literal characters. */
    static final int IS_LITCHAR = 1 << 2;

    /** bit for quoted string characters. */
    static final int IS_QSCHAR = 1 << 3;

    /** bit for quote characters. */
    static final int IS_QUOTE = 1 << 4;

    /** bit for structural characters. */
    static final int IS_STRUCTCHAR = 1 << 5;

    /** bit for space characters. */
    static final int IS_SPACE = 1 << 6;

    /** bit for characters that are safe inside a non-quoted string as-is. */
    static final int IS_ENC_STRSAFE = 1 << 7;

    /** bit for characters that are safe inside a quoted string as-is. */
    static final int IS_ENC_QSTRSAFE = 1 << 8;

    /** IS_ENC_QSTRSAFE | IS_ENC_STRSAFE. */
    static final int BIS_ENC_STRSAFE = IS_ENC_QSTRSAFE | IS_ENC_STRSAFE;

    /**
     * bits for US-ASCII characters.
     */
    static final int[] CHARBITS = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * number of array members.
     */
    static final int CHARBITS_LENGTH = CHARBITS.length;

    /**
     * table for decoding URL-encoded strings.
     */
    private static final int[] HEXDECODE_TABLE = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };
    
    static {
        for (int i = 'A'; i <= 'Z'; i++) {
            CHARBITS[i] = IS_LETTER | IS_LITCHAR | IS_QSCHAR
                    | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        }
        for (int i = 'a'; i <= 'z'; i++) {
            CHARBITS[i] = IS_LETTER | IS_LITCHAR | IS_QSCHAR
                    | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        }
        for (int i = '0'; i <= '9'; i++) {
            CHARBITS[i] = IS_DIGIT | IS_LITCHAR | IS_QSCHAR
                    | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        }
        CHARBITS['-'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['.'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['_'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['~'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['%'] = IS_LITCHAR | IS_QSCHAR;
        CHARBITS['!'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['$'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['*'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['+'] = IS_LITCHAR | IS_QSCHAR;
        CHARBITS[';'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['@'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['/'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['?'] = IS_LITCHAR | IS_QSCHAR | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        CHARBITS['\''] = IS_LITCHAR | IS_QUOTE | IS_ENC_STRSAFE;
        CHARBITS['('] = IS_STRUCTCHAR | IS_QSCHAR | IS_ENC_QSTRSAFE;
        CHARBITS[')'] = IS_STRUCTCHAR | IS_QSCHAR | IS_ENC_QSTRSAFE;
        CHARBITS[','] = IS_STRUCTCHAR | IS_QSCHAR | IS_ENC_QSTRSAFE;
        CHARBITS[':'] = IS_STRUCTCHAR | IS_QSCHAR | IS_ENC_QSTRSAFE;
        CHARBITS['&'] = IS_STRUCTCHAR;
        CHARBITS['='] = IS_STRUCTCHAR;
        //
        // Note the IS_ENC_STRSAFE | IS_ENC_QSTRSAFE. This is done on purpose.
        // Take a look at JsonUrl.Encode.getStringEncoding() and you'll see
        // why. That code tacks spaces and non-space chars separately.
        // If I didn't have IS_ENC_STRSAFE | IS_ENC_QSTRSAFE here then
        // the strmask value gets wiped anytime a space is found and that
        // forces a FULL_ENCODING, which isn't always necessary.
        //
        CHARBITS[' '] = IS_SPACE | IS_ENC_STRSAFE | IS_ENC_QSTRSAFE;
        
        for (int i = 'A'; i <= 'F'; i++) {
            HEXDECODE_TABLE[i] = 10 + (i - 'A');
        }
        for (int i = 'a'; i <= 'f'; i++) {
            HEXDECODE_TABLE[i] = 10 + (i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            HEXDECODE_TABLE[i] = i - '0';
        }
    }

    static boolean isDigit(char c) { // NOPMD - ShortVariable
        return c <= 127 && (CHARBITS[c] & IS_DIGIT) != 0;
    }

    static int hexDecode(char c) { // NOPMD - ShortVariable
        return c > 127 ? -1 : HEXDECODE_TABLE[c];
    }

    static int digits(CharSequence text, int pos, int stop) {
        for (int i = pos; i < stop; i++) {
            char c = text.charAt(i); // NOPMD - ShortVariable
            if (c > 127 || (CHARBITS[c] & IS_DIGIT) == 0) {
                return i; // NOPMD - OnlyOneReturn
            }
        }
        
        return stop;
    }

    private CharUtil() {
    }
}
