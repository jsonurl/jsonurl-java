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
 * A NumberBuilder implements the builder pattern for JSON&#x2192;URL number literals.
 * An instance of this class may be used to parse JSON&#x2192;URL number
 * literals from text as Java SE values.
 *
 * <h2>Single Use</h2>
 * 
 * <p>NumberBuilder may be single use, where the literal text is provided via
 * a constructor followed by a call to a method to test and/or build a value.
 * For example:
 * <pre>
 * // Example 1
 * {@link java.lang.Number Number} n = new NumberBuilder("1.234").build();
 * 
 * // Example 2
 * NumberBuilder nb = new NumberBuilder("1.234");
 * if (!nb.isNumber()) {
 *     // handle error
 * }
 * double d = new NumberBuilder().toDouble();
 * </pre>
 *
 * <h2>Reuse</h2>
 *
 * <p>An instance of NumberBuilder may also be reused via a call to the
 * {@link #reset()} method.
 * <pre>
 * NumberBuilder nb = new NumberBuilder();
 * if (!nb.parse("1.234")) {
 *     // handle the error
 * }
 *
 * // equal to {@link java.lang.Double new Double("1.234")}
 * {@link java.lang.Number Number} n = nb.build();
 * 
 * .
 * .
 * .
 * 
 * if (!nb.reset().parse("98765432100")) {
 *     // handle the error
 * }
 *
 * // equal to {@link java.lang.Long new Long("98765432100")}
 * n = nb.build();
 * </pre>
 * 
 * <h2>Static Methods</h2>
 *
 * <p>NumberBuilder also provides static methods to test literals without
 * allocating an instance. They're useful if you want test validity but
 * don't need access to the parsed value.
 * <pre>
 * boolean b = NumberBuilder.{@link #isNumber() isNumber("1234")};
 * boolean b = NumberBuilder.{@link #isNumber() isNumber("abcd")};
 * boolean b = NumberBuilder.{@link #isInteger() isInteger("1234.5")};
 * </pre>
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class NumberBuilder implements NumberText { // NOPMD

    /**
     * The maximum number of digits I consider when parsing a
     * {@link java.lang.Long Long}. 
     */
    private static final int LONG_MAX_DIGITS = 18;
    
    /**
     * Lookup table for exponent values.
     */
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

    /**
     * the text that has been parsed.
     */
    private CharSequence text;
    
    /**
     * the start index of {@link #text}.
     */
    private int start;

    /**
     * the stop index of {@link #text}.
     */
    private int stop;

    /**
     * The parsed exponent type.
     */
    private NumberText.Exponent exponentType = NumberText.Exponent.NONE;

    /**
     * The integer part start index. A result of calling
     * {@link #parse(CharSequence, int, int)}.
     */
    private int intIndexStart = -1;

    /**
     * The integer part stop index. A result of calling
     * {@link #parse(CharSequence, int, int)}.
     */
    private int intIndexStop = -1;

    /**
     * The fractional part start index. A result of calling
     * {@link #parse(CharSequence, int, int)}.
     */
    private int fractIndexStart = -1;

    /**
     * The fractional part stop index. A result of calling
     * {@link #parse(CharSequence, int, int)}.
     */
    private int fractIndexStop = -1;

    /**
     * The exponent part start index. A result of calling
     * {@link #parse(CharSequence, int, int)}.
     */
    private int expIndexStart = -1;

    /**
     * The exponent part stop index. A result of calling
     * {@link #parse(CharSequence, int, int)}.
     */
    private int expIndexStop = -1;

    /**
     * Create a new NumberBuilder.
     *
     * <p>This NumberBuilder will not have any text. You'll need to call
     * {@link #parse(CharSequence, int, int)}.
     */
    public NumberBuilder() {
        // EMPTY
    }

    /**
     * Create a new NumberBuilder with the given text.
     * @param s text
     * @param start start index
     * @param stop stop index
     */
    public NumberBuilder(CharSequence s, int start, int stop) {
        parse(s, start, stop);
    }

    /**
     * Create a new NumberBuilder with the given text.
     *
     * <p>This is a convenience for {@link
     * #NumberBuilder(CharSequence, int, int)
     * NumberBuilder(s, 0, s.length())}.
     */
    public NumberBuilder(CharSequence s) {
        parse(s, 0, s.length());
    }

    /**
     * Reset the instance for reuse. You may reliably call
     * {@link #parse(CharSequence, int, int)} again after calling this
     * method.
     */
    public NumberBuilder reset() {
        reset(false);
        return this;
    }

    /**
     * Reset internal state.
     * @param leaveText if true then do not touch {@link #start}, {@link #stop},
     *      or {@link #text}.
     */
    private void reset(boolean leaveText) {
        if (!leaveText) {
            text = null; // NOPMD - yes, really.
            start = stop = -1;
        }

        exponentType = NumberText.Exponent.NONE;

        intIndexStart = intIndexStop =
            fractIndexStart = fractIndexStop =
            expIndexStart = expIndexStop = -1;
    }
    
    private static final boolean hasFract(
            int start,
            int stop,
            CharSequence s) {

        if (start == stop || s.charAt(start) != '.') {
            return false;
        }

        start++;

        if (start == stop) { 
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
    @SuppressWarnings("PMD")
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
    @SuppressWarnings("PMD")
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
            fractIndexStart = pos + 1;
            fractIndexStop = pos = digits(s, pos + 1, stop);
        }

        exponentType = getExponentType(s, pos, stop);

        switch (exponentType) {
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
        
        boolean isNumber = pos == stop;

        if (!isNumber) {
            reset(false);
        }

        return isNumber;
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     *<p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, 0, s.length(), false)}.
     */
    public static boolean isNumber(CharSequence s) {
        return isNumber(s, 0, s.length(), false);
    }
    
    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     *<p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, 0, s.length(), isInteger)}.
     */
    public static boolean isNumber(CharSequence s, boolean isInteger) {
        return isNumber(s, 0, s.length(), isInteger);
    }
    
    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     * <p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)}
     * isNumber(s, 0, stop, false)}.
     * @param s a valid CharSequence
     * @param start an index
     * @param stop an index
     * @return true if the CharSequence is a JSON&#x2192;URL number
     */
    public static boolean isNumber(CharSequence s, int start, int stop) {
        return isNumber(s, start, stop, false);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     * 
     * @param s a valid CharSequence
     * @param start an index
     * @param stop an index
     * @return true if the CharSequence is a JSON&#x2192;URL number
     */
    @SuppressWarnings("PMD")
    public static boolean isNumber(
            CharSequence s,
            int start,
            int stop,
            boolean isInteger) {

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
            if (isInteger) {
                return false;
            }
            pos = digits(s, pos + 1, stop);
        }

        boolean isNegExp = false;

        switch (getExponentType(s, pos, stop)) {
        case JUST_VALUE:
            pos = digits(s, pos + 1, stop);
            break;
        case NEGATIVE_VALUE:
            isNegExp = true;
            // fall-through
        case POSITIVE_VALUE:
            pos = digits(s, pos + 2, stop);
            break;
        case NONE:
            break;
        }

        return pos == stop && (!isInteger || !isNegExp); 
    }

    /**
     * Determine if this text represents a valid JSON&#x2192;URL number literal.
     *
     * <p>This is the result of calling {@link #parse(CharSequence, int, int)}.
     * @return true if this text represents is a JSON&#x2192;URL number literal
     */
    public boolean isNumber() {
        return this.intIndexStop > this.intIndexStart;
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     *<p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, 0, s.length(), true)}.
     */
    public static boolean isInteger(CharSequence s) {
        return isNumber(s, 0, s.length(), true);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     *<p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, start, stop, true)}.
     */
    public static boolean isInteger(CharSequence s, int start, int stop) {
        return isNumber(s, start, stop, true);
    }

    /**
     * Parse this NumberText as a J2SE double.
     */
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

        return Double.parseDouble(new String(s));
    }

    /**
     * Parse the given NumberText as a {@link java.math.BigDecimal}.
     */
    public static final BigDecimal toBigDecimal(NumberText t) {
        char[] s = toChars(
                t.getText(),
                t.getStartIndex(),
                t.getStopIndex());

        return new BigDecimal(new String(s));
    }

    /**
     * Parse the given NumberText as a {@link java.math.BigDecimal}.
     */
    public BigDecimal toBigDecimal() {
        return toBigDecimal(this);
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
     *
     * <p>The benefit of this method (over {@link #toDouble(NumberText)} or
     * {@link #toBigDecimal(NumberText)}) is that it has the logic to return
     * an Object tailored to the value itself. For example, if the value
     * can be represented as a {@link Long} then it will be.
     *
     * @param t a valid NumberText
     * @param primitiveOnly if true, the returned Number will be a
     * {@link java.lang.Long Long} or
     * {@link java.lang.Double Double}. Otherwise, it may be a
     * {@link java.math.BigInteger BigInteger} or
     * {@link java.math.BigDecimal BigDecimal}. 
     * @return an instance of java.lang.Number
     */
    public static final Number build(NumberText t, boolean primitiveOnly) {
        if (!t.hasFractionalPart()) {
            switch (t.getExponentType()) { //NOPMD - no default
            case NEGATIVE_VALUE:
                break;
            case JUST_VALUE:
            case POSITIVE_VALUE:
            case NONE:
                return toNonFractional(t, primitiveOnly);
            }
        }

        return primitiveOnly ? toDouble(t) : toBigDecimal(t);
    }
    
    /**
     * Build a non-fractional number from the given NumberText.
     */
    private static final Number toNonFractional(
            NumberText t,
            boolean primitiveOnly) {

        final CharSequence text = t.getText();

        final int expValue = parseInteger(
            text,
            t.getExponentStartIndex(),
            t.getExponentStopIndex(),
            0);

        final int intIndexStart = t.getIntegerStartIndex();
        final int intIndexStop = t.getIntegerStopIndex();
        int digitCount = (intIndexStop - intIndexStart) + expValue;
    
        if (digitCount <= LONG_MAX_DIGITS) {
            //
            // this is the common case
            //
            long value = parseLong(text, t.getStartIndex(), intIndexStop, 0);
            value *= E[expValue];
            return Long.valueOf(value);
        }
    
        if (primitiveOnly) {
            return toDouble(t);
        }
    
        char[] s = toChars(
                text,
                t.getStartIndex(),
                intIndexStop);
    
        BigInteger ret = new BigInteger(new String(s));
    
        if (expValue > 0) {
            ret = ret.multiply(BigInteger.TEN.pow(expValue));
        }
    
        return ret;
    }
    
    /**
     * Parse a Java integer.
     *
     * <p>This is similar to {@link java.lang.Integer#parseInt(String)},
     * however, it accepts a bounded CharSequence and default value.
     *
     * <p>Since this private, and I will never call this with a +/-
     * prefix, I've removed that logic.
     * @param s a non-null character sequence
     * @param start start index
     * @param stop stop index
     * @return an integer
     */
    @SuppressWarnings("PMD")
    private static int parseInteger(
            CharSequence s,
            int start,
            int stop,
            int defaultValue) {
        
        if (start == stop) {
            return defaultValue;
        }

        int ret = 0;
        
        for (int i = start; i < stop; i++) {
            char c = s.charAt(i);
            ret = ret * 10 + (c - '0');
        }

        return ret;
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
    @SuppressWarnings("PMD")
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
    
    @SuppressWarnings("PMD")
    private static final char[] toChars(CharSequence s, int start, int stop) {
        final int len = stop - start;
        final char[] ret = new char[len];

        for (int i = start, j = 0; i < stop; i++, j++) {
            ret[j] = s.charAt(i);
        }
        
        return ret;
    }

    /**
     * Return a string representation of the given NumberText.
     * @param text a valid NumberText
     * @return a valid String
     */
    public static String toString(NumberText text) {
        return String.valueOf(toChars(text));
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
    public int getFractionalStartIndex() {
        return this.fractIndexStart;
    }

    @Override
    public int getFractionalStopIndex() {
        return this.fractIndexStop;
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
