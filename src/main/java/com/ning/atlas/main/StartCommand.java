package com.ning.atlas.main;

import com.ning.atlas.logging.Logger;

public class StartCommand implements Runnable
{
    private static final Logger logger = Logger.get(StartCommand.class);
    private final MainOptions mainOptions;

    public StartCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }

    @Override
    public void run()
    {
//        final ErrorCollector ec = new ErrorCollector();
//
//        JRubyTemplateParser p = new JRubyTemplateParser();
//        Template sys = p.parseSystem(new File(mainOptions.getSystemPath()));
//        Environment env = p.parseEnvironment(new File(mainOptions.getEnvironmentPath()));
//
//        BoundTemplate bound = sys.normalize(env);
//        logger.info("Generated deployment tree");
//        if (mainOptions.isFailFast() && ec.hasErrors()) {
//            ec.dumpErrorsTo(System.err);
//            return;
//        }
//
//        ExecutorService ex = Executors.newCachedThreadPool();
//        try {
//            ProvisionedElement pt = bound.provision(ec, ex).get();
//            logger.info("Provisioned system");
//            if (mainOptions.isFailFast() && ec.hasErrors()) {
//                ec.dumpErrorsTo(System.err);
//                return;
//            }
//
//            InitializedTemplate it = pt.initialize(ec, ex).get();
//            logger.info("Initialized system");
//            if (mainOptions.isFailFast() && ec.hasErrors()) {
//                ec.dumpErrorsTo(System.err);
//                return;
//            }
//
//            InstalledElement installed = it.install(ec, ex).get();
//            logger.info("Installed and Started system");
//            if (mainOptions.isFailFast() && ec.hasErrors()) {
//                ec.dumpErrorsTo(System.err);
//                return;
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
//            mapper.writeValue(System.out, installed);
//            System.out.println();
//            System.out.flush();
//        }
//        catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            ex.shutdownNow();
//        }
//
    }
}
