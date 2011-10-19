package com.ning.atlas.space;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class InMemorySpace implements Space
{

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, String> values = Maps.newConcurrentMap();

    public static Space newInstance()
    {
        return new InMemorySpace();
    }

    @Override
    public void store(Identity id, Object it)
    {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(it.getClass());
        for (PropertyDescriptor pd : pds) {
            if (!pd.getReadMethod().getDeclaringClass().equals(Object.class)) {
                String prop_name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, pd.getName());
                try {
                    Object value = pd.getReadMethod().invoke(it);
                    String json = mapper.writeValueAsString(value);
                    values.put(id.toExternalForm() + ":" + prop_name, json);
                }
                catch (Exception e) {
                    throw new IllegalStateException("unable to read property '" + pd.getName() + "' from " + it, e);
                }
            }
        }
    }

    @Override
    public void scratch(String key, String value)
    {
        this.values.put(key, value);
    }

    @Override
    public Maybe<String> get(String key)
    {
        return Maybe.elideNull(this.values.get(key));
    }

    @Override
    public <T> Maybe<T> get(Identity id, Class<T> type, Missing behavior)
    {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(type);
        T bean;
        try {
            bean = type.newInstance();
        }
        catch (Exception e) {
            throw new IllegalStateException("unable to instantiate an instance of " + type.getName(), e);
        }

        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                String key = id.toExternalForm() + ":" + CaseFormat.LOWER_CAMEL
                    .to(CaseFormat.LOWER_HYPHEN, pd.getName());
                String json_val = this.values.get(key);
                final Object val;
                if (json_val != null) {
                    try {
                        val = mapper.readValue(json_val, pd.getPropertyType());
                    }
                    catch (IOException e) {
                        throw new IllegalStateException("io exception reading from a string!", e);
                    }
                }
                else {
                    switch (behavior) {
                        case NullProperty:
                            val = null;
                            break;
                        case RequireAll:
                            return Maybe.unknown();
                        default:
                            throw new UnsupportedOperationException("Not Yet Implemented!");
                    }
                }

                try {
                    pd.getWriteMethod().invoke(bean, val);
                }
                catch (Exception e) {
                    throw new IllegalStateException("unable to write property " + pd.getName() + " to " + bean, e);
                }
            }
        }

        return Maybe.definitely(bean);
    }
}
