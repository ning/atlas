package com.ning.atlas.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deployment
{
    private final List<Instance> instances = new ArrayList<Instance>();

    public List<Instance> getInstances()
    {
        return Collections.unmodifiableList(instances);
    }

    public void addInstance(Instance instance)
    {
        this.instances.add(instance);
    }

    public List<Instance> validate()
    {
        List<Instance> bads = new ArrayList<Instance>();
        for (Instance instance : instances) {
            List<String> problems = instance.validate();
            if (!problems.isEmpty()) {
                bads.add(instance);
            }
        }

        return bads;
    }


    public static Deployment build(final EnvironmentConfig env, final DeployTemplate manifest)
    {
        final DeployTemplate ptree =
            manifest.visit(manifest.shallowClone(), new Visitor<DeployTemplate>()
            {
                private final Stack<DeployTemplate> previousParents = new Stack<DeployTemplate>();
                private final Stack<String> names = new Stack<String>();

                public DeployTemplate enterSystem(SystemTemplate node, int cardinality, DeployTemplate parent)
                {
                    names.push(node.getName());
                    previousParents.push(parent);
                    return node.shallowClone();
                }

                public DeployTemplate leaveSystem(SystemTemplate node, int cardinality, DeployTemplate newChild)
                {

                    DeployTemplate previousParent = previousParents.pop();
                    if (previousParents.isEmpty()) {
                        // annoying hack to avoid double-representation of the root in the ptree
                        // need to fix if anyone can come up with better ptree build algo
                        return newChild;
                    }
                    for (int i = 0; i < env.cardinalityFor(flatten(names), cardinality); i++) {
                        previousParent.addChild(newChild.deepClone(), 1);
                    }

                    names.pop();
                    return previousParent;
                }

                public DeployTemplate visitService(ServerTemplate node, int cardinality, DeployTemplate parent)
                {
                    names.push(node.getName());

                    if (parent.getUnitType() == DeployTemplate.UnitType.Service) {
                        // parent is a service, so parent =~ node, so there is no system,
                        // this is a single-service manifest
                        return parent;
                    }
                    // at leaf,

                    for (int i = 0; i < env.cardinalityFor(flatten(names), cardinality); i++) {
                        parent.addChild(node.shallowClone(), 1);
                    }


                    names.pop();
                    return parent;
                }
            });


        return ptree.visit(new Deployment(), new BaseVisitor<Deployment>()
        {

            private final Stack<String> names = new Stack<String>();

            @Override
            public Deployment enterSystem(SystemTemplate node, int cardinality, Deployment baton)
            {
                names.push(node.getName());
                return super.enterSystem(node, cardinality, baton);
            }

            @Override
            public Deployment leaveSystem(SystemTemplate node, int cardinality, Deployment baton)
            {
                names.pop();
                return super.leaveSystem(node, cardinality, baton);
            }

            public Deployment visitService(ServerTemplate node, int cardinality, Deployment baton)
            {
                names.push(node.getName());
                final String full_name = flatten(names);
                baton.addInstance(new Instance(full_name, node, env.propsFor(full_name)));
                names.pop();
                return baton;
            }
        });
    }

    private static String flatten(Stack<String> stack)
    {
        StringBuilder b = new StringBuilder();
        for (String s : stack) {
            b.append("/").append(s);
        }
        return b.toString();
    }

}
