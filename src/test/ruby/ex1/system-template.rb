



# example of per-whole-thing aliases

aka "ec2-ami:ami-f8b35e91", "spatulacave-2.1"
aka "ec2-ami:ami-somethingbig", "wafflehut-2.4.37"


# example of a system template

system "shebang" do

  server "geponsole", :image => "spatulacave-2.1",
                      :install => ["http://s3/gepo-2.7", "http://s3/gonsole-2.7"]

  system "ning" do
    server "resolver", :image => "spatulacave-2.1",
                       :install => "gepo:load-balancer-9.3",
                       :count => 8

    system "aclu", :count=> 2 do
      server "appcore", :image => "wafflehut-2.4.37",
                        :count => 5,
                        :install => ["gepo:app-server-2.4.37","gepo:cache-server-1.0.2"]
    end

    system "arecibo", :external => "http://something/3.1415/arecibo_template.rb"
  end
end


# examples of per-deploy overrides

override "ning/resolver.count", 2
override "ning/aclu/appcore.count", 2
