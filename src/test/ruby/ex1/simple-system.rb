
module XN
  C2Provisioner = com.ning.atlas.cruft.EC2OldProvisioner
  ChefBoot = com.ning.atlas.chef.UbuntuChefSoloInitializer
  ServerPool = com.ning.atlas.ChefTaggedServerPoolProvisioner
  NoOp = com.ning.atlas.NoOpInitializer
end



environment "cruft" do
  provisioner XN::EC2Provisioner, :security_group => "backend"

  initializer "chef-solo", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
    :ssh_user => "ubuntu",
    :ssh_key_file => "#{ENV['HOME']}/.ec2/brianm-ning.pem",
    :recipe_url => "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz"
  }

  base "java-core", {
    :ami => "ami-12345",
    :init => ['chef-solo:{ "run_list": ["role[java-core]"] }']
  }

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
