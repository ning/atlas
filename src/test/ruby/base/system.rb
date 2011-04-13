# global aliases
aka "galaxy" => "chef:galaxy-agent"

space "ec2" do
  # what do do/use when running in ec2

  provisioner com.ning.atlas.ec2.EC2Provisioner, {
    :credential_file => "~/.awscreds"
  }

  bootstrapper com.ning.atlas.bootstrap.SSHBootStrapper, {
    :key_file => "~/.ec2/brianm-ning.pem",
    :ssh_user => "ubuntu",
    :bastion_host => "mrpiffles@bastion.example.com"
  }

  base "java-core", :image =>  "ami-a6f504cf",
                    :bootstrap => ""


  base "ubuntu-small" do
   image "ami-a6f504cf"

   bootstrap <<-EOS
      sudo apt-get -y install chef
      chef-solo wibble wibble wibble
   EOS
  end

end

space "xnb3" do
  # what do do/use when running in xnb3

end

# the actual systems
system "galaxy", :external => "http://galaxy/galaxy-template.rb"
system "chef", :external => "http://chefism/chef-server.rb"

system "ning" do
  server "resolver", :base => "java-core",
                     :install => ["galaxy:load-balancer-9.3"]
                     :count => 8

  system "aclu", :count=> 2 do
    server "appcore", :base => "java-core",
                      :count => 5,
                      :install => ["galaxy:app-server-2.4.37"]

    server "content", :base => "ubuntu-small",
                      :count => 2,
                      :install => ["galaxy", "galaxy:content-service-1.0.6"]
  end
end


# examples of per-deploy overrides

# override "ning/resolver.count", 2
# override "ning/aclu/appcore.count", 2
