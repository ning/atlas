package com.ning.atlas.components.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.IpPermission;
import com.google.common.collect.Lists;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.space.Missing;

import java.util.List;
import java.util.concurrent.Future;

public class SecurityGroupListener extends BaseLifecycleListener
{
    @Override
    public Future<?> startDeployment(Deployment d)
    {

        AWSCredentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                .otherwise(new IllegalStateException("No AWS Credential available")).toAWSCredentials();


        AmazonEC2Client client = new AmazonEC2Client(creds);

        List<IpPermission> ls = Lists.newArrayList();
        IpPermission perm = new IpPermission();
        perm.setToPort(22);
        perm.setIpRanges(Lists.<String>newArrayList("0.0.0.0"));
        perm.setIpProtocol("tcp");
        ls.add(perm);
        AuthorizeSecurityGroupIngressRequest req = new AuthorizeSecurityGroupIngressRequest("default", ls);
        client.authorizeSecurityGroupIngress(req);

        return super.startDeployment(d);
    }
}
