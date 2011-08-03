

system "reinflation" do
  server "kitchen", :base => "server", :install => ["galaxy:appliances-1.3.2"]
  system "breakfast" do
    server "pancakes", :base => "server", :cardinality => 3, :install => ["galaxy:pancake-1.2.3"]
    server "bacon", :base => "server", :cardinality => 2, :install => ["galaxy:bacon-1.2.3"]
  end
end