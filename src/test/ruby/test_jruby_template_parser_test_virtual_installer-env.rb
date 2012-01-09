

environment "test" do

  installer "jdbi",
            :virtual => ["noop:{virtual.fragment}",
                         "noop:octopus"]

  base "server", :provisioner => "noop"

end