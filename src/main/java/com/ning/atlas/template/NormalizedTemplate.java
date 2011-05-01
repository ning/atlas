package com.ning.atlas.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class NormalizedTemplate
{
    private final List<ServerSpec> instances = new ArrayList<ServerSpec>();
    private final DeployTemplate tree;

    public NormalizedTemplate(DeployTemplate tree)
    {
        this.tree = tree;

        tree.visit(this, new BaseVisitor<NormalizedTemplate>()
        {
            private final Stack<String> names = new Stack<String>();

            @Override
            public NormalizedTemplate enterSystem(ConfigurableSystemTemplate node, int cardinality, NormalizedTemplate baton)
            {
                names.push(node.getName());
                return super.enterSystem(node, cardinality, baton);
            }

            @Override
            public NormalizedTemplate leaveSystem(ConfigurableSystemTemplate node, int cardinality, NormalizedTemplate baton)
            {
                names.pop();
                return super.leaveSystem(node, cardinality, baton);
            }

            public NormalizedTemplate visitServer(ConfigurableServerTemplate node, int cardinality, NormalizedTemplate baton)
            {
                names.push(node.getName());
                final String full_name = flatten(names);
                instances.add(new ServerSpec(full_name, node));
                names.pop();
                return baton;
            }
        });
    }

    public DeployTemplate getTree()
    {
        return tree;
    }

    public List<ServerSpec> getInstances()
    {
        return Collections.unmodifiableList(instances);
    }

    public static NormalizedTemplate build(final EnvironmentConfig env, final DeployTemplate root)
    {
        final DeployTemplate physical_tree = root.visit(root.shallowClone(), new Visitor<DeployTemplate>()
        {
            private final Stack<DeployTemplate> previousParents = new Stack<DeployTemplate>();
            private final Stack<String> names = new Stack<String>();

            public DeployTemplate enterSystem(ConfigurableSystemTemplate node, int cardinality, DeployTemplate parent)
            {
                names.push(node.getName());
                previousParents.push(parent);
                return node.shallowClone();
            }

            public DeployTemplate leaveSystem(ConfigurableSystemTemplate node, int cardinality, DeployTemplate newChild)
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

            public DeployTemplate visitServer(ConfigurableServerTemplate node, int cardinality, DeployTemplate parent)
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


        return new NormalizedTemplate(physical_tree);
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
