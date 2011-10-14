package com.ning.atlas.main;

public class InitializeCommand implements Runnable
{
    private final MainOptions mainOptions;

    public InitializeCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }

    @Override
    public void run()
    {
//        final ErrorCollector ec = new ErrorCollector();
//        JRubyTemplateParser p = new JRubyTemplateParser();
//        Template sys = p.parseSystem(new File(mainOptions.getSystemPath()));
//        Environment env = p.parseEnvironment(new File(mainOptions.getEnvironmentPath()));
//
//        BoundTemplate bound = sys.normalize(env);
//
//        ExecutorService ex = Executors.newCachedThreadPool();
//        try {
//            ProvisionedElement pt = bound.provision(ec, ex).get();
//            if (pt.getType().equals("__ROOT__") && pt.getChildren().size() == 1) {
//                lop off the fake root
//                pt = pt.getChildren().get(0);
//            }
//
//            InitializedTemplate it = pt.initialize(ec,ex).get();
//
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
//            mapper.writeValue(System.out, it);
//            System.out.flush();
//        }
//        catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            ex.shutdownNow();
//        }

    }
}
