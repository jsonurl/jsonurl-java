/*
 * Copyright 2020 David MacCormack
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

import java.util.Set;

/**
 * Instances of this interface are used by the Parse.parse() method to abstract
 * the behavior of creating a parse result from the parsing itself.
 */
interface ParseResultFacade<R> {

    /**
     * Set the current parse location.
     */
    ParseResultFacade<R> setLocation(int location); // NOPMD - LinguisticNaming
    
    /**
     * Get a top-level empty composite.
     */
    R getEmptyComposite();

    /**
     * Begin a new array.
     * @param depth parse depth
     */
    ParseResultFacade<R> beginArray();

    /**
     * End an array.
     * @param depth parse depth
     */
    ParseResultFacade<R> endArray();

    /**
     * Begin a new array.
     * @param depth parse depth
     */
    ParseResultFacade<R> beginObject();

    /**
     * End an array.
     * @param depth parse depth
     */
    ParseResultFacade<R> endObject();

    /**
     * Add an empty composite value. 
     * @param depth parse depth
     */
    void addEmptyComposite();

    /**
     * Add the given literal.
     */
    void addLiteral(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK);
    
    /**
     * Add a single element array.
     */
    default void addSingleElementArray(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK) {
        beginArray();
        addLiteral(text, start, stop, isEmptyUnquotedStringOK);
        endArray();
    }
    
    /**
     * Add object key.
     */
    void addObjectKey(
        CharSequence text,
        int start,
        int stop,
        boolean isEmptyUnquotedStringOK);

    /**
     * Add an array element.
     */
    ParseResultFacade<R> addArrayElement();

    /**
     * Add an object element.
     */
    ParseResultFacade<R> addObjectElement();

    /**
     * Get the result.
     */
    R getResult();
    
    /**
     * Get a top-level literal result.
     */
    R getResult(
            CharSequence text,
            int start,
            int stop,
            boolean isEmptyUnquotedStringOK);

    /**
     * Test if the given result is valid.
     */
    boolean isValid(Set<ValueType> canReturn, R result);

    /**
     * Returns true if this is an implied JSON&#x2192;URL object.
     */
    boolean isImpliedObject();

    /**
     * Returns true if this is an implied JSON&#x2192;URL array.
     */
    boolean isImpliedArray();

    /**
     * Set a value for this given key.
     *
     * <p>An implied-object or wfu-implied-object may allow missing,
     * top-level values. This method is called to supply it. It may also
     * throw a ParseException if the feature is unsupported.
     */
    ParseResultFacade<R> addMissingValue(
            CharSequence text,
            int start,
            int stop);
}