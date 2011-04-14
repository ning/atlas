space "ec2" do
  bootstrapper com.ning.atlas.UbuntuChefSoloBootStrapper
  provisioner com.ning.atlas.ec2.EC2Provisioner

  base "java-core", :ami => "ami-12345",
                    :bootstrap => ["chef-solo:role[java-core]"]

  space "front" do
    provisioner :security_group => "front-end"
    base "front-door-core", :extends => "java-core"
  end

end

# does not do own bootstrap
space "xnb3" do
  provisioner com.ning.atlas.ChefTaggedServerPoolProvisioner

  base "java-core", :tag => "core"
  base "playground", :tag => "playground"
  base "front-door-core", :extends "java-core"
end


server "gepo", :count => 2, :base => "centos-big"

system "ning" do
  server "resolver", :base => "front-door-core",
                     :install => ["galaxy:wiffle/wombat/hoot"],
                     :count => 2

  system "aclu", :count=>2  do
    server "appcore", :base => "java-core"
  end
end
