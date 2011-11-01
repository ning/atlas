package com.ning.atlas.galaxy;

import com.google.common.collect.Iterables;
import com.ning.atlas.AtlasInstaller;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.aws.EC2Helper;
import com.ning.atlas.chef.UbuntuChefSoloInstaller;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import static com.ning.atlas.aws.EC2Helper.loadSshPropertyThing;
import static com.ning.atlas.aws.TestEC2Provisioner.isAvailable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestMicroGalaxyInstaller
{

    private Deployment              d;
    private MicroGalaxyInstaller    microgalaxy;
    private Host                    host;
    private UbuntuChefSoloInstaller chef;
    private AtlasInstaller          atlas;
    private SSH                     ssh;

    @Before
    public void setUp() throws Exception
    {
        Map<String, String> ssh_props = EC2Helper.loadSshPropertyThing("ugx_user", "xncore",
                                                                       "ugx_path", "/local/home/xncore/deploy/current",
                                                                       "recipe_url",
                                                                       "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");
        this.d = EC2Helper.spinUpSingleInstance();
        this.microgalaxy = new MicroGalaxyInstaller(ssh_props);
        microgalaxy.start(d);
        this.host = Iterables.getOnlyElement(this.d.getSystemMap().findLeaves());

        this.atlas = new AtlasInstaller(loadSshPropertyThing());
        this.atlas.start(d);
        this.atlas.install(host, Uri.<Installer>valueOf("atlas"), d).get();
        chef = new UbuntuChefSoloInstaller(ssh_props);
        chef.install(host, Uri.<Installer>valueOf("chef:role[ruby_server]"), d).get();
        Server s = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();
        this.ssh = new SSH(new File(ssh_props.get("ssh_key_file")), ssh_props.get("ssh_user"), s.getExternalAddress());
    }

    @After
    public void tearDown() throws Exception
    {
        EC2Helper.destroy(this.d);
        microgalaxy.finish(d);
        chef.finish(d);
        atlas.finish(d);
        ssh.close();
    }


    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        assumeThat("ec2", isAvailable());
        String uri = "mg:https://s3.amazonaws.com/atlas-resources/echo.tar.gz";
        microgalaxy.install(host, Uri.<Installer>valueOf(uri), d).get();

        ssh.forwardLocalPortTo(1337, "localhost", 1337);
        System.out.println("forwarded!");
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress("localhost", 1337));
        sock.getOutputStream().write("hello world\n".getBytes());
        sock.getOutputStream().flush();
        byte[] buf = new byte["hello world".getBytes().length];
        System.out.println("sent!");
        sock.getInputStream().read(buf);
        sock.close();

        assertThat(new String(buf), equalTo("dlrow olleh"));
    }
}
