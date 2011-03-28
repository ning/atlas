# aliases, can be defined here or in a site descriptor
aka "ubuntu-small" => "ami-a6f504cf"
aka "galaxy" => "chef:galaxy-agent"

# example of a system template

system "chef-server", :external => "http://something/chef-bootstrap-1.0.2.rb"
system "arecibo", :external => "http://something/3.1415/arecibo_template.rb"

server "geponsole", :base => "ubuntu-small",
                    :install => ["chef:gepo-2.7", "chef:gonsole-2.7"],
                    :order => 10 # not yet supported

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
