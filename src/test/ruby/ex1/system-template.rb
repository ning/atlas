

environment "cruft" do
  # not sure how to use this concept yet, but I will figure it out :-)
end


# nasty interpolation to say 'next to this file' :-(
#system "galaxy", :external => "file:///#{File.expand_path(File.dirname(__FILE__))}/galaxy-template.rb"

#system "chef", :external => "file:///#{File.expand_path(File.dirname(__FILE__))}/chef-server.rb"


system "ning" do
  server "resolver", :base => "ubuntu-small",
                     :install => ["cast:load-balancer-9.3"],
                     :cardinality => 8

  system "aclu", :cardinality => ["aclu0", "aclu1"] do
    server "appcore", :base => "ubuntu-small",
                      :count => 5,
                      :install => ["cast:app-server-2.4.37"]

    server "content", :base => "ubuntu-small",
                      :cardinality => 2,
                      :install => ["cast:content-service-1.0.6"]
  end
end


# examples of per-deploy overrides

# override "ning/resolver.count", 2
# override "ning/aclu/appcore.count", 2
