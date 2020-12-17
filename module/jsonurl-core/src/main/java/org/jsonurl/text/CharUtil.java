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

/**
 * Character Utility class.
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
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

    /**
     * Start/End quoted string.
     */
    public static final int APOS = '\'';

    /** bit for space characters. */
    public static final int IS_SPACE = 1 << 0;

    /** bit for characters that are safe inside a non-quoted string as-is. */
    public static final int IS_NSTRSAFE = 1 << 1;

    /** bit for characters that are safe inside a quoted string as-is. */
    public static final int IS_QSTRSAFE = 1 << 2;

    /** IS_QSTRSAFE | IS_NSTRSAFE. */
    public static final int IS_ANY_STRSAFE = IS_QSTRSAFE | IS_NSTRSAFE;

    /**
     * bits for US-ASCII characters.
     */
    static final int[] CHARBITS = {
        // ASCII 0 - 15
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

        // ASCII 16 - 31
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

        // ASCII 32 (space)
        //
        // Note the IS_ANY_STRSAFE. This is done on purpose.
        //
        // Take a look at StringEncoding.getStringEncoding() and you'll see
        // why. That code tacks spaces and non-space chars separately.
        // If I didn't have IS_ANY_STRSAFE here then the strmask value gets
        // wiped anytime a space is found and that forces a FULL_ENCODING.
        //
        IS_SPACE | IS_ANY_STRSAFE,

        // ASCII 33 (!)
        IS_ANY_STRSAFE,

        // ASCII 34 (")
        0,

        // ASCII 35 (#)
        0,

        // ASCII 36 ($)
        IS_ANY_STRSAFE,

        // ASCII 37 (%)
        0,

        // ASCII 38 (&)
        0,

        // ASCII 39 (')
        IS_NSTRSAFE,

        // ASCII 40 (lparen)
        IS_QSTRSAFE,

        // ASCII 41 (rparen)
        IS_QSTRSAFE,

        // ASCII 42 (*)
        IS_ANY_STRSAFE,

        // ASCII 43 (+)
        0,

        // ASCII 44 (,)
        IS_QSTRSAFE,

        // ASCII 45 (-)
        IS_ANY_STRSAFE,

        // ASCII 46 (.)
        IS_ANY_STRSAFE,

        // ASCII 47 (/)
        IS_ANY_STRSAFE,

        // ASCII 48 (0)
        IS_ANY_STRSAFE,

        // ASCII 49 (1)
        IS_ANY_STRSAFE,

        // ASCII 50 (2)
        IS_ANY_STRSAFE,

        // ASCII 51 (3)
        IS_ANY_STRSAFE,

        // ASCII 52 (4)
        IS_ANY_STRSAFE,

        // ASCII 53 (5)
        IS_ANY_STRSAFE,

        // ASCII 54 (6)
        IS_ANY_STRSAFE,

        // ASCII 55 (7)
        IS_ANY_STRSAFE,

        // ASCII 56 (8)
        IS_ANY_STRSAFE,

        // ASCII 57 (9)
        IS_ANY_STRSAFE,

        // ASCII 58 (:)
        IS_QSTRSAFE,

        // ASCII 59 (;)
        IS_ANY_STRSAFE,

        // ASCII 60 (<)
        0,

        // ASCII 61 (=)
        0,

        // ASCII 62 (>)
        0,

        // ASCII 63 (?)
        IS_ANY_STRSAFE,

        // ASCII 64 (@)
        IS_ANY_STRSAFE,

        // ASCII 65 (A)
        IS_ANY_STRSAFE,

        // ASCII 66 (B)
        IS_ANY_STRSAFE,

        // ASCII 67 (C)
        IS_ANY_STRSAFE,

        // ASCII 68 (D)
        IS_ANY_STRSAFE,

        // ASCII 69 (E)
        IS_ANY_STRSAFE,

        // ASCII 70 (F)
        IS_ANY_STRSAFE,

        // ASCII 71 (G)
        IS_ANY_STRSAFE,

        // ASCII 72 (H)
        IS_ANY_STRSAFE,

        // ASCII 73 (I)
        IS_ANY_STRSAFE,

        // ASCII 74 (J)
        IS_ANY_STRSAFE,

        // ASCII 75 (K)
        IS_ANY_STRSAFE,

        // ASCII 76 (L)
        IS_ANY_STRSAFE,

        // ASCII 77 (M)
        IS_ANY_STRSAFE,

        // ASCII 78 (N)
        IS_ANY_STRSAFE,

        // ASCII 79 (O)
        IS_ANY_STRSAFE,

        // ASCII 80 (P)
        IS_ANY_STRSAFE,

        // ASCII 81 (Q)
        IS_ANY_STRSAFE,

        // ASCII 82 (R)
        IS_ANY_STRSAFE,

        // ASCII 83 (S)
        IS_ANY_STRSAFE,

        // ASCII 84 (T)
        IS_ANY_STRSAFE,

        // ASCII 85 (U)
        IS_ANY_STRSAFE,

        // ASCII 86 (V)
        IS_ANY_STRSAFE,

        // ASCII 87 (W)
        IS_ANY_STRSAFE,

        // ASCII 88 (X)
        IS_ANY_STRSAFE,

        // ASCII 89 (Y)
        IS_ANY_STRSAFE,

        // ASCII 90 (Z)
        IS_ANY_STRSAFE,

        // ASCII 91 ([)
        0,

        // ASCII 92 (backslash)
        0,

        // ASCII 93 (])
        0,

        // ASCII 94 (^)
        0,

        // ASCII 95 (_)
        IS_ANY_STRSAFE,

        // ASCII 96 (`)
        0,

        // ASCII 97 (a)
        IS_ANY_STRSAFE,

        // ASCII 98 (b)
        IS_ANY_STRSAFE,

        // ASCII 99 (c)
        IS_ANY_STRSAFE,

        // ASCII 100 (d)
        IS_ANY_STRSAFE,

        // ASCII 101 (e)
        IS_ANY_STRSAFE,

        // ASCII 102 (f)
        IS_ANY_STRSAFE,

        // ASCII 103 (g)
        IS_ANY_STRSAFE,

        // ASCII 104 (h)
        IS_ANY_STRSAFE,

        // ASCII 105 (i)
        IS_ANY_STRSAFE,

        // ASCII 106 (j)
        IS_ANY_STRSAFE,

        // ASCII 107 (k)
        IS_ANY_STRSAFE,

        // ASCII 108 (l)
        IS_ANY_STRSAFE,

        // ASCII 109 (m)
        IS_ANY_STRSAFE,

        // ASCII 110 (n)
        IS_ANY_STRSAFE,

        // ASCII 111 (o)
        IS_ANY_STRSAFE,

        // ASCII 112 (p)
        IS_ANY_STRSAFE,

        // ASCII 113 (q)
        IS_ANY_STRSAFE,

        // ASCII 114 (r)
        IS_ANY_STRSAFE,

        // ASCII 115 (s)
        IS_ANY_STRSAFE,

        // ASCII 116 (t)
        IS_ANY_STRSAFE,

        // ASCII 117 (u)
        IS_ANY_STRSAFE,

        // ASCII 118 (v)
        IS_ANY_STRSAFE,

        // ASCII 119 (w)
        IS_ANY_STRSAFE,

        // ASCII 120 (x)
        IS_ANY_STRSAFE,

        // ASCII 121 (y)
        IS_ANY_STRSAFE,

        // ASCII 122 (z)
        IS_ANY_STRSAFE,

        // ASCII 123 ({)
        0,

        // ASCII 124 (|)
        0,

        // ASCII 125 (})
        0,

        // ASCII 126 (~)
        IS_ANY_STRSAFE,

        // ASCII 127
        0,
    };

    /**
     * CHARBITS.length 
     */
    static final int CHARBITS_LENGTH = CHARBITS.length;

    private CharUtil() {
        // Utility class
    }
}
