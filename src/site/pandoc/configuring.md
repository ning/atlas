# Configuration

Atlas distinguishes between environment and system when it comes to configuration.

The environment configuration specifies the "how": how to provision machines, how to deploy
services onto those machines, and so forth.

The system configuration specifies the "what": which services are defined, how many of each
service should be deployed, etc.

Both files are basically ruby files that get executed. The configuration is specified in
a DSL that gets executed as normal ruby code. Thus, you can basically do whatever you want
in these files as long as you can express it in ruby.

## Environment specification

An environment specification file typically has this structure:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
# require statements etc.
# constants
# first environment definition
#   provisioner declarations
#   initializer declarations
#   installer declarations
#   base declarations
# additional environment definitions as necessary
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A simple environment configuration for EC2 might look like this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
# EC2 access configuration, could also be read from a file
ec2_access_key = "..." # EC2 access key
ec2_secret_key = "..." # EC2 secret key
ec2_keypair_id = "..." # name of the .pem file used for ssh'ing into the EC2 machines
s3_access_key  = "..." # S3 access key
s3_secret_key  = "..." # S3 secret key

environment "ec2" do
  set "xn.base-domain" => "echo.xn.io"

  set "aws.shared.access_key" => s3_access_key,
      "aws.shared.secret_key" => s3_secret_key

  provisioner com.ning.atlas.aws.EC2Provisioner, {
    :access_key => ec2_access_key,
    :secret_key => ec2_secret_key,
    :keypair_id => ec2_keypair_id
  }

  initializer "atlas", com.ning.atlas.AtlasInitializer, {
    :ssh_user     => "ubuntu",
    :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem"
  }

  initializer "chef", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
    :ssh_user     => "ubuntu",
    :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem",
    :recipe_url   => "s3://atlas-resources/chef-solo.tar.gz",
    :s3_access_key => s3_access_key,
    :s3_secret_key => s3_secret_key
  }

  installer "ugx", com.ning.atlas.galaxy.MicroGalaxyInstaller, {
    :ssh_user     => "ubuntu",
    :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem",
    :ugx_user     => "user"
  }

  base "ruby-server", :ami => "ami-e2af508b", :init => ['atlas', 'chef:role[ruby_server]']
  base "shell",       :ami => "ami-e2af508b", :init => ['atlas', 'chef:role[shell]']
end
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This configuration defines one ec2 provisionier, two initializers and one installer, and it
declares two bases that then can be used in the system configuration.

### Provisioners

A provisioner is responsible for provisioning bare machines/instances. Atlas currently has
three provisioners: ``com.ning.atlas.aws.EC2Provisioner``, ``com.ning.atlas.aws.RDSProvisioner``
and ``com.ning.atlas.virtualbox.VBoxProvisioner``.

##### com.ning.atlas.aws.EC2Provisioner

The ``EC2Provisioner`` brings up EC2 instances. It needs to be provided with three configuration
options (see [this post](http://alestic.com/2009/11/ec2-credentials) for an explanation of what
these mean):

* ``access_key``: The EC2 access key.
* ``secret_key``: The EC2 secret key.
* ``keypair_id``: The name of the private key ``.pem`` file which is used to ssh to EC2 machines,
  without the ``.pem`` file extension.

It makes use of two additional properties defined in the [base] (which is explained furtehr below):

* ``ami``: The AMI identifier, e.g. ``ami-e2af508b``.
* ``instance_type``: The [type of the instance](http://aws.amazon.com/ec2/instance-types/), e.g. ``m1.large``.

##### com.ning.atlas.aws.RDSProvisioner

The ``RDSProvisioner`` provisions [Amazon RDS](http://aws.amazon.com/rds/) databases. For details
about RDS see the [Getting Started Guide](http://docs.amazonwebservices.com/AmazonRDS/latest/GettingStartedGuide/).
The provisioner needs these configuration options:

* ``access_key``: The EC2 access key.
* ``secret_key``: The EC2 secret key.

In addition, it makes use of these [base] properties:

* ``license_model``: The license model for the database instance. One of ``license-included``, ``bring-your-own-license``,
  ``general-public-license``. Default is ``general-public-license``. For MySQL, this is the only valid option.
* ``storage_size``: The amount of storage (in gigabytes) to be initially allocated for the database instance. Must
  be an integer from 5 to 1024.
* ``instance_class``: The compute and memory capacity of the database instance. One of ``db.m1.small``, ``db.m1.large``,
  ``db.m1.xlarge``, ``db.m2.xlarge``, ``db.m2.2xlarge``, ``db.m2.4xlarge``.
* ``engine``: The name of the database engine to be used for this instance. Valid values are ``MySQL``, ``oracle-se1``,
  ``oracle-se``, ``oracle-ee``.
* ``username``: The name of master user for the client database instance. Must be 1 to 16 alphanumeric characters, and
  the first character must be a letter. Cannot be a reserved word for the chosen database engine.
* ``password``: The password for the master database instance user. Must contain 4 to 41 alphanumeric characters.

##### com.ning.atlas.virtualbox.VBoxProvisioner

TBD

## System specification

TBD
