environment "test" do

  set "xn.base-domain" => "waffles.test"

  listener com.ning.atlas.ListenerThing

  installer "ugx", com.ning.atlas.galaxy.MicroGalaxyInstaller, {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "~/.ec2/brianm-ning.pem",
      :ugx_user     => "ugx",
      :ugx_path     => "/home/ugx/deploy"
  }

  provisioner "ec2", com.ning.atlas.noop.NoOpProvisioner

  installer "chef-solo", com.ning.atlas.chef.UbuntuChefSoloInstaller, {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "#{ENV['HOME']}/.ec2/brianm-ning.pem",
      :recipe_url   => "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz"
  }

  base "concrete", {
      :provisioner => ["ec2", { :tag => "ami-1234"}]
  }
end
