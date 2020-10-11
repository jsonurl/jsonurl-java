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
 * NumberText provides access to a parsed JSON&#x2192;URL number literal.
 *
 * <p>The text of a number is broken down into three parts: integer part,
 * fractional part, and exponent. These parts may be accessed via start and stop
 * indexes into the original parsed text.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface NumberText {

    /**
     * type of parsed exponent string.
     */
    enum Exponent {
        NONE,
        JUST_VALUE,
        POSITIVE_VALUE,
        NEGATIVE_VALUE
    }

    /**
     * Get the input/parsed text.
     */
    CharSequence getText();

    /**
     * Get the start index of the number's integer part.
     */
    int getIntegerStartIndex();

    /**
     * Get the stop index of the number's integer part.
     */
    int getIntegerStopIndex();

    /**
     * Get the start index of the number's fractional part.
     */
    int getFractionalStartIndex();

    /**
     * Get the stop index of the number's fractional part.
     */
    int getFractionalStopIndex();

    /**
     * Get the start index of the number's exponent part.
     */
    int getExponentStartIndex();

    /**
     * Get the stop index of the number's exponent part.
     */
    int getExponentStopIndex();

    /**
     * Get the start index of the number (inside {@link #getText()}.
     */
    int getStartIndex();

    /**
     * Get the stop index of the number (inside {@link #getText()}.
     */
    int getStopIndex();

    /**
     * Get the parsed exponent's type.
     */
    Exponent getExponentType();

    /**
     * Test if this text holds a negative number.
     * @return true if the number is negative
     */
    default boolean isNegative() {
        return this.getIntegerStartIndex() > this.getStartIndex();
    }

    /**
     * Test if this NumberText has a fractional part.
     */
    default boolean hasFractionalPart() {
        return this.getFractionalStopIndex() > this.getFractionalStartIndex();
    }
    
    /**
     * Test if this NumberText has an integer part.
     */
    default boolean hasIntegerPart() {
        return this.getIntegerStopIndex() > this.getIntegerStartIndex();
    }
    
    /**
     * Test if this NumberText is a non-fractional number.
     * @return hasIntegerPart()
     *     &amp;&amp; !hasFractionalPart()
     *     &amp;&amp; getExponentType() != Exponent.NEGATIVE_VALUE;
     */
    default boolean isNonFractional() {
        return hasIntegerPart()
            && !hasFractionalPart()
            && getExponentType() != Exponent.NEGATIVE_VALUE;
    }
}
