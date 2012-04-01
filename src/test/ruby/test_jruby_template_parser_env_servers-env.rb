

environment "unit-test" do
  base "mythical", :provisioner => "noop"

  server "ops-thing", :base => "mythical"
end