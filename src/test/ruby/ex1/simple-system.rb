

space "ec2" do

end

server "gepo", :count => 2, :base => "centos-big"

system "ning" do
  server "resolver", :base => "ubuntu-small",
                     :install => ["galaxy:wiffle/wombat/hoot"],
                     :count => 2

  system "aclu", :count=>2  do
    server "appcore", :base => "java-core"
  end
end
