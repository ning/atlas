creds = File.open(".awscreds") do |f|
  rs = {}
  while line = f.gets
    key, name = line.split "="
    rs[key] = name.strip
  end
  rs
end


environment "ec2" do
  provisioner "ec2", com.ning.atlas.aws.EC2Provisioner, {
      access_key: creds['aws.access-key'],
      secret_key: creds['aws.secret-key'],
      keypair_id: creds['aws.key-name']
  }

  initializer "atlas", com.ning.atlas.AtlasInstaller, {
      :ssh_user     => creds['aws.ssh-user'],
      :ssh_key_file => creds['aws.key-file-path']
  }

  initializer "chef", com.ning.atlas.chef.UbuntuChefSoloInstaller, {
      :ssh_user     => creds['aws.ssh-user'],
      :ssh_key_file => creds['aws.key-file-path'],
      :recipe_url   => "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz"
  }

  base "server", {
      :provisioner => "ec2",
      :ami  => "ami-e2af508b",
      :init => ['atlas', 'chef:role[server]']
  }
end

system "simple" do
  server "shell", :base => "server",
                  :cardinality => ["eshell"]


end
