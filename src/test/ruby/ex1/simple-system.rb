
module XN
  # commented out as these classes may not exist :-)
  #EC2Provisioner = com.ning.atlas.ec2.EC2Provisioner
  #ChefBoot = com.ning.atlas.chef.UbuntuChefSoloBootStrapper
  #ServerPool = com.ning.atlas.ChefTaggedServerPoolProvisioner

  ChefBoot = "hi"
  EC2Provisioner = "world"
  ServerPool = "woot"
end

environment "ec2" do
  bootstrapper XN::ChefBoot, :ssh_user => "ubuntu",
                             :ssh_key_file => "http://keys/wafflehut.pem"

  provisioner XN::EC2Provisioner, :security_group => "backend"

  base "java-core", :ami => "ami-12345",
                    :init => ["chef-solo:role[java-core]"]

  environment "front" do
    provisioner XN::EC2Provisioner, :security_group => "front-end"
    base "front-door-core", :extends => "java-core"
  end
end

# does not do own bootstrap
environment "xnb3" do
  provisioner XN::ServerPool, :chef_server => "http://chef"

  override "root.ning.resolver:count", 1

  base "java-core", :tag => "core"
  base "playground", :tag => "playground"
  base "front-door-core", :extends => "java-core"
end


# this has a top level so that it can share file with env definitions
system "root" do
  # the system definition goes at the top level
  server "gepo", :cardinality => 2, :base => "centos-big"

  system "ning" do
    server "resolver", :base => "front-door-core",
                       :install => ["galaxy:wiffle/wombat/hoot"],
                       :cardinality => 2

    system "aclu", :count=>2  do
      server "appcore", :base => "java-core"
    end
  end
end