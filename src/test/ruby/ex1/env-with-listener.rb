environment "test" do

  set "xn.base-domain" => "waffles.test"

  listener "testy"

  installer "ugx", {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "~/.ec2/brianm-ning.pem",
      :ugx_user     => "ugx",
      :ugx_path     => "/home/ugx/deploy"
  }

  installer "ubuntu-chef-solo", {
      :ssh_user     => "ubuntu",
      :ssh_key_file => "#{ENV['HOME']}/.ec2/brianm-ning.pem",
      :recipe_url   => "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz"
  }

  base "ubuntu-small", :provisioner => "noop"

  base "concrete", {
      :provisioner => "noop"
  }
end
