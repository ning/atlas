package com.ning.atlas.components.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.AuthorizeDBSecurityGroupIngressRequest;
import com.amazonaws.services.rds.model.CreateDBSecurityGroupRequest;
import com.amazonaws.services.rds.model.DBSecurityGroup;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsRequest;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsResult;
import com.amazonaws.services.rds.model.EC2SecurityGroup;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.space.Missing;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class RDSSecurityGroupProvisioner extends ConcurrentComponent
{
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        final String group_name = uri.getFragment();
        AtlasConfiguration config = AtlasConfiguration.global();
        BasicAWSCredentials creds = new BasicAWSCredentials(config.lookup("aws.key").get(),
                                                            config.lookup("aws.secret").get());

        AmazonRDSClient rds = new AmazonRDSClient(creds);
        AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(creds);

        DescribeDBSecurityGroupsRequest req = new DescribeDBSecurityGroupsRequest();
        req.setDBSecurityGroupName(group_name);
        try {
            DescribeDBSecurityGroupsResult res = rds.describeDBSecurityGroups(req);
            DBSecurityGroup group = res.getDBSecurityGroups().get(0);

        }
        catch (AmazonServiceException e) {
            // doesn't exist!
            if ("DBSecurityGroupNotFound".equals(e.getErrorCode())) {
                createGroup(rds, group_name);
            }
            else {
                throw e;
            }
        }
        updateGroup(uri, rds, iam);

        return "okay";
    }

    private void updateGroup(Uri<? extends Component> uri, AmazonRDSClient rds, AmazonIdentityManagementClient iam)
    {
        String user_id = iam.getUser().getUser().getUserId();
        String name = uri.getFragment();
        DescribeDBSecurityGroupsRequest req = new DescribeDBSecurityGroupsRequest();
        req.setDBSecurityGroupName(name);
        DescribeDBSecurityGroupsResult res = rds.describeDBSecurityGroups(req);
        DBSecurityGroup group = res.getDBSecurityGroups().get(0);

        Set<GroupUserPair> groups_wanted = GroupUserPair.extractAll(user_id, uri.getParams());
        Set<GroupUserPair> existing  = GroupUserPair.convertAll(group.getEC2SecurityGroups());

        Set<GroupUserPair> to_add = Sets.difference(groups_wanted, existing);
        Set<GroupUserPair> to_remove = Sets.difference(existing, groups_wanted);

        if (!to_add.isEmpty()) {
            for (GroupUserPair pair : to_add) {
                AuthorizeDBSecurityGroupIngressRequest add_req = new AuthorizeDBSecurityGroupIngressRequest();
                add_req.setDBSecurityGroupName(name);
                add_req.setEC2SecurityGroupName(pair.groupName);
                add_req.setEC2SecurityGroupOwnerId(pair.userId);
                rds.authorizeDBSecurityGroupIngress(add_req);
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

    private void createGroup(AmazonRDSClient rds, String name)
    {
        CreateDBSecurityGroupRequest req = new CreateDBSecurityGroupRequest();
        req.setDBSecurityGroupName(name);
        req.setDBSecurityGroupDescription(name);
        rds.createDBSecurityGroup(req);
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
                rs.add(new GroupUserPair(group.getEC2SecurityGroupOwnerId(), group.getEC2SecurityGroupName()));
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
