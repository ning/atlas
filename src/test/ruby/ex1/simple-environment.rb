environment "test" do

  installer "ugx", com.ning.atlas.MicroGalaxyInstaller, {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "~/.ec2/brianm-ning.pem"
  }

  provisioner com.ning.atlas.ec2.EC2Provisioner, {
      :access_key => "",
      :secret_key => "",
      :keypair_id => "brianm-ning",
  }

  initializer "chef-solo", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "#{ENV['HOME']}/.ec2/brianm-ning.pem",
      :recipe_url   => "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz"
  }

  base "concrete", :tag => "ami-1234", :init => ['chef-solo:{ "run_list": "role[java-core]" }']
end
