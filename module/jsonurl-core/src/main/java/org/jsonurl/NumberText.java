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

/**
 * NumberText provides access to a parsed JSON-&gt;URL number literal.
 *
 * <p>The text of a number is broken down into three parts: integer part,
 * decimal part, and exponent. These parts may be accessed via start and stop
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
    public enum Exponent {
        NONE,
        JUST_VALUE,
        POSITIVE_VALUE,
        NEGATIVE_VALUE
    }

    /**
     * Get the input/parsed text.
     */
    public CharSequence getText();

    /**
     * Get the start index of the number's integer part.
     */
    public int getIntegerStartIndex();

    /**
     * Get the stop index of the number's integer part.
     */
    public int getIntegerStopIndex();

    /**
     * Get the start index of the number's decimal part.
     */
    public int getDecimalStartIndex();

    /**
     * Get the stop index of the number's decimal part.
     */
    public int getDecimalStopIndex();

    /**
     * Get the start index of the number's exponent part.
     */
    public int getExponentStartIndex();

    /**
     * Get the stop index of the number's exponent part.
     */
    public int getExponentStopIndex();

    /**
     * Get the start index of the number (inside {@link #getText()}.
     */
    public int getStartIndex();

    /**
     * Get the stop index of the number (inside {@link #getText()}.
     */
    public int getStopIndex();

    /**
     * Get the parsed exponent's type.
     */
    public Exponent getExponentType();

    /**
     * Test if this NumberText has a decimal part.
     */
    default boolean hasDecimalPart() {
        return this.getDecimalStopIndex() > this.getDecimalStartIndex();
    }

    /**
     * Test if this NumberText has an integer part and non-negative exponent.
     */
    default boolean isInteger() {
        return !hasDecimalPart()
                && getExponentType() != Exponent.NEGATIVE_VALUE;
    }
}
