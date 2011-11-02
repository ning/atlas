package com.ning.atlas;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.ning.atlas.spi.Maybe.definitely;

public class InformationGatheringListener extends BaseLifecycleListener
{
    public static final Identity ID = Identity.root().createChild(InformationGatheringListener.class.getName(),
                                                                  "answers");

    private final ExecutorService es = Executors.newCachedThreadPool();
    private final ImmutableMap<String, String> questions;
    private final Map<String, Object> rc = new MapMaker().concurrencyLevel(1).makeMap();

    public InformationGatheringListener(Map<String, String> questions) throws IOException
    {
        this.questions = ImmutableMap.copyOf(questions);
        final Maybe<File> rc;
        if (new File(".atlasrc").exists()) {
            rc = definitely(new File(".atlasrc"));
        }
        else if (new File(System.getProperty("user.dir"), ".atlasrc").exists()) {
            rc = definitely(new File(System.getProperty("user.home"), ".atlasrc"));
        }
        else {
            rc = Maybe.unknown();
        }
        if (rc.isKnown()) {
            File rcfile = rc.getValue();
            FileInputStream fin = new FileInputStream(rcfile);
            Map<String, Object> m = (Map<String, Object>) new Yaml().load(new FileInputStream(rcfile));
            fin.close();
            this.rc.putAll(m);
        }
    }

    @Override
    public Future<?> startDeployment(Deployment d)
    {
        Space s = d.getSpace();
        for (Map.Entry<String, String> entry : questions.entrySet()) {
            Maybe<String> answer = s.get(ID, entry.getKey());
            if (answer.isKnown()) {
                s.scratch(entry.getKey(), answer.getValue());
            }
            else {
                String new_answer = ask(entry.getKey(), entry.getValue());
                s.store(ID, entry.getKey(), new_answer);
                s.scratch(entry.getKey(), new_answer);
            }
        }

        return super.startDeployment(d);
    }

    private String ask(String name, String question)
    {
        Iterator<String> names = Splitter.on('.').split(name).iterator();
        Map<String, Object> tmp = rc;
        while (names.hasNext()) {
            String nm = names.next();
            Object nxt = tmp.get(nm);
            if (nxt != null) {
                if (!names.hasNext()) {
                    return String.valueOf(nxt);
                }
                else if (nxt instanceof Map) {
                    tmp = (Map<String, Object>) nxt;
                }
            }
        }
        if (System.console() != null) {
            System.console().printf(question + " ");
            return System.console().readLine();
        }
        else {
            throw new IllegalStateException("required config value " + name + " not in .atlasrc and no console " +
                                            "is attached to allow us to ask for it");
        }
    }


    @Override
    public Future<?> finishDeployment(Deployment d)
    {
        es.shutdown();
        return super.finishDeployment(d);
    }
}
