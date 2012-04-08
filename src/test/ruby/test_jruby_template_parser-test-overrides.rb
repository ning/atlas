
system "magic" do
  server "app", :base => "ubuntu", :cardinality => 2
end

environment "prod" do
  base "ubuntu", :provisioner => "noop"
end

environment "test" do
  cardinality "/magic.0/app" => 1

  base "ubuntu", :provisioner => "noop"
end
