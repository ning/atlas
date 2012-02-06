package com.ning.atlas.components.aws;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

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

            UserIdGroupPair pair = permission.getUserIdGroupPairs().get(0);
            return new GroupRule(pair.getUserId(),
                                 permission.getIpProtocol(),
                                 permission.getFromPort().toString(),
                                 pair.getGroupName());

        }
        else if (permission.getIpRanges().size() == 1) {
            // cidr rule!
            String cidr = permission.getIpRanges().get(0);
            return new CIDRRule(permission.getIpProtocol(),
                                permission.getFromPort().toString(),
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
            IpPermission perm = new IpPermission();
            perm.setFromPort(port);
            perm.setToPort(port);
            perm.setIpProtocol(proto);
            perm.setIpRanges(asList(ipRange));
            return perm;
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
            IpPermission perm = new IpPermission();
            perm.setIpProtocol(proto);
            perm.setToPort(port);
            perm.setFromPort(port);
            UserIdGroupPair pair = new UserIdGroupPair();
            pair.setGroupName(group);
            pair.setUserId(userId);
            perm.setUserIdGroupPairs(asList(pair));
            return perm;
        }
    }
}
