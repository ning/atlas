environment "test" do

  set "xn.base-domain" => "waffles.test"

  installer "ugx", com.ning.atlas.galaxy.MicroGalaxyInstaller, {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "~/.ec2/brianm-ning.pem",
      :ugx_user     => "ugx",
      :ugx_path     => "/home/ugx/deploy"
  }

  provisioner "ec2", com.ning.atlas.aws.EC2Provisioner, {
      :access_key => "",
      :secret_key => "",
      :keypair_id => "brianm-ning",
  }

  initializer "chef-solo", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "#{ENV['HOME']}/.ec2/brianm-ning.pem",
      :recipe_url   => "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz"
  }

  base "concrete", :provisioner => "ec2",
                   :tag => "ami-1234",
                   :init => ['chef-solo:{ "run_list": "role[java-core]" }']
end
