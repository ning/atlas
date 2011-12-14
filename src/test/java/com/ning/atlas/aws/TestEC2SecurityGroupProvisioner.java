package com.ning.atlas.aws;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
        assertThat(perm.getIpProtocol(), equalTo("tcp"));
        assertThat(perm.getIpRanges(), equalTo(asList("0.0.0.0/0")));
    }

    @Test
    public void testSecurityGroupRule() throws Exception
    {
        String raw_rule = "tcp 22 default";
        IpRule rule = IpRule.parse("1", raw_rule);
        IpPermission perm = rule.toIpPermission();
        assertThat(perm.getFromPort(), equalTo(22));
        assertThat(perm.getToPort(), equalTo(22));
        assertThat(perm.getIpProtocol(), equalTo("tcp"));
        assertThat(perm.getUserIdGroupPairs().size(), equalTo(1));
        assertThat(perm.getUserIdGroupPairs().get(0).getGroupName(), equalTo("default"));
        assertThat(perm.getUserIdGroupPairs().get(0).getUserId(), equalTo("1"));
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
        IpPermission perm = new IpPermission();
        perm.setIpProtocol("tcp");
        perm.setFromPort(22);
        perm.setToPort(22);
        perm.setIpRanges(asList("0.0.0.0/0"));

        IpRule r = IpRule.fromPermission(perm);

        IpRule r2 = IpRule.parse("1", "tcp 22 0.0.0.0/0");
        assertThat(r, equalTo(r2));
    }

    @Test
    public void testFromPermissionWithGroup() throws Exception
    {
        IpPermission perm = new IpPermission();
        perm.setIpProtocol("tcp");
        perm.setFromPort(22);
        perm.setToPort(22);
        UserIdGroupPair pair = new UserIdGroupPair();
        pair.setGroupName("woof");
        pair.setUserId("1");
        perm.setUserIdGroupPairs(asList(pair));

        IpRule r = IpRule.fromPermission(perm);

        IpRule r2 = IpRule.parse("1", "tcp 22 woof");
        assertThat(r, equalTo(r2));
    }

    @Test
    public void testRandomRule() throws Exception
    {
        IpPermission perm = new IpPermission();
        perm.setIpProtocol("tcp");
        perm.setFromPort(22);
        perm.setToPort(22);

        UserIdGroupPair pair = new UserIdGroupPair();
        pair.setGroupName("woof");
        pair.setUserId("1");

        UserIdGroupPair pair2 = new UserIdGroupPair();
        pair2.setGroupName("meow");
        pair.setUserId("2");

        perm.setUserIdGroupPairs(asList(pair, pair2));

        IpRule r = IpRule.fromPermission(perm);

        IpRule r2 = IpRule.parse("1", "tcp 22 woof");
        assertThat(r, not(equalTo(r2)));

    }
}
