package com.ning.atlas.space;

import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public abstract class BaseSpace implements Space
{
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void store(Identity id, Object it)
    {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(it.getClass());
        for (PropertyDescriptor pd : pds) {
            if (!pd.getReadMethod().getDeclaringClass().equals(Object.class)) {
                String prop_name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, pd.getName());
                try {
                    Object value = pd.getReadMethod().invoke(it);
                    Class pt = pd.getPropertyType();
                    String json;
                    if (String.class.equals(pt) || Primitives.allPrimitiveTypes().contains(pt)) {
                        json = mapper.convertValue(value, String.class);
                    }
                    else {
                        json = mapper.writeValueAsString(value);
                    }

                    write(id, prop_name, json);
                }
                catch (Exception e) {
                    throw new IllegalStateException("unable to read property '" + pd.getName() + "' from " + it, e);
                }
            }
        }
    }

    @Override
    public void store(Identity id, String key, String value)
    {
        try {
            write(id, key, value);
        }
        catch (IOException e) {
            throw new IllegalStateException("unable to write", e);
        }
    }

    @Override
    public Maybe<String> get(Identity id, String key)
    {
        try {
            // return Maybe.elideNull( mapper.readValue(read(id, key), String.class));
            return Maybe.elideNull(read(id, key));
        }
        catch (IOException e) {
            throw new IllegalStateException("unable to read from persistent store", e);
        }
    }

    @Override
    public Maybe<String> get(String idExternalForm, String key)
    {
        return get(Identity.valueOf(idExternalForm), key);
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
                String prop_name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, pd.getName());
                String json_val = get(id, prop_name).otherwise((String) null);
                Class pt = pd.getPropertyType();
                final Object val;
                if (json_val != null) {
                    if (String.class.equals(pt) || Primitives.allPrimitiveTypes().contains(pt)) {
                        val = mapper.convertValue(json_val, pt);
                    }
                    else {
                        try {
                            val = mapper.readValue(json_val, pd.getPropertyType());
                        }
                        catch (IOException e) {
                            throw new IllegalStateException(e.getMessage());
                        }
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

    @Override
    public Map<SpaceKey, String> getAllFor(Identity id)
    {
        try {
            return readAll(id);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T> Maybe<T> get(Identity id, Class<T> type)
    {
        return get(id, type, Missing.RequireAll);
    }

    @Override
    public Set<String> query(String expression)
    {
        return new QueryParser().parse(expression).query(this);
    }

    protected abstract String read(Identity id, String key) throws IOException;

    protected abstract void write(Identity id, String key, String value) throws IOException;

    protected abstract Map<SpaceKey, String> readAll(Identity prefix) throws IOException;
}
