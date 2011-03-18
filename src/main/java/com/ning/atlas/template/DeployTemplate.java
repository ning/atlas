package com.ning.atlas.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DeployTemplate
{
    private List<String> requiredProperties = new ArrayList();
    private final List<SizedChild> children = new ArrayList<SizedChild>();
    private final String name;

    public DeployTemplate(String name)
    {
        this.name = name;
    }


    public abstract UnitType getUnitType();

    public DeployTemplate addChild(DeployTemplate unit, int cardinality)
    {
        children.add(new SizedChild(unit, cardinality));
        return unit;
    }

    public final <T> T visit(T baton, Visitor<T> visitor) {
        return visit(baton, 1, visitor);
    }

    private <T> T visit(T baton, int cardinality, Visitor<T> visitor)
    {
        switch (getUnitType()) {
            case Service:
                return visitor.visitServer((ServerTemplate) this, cardinality, baton);
            case System:
                T next = visitor.enterSystem((SystemTemplate) this, cardinality, baton);
                for (SizedChild child : children) {
                    next = child.getTemplate().visit(next, child.getCardinality(), visitor);
                }
                return visitor.leaveSystem((SystemTemplate) this, cardinality, next);
            default:
                throw new UnsupportedOperationException("unknown service type!");
        }
    }

    public String getName()
    {
        return name;
    }

    public List<DeployTemplate.SizedChild> getChildren()
    {
        return Collections.unmodifiableList(this.children);
    }


    public void addRequiredProperty(String key)
    {
        this.requiredProperties.add(key);
    }

    public void addRequiredProperties(String... keys)
    {
        for (String key : keys) {
            addRequiredProperty(key);
        }
    }

    public List<String> getRequiredProperties()
    {
        return Collections.unmodifiableList(requiredProperties);
    }

    public abstract DeployTemplate shallowClone();

    public abstract DeployTemplate deepClone();


    static enum UnitType
    {
        System, Service
    }

    public class SizedChild
    {
        private final DeployTemplate template;
        private final int cardinality;

        public SizedChild(DeployTemplate template, int cardinality)
        {
            this.cardinality = cardinality;
            this.template = template;
        }

        public int getCardinality()
        {
            return cardinality;
        }

        public DeployTemplate getTemplate()
        {
            return template;
        }

        @Override
        public String toString()
        {
            return "SizedChild{" +
                   "template=" + template +
                   ", cardinality=" + cardinality +
                   '}';
        }
    }

    @Override
    public String toString()
    {
        return "DeployTemplate{" +
               "requiredProperties=" + requiredProperties +
               ", children=" + children +
               ", name='" + name + '\'' +
               '}';
    }
}

