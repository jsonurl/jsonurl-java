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

package org.jsonurl.text;

/**
 * A {@link JsonTextBuilder} that is also {@link Appendable}.
 *
 * <p>This interface implements the builder pattern. 
 * 
 * @param <A> Accumulator type
 * @param <R> Result type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2020-11-01
 */
@SuppressWarnings(
    // false positive - it's used by subclasses
    "java:S2326")
public interface JsonTextAppendable<A,R> extends JsonTextBuilder<R>, Appendable {
}
