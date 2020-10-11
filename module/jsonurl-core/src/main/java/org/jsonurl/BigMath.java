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

import java.math.MathContext;

/**
 * A simple BigMathProvider implementation.
 */
public class BigMath implements BigMathProvider {

    /**
     * MathContext for new BigDecimal instances.
     */
    private final MathContext mathContext;

    /**
     * Positive BigInteger boundary.
     */
    private final String bigIntegerBoundaryPos;

    /**
     * Negative BigInteger boundary.
     */
    private final String bigIntegerBoundaryNeg;

    /**
     * BigInteger overflow operation.
     */
    private final BigIntegerOverflow bigIntegerOverflow;

    /**
     * Create a new BigMath.
     * @param mctxt a valid MathContext or null
     * @param bigIntegerBoundaryNeg negative value boundary
     * @param bigIntegerBoundaryPos positive value boundary
     * @param bigIntegerOverflow action on boundary overflow
     */
    public BigMath(
        MathContext mctxt,
        String bigIntegerBoundaryNeg,
        String bigIntegerBoundaryPos,
        BigIntegerOverflow bigIntegerOverflow) {

        this.mathContext = mctxt;
        this.bigIntegerBoundaryNeg = bigIntegerBoundaryNeg;
        this.bigIntegerBoundaryPos = bigIntegerBoundaryPos;
        this.bigIntegerOverflow = bigIntegerOverflow;
    }

    @Override
    public MathContext getMathContext() {
        return mathContext;
    }

    @Override
    public String getBigIntegerBoundary(boolean negative) {
        return negative ? bigIntegerBoundaryNeg : bigIntegerBoundaryPos;
    }

    @Override
    public BigIntegerOverflow getBigIntegerOverflow() {
        return bigIntegerOverflow;
    }
}
