package com.ning.atlas.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class SystemManifest
{
    private final List<ServerSpec> instances = new ArrayList<ServerSpec>();

    public List<ServerSpec> getInstances()
    {
        return Collections.unmodifiableList(instances);
    }

    public void addInstance(ServerSpec instance)
    {
        this.instances.add(instance);
    }

    public static SystemManifest build(final EnvironmentConfig env, final DeployTemplate manifest)
    {

        final SystemManifest plan = new SystemManifest();


        final DeployTemplate physical_tree =
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

                public DeployTemplate visitServer(ServerTemplate node, int cardinality, DeployTemplate parent)
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


        physical_tree.visit(plan, new BaseVisitor<SystemManifest>()
        {
            private final Stack<String> names = new Stack<String>();

            @Override
            public SystemManifest enterSystem(SystemTemplate node, int cardinality, SystemManifest baton)
            {
                names.push(node.getName());
                return super.enterSystem(node, cardinality, baton);
            }

            @Override
            public SystemManifest leaveSystem(SystemTemplate node, int cardinality, SystemManifest baton)
            {
                names.pop();
                return super.leaveSystem(node, cardinality, baton);
            }

            public SystemManifest visitServer(ServerTemplate node, int cardinality, SystemManifest baton)
            {
                names.push(node.getName());
                final String full_name = flatten(names);
                baton.addInstance(new ServerSpec(full_name, node));
                names.pop();
                return baton;
            }
        });


        return plan;
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
