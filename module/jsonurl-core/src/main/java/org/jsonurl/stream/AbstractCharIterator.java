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
import java.nio.charset.MalformedInputException;
import org.jsonurl.JsonUrlLimits;
import org.jsonurl.LimitException;

/**
 * Provides the boilerplate for CharIterator implementations.
 *
 * <p>This class provides buffering and offset/line/column counting. If
 * you need to implement a new CharIterator then extending this will provide
 * that basic functionality for you.
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
public abstract class AbstractCharIterator implements CharIterator {
    /**
     * carriage return.
     */
    private static final char CR = '\r'; // NOPMD

    /**
     * line feed.
     */
    private static final char LF = '\n'; // NOPMD

    /**
     * Resource name.
     */
    private final String name;

    /**
     * A single buffered character.
     */
    private int bufChar = EOF;

    /**
     * Current line number.
     */
    private int line = 1;

    /**
     * Current column number.
     */
    private int column;

    /**
     * Current character offset.
     */
    private long offset;

    /**
     * Max parsed chars.
     */
    private final long limit;

    /**
     * Create a new AbstractCharIterator. {@link
     * org.jsonurl.JsonUrlLimits#DEFAULT_MAX_PARSE_CHARS
     * DEFAULT_MAX_PARSE_CHARS} characters will be parsed before an
     * exception is thrown.
     * @param name Resource {@link Resource#getName() name}
     */
    public AbstractCharIterator(String name) {
        this(name, JsonUrlLimits.DEFAULT_MAX_PARSE_CHARS);
    }

    /**
     * Create a new AbstractCharIterator.
     * @param name Resource {@link Resource#getName() name}
     * @param limit maximum number of parsed characters before a
     *  {@link org.jsonurl.LimitException LimitException} is thrown. 
     */
    public AbstractCharIterator(String name, long limit) {
        this.name = name;
        this.limit = limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int nextChar() throws IOException {
        if (bufChar == EOF) {
            return readUpdatePosition();
        }

        int ret = bufChar;
        bufChar = EOF;
        return ret;
    }

    @Override
    public int peekChar() throws IOException {
        if (bufChar == EOF) {
            bufChar = readUpdatePosition();
        }

        return bufChar;
    }

    @Override
    public void pushbackChar(int chr) {
        this.bufChar = chr;
    }

    /**
     * Read the next character. A subclasse must override this method to
     * provide the next character in the input stream.
     * @see #nextChar()
     */
    protected abstract int read() throws IOException;

    /**
     * Read a single character and update the current position information.
     */
    private int readUpdatePosition() throws IOException {
        int ret = read();

        switch (ret) {
        case EOF:
            return EOF;

        case CR:
            ret = read();
            if (ret != LF) {
                throw new MalformedInputException(1);
            }
            // fall-through
        case LF:
            offset++;
            line++;
            column = 1;
            break;

        default:
            offset++;
            column++;
            break;
        }

        if (offset > limit) {
            throw new LimitException(
                LimitException.Message.MSG_LIMIT_MAX_PARSE_CHARS,
                offset);
        }

        return ret;
    }
}
