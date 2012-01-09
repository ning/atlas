package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ServerTemplate extends Template
{
    private final List<Uri<Installer>> installations = new ArrayList<Uri<Installer>>();
    private final Uri<Base> base;

    public ServerTemplate(String name,
                          Uri<Base> base,
                          List<?> cardinality,
                          List<Uri<Installer>> installers,
                          Map<String, Object> my)
    {
        super(name, new My(my), cardinality);
        this.installations.addAll(installers);
        this.base = base;
    }

    @Override
    protected List<Element> _normalize(Identity parent, Environment env)
    {
        final List<Element> rs = new ArrayList<Element>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {

            Identity id = parent.createChild(getType(), node_name);
            Base base = env.findBase(getBaseUri().getScheme())
                           .otherwise(new IllegalStateException(format("no base '%s' available for %s",
                                                                       this.base.getScheme(),
                                                                       id)));

            List<Uri<Installer>> inits = expand(base.getInitUris(), env,
                                                ImmutableMap.<String, Object>of("base", getBaseUri(),
                                                                                "server", getMy().asMap()));


            List<Uri<Installer>> installs = expand(getInstallUris(), env,
                                                   ImmutableMap.<String, Object>of("base", getBaseUri(),
                                                                                   "server", getMy().asMap()));

            Uri<Provisioner> prov_uri = new UriTemplate<Provisioner>(base.getProvisionUri())
                .apply(ImmutableMap.<String, Object>of("base", getBaseUri(),
                                                       "server", getMy().asMap()));

            rs.add(new Host(id, prov_uri, inits, installs, getMy()));
        }
        return rs;
    }


    private List<Uri<Installer>> expand(List<Uri<Installer>> source, Environment env, Map<String, Object> args)
    {
        List<Uri<Installer>> inits = Lists.newArrayList();
        for (Uri<Installer> uri : source) {
            if (env.isVirtual(uri)) {
                List<Uri<Installer>> expanded = env.expand(uri);
                for (Uri<Installer> e_uri : expanded) {
                    if (e_uri.isTemplate()) {
                        Map<String, Object> my_args = Maps.newHashMap(args);
                        my_args.put("virtual", uri);
                        inits.add(new UriTemplate<Installer>(e_uri).apply(my_args));
                    }
                    else {
                        inits.add(e_uri);
                    }
                }
            }
            else {
                if (uri.isTemplate()) {
                    inits.add(new UriTemplate<Installer>(uri).apply(args));
                }
                else {
                    inits.add(uri);
                }
            }
        }
        return inits;
    }

    public Collection<? extends Element> getChildren()
    {
        return Collections.emptyList();
    }

    public List<Uri<Installer>> getInstallUris()
    {
        return installations;
    }

    public Uri<Base> getBaseUri()
    {
        return base;
    }

    private static class DeTemplaterizer<T> implements Function<Uri<T>, Uri<T>>
    {
        private final Uri<Base> base;
        private final My        my;

        public DeTemplaterizer(Uri<Base> base, My my)
        {
            this.base = base;
            this.my = my;
        }

        @Override
        public Uri<T> apply(Uri<T> input)
        {
            if (input.isTemplate()) {
                return new UriTemplate<T>(input).apply(ImmutableMap.of("base", this.base,
                                                                       "server", this.my.asMap()));
            }
            else {
                return input;
            }
        }
    }
}
