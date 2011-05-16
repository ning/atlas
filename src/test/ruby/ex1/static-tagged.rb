
environment "test" do
  provisioner com.ning.atlas.StaticTaggedServerProvisioner, :servers => { "java" => ["10.0.0.1", "10.0.0.2"],
                                                                          "php" => ["10.0.1.1", "10.0.1.2"] }

  initializer com.ning.atlas.NoOpInitializer

  base "concrete", :tag => "java", :init => "chef-solo:role[core]"
end

system "skife" do

  server "blog", :base => "concrete"
end