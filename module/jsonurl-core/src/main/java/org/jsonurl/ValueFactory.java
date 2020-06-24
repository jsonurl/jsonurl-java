package org.jsonurl;

import java.util.EnumSet;
import java.util.Set;

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
 * A ValueFactory allows {@link org.jsonurl.Parser Parser}
 * to be implementation independent. A ValueFactory provides
 * implementation-defined values for JSON&#x2192;URL
 * objects, arrays, and literals.
 * 
 * @param <V> value type (any JSON value)
 * @param <C> composite type (array or object)
 * @param <ABT> array builder type
 * @param <A> array type
 * @param <JBT> object builder type
 * @param <J> object type
 * @param <B> boolean type
 * @param <M> number type
 * @param <N> null type
 * @param <S> string type
 *
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
public interface ValueFactory<
        V,
        C extends V,
        ABT,
        A extends C,
        JBT,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> {
    
    /**
     * A ValueFactory with transparent array and object builders.
     *
     * <p>A TransparentBuilder is a ValueFactory whose JSON array and JSON
     * array builder are the same instance of the same class, and whose JSON
     * object and JSON object builder are the same instance of the same class. 
     *
     * @param V value type (any JSON value)
     * @param C composite type (array or object)
     * @param A array type
     * @param J object type
     * @param B boolean type
     * @param M number type
     * @param N null type
     * @param S string type
     *
     * @author jsonurl.org
     * @author David MacCormack
     * @since 2019-09-01
     */
    public interface TransparentBuilder<
        V,
        C extends V,
        A extends C,
        J extends C,
        B extends V,
        M extends V,
        N extends V,
        S extends V> extends ValueFactory<V,C,A,A,J,J,B,M,N,S> {
        
        @Override
        default A newArray(A builder) {
            return builder;
        }
        
        @Override
        default J newObject(J builder) {
            return builder;
        }
    }

    /**
     * get the empty composite value.
     *
     * <p>This is usually a singleton. It's a value which represents
     * the empty composite (i.e. array/object) as defined in the
     * JSON&#x2192;URL spec.
     * @return a valid composite instance
     */
    public C getEmptyComposite();

    /**
     * construct a new JSON array from the given builder.
     *
     * <p>Some implementations implement the builder pattern and separate
     * arrays from things that build arrays. This method accepts an array
     * builder and returns an array. For implementations that do not separate
     * arrays from builders this may be the identity function.
     * @param builder an array builder
     * @return a valid JSON array
     */
    public A newArray(ABT builder);
    
    /**
     * construct a new JSON object from the given builder.
     *
     * <p>Some implementations implement the builder pattern and separate
     * objects from things that build objects. This method accepts an object
     * builder and returns an object. For implementations that do not separate
     * objects from builders this may be the identity function.
     * @param builder an object builder
     * @return a valid JSON object
     */
    public J newObject(JBT builder);
    
    /**
     * get a new JSON array builder.
     *
     * <p>Some implementations implement the builder pattern and separate
     * objects from things that build objects. This method must return
     * a new JSON array builder.
     * @return a valid JSON array builder
     */
    public ABT newArrayBuilder();

    /**
     * get a new JSON object builder.
     *
     * <p>Some implementations implement the builder pattern and separate
     * objects from things that build objects. This method must return
     * a new JSON object builder.
     * @return a valid JSON object builder
     */
    public JBT newObjectBuilder();

    /**
     * add a value to an array.
     */
    public void add(ABT dest, V obj);

    /**
     * add a key/value pair to an object.
     */
    public void put(JBT dest, String key, V value);

    /**
     * get the null value.
     *
     * <p>This is usually a singleton. It's a Java Object which represents
     * the JSON value null.
     * @return a valid null instance
     */
    public N getNull();

    /**
     * get the true value.
     *
     * <p>This is usually a singleton. It's a Java Object which represents
     * the JSON boolean value true.
     * @return the JSON value true
     */
    public B getTrue();

    /**
     * get the true value.
     *
     * <p>This is usually a singleton. It's a Java Object which represents
     * the JSON boolean value false.
     * @return the JSON value false
     */
    public B getFalse();
    
    /**
     * get a JSON string value.
     *
     * @return a valid string whose text is the given character sequence
     */
    public S getString(CharSequence s, int start, int stop);

    /**
     * get a JSON string value.
     *
     * @return a valid string whose text is the given Java String
     */
    default S getString(String s) {
        return getString(s, 0, s.length());
    }

    /**
     * Get a number value for the given parsed text.
     *
     * @param text the parsed text of a JSON&#x2192;URL number literal 
     * @return a number object for the given text
     */
    public M getNumber(NumberText text);

    /**
     * Get a boolean value.
     *
     * @param b a boolean value
     * @return if <i>b</i> is true then return true; otherwise, return false.
     */
    default B getBoolean(boolean b) {
        return b ? getTrue() : getFalse();
    }

    /**
     * Test if the given {@code value} has the given {@code type}.
     * This simply calls {@link #isValid(Set, Object)
     * isValid(EnumSet.of(type), value)}. 
     * @param type allowed type
     * @param value value to test
     */
    default boolean isValid(ValueType type, V value) {
        return isValid(EnumSet.of(type), value);
    }

    /**
     * Test if the type of the given {@code value} is present in {@code types}. 
     * @param types set of allowed types
     * @param value value to test
     * @return true if the type of value is in the given types set.
     */
    public boolean isValid(Set<ValueType> types, V value);

    /**
     * Test if the given object is the empty value.
     *
     * @param obj a Java Object
     * @return true if obj is the empty composite; false otherwise.
     */
    default boolean isEmpty(Object obj) {
        return obj == getEmptyComposite();
    }

    /**
     * Test if the given object is the null value.
     *
     * @param obj a Java Object
     * @return true if obj is the null value; false otherwise.
     */
    default boolean isNull(Object obj) {
        return obj == getNull();
    }

    /**
     * get a true, false, or null value for the given text.
     *
     * @param s the text
     * @param start the start index
     * @param stop the stop index
     * @return a valid value or null
     */
    @SuppressWarnings("PMD")
    default V getTrueFalseNull(
            CharSequence s,
            int start,
            int stop) {

        keyword: switch (stop - start) {
        case 4:
            switch (s.charAt(start)) {
            case 't':
                if (s.charAt(start + 1) != 'r'
                        || s.charAt(start + 2) != 'u'
                        || s.charAt(start + 3) != 'e') {
                    break keyword;
                }
                return getTrue();

            case 'n':
                if (s.charAt(start + 1) != 'u'
                        || s.charAt(start + 2) != 'l'
                        || s.charAt(start + 3) != 'l') {
                    break keyword;
                }
                return getNull();

            default:
                break keyword;
            }
            // this can never happen but checkstyle gets angry, so:
            // fall through

        case 5:
            if (s.charAt(start) != 'f'
                    || s.charAt(start + 1) != 'a'
                    || s.charAt(start + 2) != 'l'
                    || s.charAt(start + 3) != 's'
                    || s.charAt(start + 4) != 'e') {
                break keyword;
            }
            return getFalse();

        default:
            break;
        }

        return null;
    }

}
