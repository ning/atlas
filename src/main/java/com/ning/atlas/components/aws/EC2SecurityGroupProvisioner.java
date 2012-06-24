package com.ning.atlas.components.aws;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.jclouds.aws.ec2.AWSEC2Client;
import org.jclouds.ec2.domain.IpPermission;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.iam.IAMApi;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.protocols.AWS.Credentials;

public class EC2SecurityGroupProvisioner extends ConcurrentComponent
{
    
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        
        AWSEC2Client ec2Api = AWS.ec2Api(creds);
        IAMApi iam = AWS.iamApi(creds);

        final String group_name = uri.getFragment();

        Set<SecurityGroup> groups = ec2Api.getSecurityGroupServices().describeSecurityGroupsInRegion(null, group_name);

        SecurityGroup security_group;
        if (groups.size() > 0) {
            security_group = Iterables.get(groups, 0);
        } else {
            security_group = createNewGroup(ec2Api, group_name);
        }

        updateGroup(uri, ec2Api, iam, security_group);


        return "okay";
    }

    private void updateGroup(Uri<? extends Component> uri,
                             AWSEC2Client ec2,
                             IAMApi iam,
                             SecurityGroup group)
    {
        String user_id = iam.getCurrentUser().getId();

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
            ec2.getSecurityGroupServices().authorizeSecurityGroupIngressInRegion(null, group.getName(), adds);
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

    private SecurityGroup createNewGroup(AWSEC2Client ec2Api, String group_name)
    {
        ec2Api.getSecurityGroupServices().createSecurityGroupInRegion(null, group_name, group_name);
        Set<SecurityGroup> groups = ec2Api.getSecurityGroupServices().describeSecurityGroupsInRegion(null, group_name);
        // eventual consistency alert!! could be zero
        if (groups.size() == 0)
            groups = ec2Api.getSecurityGroupServices().describeSecurityGroupsInRegion(null, group_name);
        return groups.iterator().next();
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
