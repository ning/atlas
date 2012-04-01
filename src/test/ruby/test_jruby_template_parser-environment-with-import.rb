

environment "unit-test" do
  import "thingy", :url => "file://#{File.dirname __FILE__}/test_jruby_template_parser_env_servers-env.rb"
end