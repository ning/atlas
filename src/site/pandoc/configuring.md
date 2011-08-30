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
#   base element declarations
# additional environment definitions as necessary
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The specification consists of these parts:

* Provisioners setup basic machines (bare machine + OS). For example, the EC2 provisioner will setup an instance with a chosen AMI.
* Initializers put basic system software onto these machines (e.g. ruby, java, ...).
* Installers deploy the actual software onto these machines. These are primarily used in the system definition.
* Base elements tie provisioners and initializers together to form blueprints for individual servers.

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
    :recipe_url   => "s3://my-atlas-resources/chef-solo.tar.gz",
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

This configuration defines one ec2 provisioner, two initializers and one installer, and it
declares two bases that then can be used in the system configuration.

Environments can be nested. The inner environment will inherit all definitions from the outer
environment, but not the other way around. This is mostly useful for providing provisioner
scoping for [base elements](#base-element) as they automatically use the provisioner defined
in the environment, and there can only be one provisioner per environment. For instance, for
an EC2 environment with an RDS database, you'd use a structure like:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
environment "ec2" do
  ...
  environment "rds-databases" do
  ...
  end
end
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Reading configuration values from a file

Atlas comes with a helper class to read e.g. EC2 keys and related info from a file. If you
put this at the top of your environment configuration:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
require 'atlas/tools/rc'

rc = Atlas::Tools::RC.read_rc['aws']
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

then that will read from a file ``$HOME/.atlasrc`` by default, which is assumed to be a YAML
file with this structure:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
aws:
   secret_key: <EC2 secret key>
   access_key: <EC2 access key>
   keypair_id: <name of the ssh key>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The configuration values can then be used like this in the environment configuration file:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
provisioner com.ning.atlas.aws.EC2Provisioner, {
  :access_key => rc['access_key'],
  :secret_key => rc['secret_key'],
  :keypair_id => rc['keypair_id']
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Base element

The base element defines blueprints for servers which is provisioned and initialized. The system 
configuration contains the corresponding server elements which 'instantiates' the base element blueprint
and deploys services via installers.

The base element consists of a set of attributes to be used by the provisioners and initializers described
further below, plus the optional special attribute ``init`` that defines any initializers to use for for
the blueprint.

The base element will automatically use the provisioner defined in the environment in which the base
element is defined. It is up to provisioners to determine if they should run or not. For instance, the
EC2 provisioner checks for the presence of the ``ami`` attribute. If it is defined, then it will run
when a server is created using the base element.

Examples:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
base "ruby-server", {
  :ami => "ami-e2af508b",
  :instance_type => "m1.large"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This defines a blue print called ``ruby-server`` for an EC2 instance using the AMI ``ami-e2af508b`` on
an ``m1.large`` instance. Both of these attributes are used by the EC2 provisioner described
[below](com.ning.atlas.aws.EC2Provisioner).

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
base "ruby-server", {
  :ami => "ami-e2af508b",
  :init => ['atlas', 'chef:role[ruby_server]']
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This is a variation of the above that doesn't specify the instance type for the EC2 provisioner (which
then will use the default value), and instead it specifies two initializers, ``atlas`` and ``chef``.

### Provisioners

A provisioner is responsible for provisioning bare machines/instances. Atlas currently has
these provisioners:

* ``com.ning.atlas.aws.EC2Provisioner`` for provisioning on Amazon EC2
* ``com.ning.atlas.aws.RDSProvisioner`` for provisioning on Amazon RDS 
* ``com.ning.atlas.virtualbox.VBoxProvisioner`` for provisioning VirtualBox instances.
* ``com.ning.atlas.StaticTaggedServerProvisioner`` for incorporating already provisioned machines.

A given environment can have only one provisioner defined which will then automatically be used
by all base elements defined in that environment.

##### com.ning.atlas.aws.EC2Provisioner

The ``EC2Provisioner`` brings up EC2 instances. It needs to be provided with three environment
configuration options (see [this post](http://alestic.com/2009/11/ec2-credentials) for an
explanation of what these mean):

* ``access_key``: The EC2 access key.
* ``secret_key``: The EC2 secret key.
* ``keypair_id``: The name of the private key ``.pem`` file which is used to ssh to EC2 machines,
  without the ``.pem`` file extension.

It makes use of two additional properties defined in the [base element](#base-element):

* ``ami``: The AMI identifier, e.g. ``ami-e2af508b``.
* ``instance_type``: The [type of the instance](http://aws.amazon.com/ec2/instance-types/), e.g. ``m1.large``.
  Note that the AMI has to support the instance type.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
provisioner com.ning.atlas.aws.EC2Provisioner, {
  :access_key => ec2_access_key,
  :secret_key => ec2_secret_key,
  :keypair_id => ec2_keypair_id
}

base "ruby-server", {
  :ami => "ami-e2af508b",
  :instance_type => "m1.large"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

##### com.ning.atlas.aws.RDSProvisioner

The ``RDSProvisioner`` provisions [Amazon RDS](http://aws.amazon.com/rds/) databases. For details
about RDS see the [Getting Started Guide](http://docs.amazonwebservices.com/AmazonRDS/latest/GettingStartedGuide/).
The provisioner needs these environment configuration options:

* ``access_key``: The EC2 access key.
* ``secret_key``: The EC2 secret key.

In addition, it makes use of these [base element](#base-element) properties:

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

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
provisioner com.ning.atlas.aws.RDSProvisioner, {
  :access_key => ec2_access_key,
  :secret_key => ec2_secret_key
}

base "oracle", {
  :storage_size => 10,
  :instance_class => "db.m1.small",
  :engine => "oracle-se1",
  :username => "user",
  :password => "password",
  :license_model => "bring-your-own-license"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

##### com.ning.atlas.virtualbox.VBoxProvisioner

The ``VBoxProvisioner`` provisions [VirtualBox](http://www.virtualbox.org/) instances. For more information about VirtualBox 
see the [VirtualBox user documentation](http://www.virtualbox.org/manual/UserManual.html).

For this provisioner to work, Oracle VirtualBox 4.1.0 or newer is required. The VirtualBox image must also contain the
[Guest Additions](http://www.virtualbox.org/manual/ch04.html) for Atlas to work correctly. It is important to note that
the ``virtualbox-ose-guest-utils`` Debian package will not work for this. Instead, use the correct version of the
VBoxGuestAdditions ISO provided from the [official site](http://download.virtualbox.org/virtualbox/).

The provisioner needs these environment configuration options:

* ``pub_key_file``: The file path to the public key to allow password-less SSH login into the instances.
* ``intnet_name``: The name of the internal network that VirtualBox will use.
* ``bridgedif_name``: The name of the host interface the given virtual network interface will use.

You can find the appropriate values by running the following command in terminal:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ VBoxManage list bridgedifs
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

It makes use of these additional properties defined in the [base element](#base-element):

* ``image``: The file path to the virtual appliance in Open Virtualization Format (OVF) or Open Virtualization Archive (OVA)
which will be imported to create the virtual machines. This is explained in more detail in the
[VirtualBox user documentation](http://www.virtualbox.org/manual/ch05.html#vdidetails).
* ``username``: The login username of the guest image.
* ``password``: The login password of the guest image.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
provisioner com.ning.atlas.virtualbox.VBoxProvisioner, {
  :pub_key_file   => "#{ENV['HOME']}/.atlas/#{rc['keypair_id']}.pub",
  :intnet_name    => "atlas-intnet",
  :bridgedif_name => "en0: Ethernet"
}

base "server", {
  :image => "#{ENV['HOME']}/atlas-natty32/atlas-natty32.ovf",
  :username => "atlasuser",
  :password => "atlasuser"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

##### com.ning.atlas.StaticTaggedServerProvisioner

This provider can be used to incorporate fixed, already provisioned servers into the environment. The provider itself
is used to specify the available servers, keyed by tags that can then be used in the [base element](#base-element) to
retrieve a server for initializer/installer use.

Eample:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
provisioner com.ning.atlas.StaticTaggedServerProvisioner, {
  :tag1  => ["server1", "server2"]
  :tag2  => ["server3"]
}

base "server", {
  :tag => "tag1"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Initializers

Initializers are used to configure the provisioned instances so that they can actually run services. Think of them
as the part of the tool that deploys all that base software that is needed to run your software, but is not
actually part of your software. Things like ruby, java, ...

Atlas defines these initializers at the moment:
* ``com.ning.atlas.AtlasInitializer`` for initializing a provisioned system with Atlas-related info such as the system map.
* ``com.ning.atlas.galaxy.MicroGalaxyInitializer`` for deploying software with [uGalaxy](https://github.com/brianm/ugalaxy).
* ``com.ning.atlas.chef.UbuntuChefSoloInitializer`` for deploying software with [Chef Solo](http://wiki.opscode.com/display/chef/Chef+Solo).

Initializers are triggered via a URN in the ``init`` attribute of the ``base`` element which reference previously
declared initializers. For instance given these initializer definitions:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
initializer "atlas", com.ning.atlas.AtlasInitializer, {
	...
}
initializer "chef", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
	...
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

this ``base``	element:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
base "ruby-server", {
  :ami => "ami-e2af508b",
  :init => ['atlas', 'chef:role[ruby_server]']
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

will trigger the two named initializers, ``atlas`` and ``chef``. For every server that is deployed with this base system
``ruby-server``, these two initializers will run in the specified order.

##### com.ning.atlas.AtlasInitializer

This initializer is required for proper working of Atlas because other initializers usually depend on the data
it puts on the individual instances. It performs two simple functions:

* Upload the system map to every provisioned instance. It can then be found under ``/etc/atlas/system_map.json``.
* Upload the node info for each every provisioned instance to that instance. Can be found under ``/etc/atlas/node_info.json``.

The initializer needs these environment configuration options:

* ``ssh_user``: The ssh user name for the instance.
* ``ssh_key_file``: The ssh private key file.

Both of these depends on the how the instance is setup. For EC2 for instance, the ssh user is defined by the AMI
and the private key file can be retrieved from AWS.

This initializer is triggered by specifying the atlas initializer's name in the ``init`` property of the ``base`` element.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
initializer "atlas", com.ning.atlas.AtlasInitializer, {
  :ssh_user     => "ubuntu",
  :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem"
}
base "ruby-server", {
  :ami => "ami-e2af508b",
  :init => ['atlas', 'chef:role[ruby_server]']
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

##### com.ning.atlas.galaxy.MicroGalaxyInitializer

This initializer uses a standalone version of [Galaxy](https://github.com/ning/galaxy) called
[uGalaxy](https://github.com/brianm/ugalaxy) to deploy software. It is basically the same as the
[com.ning.atlas.galaxy.MicroGalaxyInstaller](#com.ning.atlas.galaxy.MicroGalaxyInstaller) explained
further below except that it can be run in the initializer phase and thus can be used to install software
required for Galaxy or uGalaxy.

The initializer needs these environment configuration options:

* ``ssh_user``: The ssh user name for the instance.
* ``ssh_key_file``: The ssh private key file.
* ``ugx_user``: The user to run uGalaxy as.

The uGalaxy initializer is triggered by using 

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<uGalaxy initializer name>:<url>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

in the ``init`` property of the ``base`` element where the url points to the tarball containing the software to
deploy.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
installer "ugx", com.ning.atlas.galaxy.MicroGalaxyInstaller, {
  :ssh_user     => "ubuntu",
  :ssh_key_file => "#{ENV['HOME']}/.ec2/#{rc['keypair_id']}.pem",
  :ugx_user     => "ugx"
}

base "ruby-server", {
  :ami => "ami-e2af508b",
  :init => ["ugx:http://my-atlas-resources.s3.amazonaws.com/ruby-server.tar.gz"]
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


##### com.ning.atlas.chef.UbuntuChefSoloInitializer

This initializer uses [Chef Solo](http://wiki.opscode.com/display/chef/Chef+Solo) to put bits on the provisioned
machines/instances. There are lots of tutorials on chef solo, so we won't go into detail here on what it can do
and how it does things.

The initializer needs these environment configuration options:

* ``ssh_user``: The ssh user name for the instance (same as for the atlas initializer).
* ``ssh_key_file``: The ssh private key file (same as for the atlas initializer).
* ``recipe_url``: The url to the chef solo recipe bundle. For EC2, this is usually an S3 url.

If the ``recipe_url`` is an S3 url, then the initializer also uses these two configuration values if given:

* ``s3_access_key``: The S3 access key.
* ``s3_secret_key``: The S3 secret key.

They can be used to access private S3 files. For that, they only need to be read-only as the initializer only
uses them to download the resource from S3.

This initializer is triggered by using a URN of the form

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<chef initializer name>:<spec>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

in the ``init`` property of the ``base`` element. The ``spec`` part can either contain a complete chef
[run_list](http://wiki.opscode.com/display/chef/Setting+the+run_list+in+JSON+during+run+time):

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
chef:{"run_list":["role[base]","role[master]","recipe[nagios::server]"]}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

or a simplified form where Atlas builds the run list out of specified rols and recipes

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
chef:role[base],role[master],recipe[nagios::server]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The initializer will then use Chef Solo to apply all recipes (found at the specified recipe url) necessary to
put the machine/instance into these roles.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
initializer "chef", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
  :ssh_user     => "ubuntu",
  :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem",
  :recipe_url   => "s3://my-atlas-resources/chef-solo.tar.gz",
  :s3_access_key => s3_access_key,
  :s3_secret_key => s3_secret_key
}

base "ruby-server", {
  :ami => "ami-e2af508b",
  :init => ['atlas', 'chef:role[ruby_server]']
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This will provision an EC2 instance (provisioner not shown) using the Ubuntu AMI ami-e2af508b, and then use Chef Solo
to apply the ``ruby-server`` role as defined in the corresponding recipse in ``s3://my-atlas-resources/chef-solo.tar.gz``.

### Installers

Installers are what you use to deploy your services onto specific servers. The distinction between initializers
and installers is a bit arbitrary. You can perfectly well only use initializers if you have packages that for
instance can be installed with Chef Solo.

There are two main differences between initializers and installers:
* Initializers are part of the environment specification and used via base elements (blueprints). Installers on the other hand
  are used to put the actual service onto these provisioned/initialized system when a server is actually created, and are thus
  part of the system specification.
* Installers are designed in a way that they can be run multiple times against the same already provisioned/initialized
  machine/instance. Initializers don't require this explicitly (though depending on the initializer, they might support it).

You would use installers if a distinction between base packages (such as Ruby or Apache Httpd) and your service
(e.g. a Rails app or some web pages served by Apache Httpd) makes sense. You can usually tell by the frequency
of the updates to the component: Ruby updates tend to be rare whereas you might deploy your Rails app several
times a week or even more often. In this case, you would use an installer for the rails app.

Installers are usually tied to a separate tool that manages deployments. Atlas currently only supports
[Galaxy](https://github.com/ning/galaxy) but adding support for other tools such as 
[Capistrano](https://github.com/capistrano/capistrano/wiki/), [Deployinator](https://github.com/etsy/deployinator)
or [Cast](http://cast-project.org/) should be straightforward.

Both the environment and the system specification contain properties used by the installers. The environment
specification contains the settings that allow the deployment tool to be used in the environemnt, such as SSH
settings. The system specification on the other hand contains the settings that tell the deployment tool what it
should actually install. Most installers simply use a URL/URN scheme in the ``install`` property of the
``server`` element (described below in the system specification).

Atlas also uses installers to deploy other types of services, such as load balancers (i.e.
[Amazon ELB](http://aws.amazon.com/elasticloadbalancing/) and databases. This makes it easy to initialize these
services, e.g. setup a schema and data in a database or configure the load balancer for a set of services.

##### com.ning.atlas.galaxy.MicroGalaxyInstaller

This installer uses a standalone version of [Galaxy](https://github.com/ning/galaxy) called
[uGalaxy](https://github.com/brianm/ugalaxy) to deploy services. It is basically the same as the
[com.ning.atlas.galaxy.MicroGalaxyInitializer](#com.ning.atlas.galaxy.MicroGalaxyInitializer) explained
further above except that it is run in the installer phase.

The installer needs these environment configuration options:

* ``ssh_user``: The ssh user name for the instance.
* ``ssh_key_file``: The ssh private key file.
* ``ugx_user``: The user to run uGalaxy as.

To use the uGalaxy installer, specify a URL of the form

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<uGalaxy installer name>:<url>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

in the ``install`` property for the ``server`` element. The url points to the tarball containing the service to
deploy.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
installer "ugx", com.ning.atlas.galaxy.MicroGalaxyInstaller, {
  :ssh_user     => "ubuntu",
  :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem",
  :ugx_user     => "xncore"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
server "echo",
  :base => "ruby-server",
  :install => ["ugx:http://my-atlas-resources.s3.amazonaws.com/echo.tar.gz"]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

##### com.ning.atlas.galaxy.GalaxyInstaller

This installer uses [Galaxy](https://github.com/ning/galaxy). It needs these environment configuration options:

* ``ssh_user``: The ssh user name for the instance.
* ``ssh_key_file``: The ssh private key file.

There are two ways to trigger the galaxy installer:

* Specify a galaxy role via the ``galaxy`` property on the ``server`` element.
* Use an ``install`` url of the form 

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<galaxy installer name>:<env>/<version>/<type>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

where ``env`` specifies the environment, ``version`` the version of the service, and ``type`` the type of the service.

The galaxy role form is used to deploy parts of galaxy itself. The ``galaxy`` property has three possible values:

* ``shell``: A generic shell server including the galaxy commandline tool.
* ``console``: The galaxy console.
* ``repository``: The file repository containing the deployable artifacts.

The ``console`` and ``repository`` are required or galaxy won't be able to deploy anything else. A shell is not strictly
required but useful if you want to interact with galaxy manually.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
installer "galaxy", com.ning.atlas.galaxy.GalaxyInstaller, {
  :ssh_user     => "ubuntu",
  :ssh_key_file => "#{ENV['HOME']}/.ec2/#{ec2_keypair_id}.pem",
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
server "echo",
  :base => "ruby-server",
  :install => ["galaxy:myenv/v1/echo"]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

##### com.ning.atlas.oracle.OracleLoaderInstaller

The Oracle installer provides a way to initialize the schema and data in an Amazon RDS Oracle database instance by
passing specified text files containing DDL/DML statements to Oracle's
[SQL*Plus](http://download.oracle.com/docs/cd/B28359_01/server.111/b31189/toc.htm).

It needs these environment configuration options:

* ``ssh_user``: The ssh user name for the instance.
* ``ssh_key_file``: The ssh private key file.
* ``sql_url_template``: This is a [StringTemplate](http://www.stringtemplate.org/) to generate the S3 urls for the schema files.
  This is explained further below.

The installer is triggered by using an URN of the form

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<oracle installer name>:<data>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

in the ``install`` property for the ``server`` element. The data part of the URN contains key value pairs of the form

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
<oracle installer name>:key1=value1;key2=value2;...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

These keys can then be referenced in the url template. E.g. typically you template the file name like so:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
installer "oracle", com.ning.atlas.oracle.OracleLoaderInstaller, {
  :sql_url_template => "s3://my-atlas-resources/$file$",
  :ssh_user     => "ubuntu",
  :ssh_key_file => "#{ENV['HOME']}/.ec2/#{rc['keypair_id']}.pem"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
server "userdb",
  :base => "oracle",
  :install => ["oracle:file=userdb-ddl.sql"],
  :db_role => ['user']
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This will make the oracle installer look for a file ``s3://my-atlas-resources/userdb-ddl.sql`` when initializing a server ``userdb``.

##### com.ning.atlas.aws.ELBInstaller

This installer allows to register servers with Amazon's [Elastic Load Balancer](https://aws.amazon.com/elasticloadbalancing/).

For this, it needs these environment configuration options:

* ``access_key``: The EC2 access key.
* ``secret_key``: The EC2 secret key.

The installer is triggered by specifying the name of the installer in the ``install`` property for the ``server`` element.

Example:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
installer "ugx", com.ning.atlas.galaxy.MicroGalaxyInstaller, {
	...
}

installer "elb", com.ning.atlas.aws.ELBInstaller, {
  :access_key => rc['access_key'],
  :secret_key => rc['secret_key']
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.ruby}
server "echo",
  :base => "ruby-server",
  :install => ["ugx:http://my-atlas-resources.s3.amazonaws.com/echo.tar.gz", "elb"]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## System specification

