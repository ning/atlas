creds = File.open(".awscreds") do |f|
  rs = {}
  while line = f.gets
    key, name = line.split "="
    rs[key] = name.strip
    puts "#{key}=#{name}"
  end
  rs
end


environment "ec2" do
  provisioner com.ning.atlas.aws.EC2Provisioner, {
      :access_key => creds['aws.access-key'],
      :secret_key => creds['aws.secret-key'],
      :keypair_id => creds['aws.key-name']
  }

  initializer "chef", com.ning.atlas.chef.UbuntuChefSoloInitializer, {
      :ssh_user     => creds['aws.ssh-user'],
      :ssh_key_file => creds['aws.key-file-path'],
      :recipe_url   => "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz"
  }

  installer "ugx", com.ning.atlas.MicroGalaxyInstaller, {
      :ssh_user     => creds['aws.ssh-user'],
      :ssh_key_file => creds['aws.key-file-path'],
      :ugx_user     => "ubuntu",
      :ugx_path     => "/home/ubuntu/deploy"
  }


  base "gonsole", {
      :ami  => "ami-e2af508b",
      :init => ['chef:role[gonsole]']
  }

  base "server", {
      :ami  => "ami-e2af508b",
      :init => ['chef:role[server]']
  }
end

system "simple" do
  server "shell", {
      :base        => "server",
      :cardinality => ["eshell"],
      :install     => ["ugx:https://s3.amazonaws.com/atlas-resources/echo-server-0.0.1.tar.gz"]
  }

end
