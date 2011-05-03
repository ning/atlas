
module XN
  C2Provisioner = com.ning.atlas.ec2.EC2Provisioner
  ChefBoot = com.ning.atlas.chef.UbuntuChefSoloInitializer
  ServerPool = com.ning.atlas.ChefTaggedServerPoolProvisioner
  NoOp = com.ning.atlas.NoOpInitializer
end



environment "ec2" do
  initializer XN::ChefBoot, :ssh_user => "ubuntu",
                            :ssh_key_file => "http://keys/wafflehut.pem"

  provisioner XN::EC2Provisioner, :security_group => "backend"

  base "java-core", :ami => "ami-12345",
                    :init => ["chef-solo:role[java-core]"]

  base "big-server", :ami => "ami-9999",
                     :init => ["chef-solo:role[utility]"]
  environment "front" do
    provisioner XN::EC2Provisioner, :security_group => "front-end"
    base "front-door-core", :extends => "java-core"
  end
end



# does not do own bootstrap
environment "xnb3" do
  provisioner XN::ServerPool, :chef_server => "http://chef-server:4000/"
  initializer XN::NoOp

  override "root.ning.resolver:count", 1

  base "big-server", :tag => "utility"
  base "java-core", :tag => "core"
  base "playground", :tag => "playground"
  base "front-door-core", :extends => "java-core"
end





# this has a top level so that it can share file with env definitions
system "root" do
  # the system definition goes at the top level
  server "gepo", :cardinality => 2,
                 :base => "big-server",
                 :order => 10

  system "ning" do
    server "resolver", :base => "front-door-core",
                       :install => ["galaxy:wiffle/wombat/hoot"],
                       :cardinality => 2,
                       :order => 20

    system "aclu", :cardinality => 2  do
      server "appcore", :base => "java-core",
                        :order => 20
    end
  end
end
