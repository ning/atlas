package com.ning.atlas.components.aws;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jclouds.ec2.domain.IpPermission;
import org.jclouds.ec2.domain.IpProtocol;
import org.jclouds.ec2.util.IpPermissions;

abstract class IpRule
{
    private static final Pattern CIDR_RULE  = Pattern.compile("\\s*(\\w+)\\s+(\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)\\s*");
    private static final Pattern GROUP_RULE = Pattern.compile("\\s*(\\w+)\\s+(\\d+)\\s+(\\w+)\\s*");

    static IpRule parse(String userId, String descriptor)
    {
        Matcher cidr = CIDR_RULE.matcher(descriptor);
        Matcher group = GROUP_RULE.matcher(descriptor);
        if (cidr.matches()) {
            return new CIDRRule(cidr.group(1), cidr.group(2), cidr.group(3));
        }
        else if (group.matches()) {
            return new GroupRule(userId, group.group(1), group.group(2), group.group(3));
        }
        throw new IllegalStateException(descriptor + " does not appear to be a CIDR or group rule");
    }

    public abstract IpPermission toIpPermission();

    public static IpRule fromPermission(final IpPermission permission)
    {
        if (permission.getUserIdGroupPairs().size() == 1) {
            // group rule!

            Entry<String, String> pair = permission.getUserIdGroupPairs().entries().iterator().next();
            return new GroupRule(pair.getKey(),
                                 permission.getIpProtocol().toString(),
                                 permission.getFromPort() + "",
                                 pair.getValue());

        }
        else if (permission.getIpRanges().size() == 1) {
            // cidr rule!
            String cidr = permission.getIpRanges().iterator().next();
            return new CIDRRule(permission.getIpProtocol().toString(),
                                permission.getFromPort() + "",
                                cidr);
        }
        else {
            // an unparseable rule, so we return the perm by itself so we can whack it
            return new IpRule() {

                @Override
                public IpPermission toIpPermission()
                {
                    return permission;
                }
            };
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    private static class CIDRRule extends IpRule
    {

        private final String ipRange;
        private final int    port;
        private final String proto;

        CIDRRule(String proto, String port, String ipRange)
        {
            this.ipRange = ipRange;
            this.port = Integer.parseInt(port);
            this.proto = proto;
        }

        @Override
        public IpPermission toIpPermission()
        {
            return IpPermissions.permit(IpProtocol.fromValue(proto)).fromPort(port).to(port).originatingFromCidrBlock(ipRange);
        }
    }

    private static class GroupRule extends IpRule
    {
        private final String userId;
        private final int    port;
        private final String proto;
        private final String group;

        public GroupRule(String userId, String proto, String port, String group)
        {
            this.userId = userId;
            this.proto = proto;
            this.group = group;
            this.port = Integer.parseInt(port);
        }

        @Override
        public IpPermission toIpPermission()
        {
            return IpPermissions.permit(IpProtocol.fromValue(proto)).fromPort(port).to(port).originatingFromUserAndSecurityGroup(userId, group);
        }
    }
}
