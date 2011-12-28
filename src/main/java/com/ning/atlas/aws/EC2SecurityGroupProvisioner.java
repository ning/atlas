package com.ning.atlas.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import static java.util.Arrays.asList;

public class EC2SecurityGroupProvisioner extends ConcurrentComponent
{
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {

        AWSCredentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                .otherwise(new IllegalStateException("No AWS Credentials available"))
                                .toAWSCredentials();

        AmazonEC2Client ec2 = new AmazonEC2Client(creds);
        AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(creds);

        final String group_name = uri.getFragment();


        DescribeSecurityGroupsRequest describe_request = new DescribeSecurityGroupsRequest();
        describe_request.setGroupNames(asList(group_name));
        SecurityGroup security_group;
        try {
            DescribeSecurityGroupsResult describe_result = ec2.describeSecurityGroups(describe_request);
            security_group = describe_result.getSecurityGroups().get(0);
        }
        catch (AmazonServiceException e) {
            if (e.getErrorCode().equals("InvalidGroup.NotFound")) {
                security_group = createNewGroup(ec2, group_name);
            }
            else {
                throw e;
            }
        }

        updateGroup(uri, ec2, iam, security_group);


        return "okay";
    }

    private void updateGroup(Uri<? extends Component> uri,
                             AmazonEC2Client ec2,
                             AmazonIdentityManagementClient iam,
                             SecurityGroup group)
    {
        String user_id = iam.getUser().getUser().getUserId();

        Set<String> raw_rules = Sets.newHashSet();
        for (Map.Entry<String, Collection<String>> entry : uri.getFullParams().entrySet()) {
            raw_rules.addAll(entry.getValue());
        }

        Set<IpRule> rules = Sets.newHashSet();
        for (String raw_thing : raw_rules) {
            rules.add(IpRule.parse(user_id, raw_thing));
        }

        Map<IpRule, IpPermission> map = Maps.newHashMap();
        Set<IpRule> existing_rules = Sets.newHashSet();
        for (IpPermission permission : group.getIpPermissions()) {
            IpRule r = IpRule.fromPermission(permission);
            existing_rules.add(r);
            map.put(r, permission);
        }

        Set<IpRule> to_remove = Sets.difference(existing_rules, rules);
        Set<IpRule> to_add = Sets.difference(rules, existing_rules);

        List<IpPermission> adds = Lists.newArrayListWithExpectedSize(to_add.size());
        for (IpRule rule : to_add) {
            adds.add(rule.toIpPermission());
        }
        if (!adds.isEmpty()) {
            ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(group.getGroupName(), adds));
        }

        List<IpPermission> removals = Lists.newArrayListWithExpectedSize(to_remove.size());
        for (IpRule rule : to_remove) {
            removals.add(map.get(rule));
        }

        // removals are kind of a pain, if you add somethign for debugging it gets wiped out.
//        if (!removals.isEmpty()) {
//            ec2.revokeSecurityGroupIngress(new RevokeSecurityGroupIngressRequest(group.getGroupName(), removals));
//        }

    }

    private SecurityGroup createNewGroup(AmazonEC2Client ec2, String group_name)
    {
        ec2.createSecurityGroup(new CreateSecurityGroupRequest(group_name, group_name));
        DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest();
        req.setGroupNames(asList(group_name));
        return ec2.describeSecurityGroups(req).getSecurityGroups().get(0);
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

}
