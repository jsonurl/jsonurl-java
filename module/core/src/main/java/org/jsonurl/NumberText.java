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
 * NumberText provides access to a parsed JSON->URL number literal.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface NumberText {

    /**
     * type of exponent value.
     */
    public enum Exponent {
        NONE,
        JUST_VALUE,
        POSITIVE_VALUE,
        NEGATIVE_VALUE
    }

    public CharSequence getText();

    public int getIntegerStartIndex();

    public int getIntegerStopIndex();

    public int getDecimalStartIndex();

    public int getDecimalStopIndex();

    public int getExponentStartIndex();

    public int getExponentStopIndex();

    public int getStartIndex();

    public int getStopIndex();

    public Exponent getExponentType();

    default boolean hasDecimalPart() {
        return this.getDecimalStopIndex() > this.getDecimalStartIndex();
    }
    
    default boolean isInteger() {
        return !hasDecimalPart()
                && getExponentType() != Exponent.NEGATIVE_VALUE;
    }
}
