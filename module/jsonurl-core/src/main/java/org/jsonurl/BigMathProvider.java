package org.jsonurl;

import java.math.MathContext;

/**
 * Provides an instance of MathContext and related content.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-08-02
 */
public interface BigMathProvider {
    

    /**
     * 32-bit integer negative boundary.
     */
    public static final String BIG_INTEGER32_BOUNDARY_NEG =
        String.valueOf(Integer.MIN_VALUE).substring(1);

    /**
     * 32-bit integer positive boundary.
     */
    public static final String BIG_INTEGER32_BOUNDARY_POS =
        String.valueOf(Integer.MAX_VALUE);
    
    /**
     * 64-bit integer negative boundary.
     */
    public static final String BIG_INTEGER64_BOUNDARY_NEG =
        String.valueOf(Long.MIN_VALUE).substring(1);

    /**
     * 64-bit integer positive boundary.
     */
    public static final String BIG_INTEGER64_BOUNDARY_POS =
        String.valueOf(Long.MAX_VALUE);

    /**
     * 128-bit integer negative boundary.
     */
    public static final String BIG_INTEGER128_BOUNDARY_NEG =
        "170141183460469231731687303715884105728";

    /**
     * 128-bit integer positive boundary.
     */
    public static final String BIG_INTEGER128_BOUNDARY_POS =
        "170141183460469231731687303715884105727";


    /**
     * POSITIVE_INFINITY as an instance of Double.
     */
    public final Double POSITIVE_INFINITY =
        Double.valueOf(Double.POSITIVE_INFINITY);
    
    /**
     * NEGATIVE_INFINITY as an instance of Double.
     */
    public final Double NEGATIVE_INFINITY =
        Double.valueOf(Double.NEGATIVE_INFINITY);
    
    /**
     * Enumeration of BigInteger overflow operations.
     * When a value is too big (or too small) take one of these actions.
     */
    public enum BigIntegerOverflow {
        /**
         * produce a Double instead.
         */
        DOUBLE,

        /**
         * produce a BigDecmial instead.
         */
        BIG_DECIMAL,
        
        /**
         * Return {@link #NEGATIVE_INFINITY} if the value is too small
         * or {@link #POSITIVE_INFINITY} if the value is too big.
         */
        INFINITY
    }

    /**
     * Get the MathContext from this provider.
     * @return a valid MathContext or null
     */
    public MathContext getMathContext();

    /**
     * Get the negative or positive BigInteger boundary.
     * @return a valid string or null
     */
    public String getBigIntegerBoundary(boolean negative);

    /**
     * Determine what happens on overflow. If this method returns null then an
     * exception will be thrown.
     *
     * @return a valid BigIntegerOverflow or null
     */
    public BigIntegerOverflow getBigIntegerOverflow();
}