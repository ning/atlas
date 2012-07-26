package com.ning.atlas.components.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Map.Entry;

import org.jclouds.ec2.domain.IpPermission;
import org.jclouds.ec2.domain.IpProtocol;
import org.jclouds.ec2.util.IpPermissions;
import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

public class TestEC2SecurityGroupProvisioner
{
    @Test
    public void testCidrRule() throws Exception
    {
        String raw_rule = "tcp 22 0.0.0.0/0";
        IpRule rule = IpRule.parse("1", raw_rule);
        IpPermission perm = rule.toIpPermission();
        assertThat(perm.getFromPort(), equalTo(22));
        assertThat(perm.getToPort(), equalTo(22));
        assertThat(perm.getIpProtocol(), equalTo(IpProtocol.TCP));
        assertEquals(perm.getIpRanges(), ImmutableSet.<String>of("0.0.0.0/0"));
    }

    @Test
    public void testSecurityGroupRule() throws Exception
    {
        String raw_rule = "tcp 22 default";
        IpRule rule = IpRule.parse("1", raw_rule);
        IpPermission perm = rule.toIpPermission();
        assertThat(perm.getFromPort(), equalTo(22));
        assertThat(perm.getToPort(), equalTo(22));
        assertThat(perm.getIpProtocol(), equalTo(IpProtocol.TCP));
        assertThat(perm.getUserIdGroupPairs().size(), equalTo(1));
        Entry<String, String> pair = perm.getUserIdGroupPairs().entries().iterator().next();
        assertThat(pair.getValue(), equalTo("default"));
        assertThat(pair.getKey(), equalTo("1"));
    }

    @Test
    public void testEquals() throws Exception
    {
        IpRule ip = IpRule.parse("1", "tcp 22 0.0.0.0/0");
        IpRule ip2 = IpRule.parse("1", "tcp 22 0.0.0.0/0");
        assertThat(ip, equalTo(ip2));
    }

    @Test
    public void testFromPermission() throws Exception
    {
        IpPermission perm = IpPermissions.permit(IpProtocol.TCP).fromPort(22).to(22).originatingFromCidrBlock("0.0.0.0/0");

        IpRule r = IpRule.fromPermission(perm);

        IpRule r2 = IpRule.parse("1", "tcp 22 0.0.0.0/0");
        assertThat(r, equalTo(r2));
    }

    @Test
    public void testFromPermissionWithGroup() throws Exception
    {
        IpPermission perm = IpPermissions.permit(IpProtocol.TCP).fromPort(22).to(22).originatingFromUserAndSecurityGroup("1", "woof");

        IpRule r = IpRule.fromPermission(perm);

        IpRule r2 = IpRule.parse("1", "tcp 22 woof");
        assertThat(r, equalTo(r2));
    }

    @Test
    public void testRandomRule() throws Exception
    {
        IpPermission perm = IpPermissions.permit(IpProtocol.TCP).fromPort(22).to(22).toEC2SecurityGroups(
                ImmutableMultimap.of("1", "woof", "2", "meow"));

        IpRule r = IpRule.fromPermission(perm);

        IpRule r2 = IpRule.parse("1", "tcp 22 woof");
        assertThat(r, not(equalTo(r2)));

    }
}
