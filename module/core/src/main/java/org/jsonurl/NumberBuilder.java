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

import static org.jsonurl.CharUtil.digits;
import static org.jsonurl.CharUtil.isDigit;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A NumberBuilder implements the builder pattern for JSON->URL numbers.
 *
 * <p>An instance of this class may be used to parse JSON->URL number
 * values from text and create J2SE8 Numbers.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class NumberBuilder implements NumberText {

    private static final int LONG_MAX_DIGITS = 18;
    private static final long[] E = {
        1,                      // 0
        10,                     // 1
        100,                    // 2
        1000,                   // 3
        10000,                  // 4
        100000,                 // 5
        1000000,                // 6
        10000000,               // 7
        100000000,              // 8
        1000000000,             // 9
        10000000000L,           // 10
        100000000000L,          // 11
        1000000000000L,         // 12
        10000000000000L,        // 13
        100000000000000L,       // 14
        1000000000000000L,      // 15
        10000000000000000L,     // 16
        100000000000000000L,    // 17
        1000000000000000000L,   // 18
    };

    public static final long MIN_LONG = -999999999999999999L;
    public static final long MAX_LONG = 999999999999999999L;

    private CharSequence text;
    private int start;
    private int stop;

    private NumberText.Exponent exponentType = NumberText.Exponent.NONE;
    private int intIndexStart = -1;
    private int intIndexStop = -1;
    private int decIndexStart = -1;
    private int decIndexStop = -1;
    private int expIndexStart = -1;
    private int expIndexStop = -1;
    
    public NumberBuilder() {
    }

    public NumberBuilder(CharSequence s, int start, int stop) {
        parse(s, start, stop);
    }
    
    public NumberBuilder(CharSequence s) {
        parse(s, 0, s.length());
    }

    /**
     * Reset the internal state.
     */
    public NumberBuilder reset() {
        text = null;
        exponentType = NumberText.Exponent.NONE;

        start = stop = 
                intIndexStart = intIndexStop =
                decIndexStart = decIndexStop =
                expIndexStart = expIndexStop = -1;

        return this;
    }
    
    private static final boolean hasFract(
            int start,
            int stop,
            CharSequence s) {

        if (start == stop || s.charAt(start) != '.') {
            return false;
        }
        if (++start == stop) {
            return false;
        }

        return isDigit(s.charAt(start));
    }
    
    @Override
    public Exponent getExponentType() {
        return this.exponentType;
    }

    /**
     * Calculate exponent string.
     */
    private static final NumberText.Exponent getExponentType(
            CharSequence s,
            int start,
            int stop) {

        if (start == stop) {
            return NumberText.Exponent.NONE;
        }

        switch (s.charAt(start)) {
        case 'e':
        case 'E':
            if (++start == stop) {
                return NumberText.Exponent.NONE;
            }
            break;
        default:
            return NumberText.Exponent.NONE;
        }

        NumberText.Exponent ret = null;
        char c = s.charAt(start);

        switch (c) {
        case '+':
            if (++start == stop) {
                return NumberText.Exponent.NONE;
            }
            c = s.charAt(start);
            ret = NumberText.Exponent.POSITIVE_VALUE;
            break;
        case '-':
            if (++start == stop) {
                return NumberText.Exponent.NONE;
            }
            c = s.charAt(start);
            ret = NumberText.Exponent.NEGATIVE_VALUE;
            break;
        default:
            ret = NumberText.Exponent.JUST_VALUE;
            break;
        }
        if (!isDigit(c)) {
            return NumberText.Exponent.NONE;
        }

        return ret;
    }

    /**
     * parse the given character sequence.
     *
     * @param s a valid CharSequence
     * @param start an index
     * @param stop an index
     * @return true if the CharSequence was successfully parsed
     */
    public boolean parse(CharSequence s, int start, int stop) {
        int pos = this.start = start;

        char c = s.charAt(start);

        if (c == '-') {
            if (++pos == stop) {
                return false;
            }

            c = s.charAt(pos);
        }

        switch (c) {
        case '0':
            intIndexStart = pos;
            pos++;
            break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            intIndexStart = pos;
            pos = digits(s, pos + 1, stop);
            break;
        default:
            return false;
        }

        intIndexStop = pos;

        if (hasFract(pos, stop, s)) {
            decIndexStart = pos + 1;
            decIndexStop = pos = digits(s, pos + 1, stop);
        }

        switch (exponentType = getExponentType(s, pos, stop)) {
        case JUST_VALUE:
            expIndexStart = pos + 1;
            pos = expIndexStop = digits(s, expIndexStart, stop);
            break;
        case NEGATIVE_VALUE:
        case POSITIVE_VALUE:
            expIndexStart = pos + 2;
            pos = expIndexStop = digits(s, expIndexStart, stop);
            break;
        case NONE:
            break;
        }

        this.text = s;
        this.stop = stop;

        return pos == stop;
    }
    
    public static boolean isNumber(CharSequence s) {
        return isNumber(s, 0, s.length());
    }

    /**
     * Determine if the given CharSequence is a valid JSON->URL number.
     * 
     * @param s a valid CharSequence
     * @param start an index
     * @param stop an index
     * @return true if the CharSequence is a JSON->URL number
     */
    public static boolean isNumber(CharSequence s, int start, int stop) {
        int pos = start;

        char c = s.charAt(start);

        if (c == '-') {
            if (++pos == stop) {
                return false;
            }

            c = s.charAt(pos);
        }

        switch (c) {
        case '0':
            pos++;
            break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            pos = digits(s, pos + 1, stop);
            break;
        default:
            return false;
        }

        if (hasFract(pos, stop, s)) {
            pos = digits(s, pos + 1, stop);
        }

        switch (getExponentType(s, pos, stop)) {
        case JUST_VALUE:
            pos = digits(s, pos + 1, stop);
            break;
        case NEGATIVE_VALUE:
        case POSITIVE_VALUE:
            pos = digits(s, pos + 2, stop);
            break;
        case NONE:
            break;
        }

        return pos == stop;
    }

    public double toDouble() {
        return toDouble(this);
    }

    /**
     * Parse the given NumberText as a J2SE double.
     */
    public static final double toDouble(NumberText t) {
        char[] s = toChars(
                t.getText(),
                t.getStartIndex(),
                t.getStopIndex());

        return Double.valueOf(new String(s));
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     *
     * <p>This simply calls {@link #build(NumberText, boolean)
     * build(this, primitiveOnly)}.
     */
    public Number build(boolean primitiveOnly) {
        return build(this, primitiveOnly);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     * @param t a valid NumberText
     * @param primitiveOnly if true, the returned Number will be an
     * {@link java.lang.Integer Integer}, {@link java.lang.Long Long}, or
     * {@link java.lang.Double Double}. Otherwise, it may be a
     * {@link java.math.BigInteger BigInteger} or
     * {@link java.math.BigDecimal BigDecimal}. 
     * @return an instance of java.lang.Number
     */
    public static final Number build(NumberText t, boolean primitiveOnly) {
        CharSequence text = t.getText();

        if (!t.hasDecimalPart()) {
            switch (t.getExponentType()) {
            case NEGATIVE_VALUE:
                break;
            case JUST_VALUE:
            case POSITIVE_VALUE:
            case NONE:
                int expValue = parseInteger(
                        text,
                        t.getExponentStartIndex(),
                        t.getExponentStopIndex(),
                        0);

                int intIndexStart = t.getIntegerStartIndex();
                int intIndexStop = t.getIntegerStopIndex();
                int start = t.getStartIndex();
                int digitCount = (intIndexStop - intIndexStart) + expValue;

                if (digitCount <= LONG_MAX_DIGITS) {
                    //
                    // this is the common case
                    //
                    long value = parseLong(text, start, intIndexStop, 0);
                    value *= E[expValue];
                    return Long.valueOf(value);
                }

                if (primitiveOnly) {
                    char[] s = toChars(
                            text,
                            start,
                            t.getStopIndex());

                    return Double.valueOf(new String(s));
                }

                char[] s = toChars(
                        text,
                        intIndexStart,
                        intIndexStop);

                BigInteger ret = new BigInteger(new String(s));

                return ret.pow(expValue);
            }
        }

        char[] s = toChars(
                text,
                t.getStartIndex(),
                t.getStopIndex());

        if (primitiveOnly) {
            return Double.valueOf(new String(s));
        }

        return new BigDecimal(s);
    }
    
    /**
     * Parse a Java integer.
     *
     * <p>This is similar to {@link java.lang.Integer#parseInt(String)},
     * however, it accepts a bounded CharSequence and default value.
     * @param s a non-null character sequence
     * @param start start index
     * @param stop stop index
     * @return an integer
     */
    private static int parseInteger(
            CharSequence s,
            int start,
            int stop,
            int defaultValue) {
        
        if (start == stop) {
            return defaultValue;
        }

        int ret = 0;
        boolean isneg = false;

        char c = s.charAt(start);
        
        switch (c) {
        case '-':
            isneg = true;
            // fall through
        case '+':
            start++;
            break;
        default:
            break;
        }
        
        for (int i = start; i < stop; i++) {
            c = s.charAt(i);
            ret = ret * 10 + (c - '0');
        }

        return isneg ? -ret : ret;
    }
    
    /**
     * Parse a Java integer.
     *
     * <p>This is similar to {@link java.lang.Long#parseLong(String)},
     * however, it accepts a bounded CharSequence and default value.
     * @param s a non-null character sequence
     * @param start start index
     * @param stop stop index
     * @return a long
     */
    private static long parseLong(
            CharSequence s,
            int start,
            int stop,
            int defaultValue) {
        
        if (start == stop) {
            return defaultValue;
        }

        long ret = 0;
        boolean isneg = false;

        char c = s.charAt(start);
        
        switch (c) {
        case '-':
            isneg = true;
            // fall through
        case '+':
            start++;
            break;
        default:
            break;
        }
        
        for (int i = start; i < stop; i++) {
            c = s.charAt(i);
            ret = ret * 10 + (c - '0');
        }

        return isneg ? -ret : ret;
    }

    /**
     * Calls {@link #toChars(NumberText) toChars(this)}.
     */
    public char[] toChars() {
        return toChars(text, start, stop);
    }

    /**
     * Create a new character array with the text from a NumberText.
     * 
     * @param text a valid NumberText
     * @return a newly allocated char[]
     */
    public static final char[] toChars(NumberText text) {
        return toChars(
                text.getText(),
                text.getStartIndex(),
                text.getStopIndex());
    }
    

    private static final char[] toChars(CharSequence s, int start, int stop) {
        int len = stop - start;
        char[] ret = new char[len];

        for (int i = start, j = 0; i < stop; i++, j++) {
            ret[j] = s.charAt(i);
        }
        
        return ret;
    }


    @Override
    public String toString() {
        return String.valueOf(toChars());
    }

    @Override
    public CharSequence getText() {
        return this.text;
    }

    @Override
    public int getIntegerStartIndex() {
        return this.intIndexStart;
    }

    @Override
    public int getIntegerStopIndex() {
        return this.intIndexStop;
    }

    @Override
    public int getDecimalStartIndex() {
        return this.decIndexStart;
    }

    @Override
    public int getDecimalStopIndex() {
        return this.decIndexStop;
    }

    @Override
    public int getExponentStartIndex() {
        return this.expIndexStart;
    }

    @Override
    public int getExponentStopIndex() {
        return this.expIndexStop;
    }

    @Override
    public int getStartIndex() {
        return this.start;
    }
    
    @Override
    public int getStopIndex() {
        return this.stop;
    }
}
