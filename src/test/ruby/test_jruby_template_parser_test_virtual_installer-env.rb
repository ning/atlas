

environment "test" do

  installer "jdbi", :virtual => ["noop:world", "noop:octopus"]

  base "server", :provisioner => "noop"

end