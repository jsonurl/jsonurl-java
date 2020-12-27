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

import static org.jsonurl.BigMathProvider.NEGATIVE_INFINITY;
import static org.jsonurl.BigMathProvider.POSITIVE_INFINITY;
import static org.jsonurl.JsonUrlOption.optionAQF;
import static org.jsonurl.LimitException.Message.MSG_LIMIT_INTEGER;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Set;
import org.jsonurl.BigMathProvider;
import org.jsonurl.BigMathProvider.BigIntegerOverflow;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.LimitException;

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
 * {@link java.lang.Number Number} n = new NumberBuilder("1.234").{@link #build()};
 * 
 * // Example 2
 * NumberBuilder nb = new NumberBuilder("1.234");
 * if (!nb.{@link #isNumber()}) {
 *     // handle error
 * }
 * double d = nb.{@link #toDouble()};
 * </pre>
 *
 * <h2>Reuse</h2>
 *
 * <p>An instance of NumberBuilder may also be reused via a call to the
 * {@link #reset()} method.
 * <pre>
 * NumberBuilder nb = new NumberBuilder();
 * if (!nb.{@link #parse(CharSequence, int, int) parse("1.234")}) {
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
     * minus/dash.
     */
    private static final char MINUS = '-';

    /**
     * minus/dash.
     */
    private static final char PLUS = '+';
    
    /**
     * Lookup table for exponent values.
     */
    @SuppressWarnings({
        "PMD.UseUnderscoresInNumericLiterals",
        "PMD.ShortVariable"})
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
    private final BigMathProvider mcp;

    /**
     * Create a new NumberBuilder.
     *
     * <p>This NumberBuilder will not have any text. You'll need to call
     * {@link #parse(CharSequence, int, int)}.
     */
    public NumberBuilder() {
        mcp = null;
    }

    /**
     * Create a new NumberBuilder.
     *
     * <p>This NumberBuilder will not have any text. You'll need to call
     * {@link #parse(CharSequence, int, int)}.
     */
    public NumberBuilder(BigMathProvider mcp) {
        this.mcp = mcp;
    }

    /**
     * Create a new NumberBuilder with the given text.
     *
     * <p>This is a convenience for {@link
     * #NumberBuilder(CharSequence, int, int)
     * NumberBuilder(s, 0, s.length())}.
     */
    public NumberBuilder(CharSequence text) {
        this(text, null);
    }

    /**
     * Create a new NumberBuilder with the given text.
     *
     * <p>This is a convenience for {@link
     * #NumberBuilder(CharSequence, int, int)
     * NumberBuilder(s, 0, s.length())}.
     */
    public NumberBuilder(CharSequence text, BigMathProvider mcp) {
        this(text, 0, text.length(), mcp, null);
    }

    /**
     * Create a new NumberBuilder with the given text.
     * @param text text
     * @param start start index
     * @param stop stop index
     */
    public NumberBuilder(CharSequence text, int start, int stop) {
        this(text, start, stop, null);
    }

    /**
     * Create a new NumberBuilder with the given text.
     * @param text text
     * @param start start index
     * @param stop stop index
     */
    public NumberBuilder(
            CharSequence text,
            int start,
            int stop,
            Set<JsonUrlOption> options) {
        this(text, start, stop, null, options);
    }

    /**
     * Create a new NumberBuilder with the given text.
     * @param text text
     * @param start start index
     * @param stop stop index
     * @param mcp a valid BigMathProvider or {@code null}
     * @param options a valid set of options or {@code null}
     */
    public NumberBuilder(
            CharSequence text,
            int start,
            int stop,
            BigMathProvider mcp,
            Set<JsonUrlOption> options) {
        this.mcp = mcp;
        parse(text, start, stop, options);
    }

    /**
     * Create a new NumberBuilder from the given NumberText.
     */
    public NumberBuilder(NumberText text) {
        this(text, text instanceof NumberBuilder
            ? ((NumberBuilder)text).mcp : null); // NOPMD - NullAssignment
    }

    /**
     * Create a new NumberBuilder from the given NumberText.
     */
    public NumberBuilder(NumberText text, BigMathProvider mcp) {
        this.expIndexStart = text.getExponentStartIndex();
        this.expIndexStop = text.getExponentStopIndex();
        this.exponentType = text.getExponentType();
        this.fractIndexStart = text.getFractionalStartIndex();
        this.fractIndexStop = text.getFractionalStopIndex();
        this.intIndexStart = text.getIntegerStartIndex();
        this.intIndexStop = text.getIntegerStartIndex();
        this.start = text.getStartIndex();
        this.stop = text.getStopIndex();
        this.text = text.getText();
        this.mcp = mcp;
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

    private static boolean hasFract(
            CharSequence text,
            int start,
            int stop) {

        int i = start; // NOPMD

        if (stop <= i || text.charAt(i) != '.') {
            return false;
        }

        i++;

        if (i == stop) { 
            return false;
        }

        return isDigit(text.charAt(i));
    }
    
    @Override
    public Exponent getExponentType() {
        return this.exponentType;
    }

    /**
     * Calculate exponent string.
     */
    private static NumberText.Exponent getExponentType(//NOPMD
            CharSequence text,
            int start,
            int stop,
            Set<JsonUrlOption> options) {

        if (stop <= start) {
            return NumberText.Exponent.NONE;
        }

        int i = start; // NOPMD

        switch (text.charAt(i)) {
        case 'e':
        case 'E':
            i++;
            if (i == stop) {
                return NumberText.Exponent.NONE;
            }
            break;
        default:
            return NumberText.Exponent.NONE;
        }

        final NumberText.Exponent ret;
        char c = text.charAt(i); //NOPMD

        switch (c) {
        case PLUS:
            i++;
            if (i == stop || optionAQF(options)) {
                return NumberText.Exponent.NONE;
            }
            c = text.charAt(i);
            ret = NumberText.Exponent.POSITIVE_VALUE; //NOPMD
            break;
        case MINUS:
            i++;
            if (i == stop) {
                return NumberText.Exponent.NONE;
            }
            c = text.charAt(i);
            ret = NumberText.Exponent.NEGATIVE_VALUE; //NOPMD
            break;
        default:
            ret = NumberText.Exponent.JUST_VALUE; //NOPMD
            break;
        }
        if (!isDigit(c)) {
            return NumberText.Exponent.NONE;
        }

        return ret;
    }

    /**
     * Parse the given character sequence.
     * This is a convenience for {@link #parse(CharSequence, int, int)
     * parse(text, 0, text.length)}.
     *
     * @param text a valid CharSequence
     */
    public boolean parse(CharSequence text) {
        return parse(text, 0, text.length(), null);
    }

    /**
     * Parse the given character sequence.
     * This is a convenience for {@link #parse(CharSequence, int, int)
     * parse(text, 0, text.length)}.
     *
     * @param text a valid CharSequence
     */
    public boolean parse(CharSequence text, Set<JsonUrlOption> options) {
        return parse(text, 0, text.length(), options);
    }

    /**
     * Parse the given character sequence.
     * 
     * <p>Any {@link BigMathProvider} given
     * in the constructor is not used in this method, so it is safe to assume
     * you can parse text without regard to how a number might be built later
     * via the {@link #build(boolean)} method.
     *
     * @param text a valid CharSequence
     * @param start an index
     * @param stop an index
     * @return true if the CharSequence was successfully parsed
     */
    public boolean parse(//NOPMD
            CharSequence text,
            int start,
            int stop,
            Set<JsonUrlOption> options) {
        int pos = this.start = start; //NOPMD

        char c = text.charAt(start); //NOPMD

        if (c == MINUS) {
            pos++;

            if (pos == stop) {
                return false;
            }

            c = text.charAt(pos);
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
            pos = digits(text, pos + 1, stop);
            break;
        default:
            return false;
        }

        intIndexStop = pos;

        if (hasFract(text, pos, stop)) {
            fractIndexStart = pos + 1;
            fractIndexStop = pos = digits(text, pos + 1, stop);
        }

        exponentType = getExponentType(text, pos, stop, options);

        switch (exponentType) { // NOPMD - SwitchStmtsShouldHaveDefault
        case JUST_VALUE:
            expIndexStart = pos + 1;
            pos = expIndexStop = digits(text, expIndexStart, stop);
            break;
        case NEGATIVE_VALUE:
        case POSITIVE_VALUE:
            expIndexStart = pos + 2;
            pos = expIndexStop = digits(text, expIndexStart, stop);
            break;
        case NONE:
            break;
        }

        this.text = text;
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
    public static boolean isNumber(CharSequence text) {
        return isNumber(text, 0, text.length(), false, null);
    }
    
    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     * <p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, 0, s.length(), isInteger)}.
     */
    public static boolean isNumber(CharSequence text, boolean isNonFractional) {
        return isNumber(text, 0, text.length(), isNonFractional, null);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     * <p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)}
     * isNumber(s, 0, stop, false)}.
     * @param text a valid CharSequence
     * @param start an index
     * @param stop an index
     * @return true if the CharSequence is a JSON&#x2192;URL number
     */
    public static boolean isNumber(
            CharSequence text,
            int start,
            int stop) {
        return isNumber(text, start, stop, false, null);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     * <p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)}
     * isNumber(s, 0, stop, false)}.
     * @param text a valid CharSequence
     * @param start an index
     * @param stop an index
     * @param options a valid set of options or {@code null}
     * @return true if the CharSequence is a JSON&#x2192;URL number
     */
    public static boolean isNumber(
            CharSequence text,
            int start,
            int stop,
            Set<JsonUrlOption> options) {
        return isNumber(text, start, stop, false, options);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     * 
     * @param text a valid CharSequence
     * @param start an index
     * @param stop an index
     * @param options a valid set of options or {@code null}
     * @return true if the CharSequence is a JSON&#x2192;URL number
     */
    public static boolean isNumber(//NOPMD
            CharSequence text,
            int start,
            int stop,
            boolean isNonFractional,
            Set<JsonUrlOption> options) {

        int pos = start; //NOPMD - DataflowAnomalyAnalysis

        char c = text.charAt(start); //NOPMD

        if (c == MINUS) {
            pos++;

            if (pos == stop) {
                return false;
            }

            c = text.charAt(pos);
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
            pos = digits(text, pos + 1, stop);
            break;
        default:
            return false;
        }

        if (hasFract(text, pos, stop)) {
            if (isNonFractional) {
                return false;
            }
            pos = digits(text, pos + 1, stop);
        }

        final int expDigitSkip;

        switch (getExponentType(text, pos, stop, options)) {
        case JUST_VALUE:
            expDigitSkip = 1;
            pos = digits(text, pos + 1, stop);
            break;
        case NEGATIVE_VALUE:
            if (isNonFractional) {
                return false;
            }
            // fall through
        case POSITIVE_VALUE:
            expDigitSkip = 2;
            break;
        default:
            expDigitSkip = 0;
            break;
        }
        
        if (expDigitSkip > 0) {
            pos = digits(text, pos + expDigitSkip, stop);
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
    public static boolean isNonFractional(CharSequence text) {
        return isNumber(text, 0, text.length(), true, null);
    }

    /**
     * Determine if the given CharSequence is a valid JSON&#x2192;URL number literal.
     *
     *<p>Convenience for {@link #isNumber(CharSequence, int, int, boolean)
     * isNumber(s, start, stop, true)}.
     */
    public static boolean isNonFractional(
            CharSequence text,
            int start,
            int stop) {
        return isNumber(text, start, stop, true, null);
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
    public static final double toDouble(NumberText num) {
        char[] chars = toChars(
                num.getText(),
                num.getStartIndex(),
                num.getStopIndex());

        return Double.parseDouble(new String(chars));
    }

    /**
     * Parse the given NumberText as a {@link java.math.BigDecimal}.
     */
    public static final BigDecimal toBigDecimal(
            NumberText num,
            BigMathProvider mcp) {

        char[] str = toChars(
                num.getText(),
                num.getStartIndex(),
                num.getStopIndex());

        MathContext mctxt = mcp == null ? null : mcp.getMathContext();

        return mctxt == null
                ? new BigDecimal(str) : new BigDecimal(str, mctxt);
    }

    /**
     * Build a {@link java.math.BigDecimal BigDecimal}.
     * This simply calls
     * {@link #toBigDecimal(NumberText, BigMathProvider)
     * toBigDecimal(this, getBigMathProvider())}.
     * @see #getBigMathProvider()
     */
    public BigDecimal toBigDecimal() {
        return toBigDecimal(this, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number}.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(this, true, getBigMathProvider())}.
     * @see #getBigMathProvider()
     */
    public Number build() {
        return build(this, true, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number}.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(this, primitiveOnly, getBigMathProvider())}.
     *
     * @param primitiveOnly return a Long or Double
     * @see #getBigMathProvider()
     */
    public Number build(boolean primitiveOnly) {
        return build(this, primitiveOnly, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number}.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(t, false, mcp)}.
     * 
     * @param num a valid NumberText
     * @param mcp a valid BigMathProvider or null
     */
    public static final Number build(
            NumberText num,
            BigMathProvider mcp) {
        return build(num, false, mcp);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     * Convenience for {@link #build(NumberText, boolean, BigMathProvider)
     * build(t, primitiveOnly, null)}.
     * 
     * @param num a valid NumberText
     * @param primitiveOnly return a Long or Double
     */
    public static final Number build(
            NumberText num,
            boolean primitiveOnly) {
        return build(num, primitiveOnly, null);
    }

    /**
     * Build a {@link java.lang.Number Number} from the given NumberText.
     *
     * <p>The benefit of this method (over {@link #toDouble(NumberText)} or
     * {@link #toBigDecimal(NumberText, BigMathProvider)}) is that it has the
     * logic to return a Number tailored to the value itself. For example, if
     * the value can be represented as a {@link Long} then it will be.
     *
     * @param num a valid NumberText
     * @param primitiveOnly return a Long or Double
     * @param mcp a valid BigMathProvider or null
     * @return an instance of java.lang.Number
     */
    public static final Number build(
            NumberText num, 
            boolean primitiveOnly,
            BigMathProvider mcp) {

        if (!num.hasFractionalPart()) {
            switch (num.getExponentType()) { //NOPMD - no default
            case NEGATIVE_VALUE:
                break;
            case JUST_VALUE:
            case POSITIVE_VALUE:
            case NONE:
                return toNonFractional(num, primitiveOnly, mcp);
            }
        }

        return primitiveOnly ? toDouble(num) : toBigDecimal(num, mcp);
    }

    /**
     * Calculate an overflow value.
     */
    private static Number getOverflow(//NOPMD
        String strLimit,
        Double infinity,
        String text,
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
            || text.compareTo(strLimit) > 0;
            
        if (!isOver) {
            // close, but still within limit
            return null;
        }

        final BigIntegerOverflow overOp =
            mcp == null ? null : mcp.getBigIntegerOverflow();

        if (overOp == null) {
            throw new LimitException(MSG_LIMIT_INTEGER);
        }

        switch (overOp) { //NOPMD - SwitchStmtsShouldHaveDefault
        case DOUBLE:
            return Double.valueOf(text);
        case BIG_DECIMAL:
            //
            // I don't have to check for mcp == null here as the
            // check above ensures that, if it's null, I'll throw
            // an exception before I get here.
            //
            MathContext mctxt = mcp.getMathContext();
            return mctxt == null
                    ? new BigDecimal(text) : new BigDecimal(text, mctxt);
        case INFINITY:
            return infinity;
        }

        return null;
    }

    /**
     * get the value of the exponent in {@code t}.
     */
    private static int getExponentValue(NumberText num) {
        return num.getExponentType() == null ? 0 : parseInteger(
            num.getText(),
            num.getExponentStartIndex(),
            num.getExponentStopIndex(),
            0);
    }
    
    /**
     * Test if the given NumberText can be stored in a {@code long}.
     * @param num a valid NumberText
     */
    public static final boolean isLong(NumberText num) {
        if (!num.isNonFractional()) {
            return false;
        }
        final int expValue = getExponentValue(num);
        final int intIndexStart = num.getIntegerStartIndex();
        final int intIndexStop = num.getIntegerStopIndex();
        final int digitCount = (intIndexStop - intIndexStart) + expValue;

        return isLong(
            num.getText(),
            intIndexStart,
            intIndexStop,
            digitCount,
            num.isNegative());
    }
    
    private static boolean isLong(
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
            char a = text.charAt(i); // NOPMD
            char b = s.charAt(j); // NOPMD
            
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
     * @param num a valid NumberText
     */
    public static final long toLong(NumberText num) {
        return toLong(
            num.getText(),
            num.getStartIndex(),
            num.getIntegerStopIndex(),
            getExponentValue(num));
    }

    /**
     * Convert the given CharSequence to a long value.
     */
    private static long toLong(
            CharSequence text,
            int start,
            int stop,
            int expValue) {
        return parseLong(text, start, stop, 0) * E[expValue];
    }
    
    /**
     * Attempt to build a non-fractional Number from the given NumberText.
     */
    private static Number toNonFractional(
            NumberText num,
            boolean primitiveOnly,
            BigMathProvider mcp) {

        final CharSequence text = num.getText();

        final int expValue = getExponentValue(num);
        final int intIndexStart = num.getIntegerStartIndex();
        final int intIndexStop = num.getIntegerStopIndex();
        final int digitCount = (intIndexStop - intIndexStart) + expValue;
        final boolean isneg = num.isNegative();

        if (isLong(text, intIndexStart, intIndexStop, digitCount, isneg)) {
            //
            // this is the common case
            //
            return Long.valueOf(toLong(
                text,
                num.getStartIndex(),
                intIndexStop,
                expValue));
        }

        if (primitiveOnly) {
            //
            // only option left
            //
            return toDouble(num);
        }

        final char[] chars = toChars(
                text,
                num.getStartIndex(),
                intIndexStop);

        final String str = new String(chars);

        final Number overflow = getOverflow(
            mcp == null ? null : mcp.getBigIntegerBoundary(isneg),
            isneg ? NEGATIVE_INFINITY : POSITIVE_INFINITY,
            str,
            digitCount,
            mcp);

        if (overflow != null) {
            return overflow;
        }

        BigInteger ret = new BigInteger(str);
    
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
     *
     * @param text a non-null character sequence
     * @param start start index
     * @param stop stop index
     * @return an integer
     */
    private static int parseInteger(
            CharSequence text,
            int start,
            int stop,
            int defaultValue) {
        
        if (start == stop) {
            return defaultValue;
        }

        int ret = 0;
        
        for (int i = start; i < stop; i++) {
            char cur = text.charAt(i);
            ret = ret * 10 + (cur - '0');
        }

        return ret;
    }
    
    /**
     * Parse a Java integer.
     *
     * <p>This is similar to {@link java.lang.Long#parseLong(String)},
     * however, it accepts a bounded CharSequence and default value.
     * @param text a non-null character sequence
     * @param start start index
     * @param stop stop index
     * @return a long
     */
    private static long parseLong(
            CharSequence text,
            int start, // NOPMD
            int stop,
            int defaultValue) {
        
        if (start == stop) {
            return defaultValue;
        }

        long ret = 0;
        boolean isneg = false; // NOPMD

        char c = text.charAt(start); // NOPMD
        
        switch (c) {
        case MINUS:
            isneg = true;
            start++;
            break;
        case PLUS:
            start++;
            break;
        default:
            break;
        }
        
        for (int i = start; i < stop; i++) {
            c = text.charAt(i);
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
    
    @SuppressWarnings({
        "PMD.DataflowAnomalyAnalysis",
        "PMD.ForLoopVariableCount"})
    private static char[] toChars(CharSequence text, int start, int stop) {
        final int len = stop - start;
        final char[] ret = new char[len];

        for (int i = start, j = 0; i < stop; i++, j++) { 
            ret[j] = text.charAt(i);
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
    public BigMathProvider getBigMathProvider() {
        return mcp;
    }

    /**
     * Test if the given character is a digit.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static boolean isDigit(char value) {
        switch (value) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return true;
        default:
            return false;
        }
    }
    
    /**
     * Find the index of the last digit.
     */
    private static int digits(CharSequence text, int start, int stop) {
        for (int i = start; i < stop; i++) {
            if (!isDigit(text.charAt(i))) {
                return i;
            }
        }
        
        return stop;
    }
}
