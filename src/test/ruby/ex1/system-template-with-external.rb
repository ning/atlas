
system "with-ext" do
  system "stuff", :external => "#{File.dirname __FILE__}/sys-external.rb"
  server "resolver", :base => "ubuntu-small"
end


# examples of per-deploy overrides

# override "ning/resolver.count", 2
# override "ning/aclu/appcore.count", 2
