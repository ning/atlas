system "with-import" do
  import "external", :url => "#{File.dirname __FILE__}/sys-external.rb"
  server "resolver", :base => "ubuntu-small"
end
