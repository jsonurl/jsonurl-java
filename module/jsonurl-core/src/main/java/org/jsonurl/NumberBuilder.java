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

import static org.jsonurl.BigMathProvider.NEGATIVE_INFINITY;
import static org.jsonurl.BigMathProvider.POSITIVE_INFINITY;
import static org.jsonurl.CharUtil.digits;
import static org.jsonurl.CharUtil.isDigit;
import static org.jsonurl.LimitException.ERR_MSG_LIMIT_INTEGER;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import org.jsonurl.BigMathProvider.BigIntegerOverflow;

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
 * boolean b = NumberBuilder.{@link #isNonFractional() isInteger("1234.5")};
 * </pre>
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public class NumberBuilder implements NumberText { // NOPMD

    /**
     * The negative boundary for a long.
     */
    private static final String LONG_BOUNDARY_NEG =
        String.valueOf(Long.MIN_VALUE).substring(1);

    /**
     * The positive boundary for a long.
     */
    private static final String LONG_BOUNDARY_POS =
        String.valueOf(Long.MAX_VALUE);

    /**
     * The maximum number of digits I consider when parsing a Long. 
     */
    private static final int LONG_MAX_DIGITS =
        LONG_BOUNDARY_POS.length();
    
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
     * The user supplied math context for use with BigDecimals.
     */
    private BigMathProvider mcp;

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
    // "PMD.CyclomaticComplexity" - but PMD compains about dup literals
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
            start++;
            if (start == stop) {
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
            start++;
            if (start == stop) {
                return NumberText.Exponent.NONE;
            }
            c = s.charAt(start);
            ret = NumberText.Exponent.POSITIVE_VALUE;
            break;
        case '-':
            start++;
            if (start == stop) {
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
            pos++;

            if (pos == stop) {
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
     * <p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, 0, s.length(), false)}.
     */
    public static boolean isNumber(CharSequence s) {
        return isNumber(s, 0, s.length(), false);
    }
    
    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     * <p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, 0, s.length(), isInteger)}.
     */
    public static boolean isNumber(CharSequence s, boolean isNonFractional) {
        return isNumber(s, 0, s.length(), isNonFractional);
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
    @SuppressWarnings({
        "PMD.CyclomaticComplexity",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NPathComplexity"})
    public static boolean isNumber(
            CharSequence s,
            int start,
            int stop,
            boolean isNonFractional) {

        int pos = start;

        char c = s.charAt(start);

        if (c == '-') {
            pos++;

            if (pos == stop) {
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
            if (isNonFractional) {
                return false;
            }
            pos = digits(s, pos + 1, stop);
        }

        switch (getExponentType(s, pos, stop)) { // NOPMD - fall-through
        case JUST_VALUE:
            pos = digits(s, pos + 1, stop);
            break;
        case NEGATIVE_VALUE:
            if (isNonFractional) {
                return false;
            }
            // fall-through
        case POSITIVE_VALUE:
            pos = digits(s, pos + 2, stop);
            break;
        case NONE:
            break;
        }

        return pos == stop; 
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
    public static boolean isNonFractional(CharSequence s) {
        return isNumber(s, 0, s.length(), true);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     *<p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, start, stop, true)}.
     */
    public static boolean isNonFractional(CharSequence s, int start, int stop) {
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
        char[] chars = toChars(
                t.getText(),
                t.getStartIndex(),
                t.getStopIndex());

        return Double.parseDouble(new String(chars));
    }

    /**
     * Parse the given NumberText as a {@link java.math.BigDecimal}.
     */
    public static final BigDecimal toBigDecimal(
            NumberText t,
            BigMathProvider mcp) {

        char[] s = toChars(
                t.getText(),
                t.getStartIndex(),
                t.getStopIndex());

        MathContext mc = mcp == null ? null : mcp.getMathContext();
        return mc == null ? new BigDecimal(s) : new BigDecimal(s, mc);
    }

    /**
     * Parse the given NumberText as a {@link java.math.BigDecimal}.
     */
    public BigDecimal toBigDecimal() {
        return toBigDecimal(this, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(this, primitiveOnly, mcp)}.
     */
    public Number build(boolean primitiveOnly) {
        return build(this, primitiveOnly, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(t, false, mcp)}.
     */
    public static final Number build(
            NumberText t,
            BigMathProvider mcp) {
        return build(t, false, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(t, primitiveOnly, null)}.
     */
    public static final Number build(
            NumberText t,
            boolean primitiveOnly) {
        return build(t, primitiveOnly, null);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     *
     * <p>The benefit of this method (over {@link #toDouble(NumberText)} or
     * {@link #toBigDecimal(NumberText, BigMathProvider)}) is that it has the
     * logic to return a Number tailored to the value itself. For example, if
     * the value can be represented as a {@link Long} then it will be.
     *
     * @param t a valid NumberText
     * @param primitiveOnly return a Long or Double
     * @return an instance of java.lang.Number
     */
    public static final Number build(
            NumberText t, 
            boolean primitiveOnly,
            BigMathProvider mcp) {

        if (!t.hasFractionalPart()) {
            switch (t.getExponentType()) { //NOPMD - no default
            case NEGATIVE_VALUE:
                break;
            case JUST_VALUE:
            case POSITIVE_VALUE:
            case NONE:
                return toNonFractional(t, primitiveOnly, mcp);
            }
        }

        return primitiveOnly ? toDouble(t) : toBigDecimal(t, mcp);
    }

    /**
     * Calculate an overflow value.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static final Number getOverflow(
        String strLimit,
        Double infinity,
        String s,
        int digitCount,
        BigMathProvider mcp) {

        if (strLimit == null) {
            // no limit, no overflow
            return null;
        }

        if (digitCount < strLimit.length()) {
            // definitely under limit
            return null;
        }

        boolean isOver = digitCount > strLimit.length()
            || s.compareTo(strLimit) > 0;
            
        if (!isOver) {
            // close, but still within limit
            return null;
        }

        final BigIntegerOverflow overOp =
            mcp == null ? null : mcp.getBigIntegerOverflow();

        if (overOp == null) {
            throw new LimitException(ERR_MSG_LIMIT_INTEGER);
        }

        switch (overOp) { // NOPMD - no default case
        case DOUBLE:
            return Double.valueOf(s);
        case BIG_DECIMAL:
            //
            // I don't have to check for mcp == null here as the
            // check above ensures that, if it's null, I'll throw
            // an exception before I get here.
            //
            MathContext mc = mcp.getMathContext();
            return mc == null ? new BigDecimal(s) : new BigDecimal(s, mc);
        case INFINITY:
            return infinity;
        }

        return null;
    }

    /**
     * get the value of the exponent in {@code t}.
     */
    private static final int getExponentValue(NumberText t) {
        return t.getExponentType() == null ? 0 : parseInteger(
            t.getText(),
            t.getExponentStartIndex(),
            t.getExponentStopIndex(),
            0);
    }
    
    /**
     * Test if the given NumberText can be stored in a {@code long}.
     * @param t a valid NumberText
     */
    public static final boolean isLong(NumberText t) {
        if (!t.isNonFractional()) {
            return false;
        }
        final int expValue = getExponentValue(t);
        final int intIndexStart = t.getIntegerStartIndex();
        final int intIndexStop = t.getIntegerStopIndex();
        final int digitCount = (intIndexStop - intIndexStart) + expValue;

        return isLong(
            t.getText(),
            intIndexStart,
            intIndexStop,
            digitCount,
            t.isNegative());
    }
    
    private static final boolean isLong(
        CharSequence text,
        int start,
        int stop,
        int digitCount,
        boolean isneg) {

        if (digitCount < LONG_MAX_DIGITS) {
            return true;
        }
        
        if (digitCount > LONG_MAX_DIGITS) {
            return false;
        }

        final String s = isneg ? LONG_BOUNDARY_NEG : LONG_BOUNDARY_POS; // NOPMD
        
        for (int i = start, j = 0; i < stop; i++, j++) { // NOPMD
            char a = text.charAt(i);
            char b = s.charAt(j);
            
            if (a < b) {
                return true;
            }
            
            if (a > b) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Convert the given NumberText to a long value.
     * 
     * <p>You must call {@link #isLong(NumberText)} first to determine
     * if the number is a valid long value. Otherwise, you may trigger an
     * exception or get a nonsense result.
     *
     * @param t a valid NumberText
     */
    public static final long toLong(NumberText t) {
        return toLong(
            t.getText(),
            t.getStartIndex(),
            t.getIntegerStopIndex(),
            getExponentValue(t));
    }

    /**
     * Convert the given CharSequence to a long value.
     */
    private static final long toLong(
            CharSequence text,
            int start,
            int stop,
            int expValue) {
        return parseLong(text, start, stop, 0) * E[expValue];
    }
    
    /**
     * Attempt to build a non-fractional Number from the given NumberText.
     */
    @SuppressWarnings({
        "PMD.CyclomaticComplexity",
        "PMD.NPathComplexity"})
    private static final Number toNonFractional(
            NumberText t,
            boolean primitiveOnly,
            BigMathProvider mcp) {

        final CharSequence text = t.getText();

        final int expValue = getExponentValue(t);
        final int intIndexStart = t.getIntegerStartIndex();
        final int intIndexStop = t.getIntegerStopIndex();
        final int digitCount = (intIndexStop - intIndexStart) + expValue;
        final boolean isneg = t.isNegative();

        if (isLong(text, intIndexStart, intIndexStop, digitCount, isneg)) {
            //
            // this is the common case
            //
            return Long.valueOf(toLong(
                text,
                t.getStartIndex(),
                intIndexStop,
                expValue));
        }

        if (primitiveOnly) {
            //
            // only option left
            //
            return toDouble(t);
        }

        final char[] chars = toChars(
                text,
                t.getStartIndex(),
                intIndexStop);

        final String s = new String(chars);

        final Number overflow = getOverflow(
            mcp == null ? null : mcp.getBigIntegerBoundary(isneg),
            isneg ? NEGATIVE_INFINITY : POSITIVE_INFINITY,
            s,
            digitCount,
            mcp);

        if (overflow != null) {
            return overflow;
        }

        BigInteger ret = new BigInteger(s);
    
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
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
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
        
        switch (c) { // NOPMD - fall through
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
    
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private static final char[] toChars(CharSequence s, int start, int stop) {
        final int len = stop - start;
        final char[] ret = new char[len];

        for (int i = start, j = 0; i < stop; i++, j++) { // NOPMD - ForLoopVariableCount
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

    /**
     * Get the BigMathProvider for this NumberBuilder.
     * @return a valid BigMathProvider or null
     */
    public BigMathProvider getMathContextProvider() {
        return mcp;
    }

    /**
     * Set the BigMathProvider for this NumberBuilder.
     * @param mcp a valid BigMathProvider or null
     */
    public void setMathContextProvider(BigMathProvider mcp) {
        this.mcp = mcp;
    }
}
