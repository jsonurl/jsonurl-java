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

package org.jsonurl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Maps an input class to the class of its coerced type.
 *
 * <table>
 * <tr><th>Key</th><th>Value</th></tr>
 * <tr><td>{@link java.lang.Byte Byte}</td><td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.lang.Short Short}</td><td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.lang.Integer Integer}</td><td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.lang.Long Long}</td><td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.util.concurrent.atomic.AtomicInteger AtomicInteger}</td>
 *      <td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.util.concurrent.atomic.AtomicLong AtomicLong}</td>
 *      <td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.util.concurrent.atomic.LongAccumulator LongAccumulator}</td>
 *      <td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.util.concurrent.atomic.LongAdder LongAdder}</td>
 *      <td>{@link java.lang.Long Long}</td></tr>
 * <tr><td>{@link java.math.BigInteger BigInteger}</td>
 *      <td>{@link java.math.BigInteger BigInteger}</td></tr>
 * <tr><td>{@link java.lang.Float Float}</td><td>{@link java.lang.Double Double}</td></tr>
 * <tr><td>{@link java.lang.Double Double}</td><td>{@link java.lang.Double Double}</td></tr>
 * <tr><td>{@link java.util.concurrent.atomic.DoubleAccumulator DoubleAccumulator}</td>
 *      <td>{@link java.lang.Double Double}</td></tr>
 * <tr><td>{@link java.util.concurrent.atomic.DoubleAdder DoubleAdder}</td>
 *      <td>{@link java.lang.Double Double}</td></tr>
 * </table>
 */
final class NumberCoercionMap { //NOPMD - ClassNamingConventions

    /**
     * Static map of input type to coerced type.
     */
    private static final Map<
            Class<? extends Number>,
            Class<? extends Number>> MAP = new HashMap<>();

    static {
        MAP.put(Byte.class, Long.class);
        MAP.put(Short.class, Long.class);
        MAP.put(Integer.class, Long.class);
        MAP.put(Long.class, Long.class);
        MAP.put(AtomicInteger.class, Long.class);
        MAP.put(AtomicLong.class, Long.class);
        MAP.put(LongAccumulator.class, Long.class);
        MAP.put(LongAdder.class, Long.class);
        MAP.put(BigInteger.class, BigInteger.class);
        MAP.put(Float.class, Double.class);
        MAP.put(Double.class, Double.class);
        MAP.put(DoubleAccumulator.class, Double.class);
        MAP.put(DoubleAdder.class, Double.class);
    }
    
    private NumberCoercionMap() {
    }

    /**
     * Get the coerced type for the given input type.
     * @param clazz a valid class
     * @return a valid class
     */
    public static Class<? extends Number> getType(Class<? extends Number> clazz) {
        Class<? extends Number> ret = MAP.get(clazz);
        return ret == null ? BigDecimal.class : ret;
    }
}

