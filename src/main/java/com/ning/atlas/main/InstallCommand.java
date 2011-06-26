package com.ning.atlas.main;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.BoundTemplate;
import com.ning.atlas.Environment;
import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.InstalledTemplate;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.Template;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InstallCommand implements Runnable
{
    private final MainOptions mainOptions;

    public InstallCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }

    @Override
    public void run()
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template sys = p.parseSystem(new File(mainOptions.getSystemPath()));
        Environment env = p.parseEnvironment(new File(mainOptions.getEnvironmentPath()));

        BoundTemplate bound = sys.normalize(env);

        ExecutorService ex = Executors.newCachedThreadPool();
        try {
            ProvisionedTemplate pt = bound.provision(ex).get();
            if (pt.getType().equals("__ROOT__") && pt.getChildren().size() == 1) {
                // lop off the fake root
                pt = pt.getChildren().get(0);
            }


            InitializedTemplate it = pt.initialize(ex, pt).get();

            InstalledTemplate installed = it.install(ex).get();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
            mapper.writeValue(System.out, installed);
            System.out.flush();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            ex.shutdownNow();
        }

    }
}
