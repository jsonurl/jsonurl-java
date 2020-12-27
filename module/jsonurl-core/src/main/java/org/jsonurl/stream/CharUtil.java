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

/**
 * Character Utility class.
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
final class CharUtil {

    /** bit for letters. */
    static final int IS_LETTER = 1 << 0;

    /** bit for CGI name/value separators . */
    static final int IS_CGICHAR = 1 << 1;

    /** bit for literal characters. */
    static final int IS_LITCHAR = 1 << 2;

    /** bit for quoted string characters. */
    static final int IS_QSCHAR = 1 << 3;

    /** bit for quote characters. */
    static final int IS_QUOTE = 1 << 4;

    /** bit for structural characters. */
    static final int IS_STRUCTCHAR = 1 << 5;

    /** bit for exclamation point. */
    static final int IS_BANG = 1 << 6;

    /** bit for percent characters. */
    static final int IS_SPACE = 1 << 7;

    /**
     * bits for US-ASCII characters.
     */
    static final int[] CHARBITS = {
        // ASCII 0 - 15
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

        // ASCII 16 - 31
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

        // ASCII 32 (space)
        IS_SPACE,

        // ASCII 33 (!)
        IS_BANG | IS_LITCHAR | IS_QSCHAR,

        // ASCII 34 (")
        0,

        // ASCII 35 (#)
        0,

        // ASCII 36 ($)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 37 (%)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 38 (&)
        IS_STRUCTCHAR | IS_CGICHAR,

        // ASCII 39 (')
        IS_LITCHAR | IS_QUOTE,

        // ASCII 40 (lparen)
        IS_STRUCTCHAR | IS_QSCHAR,

        // ASCII 41 (rparen)
        IS_STRUCTCHAR | IS_QSCHAR,

        // ASCII 42 (*)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 43 (+)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 44 (,)
        IS_STRUCTCHAR | IS_QSCHAR,

        // ASCII 45 (-)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 46 (.)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 47 (/)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 48 (0)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 49 (1)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 50 (2)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 51 (3)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 52 (4)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 53 (5)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 54 (6)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 55 (7)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 56 (8)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 57 (9)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 58 (:)
        IS_STRUCTCHAR | IS_QSCHAR,

        // ASCII 59 (;)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 60 (<)
        0,

        // ASCII 61 (=)
        IS_STRUCTCHAR | IS_CGICHAR,

        // ASCII 62 (>)
        0,

        // ASCII 63 (?)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 64 (@)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 65 (A)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 66 (B)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 67 (C)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 68 (D)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 69 (E)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 70 (F)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 71 (G)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 72 (H)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 73 (I)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 74 (J)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 75 (K)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 76 (L)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 77 (M)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 78 (N)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 79 (O)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 80 (P)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 81 (Q)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 82 (R)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 83 (S)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 84 (T)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 85 (U)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 86 (V)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 87 (W)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 88 (X)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 89 (Y)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 90 (Z)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 91 ([)
        0,

        // ASCII 92 (backslash)
        0,

        // ASCII 93 (])
        0,

        // ASCII 94 (^)
        0,

        // ASCII 95 (_)
        IS_LITCHAR | IS_QSCHAR,

        // ASCII 96 (`)
        0,

        // ASCII 97 (a)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 98 (b)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 99 (c)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 100 (d)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 101 (e)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 102 (f)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 103 (g)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 104 (h)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 105 (i)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 106 (j)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 107 (k)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 108 (l)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 109 (m)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 110 (n)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 111 (o)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 112 (p)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 113 (q)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 114 (r)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 115 (s)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 116 (t)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 117 (u)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 118 (v)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 119 (w)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 120 (x)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 121 (y)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 122 (z)
        IS_LETTER | IS_LITCHAR | IS_QSCHAR,

        // ASCII 123 ({)
        0,

        // ASCII 124 (|)
        0,

        // ASCII 125 (})
        0,

        // ASCII 126 (~)
        IS_LITCHAR | IS_QSCHAR,

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
