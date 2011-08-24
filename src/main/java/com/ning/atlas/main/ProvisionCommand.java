package com.ning.atlas.main;

import com.ning.atlas.BoundTemplate;
import com.ning.atlas.Environment;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.ProvisionedElement;
import com.ning.atlas.Template;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvisionCommand implements Runnable
{
    private final MainOptions mainOptions;

    public ProvisionCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }

    public void run()
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template sys = p.parseSystem(new File(mainOptions.getSystemPath()));
        Environment env = p.parseEnvironment(new File(mainOptions.getEnvironmentPath()));

        BoundTemplate bound = sys.normalize(env);

        ExecutorService ex = Executors.newCachedThreadPool();
        try {
            ProvisionedElement t = bound.provision(ex).get();
            if (t.getType().equals("__ROOT__") && t.getChildren().size() == 1) {
                // lop off the fake root
                t = t.getChildren().get(0);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
            mapper.writeValue(System.out, t);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            ex.shutdownNow();
        }
    }
}
