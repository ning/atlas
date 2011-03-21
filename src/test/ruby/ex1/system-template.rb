
# example of per-whole-thing aliases

# aka "ec2-ami:ami-f8b35e91", "spatulacave-2.1"
# aka "ec2-ami:ami-somethingbig", "wafflehut-2.4.37"


# example of a system template

system "shebang" do

  system "chef", :external => "file:///#{File.dirname(File.expand_path(__FILE__))}/chef-template.rb"

  server "geponsole", :image => "ami-a6f504cf",
                      :install => ["chef:gepo-2.7", "chef:gonsole-2.7"]

  system "ning" do
    server "resolver", :image => "ami-a6f504cf",
                       :install => ["chef:galaxy", "galaxy:load-balancer-9.3"],
                       :count => 8

    system "aclu", :count=> 2 do
      server "appcore", :image => "ami-a6f504cf",
                        :count => 5,
                        :install => ["chef:galaxy", "galaxy:app-server-2.4.37"]

      server "content", :image => "ami-a6f504cf",
                        :count => 2,
                        :install => ["chef:galaxy", "galaxy:content-service-1.0.6"]
    end

    system "arecibo", :external => "http://something/3.1415/arecibo_template.rb"
  end
end


# examples of per-deploy overrides

# override "ning/resolver.count", 2
# override "ning/aclu/appcore.count", 2
