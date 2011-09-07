system "test" do
  server "syrup", :base => "concrete", :waffles => "pancakes", :install => ["fancy"]
  server "yummy", { base: "concrete", waffles: "pancakes", butter: "yes", install: ["fancy"] }
end

environment "waffles" do

  initializer "noop", com.ning.atlas.noop.NoOpInstaller
  provisioner "static", com.ning.atlas.StaticTaggedServerProvisioner, :servers => {
      "java" => ["10.0.0.1", "10.0.0.2"],
      "php"  => ["10.0.1.1", "10.0.1.2"]
  }
  installer "fancy", com.ning.atlas.FancyInstaller

  base "concrete", :tag => "java", :provisioner => "static"
  base "macadam", :tag => "php", :provisioner => "static"

end