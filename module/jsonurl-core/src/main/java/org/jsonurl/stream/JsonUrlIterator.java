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

package org.jsonurl.stream;

import java.util.EnumSet;
import java.util.Set;
import org.jsonurl.CompositeType;
import org.jsonurl.ExceptionProvider;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.JsonUrlOption;
import org.jsonurl.ValueType;
import org.jsonurl.text.NumberText;

/**
 * An JSON&#x2192;URL iterator provides streaming (e.g. forward,
 * read-only) access to parsed JSON&#x2192;URL text as a sequence of one or
 * more {@link org.jsonurl.stream.JsonUrlEvent events}.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public interface JsonUrlIterator extends ExceptionProvider {

    /**
     * Get the next event. Unlike a traditional {@link java.util.Iterator
     * iterator} there is no need to call a {@code hasNext()}
     * style method. You may simply continue to call this method until
     * {@link JsonUrlEvent#END_STREAM END_STREAM} is returned.
     */
    JsonUrlEvent next();

    /**
     * Get the string value. This method may be called after any literal value
     * event is returned (e.g. {@link JsonUrlEvent#VALUE_STRING VALUE_STRING},
     * {@link JsonUrlEvent#VALUE_NUMBER VALUE_NUMBER}, etc).
     */
    String getString();

    /**
     * Get the number text. This method may be called after a
     * {@link JsonUrlEvent#VALUE_NUMBER VALUE_NUMBER} event is returned.
     * Note that the returned object may be reused parse subsequent numbers
     * and is not valid if {@link #next()} is called again.
     */
    NumberText getNumberText();

    /**
     * Set the result and implied types to be parsed. This must be called
     * before the first call to {@link #next()}.
     * @param resultType a valid Set or {@code null}
     * @param impliedType valid CompositeType or {@code null}
     */
    void setType(Set<ValueType> resultType, CompositeType impliedType);

    /**
     * Set the result and implied types to be parsed. This simply calls
     * {@link #setType(Set, CompositeType)
     * setType(EnumSet.of(resultType), impliedType)}.
     */
    default void setType(ValueType resultType, CompositeType impliedType) {
        setType(
            resultType == null ? null : EnumSet.of(resultType),
            impliedType);
    }

    /**
     * Set the implied type. This simply calls
     * {@link #setType(Set, CompositeType) setType(null, impliedType)}.
     */
    default void setType(CompositeType impliedType) {
        setType((Set<ValueType>)null, impliedType);
    }

    /**
     * Set the result type. This simply calls
     * {@link #setType(Set, CompositeType) setType(resultType, null)}.
     */
    default void setType(Set<ValueType> resultType) {
        setType(resultType, null);
    }

    /**
     * Set the result type. This simply calls
     * {@link #setType(Set, CompositeType) setType(resultType, null)}.
     */
    default void setType(ValueType resultType) {
        setType(resultType == null ? null : EnumSet.of(resultType));
    }

    /**
     * Create a new JsonUrlIterator for the JSON&#x2192;URL grammar.
     * @param text input text
     * @param limits a valid JsonUrlLimits or null
     * @param options valid JsonUrlOptions or null
     */
    static JsonUrlIterator newInstance(
            CharIterator text,
            JsonUrlLimits limits,
            Set<JsonUrlOption> options) {
        return new JsonUrlGrammar(text, limits, options);
    }
}
