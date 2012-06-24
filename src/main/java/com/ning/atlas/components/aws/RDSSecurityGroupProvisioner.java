package com.ning.atlas.components.aws;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.jclouds.iam.IAMApi;
import org.jclouds.rds.RDSApi;
import org.jclouds.rds.domain.EC2SecurityGroup;
import org.jclouds.rds.domain.SecurityGroup;

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

public class RDSSecurityGroupProvisioner extends ConcurrentComponent
{
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        final String group_name = uri.getFragment();
        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        
        final RDSApi rdsApi = AWS.rdsApi(creds);
        IAMApi iam = AWS.iamApi(creds);
        
        if (rdsApi.getSecurityGroupApi().get(group_name) == null)
            createGroup(rdsApi, group_name);
        
        updateGroup(uri, rdsApi, iam);

        return "okay";
    }

    private void updateGroup(Uri<? extends Component> uri, RDSApi rdsApi, IAMApi iam)
    {
        String user_id = iam.getCurrentUser().getId();
        String name = uri.getFragment();
        SecurityGroup group = rdsApi.getSecurityGroupApi().get(name);

        Set<GroupUserPair> groups_wanted = GroupUserPair.extractAll(user_id, uri.getParams());
        Set<GroupUserPair> existing  = GroupUserPair.convertAll(group.getEC2SecurityGroups());

        Set<GroupUserPair> to_add = Sets.difference(groups_wanted, existing);
        Set<GroupUserPair> to_remove = Sets.difference(existing, groups_wanted);

        if (!to_add.isEmpty()) {
            for (GroupUserPair pair : to_add) {
                rdsApi.getSecurityGroupApi().authorizeIngressToEC2SecurityGroupOfOwner(name, pair.groupName, pair.userId);
            }
        }

        // removals are a pain, if you ad soemthing for debugging it gets wiped out.
//        if (!to_remove.isEmpty()) {
//            for (GroupUserPair pair : to_remove) {
//                RevokeDBSecurityGroupIngressRequest rm_req = new RevokeDBSecurityGroupIngressRequest();
//                rm_req.setEC2SecurityGroupName(name);
//                rm_req.setEC2SecurityGroupName(pair.groupName);
//                rm_req.setEC2SecurityGroupOwnerId(user_id);
//                rds.revokeDBSecurityGroupIngress(rm_req);
//            }
//        }
    }

    private void createGroup(RDSApi rdsApi, String name)
    {
        rdsApi.getSecurityGroupApi().createWithNameAndDescription(name, name);
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        return "okay";
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("no");
    }


    private static class GroupUserPair
    {
        private final String userId;
        private final String groupName;

        GroupUserPair(String userId, String groupName) {
            this.userId = userId;
            this.groupName = groupName;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupUserPair that = (GroupUserPair) o;
            return groupName.equals(that.groupName) && userId.equals(that.userId);

        }

        @Override
        public int hashCode()
        {
            int result = userId.hashCode();
            result = 31 * result + groupName.hashCode();
            return result;
        }

        public static Set<GroupUserPair> convertAll(Iterable<EC2SecurityGroup> groups) {
            Set<GroupUserPair> rs = Sets.newHashSet();
            for (EC2SecurityGroup group : groups) {
                rs.add(new GroupUserPair(group.getOwnerId(), group.getName()));
            }
            return rs;
        }

        public static Set<GroupUserPair> extractAll(String userId, Map<String, String> params)
        {
            Set<GroupUserPair> rs = Sets.newHashSet();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("allow_")) {
                    rs.add(new GroupUserPair(userId, entry.getValue()));
                }
            }

            return rs;
        }
    }
}
