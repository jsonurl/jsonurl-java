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

import java.io.IOException;
import org.jsonurl.JsonUrlLimits;

/**
 * A {@link CharIterator} that reads from a {@link CharSequence}.
 *
 * <p>This class provides a {@code CharIterator} interface for any CharSquence,
 * such as {@link String}. Note that the length is cached in the constructor,
 * so don't use this class if the underlying content can change after it is
 * constructed (e.g. a {@link StringBuilder} that you're appending to).
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public class JsonUrlCharSequence extends AbstractCharIterator
        implements CharSequence {

    /**
     * caller provided text.
     */
    private final CharSequence text;

    /**
     * Ending length.
     */
    private final int length; // NOPMD - AvoidFieldNameMatchingMethodName

    /**
     * Current iterator position.
     */
    private int position;

    /**
     * Current iterator position.
     */
    private final int endPosition;

    /**
     * Create a new JsonUrlCharSequence.
     * @param text a valid CharSequence
     * @see #JsonUrlCharSequence(String, CharSequence, long)
     */
    public JsonUrlCharSequence(CharSequence text) {
        this(null, text);
    }

    /**
     * Create a new JsonUrlCharSequence.
     * @param name the resource {@link Resource#getName() name}
     * @param text a valid CharSequence
     * @see #JsonUrlCharSequence(String, CharSequence, long)
     */
    public JsonUrlCharSequence(String name, CharSequence text) {
        this(name, text, 0, text.length(),
                JsonUrlLimits.DEFAULT_MAX_PARSE_CHARS);
    }

    /**
     * Create a new JsonUrlCharSequence.
     * @param name the resource {@link Resource#getName() name}
     * @param text a valid CharSequence
     * @param limit maximum number of parsed characters before a
     *     {@link org.jsonurl.LimitException LimitException} is thrown.
     */
    public JsonUrlCharSequence(String name, CharSequence text, long limit) {
        this(name, text, 0, text.length(), limit);
    }

    /**
     * Create a new JsonUrlCharSequence.
     * @param name the resource {@link Resource#getName() name}
     * @param text a valid CharSequence
     * @param offset starting offset
     * @param length stop when this length is reached
     * @param limit maximum number of parsed characters before a
     *     {@link org.jsonurl.LimitException LimitException} is thrown.
     */
    public JsonUrlCharSequence(
            String name,
            CharSequence text,
            int offset,
            int length,
            long limit) {

        super(name, limit);
        this.text = text;
        this.position = offset;
        this.endPosition = offset + length;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return text.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return text.subSequence(start, end);
    }

    @Override
    protected int read() throws IOException {
        if (endPosition <= position) {
            return EOF;
        }

        char ret = text.charAt(position);
        position++;
        return ret;
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
