

environment "unit-test" do

  server "ops-thing", base: "mythical"

  base "mythical", provisioner: "noop"
end