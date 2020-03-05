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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A JsonTextBuilder that appends JSON->URL text to any Appendable.
 * @param <A> Accumulator type
 * @param <R> Result type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public abstract class JsonUrlTextAppender<A extends Appendable, R>
        implements JsonTextBuilder<A, R>, Appendable {

    protected A out;

    /**
     * Create a new JsonUrlTextAppender.
     *
     * @param out JSON->URL text destination
     */
    public JsonUrlTextAppender(A out) {
        this.out = out;
    }

    @Override
    public JsonUrlTextAppender<A,R> beginObject() throws IOException {
        out.append('(');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> endObject() throws IOException {
        out.append(')');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> beginArray() throws IOException {
        out.append('(');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> endArray() throws IOException {
        out.append(')');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> valueSeparator() throws IOException {
        out.append(',');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> nameSeparator() throws IOException {
        out.append(':');
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> addNull() throws IOException {
        out.append("null");
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(BigDecimal value) throws IOException {
        out.append(String.valueOf(value));
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(BigInteger value) throws IOException {
        out.append(String.valueOf(value));
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(long value) throws IOException {
        out.append(String.valueOf(value));
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(double value) throws IOException {
        out.append(String.valueOf(value));
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(boolean value) throws IOException {
        out.append(String.valueOf(value));
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> add(
            CharSequence s,
            int start,
            int end,
            boolean isKey) throws IOException {
        JsonUrl.appendLiteral(out, s, start, end, isKey);
        return this;
    }
    
    @Override
    public JsonUrlTextAppender<A,R> append(CharSequence csq) throws IOException {
        out.append(csq);
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> append(
            CharSequence csq,
            int start,
            int end) throws IOException {
        out.append(csq, start, end);
        return this;
    }

    @Override
    public JsonUrlTextAppender<A,R> append(char c) throws IOException {
        out.append(c);
        return this;
    }
}
