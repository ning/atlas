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
import java.util.Map;

public abstract class BaseSpace implements Space
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, String> scratchSpace = Maps.newConcurrentMap();

    @Override
    public void store(Identity id, Object it)
    {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(it.getClass());
        for (PropertyDescriptor pd : pds) {
            if (!pd.getReadMethod().getDeclaringClass().equals(Object.class)) {
                String prop_name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, pd.getName());
                try {
                    Object value = pd.getReadMethod().invoke(it);
                    store(id, prop_name, String.valueOf(value));
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
            write(id.toExternalForm() + ":" + key, value);
        }
        catch (IOException e) {
            throw new IllegalStateException("unable to write", e);
        }
    }

    @Override
    public void scratch(String key, String value)
    {
        this.scratchSpace.put(key, value);
    }

    @Override
    public void scratch(Identity id, String key, String value)
    {
        this.scratchSpace.put(id.toExternalForm() + ":" + key, value);
    }

    @Override
    public void scratch(Identity id, Object it)
    {
        store(id, it);
    }

    @Override
    public Maybe<String> get(String key)
    {
        try {
            return Maybe.elideNull(this.scratchSpace.get(key)).otherwise(Maybe.elideNull(read(key)));
        }
        catch (IOException e) {
            throw new IllegalStateException("unable to read value", e);
        }
    }

    @Override
    public Maybe<String> get(Identity id, String key)
    {
        return get(id.toExternalForm() + ":" + key);
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
                String key = id.toExternalForm() + ":" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN,
                                                                                   pd.getName());
                String json_val = get(key).otherwise((String) null);
                final Object val;
                if (json_val != null) {
                    val = mapper.convertValue(String.valueOf(json_val), pd.getPropertyType());
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
    public String require(String s)
    {
        Maybe<String> m = get(s);
        if (m.isKnown()) {
            return m.getValue();
        }
        else {
            throw new IllegalStateException("required value for " + s + " has not been defined");
        }
    }

    @Override
    public Map<String, String> getAllFor(Identity id)
    {
        Map<String, String> rs = null;
        try {
            rs = readAll(id.toExternalForm());
        }
        catch (IOException e) {
            throw new IllegalStateException("unable to read from storage", e);
        }
        for (Map.Entry<String, String> entry : scratchSpace.entrySet()) {
            if (entry.getKey().startsWith(id.toString() + ":")) {
                rs.put(entry.getKey(), entry.getValue());
            }
        }
        return rs;
    }

    protected abstract String read(String key) throws IOException;
    protected abstract void write(String key, String value) throws IOException;
    protected abstract Map<String, String> readAll(String prefix) throws IOException;
}
