
# aliases, can be defined here or in a site descriptor
aka "ubuntu-small" => "ami-a6f504cf"

# example of a system template

system "root" do

  system "chef-server", :external => "http://something/chef-bootstrap-1.0.2.rb"

  server "geponsole", :image => "ubuntu-small",
                      :install => ["chef:gepo-2.7", "chef:gonsole-2.7"]

  system "ning" do
    server "resolver", :image => "ubuntu-small",
                       :install => ["chef:galaxy", "galaxy:load-balancer-9.3"],
                       :count => 8

    system "aclu", :count=> 2 do
      server "appcore", :image => "ubuntu-small",
                        :count => 5,
                        :install => ["chef:galaxy", "galaxy:app-server-2.4.37"]

      server "content", :image => "ubuntu-small",
                        :count => 2,
                        :install => ["chef:galaxy", "galaxy:content-service-1.0.6"]
    end

    system "arecibo", :external => "http://something/3.1415/arecibo_template.rb"
  end
end


# examples of per-deploy overrides

# override "ning/resolver.count", 2
# override "ning/aclu/appcore.count", 2
