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

package org.jsonurl.j2se;

import java.math.MathContext;
import java.util.List;
import java.util.Map;
import org.jsonurl.BigMathProvider.BigIntegerOverflow;
import org.jsonurl.factory.AbstractParseTest;

/**
 * Abstract base class for Parser + JavaValueFactory unit tests.
 * 
 * @author jsonurl.org
 * @author David MacCormack
 * @since 2019-09-01
 */
abstract class AbstractJavaValueFactoryParseTest extends AbstractParseTest<
        Object,
        Object,
        List<Object>,
        List<Object>,
        Map<String,Object>,
        Map<String,Object>,
        Boolean,
        Number,
        Object,
        String> {

    /**
     * Create a new JavaValueFactoryParseTest.
     */
    public AbstractJavaValueFactoryParseTest(JavaValueFactory factory) {
        super(factory);
    }

    @Override
    protected boolean getBoolean(String key, Map<String,Object> value) {
        Object ret = value.get(key);

        if (ret instanceof Boolean) {
            return ((Boolean)ret).booleanValue();
        }

        throw new IllegalArgumentException("value not boolean: " + ret);
    }
    
    @Override
    protected boolean getNull(String key, Map<String,Object> value) {
        Object ret = value.get(key);
        return factory.isNull(ret);
    }

    @Override
    protected boolean getEmptyComposite(String key, Map<String,Object> value) {
        return factory.isEmptyComposite(value.get(key));
    }

    @Override
    protected boolean getEmptyComposite(int index, List<Object> value) {
        return factory.isEmptyComposite(value.get(index));
    }
    
    @SuppressWarnings("unchecked") //NOPMD
    @Override
    protected List<Object> getArray(String key, Map<String,Object> value) {
        return (List<Object>)value.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object> getArray(int index, List<Object> value) {
        return (List<Object>)value.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String,Object> getObject(int index, List<Object> value) {
        return (Map<String,Object>)value.get(index);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String,Object> getObject(String key, Map<String,Object> value) {
        return (Map<String,Object>)value.get(key);
    }

    @Override
    protected Number getNumber(int index, List<Object> value) {
        return (Number)value.get(index);
    }
    
    @Override
    protected Number getNumber(String key, Map<String,Object> value) {
        return (Number)value.get(key);
    }

    @Override
    protected String getString(String key, Map<String,Object> value) {
        return (String)value.get(key);
    }

    @Override
    protected String getString(int index, List<Object> value) {
        return (String)value.get(index);
    }

    @Override
    protected Number getNumberValue(Object value) {
        return value instanceof Number ? (Number)value : null;
    }

    @Override
    protected String getStringValue(Object value) {
        return value instanceof String ? (String)value : null;
    }

    @Override
    protected int getSize(List<Object> value) {
        return factory.isNull(value) ? 0 : value.size();
    }

    @Override
    protected boolean isEmptyObject(Map<String,Object> value) {
        return !factory.isNull(value) && value.isEmpty();
    }

    @Override
    protected JavaValueFactory newBigMathFactory(
                MathContext mctxt,
                String boundNeg,
                String boundPos,
                BigIntegerOverflow over) {

        return new JavaValueFactory.BigMathFactory(
                mctxt,
                boundNeg,
                boundPos,
                over);
    }
}
