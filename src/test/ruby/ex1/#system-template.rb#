# aliases, can be defined here or in a site descriptor
aka "galaxy" => "chef:galaxy-agent"
aka "ubuntu-small" => "ami-a6f504cf"

# example of a system template

space "ec2" do
  # not sure how to use this concept yet, but I will figure it out :-)
end


# nasty interpolation to say 'next to this file' :-(
system "galaxy", :external => "file:///#{File.expand_path(File.dirname(__FILE__))}/galaxy-template.rb"
system "chef", :external => "file:///#{File.expand_path(File.dirname(__FILE__))}/chef-server.rb"


system "ning" do
  server "resolver", :base => "ubuntu-small",
                     :install => ["galaxy", "galaxy:load-balancer-9.3"],
                     :count => 8

  system "aclu", :count=> 2 do
    server "appcore", :base => "ubuntu-small",
                      :count => 5,
                      :install => ["galaxy", "galaxy:app-server-2.4.37"]

    server "content", :base => "ubuntu-small",
                      :count => 2,
                      :install => ["galaxy", "galaxy:content-service-1.0.6"]
  end
end


# examples of per-deploy overrides

# override "ning/resolver.count", 2
# override "ning/aclu/appcore.count", 2
