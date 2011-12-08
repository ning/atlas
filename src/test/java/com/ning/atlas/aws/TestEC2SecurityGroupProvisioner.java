package com.ning.atlas.aws;

import com.amazonaws.services.ec2.model.IpPermission;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestEC2SecurityGroupProvisioner
{
    @Test
    public void testCidrRule() throws Exception
    {
        String raw_rule = "tcp 22 0.0.0.0/0";
        EC2SecurityGroupProvisioner.Rule rule = EC2SecurityGroupProvisioner.Rule.parse("1", raw_rule);
        IpPermission perm = rule.toIpPermission();
        assertThat(perm.getFromPort(), equalTo(22));
        assertThat(perm.getToPort(), equalTo(22));
        assertThat(perm.getIpProtocol(), equalTo("tcp"));
        assertThat(perm.getIpRanges(), equalTo(asList("0.0.0.0/0")));
    }

    @Test
    public void testSecurityGroupRule() throws Exception
    {
        String raw_rule = "tcp 22 default";
        EC2SecurityGroupProvisioner.Rule rule = EC2SecurityGroupProvisioner.Rule.parse("1", raw_rule);
        IpPermission perm = rule.toIpPermission();
        assertThat(perm.getFromPort(), equalTo(22));
        assertThat(perm.getToPort(), equalTo(22));
        assertThat(perm.getIpProtocol(), equalTo("tcp"));
        assertThat(perm.getUserIdGroupPairs().size(), equalTo(1));
        assertThat(perm.getUserIdGroupPairs().get(0).getGroupName(), equalTo("default"));
        assertThat(perm.getUserIdGroupPairs().get(0).getUserId(), equalTo("1"));
    }
}
