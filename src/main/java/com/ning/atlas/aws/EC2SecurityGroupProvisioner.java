package com.ning.atlas.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.space.Missing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class EC2SecurityGroupProvisioner extends ConcurrentComponent
{
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        final String group_name = uri.getFragment();

        AWSCredentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                .otherwise(new IllegalStateException("No AWS Credentials available"))
                                .toAWSCredentials();

        AmazonEC2Client ec2 = new AmazonEC2Client(creds);
        AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(creds);
        String user_id = iam.getUser().getUser().getUserId();

        Set<String> raw_rules = Sets.newHashSet();
        for (Map.Entry<String, Collection<String>> entry : uri.getFullParams().entrySet()) {
            raw_rules.addAll(entry.getValue());
        }

        List<Rule> rules = Lists.newArrayList();
        for (String raw_thing : raw_rules) {
            rules.add(Rule.parse(user_id, raw_thing));
        }

        DescribeSecurityGroupsRequest dsgreq = new DescribeSecurityGroupsRequest();
        dsgreq.setGroupNames(asList(group_name));
        DescribeSecurityGroupsResult dsgres = ec2.describeSecurityGroups(dsgreq);

        SecurityGroup group = dsgres.getSecurityGroups().get(0);


        // find permissions to remove

        for (IpPermission permission : group.getIpPermissions()) {
            // ensure all the allowances are there

        }


        return "okay";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("no!");
    }

    abstract static class Rule
    {
        private static final Pattern CIDR_RULE  = Pattern.compile("\\s*(\\w+)\\s+(\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)\\s*");
        private static final Pattern GROUP_RULE = Pattern.compile("\\s*(\\w+)\\s+(\\d+)\\s+(\\w+)\\s*");

        static Rule parse(String userId, String descriptor)
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

        private static class CIDRRule extends Rule
        {

            private final String ipRange;
            private final int port;
            private final String proto;

            CIDRRule(String proto, String port, String ipRange) {
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

        private static class GroupRule extends Rule
        {
            private final String userId;
            private final int port;
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
}
