package com.ning.atlas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.protocols.SSHCredentials;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AWSConfigurator extends BaseLifecycleListener
{
    private final ExecutorService              es            = Executors.newCachedThreadPool();
    private final List<Pair<String, String>> credentialIds = new CopyOnWriteArrayList<Pair<String, String>>();

    public AWSConfigurator(Map<String, String> attributes)
    {
        Splitter s = Splitter.on('@').trimResults();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getKey().startsWith("ssh")) {
                Iterator<String> i = s.split(entry.getValue()).iterator();

                String user = i.next();
                String name = i.next();

                credentialIds.add(Pair.of(user, name));
            }
        }
    }

    @Override
    public Future<?> startDeployment(final Deployment d)
    {
        return es.submit(new Runnable()
        {
            @Override
            public void run()
            {
                final Space s = d.getSpace();
                final AWS.Credentials creds;
                if (!s.get(AWS.ID, AWS.Credentials.class, Missing.RequireAll).isKnown()) {
                    // no creds in space, ask for em and save em

                    System.console().printf("What is your AWS access key? ");
                    String access_key = System.console().readLine().trim();

                    System.console().printf("What is your AWS secret key? ");
                    String secret_key = System.console().readLine().trim();

                    creds = new AWS.Credentials();
                    creds.setAccessKey(access_key);
                    creds.setSecretKey(secret_key);

                    s.store(AWS.ID, creds);
                }
                else {
                    creds = s.get(AWS.ID, AWS.Credentials.class, Missing.RequireAll).getValue();
                }

                final AWS.SSHKeyPairInfo info;
                if (!s.get(AWS.ID, AWS.SSHKeyPairInfo.class, Missing.RequireAll).isKnown()) {
                    // no ssh keypair stuff, make one!
                    AmazonEC2Client client = new AmazonEC2Client(new BasicAWSCredentials(creds.getAccessKey(),
                                                                                         creds.getSecretKey()));
                    final String name = UUID.randomUUID().toString();
                    CreateKeyPairRequest req = new CreateKeyPairRequest(name);

                    CreateKeyPairResult res = client.createKeyPair(req);

                    File config_dir = new File(".atlas");
                    if (!config_dir.exists()) {
                        config_dir.mkdir();
                    }
                    File pemfile = new File(config_dir, "ec2.pem");
                    try {
                        Files.write(res.getKeyPair().getKeyMaterial(),
                                    pemfile,
                                    Charset.forName("UTF8"));

                        // good god the perm api on file is horrible
                        pemfile.setWritable(false, false); // no one may write
                        pemfile.setReadable(false, false); // no one may read
                        pemfile.setReadable(true, true); // owner may read
                    }
                    catch (IOException e) {
                        throw new IllegalStateException("Unable to write out pem file for keypair " + name, e);
                    }

                    info = new AWS.SSHKeyPairInfo();
                    info.setPrivateKeyFile(pemfile.getAbsolutePath());
                    info.setKeyPairId(name);
                    s.store(AWS.ID, info);
                }
                else {
                    info = s.get(AWS.ID, AWS.SSHKeyPairInfo.class, Missing.RequireAll).getValue();
                }

                for (Pair<String, String> pair : credentialIds) {
                    SSHCredentials ssh_creds = new SSHCredentials();
                    ssh_creds.setKeyFilePath(info.getPrivateKeyFile());
                    ssh_creds.setUserName(pair.getKey());
                    SSHCredentials.store(s, ssh_creds, pair.getValue());
                }
            }
        });
    }


    @Override
    public Future<?> finishDeployment(Deployment d)
    {
        es.shutdown();
        return super.finishDeployment(d);
    }
}
