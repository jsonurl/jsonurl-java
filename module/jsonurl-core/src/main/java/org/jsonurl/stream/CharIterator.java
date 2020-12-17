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

/**
 * A named sequence of Java {@code char}s with a current position. 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public interface CharIterator extends Resource {
    
    /**
     * end-of-file marker.
     */
    int EOF = -1;

    /**
     * Get the current character offset.
     */
    long getOffset();

    /**
     * Get the current line number.
     */
    int getLineNumber();

    /**
     * Get the current column number.
     */
    int getColumnNumber();

    /**
     * Get the next {@code char}. If no characters are remaining, or the
     * iterator is closed, then {@link #EOF} is returned.
     */
    int nextChar() throws IOException;

    /**
     * Peek at the next {@code char}. If no characters are remaining, or the
     * iterator is closed, then {@link #EOF} is returned.
     */
    int peekChar() throws IOException;

    /**
     * Pushback a single {@code char}.
     * @see #nextChar()
     */
    void pushbackChar(int chr);

    /**
     * Test if the iterator has reached end-of-text. 
     */
    default boolean atEnd() throws IOException {
        return peekChar() == EOF;
    }

    /**
     * A {@link Object#toString() toString} implementation that includes the
     * value from {@link #getOffset()}.
     * @param iter a valid CharIterator
     */
    static String toStringWithOffset(CharIterator iter) {
        StringBuilder buf = new StringBuilder(64);

        String name = iter.getName();
        long offset = iter.getOffset();
        buf.append(name == null ? "<input>" : name);
        
        if (offset > -1) {
            buf.append(':').append(offset);
        }

        return buf.toString();
    }

    /**
     * A {@link Object#toString() toString} implementation that includes the
     * value from {@link #getLineNumber()} and {@link #getColumnNumber()}.
     * @param iter a valid CharIterator
     */
    static String toStringWithLine(CharIterator iter) {
        StringBuilder buf = new StringBuilder(64);

        String name = iter.getName();
        int line = iter.getLineNumber();
        buf.append(name == null ? "<input>" : name);
        
        if (line > 0) {
            buf.append(':').append(line);
            
            int col = iter.getColumnNumber();
            if (col > -1) {
                buf.append(':').append(col);
            }
        }

        return buf.toString();
    }
}
