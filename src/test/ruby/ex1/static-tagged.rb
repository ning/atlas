
environment "test" do
  initializer "noop", com.ning.atlas.NoOpInitializer
  provisioner com.ning.atlas.StaticTaggedServerProvisioner, :servers => {
    "java" => ["10.0.0.1", "10.0.0.2"],
    "php" => ["10.0.1.1", "10.0.1.2"]
  }

  base "concrete", :tag => "java"
  base "macadam", :tag => "php"
end



system "skife" do
  server "blog", :base => "concrete"
  system "data" do
    server "memcached", :base => "macadam", :cardinality => 2
    server "db", :base => "concrete"
  end
end
