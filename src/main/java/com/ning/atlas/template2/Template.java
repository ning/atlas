package com.ning.atlas.template2;

import com.ning.atlas.template.Environment;
import com.ning.atlas.tree.MagicVisitor;
import com.ning.atlas.tree.Tree;
import com.ning.atlas.tree.Trees;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Template implements Tree<Template>
{
    private final String name;
    private final AtomicInteger cardinality = new AtomicInteger(0);

    public Template(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public int getCount() {
        return this.cardinality.get();
    }

    public void setCount(int count) {
        this.cardinality.set(count);
    }

    protected abstract Template duplicate();

    public Template normalizeFor(Environment environment) {
        return Trees.visit(this, this.duplicate(), new MagicVisitor<Template, Template>(new Object() {
            public Template enter(SystemTemplate sys, Template parent) {
                return sys.duplicate();
            }

            public Template exit(SystemTemplate sys, Template parent) {


            }

            public Template on(ServerTemplate serv, Template parent) {

            }

        }));
    }

}
