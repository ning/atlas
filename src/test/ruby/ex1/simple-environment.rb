
environment "test" do
  provisioner com.ning.atlas.StaticTaggedServerProvisioner, :servers => {
    "java" => ["10.0.0.1", "10.0.0.2"],
    "php" => ["10.0.1.1", "10.0.1.2"]
  }

  initializer com.ning.atlas.NoOpInitializer

  base "concrete", :tag => "ami-1234", :init => "chef-solo:role[core]"
end
