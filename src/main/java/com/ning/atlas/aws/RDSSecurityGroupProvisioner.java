package com.ning.atlas.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBSecurityGroup;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsRequest;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsResult;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.space.Missing;

import java.util.concurrent.Future;

public class RDSSecurityGroupProvisioner extends ConcurrentComponent
{
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        final String group_name = uri.getFragment();
        AWSCredentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                .otherwise(new IllegalStateException("No AWS Credentials available"))
                                .toAWSCredentials();

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
            System.out.println(e.getErrorCode());
        }

        return "okay";
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
}
